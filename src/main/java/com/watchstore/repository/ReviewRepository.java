package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReviewRepository {

    public List<Review> findApprovedByProductId(int productId) {
        List<Review> list = new ArrayList<>();
        String sql = """
                SELECT r.ReviewID, r.ProductID, r.OrderItemID, r.UserID, r.Rating, r.ReviewTitle, r.ReviewContent, r.IsVerifiedPurchase, r.Status, r.CreatedAt,
                       u.FullName AS CustomerName,
                       rr.ReplyContent, rr.CreatedAt AS ReplyCreatedAt, su.FullName AS ReplierName
                FROM Reviews r
                JOIN Users u ON r.UserID = u.UserID
                LEFT JOIN ReviewReplies rr ON r.ReviewID = rr.ReviewID
                LEFT JOIN Users su ON rr.StaffID = su.UserID
                WHERE r.ProductID = ? AND r.Status = 'APPROVED'
                ORDER BY r.CreatedAt DESC
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToReview(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Review> findAll() {
        List<Review> list = new ArrayList<>();
        String sql = """
                SELECT r.ReviewID, r.ProductID, r.OrderItemID, r.UserID, r.Rating, r.ReviewTitle, r.ReviewContent, r.IsVerifiedPurchase, r.Status, r.CreatedAt,
                       u.FullName AS CustomerName, p.ProductName,
                       rr.ReplyContent, rr.CreatedAt AS ReplyCreatedAt, su.FullName AS ReplierName
                FROM Reviews r
                JOIN Users u ON r.UserID = u.UserID
                JOIN Products p ON r.ProductID = p.ProductID
                LEFT JOIN ReviewReplies rr ON r.ReviewID = rr.ReviewID
                LEFT JOIN Users su ON rr.StaffID = su.UserID
                ORDER BY r.CreatedAt DESC
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToReview(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean add(Review review) {
        String sql = """
                INSERT INTO Reviews (ProductID, OrderItemID, UserID, Rating, ReviewTitle, ReviewContent, IsVerifiedPurchase, Status, CreatedAt, UpdatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, review.getProductId());
            if (review.getOrderItemId() != null) {
                ps.setLong(2, review.getOrderItemId());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getRating());
            ps.setString(5, review.getReviewTitle() != null ? review.getReviewTitle() : "");
            ps.setString(6, review.getReviewContent() != null ? review.getReviewContent() : "");
            ps.setBoolean(7, review.isVerifiedPurchase());
            ps.setString(8, review.getStatus() != null ? review.getStatus() : "PENDING");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatus(long reviewId, String status) {
        String sql = "UPDATE Reviews SET Status = ?, UpdatedAt = GETDATE() WHERE ReviewID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, reviewId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addOrUpdateReply(long reviewId, int staffId, String content) {
        String checkSql = "SELECT 1 FROM ReviewReplies WHERE ReviewID = ?";
        String updateSql = "UPDATE ReviewReplies SET StaffID = ?, ReplyContent = ?, CreatedAt = GETDATE() WHERE ReviewID = ?";
        String insertSql = "INSERT INTO ReviewReplies (ReviewID, StaffID, ReplyContent, CreatedAt) VALUES (?, ?, ?, GETDATE())";

        try (Connection conn = DBContext.getConnection()) {
            boolean exists = false;
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setLong(1, reviewId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setInt(1, staffId);
                    psUpdate.setString(2, content);
                    psUpdate.setLong(3, reviewId);
                    return psUpdate.executeUpdate() > 0;
                }
            } else {
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    psInsert.setLong(1, reviewId);
                    psInsert.setInt(2, staffId);
                    psInsert.setString(3, content);
                    return psInsert.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasPurchased(int userId, int productId) {
        String sql = """
                SELECT TOP 1 oi.OrderItemID
                FROM OrderItems oi
                JOIN Orders o ON oi.OrderID = o.OrderID
                WHERE o.CustomerID = ? 
                  AND oi.VariantID IN (SELECT VariantID FROM ProductVariants WHERE ProductID = ?) 
                  AND o.OrderStatus = 'COMPLETED'
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Long getCompletedOrderItemId(int userId, int productId) {
        String sql = """
                SELECT TOP 1 oi.OrderItemID
                FROM OrderItems oi
                JOIN Orders o ON oi.OrderID = o.OrderID
                WHERE o.CustomerID = ? 
                  AND oi.VariantID IN (SELECT VariantID FROM ProductVariants WHERE ProductID = ?) 
                  AND o.OrderStatus = 'COMPLETED'
                ORDER BY o.CreatedAt DESC
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("OrderItemID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Review mapResultSetToReview(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setReviewId(rs.getLong("ReviewID"));
        r.setProductId(rs.getInt("ProductID"));
        
        long orderItemId = rs.getLong("OrderItemID");
        r.setOrderItemId(rs.wasNull() ? null : orderItemId);
        
        r.setUserId(rs.getInt("UserID"));
        r.setRating(rs.getInt("Rating"));
        r.setReviewTitle(rs.getString("ReviewTitle"));
        r.setReviewContent(rs.getString("ReviewContent"));
        r.setVerifiedPurchase(rs.getBoolean("IsVerifiedPurchase"));
        r.setStatus(rs.getString("Status"));
        r.setCreatedAt(rs.getTimestamp("CreatedAt"));
        
        r.setCustomerName(rs.getString("CustomerName"));
        
        // Product name if joined
        try {
            r.setProductName(rs.getString("ProductName"));
        } catch (SQLException ignored) {}

        // Reply details if joined
        r.setReplyContent(rs.getString("ReplyContent"));
        r.setReplyCreatedAt(rs.getTimestamp("ReplyCreatedAt"));
        r.setReplierName(rs.getString("ReplierName"));
        
        return r;
    }
}
