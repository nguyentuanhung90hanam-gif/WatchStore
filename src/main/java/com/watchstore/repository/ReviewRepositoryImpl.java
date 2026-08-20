package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Review;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewRepositoryImpl implements ReviewRepository {

    @Override
    public boolean createReview(Review review) throws SQLException {
        if (review == null || review.getUserId() == null || review.getProductId() == null || review.getOrderId() == null) {
            throw new IllegalArgumentException("Thông tin đánh giá không đầy đủ.");
        }
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Số sao đánh giá phải từ 1 đến 5 sao.");
        }
        if (review.getContent() == null || review.getContent().trim().length() < 3 || review.getContent().trim().length() > 1000) {
            throw new IllegalArgumentException("Nội dung đánh giá phải từ 3 đến 1000 ký tự.");
        }

        String checkOrderSql = "SELECT OrderStatus FROM dbo.Orders WHERE OrderID = ? AND CustomerID = ?";
        String checkItemSql = """
            SELECT COUNT(*) 
            FROM dbo.OrderItems oi
            JOIN dbo.ProductVariants pv ON pv.VariantID = oi.VariantID
            WHERE oi.OrderID = ? AND pv.ProductID = ?
            """;
        String checkReviewedSql = "SELECT COUNT(*) FROM dbo.Reviews WHERE OrderID = ? AND ProductID = ? AND UserID = ?";
        String insertSql = """
            INSERT INTO dbo.Reviews (ProductID, UserID, OrderID, Rating, Title, Content, Status, CreatedAt)
            VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATETIME())
            """;

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            // 1. Kiểm tra đơn hàng thuộc user và đã hoàn thành
            try (PreparedStatement ps = conn.prepareStatement(checkOrderSql)) {
                ps.setLong(1, review.getOrderId());
                ps.setInt(2, review.getUserId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Đơn hàng không tồn tại hoặc không thuộc tài khoản của bạn.");
                    }
                    String status = rs.getString("OrderStatus");
                    if (!"COMPLETED".equalsIgnoreCase(status)) {
                        throw new SQLException("Chỉ có thể đánh giá sản phẩm từ đơn hàng đã giao thành công (COMPLETED).");
                    }
                }
            }

            // 2. Kiểm tra sản phẩm có trong đơn hàng
            try (PreparedStatement ps = conn.prepareStatement(checkItemSql)) {
                ps.setLong(1, review.getOrderId());
                ps.setInt(2, review.getProductId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        throw new SQLException("Sản phẩm không thuộc đơn hàng này.");
                    }
                }
            }

            // 3. Kiểm tra đã đánh giá chưa
            try (PreparedStatement ps = conn.prepareStatement(checkReviewedSql)) {
                ps.setLong(1, review.getOrderId());
                ps.setInt(2, review.getProductId());
                ps.setInt(3, review.getUserId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException("Bạn đã gửi đánh giá cho sản phẩm này trong đơn hàng rồi.");
                    }
                }
            }

            // 4. Thêm Review
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, review.getProductId());
                ps.setInt(2, review.getUserId());
                ps.setLong(3, review.getOrderId());
                ps.setInt(4, review.getRating());
                if (review.getTitle() != null && !review.getTitle().isBlank()) {
                    ps.setString(5, review.getTitle().trim());
                } else {
                    ps.setNull(5, Types.NVARCHAR);
                }
                ps.setString(6, review.getContent().trim());
                ps.setString(7, review.getStatus() != null ? review.getStatus() : "APPROVED");
                ps.executeUpdate();
            }

            // 5. Cập nhật RatingAverage và RatingCount của sản phẩm
            refreshProductRating(review.getProductId(), conn);

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    private void refreshProductRating(int productId, Connection conn) throws SQLException {
        String calcSql = """
            SELECT COUNT(*) AS ReviewCount, AVG(CAST(Rating AS DECIMAL(4,2))) AS AvgRating
            FROM dbo.Reviews
            WHERE ProductID = ? AND Status = 'APPROVED'
            """;
        String updateSql = """
            UPDATE dbo.Products
            SET RatingAverage = ?, RatingCount = ?, UpdatedAt = SYSDATETIME()
            WHERE ProductID = ?
            """;

        double avgRating = 0.0;
        int reviewCount = 0;

        try (PreparedStatement ps = conn.prepareStatement(calcSql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    reviewCount = rs.getInt("ReviewCount");
                    BigDecimal avg = rs.getBigDecimal("AvgRating");
                    if (avg != null) {
                        avgRating = avg.doubleValue();
                    }
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setBigDecimal(1, BigDecimal.valueOf(avgRating));
            ps.setInt(2, reviewCount);
            ps.setInt(3, productId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Review> findByProductId(int productId) {
        List<Review> list = new ArrayList<>();
        String sql = """
            SELECT r.*, u.FullName, u.AvatarUrl, p.ProductName
            FROM dbo.Reviews r
            JOIN dbo.Users u ON u.UserID = r.UserID
            JOIN dbo.Products p ON p.ProductID = r.ProductID
            WHERE r.ProductID = ? AND r.Status = 'APPROVED'
            ORDER BY r.ReviewID DESC
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Review> findByUserId(int userId) {
        List<Review> list = new ArrayList<>();
        String sql = """
            SELECT r.*, u.FullName, u.AvatarUrl, p.ProductName, o.OrderCode,
                   (SELECT TOP 1 ImageUrl FROM dbo.ProductImages WHERE ProductID = p.ProductID ORDER BY IsPrimary DESC, DisplayOrder ASC) AS ProductImage
            FROM dbo.Reviews r
            JOIN dbo.Users u ON u.UserID = r.UserID
            JOIN dbo.Products p ON p.ProductID = r.ProductID
            LEFT JOIN dbo.Orders o ON o.OrderID = r.OrderID
            WHERE r.UserID = ?
            ORDER BY r.ReviewID DESC
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> findPendingReviewItems(int userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT DISTINCT o.OrderID, o.OrderCode, o.CompletedAt, o.CreatedAt, p.ProductID, p.ProductName, pv.VariantName,
                   (SELECT TOP 1 ImageUrl FROM dbo.ProductImages WHERE ProductID = p.ProductID ORDER BY IsPrimary DESC, DisplayOrder ASC) AS ProductImage
            FROM dbo.Orders o
            JOIN dbo.OrderItems oi ON oi.OrderID = o.OrderID
            JOIN dbo.ProductVariants pv ON pv.VariantID = oi.VariantID
            JOIN dbo.Products p ON p.ProductID = pv.ProductID
            WHERE o.CustomerID = ? AND o.OrderStatus = 'COMPLETED'
              AND NOT EXISTS (
                  SELECT 1 FROM dbo.Reviews r WHERE r.OrderID = o.OrderID AND r.ProductID = p.ProductID AND r.UserID = o.CustomerID
              )
            ORDER BY o.OrderID DESC
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("orderId", rs.getLong("OrderID"));
                    map.put("orderCode", rs.getString("OrderCode"));
                    map.put("completedAt", rs.getTimestamp("CompletedAt"));
                    map.put("createdAt", rs.getTimestamp("CreatedAt"));
                    map.put("productId", rs.getInt("ProductID"));
                    map.put("productName", rs.getString("ProductName"));
                    map.put("variantName", rs.getString("VariantName"));
                    map.put("productImage", rs.getString("ProductImage"));
                    list.add(map);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean hasUserReviewedProductInOrder(int userId, long orderId, int productId) {
        String sql = "SELECT COUNT(*) FROM dbo.Reviews WHERE OrderID = ? AND ProductID = ? AND UserID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setInt(2, productId);
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Review> findAll() {
        return search(null, null, null);
    }

    @Override
    public List<Review> search(String keyword, String status, Integer rating) {
        List<Review> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT r.*, u.FullName, u.AvatarUrl, u.Email, p.ProductName, o.OrderCode,
                   (SELECT TOP 1 ImageUrl FROM dbo.ProductImages WHERE ProductID = p.ProductID ORDER BY IsPrimary DESC, DisplayOrder ASC) AS ProductImage
            FROM dbo.Reviews r
            JOIN dbo.Users u ON u.UserID = r.UserID
            JOIN dbo.Products p ON p.ProductID = r.ProductID
            LEFT JOIN dbo.Orders o ON o.OrderID = r.OrderID
            WHERE 1=1
            """);

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(u.FullName) LIKE ? OR LOWER(p.ProductName) LIKE ? OR LOWER(r.Content) LIKE ? OR LOWER(o.OrderCode) LIKE ?)");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND r.Status = ?");
        }
        if (rating != null && rating > 0) {
            sql.append(" AND r.Rating = ?");
        }
        sql.append(" ORDER BY r.ReviewID DESC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword.trim().toLowerCase() + "%";
                ps.setString(idx++, k);
                ps.setString(idx++, k);
                ps.setString(idx++, k);
                ps.setString(idx++, k);
            }
            if (status != null && !status.isBlank()) {
                ps.setString(idx++, status.trim());
            }
            if (rating != null && rating > 0) {
                ps.setInt(idx++, rating);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean updateStatus(long reviewId, String status) {
        String updateSql = "UPDATE dbo.Reviews SET Status = ? WHERE ReviewID = ?";
        String getProductSql = "SELECT ProductID FROM dbo.Reviews WHERE ReviewID = ?";

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            int productId = 0;
            try (PreparedStatement ps = conn.prepareStatement(getProductSql)) {
                ps.setLong(1, reviewId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        productId = rs.getInt("ProductID");
                    }
                }
            }

            if (productId == 0) return false;

            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, status);
                ps.setLong(2, reviewId);
                ps.executeUpdate();
            }

            refreshProductRating(productId, conn);

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public Review findById(long reviewId) {
        String sql = """
            SELECT r.*, u.FullName, u.AvatarUrl, p.ProductName, o.OrderCode,
                   (SELECT TOP 1 ImageUrl FROM dbo.ProductImages WHERE ProductID = p.ProductID ORDER BY IsPrimary DESC, DisplayOrder ASC) AS ProductImage
            FROM dbo.Reviews r
            JOIN dbo.Users u ON u.UserID = r.UserID
            JOIN dbo.Products p ON p.ProductID = r.ProductID
            LEFT JOIN dbo.Orders o ON o.OrderID = r.OrderID
            WHERE r.ReviewID = ?
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, reviewId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Review mapResultSet(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setReviewId(rs.getLong("ReviewID"));
        r.setProductId(rs.getInt("ProductID"));
        r.setUserId(rs.getInt("UserID"));
        long orderId = rs.getLong("OrderID");
        if (!rs.wasNull()) {
            r.setOrderId(orderId);
        }
        r.setRating(rs.getInt("Rating"));
        r.setTitle(rs.getString("Title"));
        r.setContent(rs.getString("Content"));
        r.setStatus(rs.getString("Status"));
        r.setCreatedAt(rs.getTimestamp("CreatedAt"));

        try { r.setUserFullName(rs.getString("FullName")); } catch (SQLException ignored) {}
        try { r.setUserAvatar(rs.getString("AvatarUrl")); } catch (SQLException ignored) {}
        try { r.setProductName(rs.getString("ProductName")); } catch (SQLException ignored) {}
        try { r.setProductImage(rs.getString("ProductImage")); } catch (SQLException ignored) {}
        try { r.setOrderCode(rs.getString("OrderCode")); } catch (SQLException ignored) {}

        return r;
    }
}
