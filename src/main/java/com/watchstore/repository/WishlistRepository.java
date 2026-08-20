package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WishlistRepository {

    public boolean toggle(int userId, int productId) throws SQLException {
        if (userId <= 0 || productId <= 0) return false;

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            // 1. Lấy hoặc tạo Wishlist cho User
            int wishlistId = getOrCreateWishlistId(conn, userId);

            // 2. Kiểm tra xem sản phẩm đã có trong WishlistItems chưa
            String checkSql = "SELECT WishlistItemID FROM dbo.WishlistItems WHERE WishlistID = ? AND ProductID = ?";
            Integer itemId = null;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, wishlistId);
                ps.setInt(2, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        itemId = rs.getInt("WishlistItemID");
                    }
                }
            }

            boolean isFavorite;
            if (itemId != null) {
                String deleteSql = "DELETE FROM dbo.WishlistItems WHERE WishlistItemID = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setInt(1, itemId);
                    ps.executeUpdate();
                }
                isFavorite = false;
            } else {
                String insertSql = "INSERT INTO dbo.WishlistItems (WishlistID, ProductID, AddedAt) VALUES (?, ?, SYSDATETIME())";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, wishlistId);
                    ps.setInt(2, productId);
                    ps.executeUpdate();
                }
                isFavorite = true;
            }

            conn.commit();
            return isFavorite;
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

    public List<Product> findAll(int userId) throws SQLException {
        String sql = """
            SELECT p.ProductID, p.ProductName, p.ShortDescription, p.RatingAverage, p.RatingCount, b.BrandName,
                   (SELECT TOP 1 ImageUrl FROM dbo.ProductImages WHERE ProductID = p.ProductID ORDER BY IsPrimary DESC, DisplayOrder ASC) AS ImageUrl,
                   (SELECT MIN(SalePrice) FROM dbo.ProductVariants WHERE ProductID = p.ProductID AND Status = 'ACTIVE') AS Price,
                   (SELECT MIN(CompareAtPrice) FROM dbo.ProductVariants WHERE ProductID = p.ProductID AND Status = 'ACTIVE') AS CompareAtPrice,
                   10 AS Quantity
            FROM dbo.Wishlists w
            JOIN dbo.WishlistItems wi ON wi.WishlistID = w.WishlistID
            JOIN dbo.Products p ON p.ProductID = wi.ProductID
            JOIN dbo.Brands b ON b.BrandID = p.BrandID
            WHERE w.UserID = ?
            ORDER BY wi.AddedAt DESC
            """;
        List<Product> out = new ArrayList<>();
        try (Connection c = DBContext.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setProductId(rs.getInt("ProductID"));
                    p.setProductName(rs.getString("ProductName"));
                    p.setShortDescription(rs.getString("ShortDescription"));
                    p.setBrandName(rs.getString("BrandName"));
                    p.setImageUrl(rs.getString("ImageUrl"));
                    p.setPrice(rs.getBigDecimal("Price"));
                    p.setCompareAtPrice(rs.getBigDecimal("CompareAtPrice"));
                    p.setStock(rs.getInt("Quantity"));
                    p.setRatingAverage(rs.getDouble("RatingAverage"));
                    p.setRatingCount(rs.getInt("RatingCount"));
                    out.add(p);
                }
            }
        }
        return out;
    }

    private int getOrCreateWishlistId(Connection conn, int userId) throws SQLException {
        String selectSql = "SELECT WishlistID FROM dbo.Wishlists WHERE UserID = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("WishlistID");
            }
        }

        String insertSql = "INSERT INTO dbo.Wishlists (UserID, CreatedAt) VALUES (?, SYSDATETIME())";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Không thể tạo Wishlist.");
    }
}
