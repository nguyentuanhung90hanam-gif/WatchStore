package com.watchstore.repository;

import com.watchstore.config.DBContext;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class CartRepository {

    public void add(int userId, int variantId, int quantity) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Khách hàng cần đăng nhập để thêm sản phẩm vào giỏ.");
        }
        if (variantId <= 0) {
            throw new IllegalArgumentException("Biến thể sản phẩm không hợp lệ.");
        }
        if (quantity <= 0 || quantity > 99) {
            throw new IllegalArgumentException("Số lượng phải từ 1 đến 99.");
        }

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            // 1. Kiểm tra variant & product active
            String checkVariantSql = """
                SELECT pv.VariantID, pv.ProductID, pv.SalePrice, p.Status AS ProductStatus,
                       ISNULL((SELECT SUM(AvailableQuantity) FROM dbo.InventoryBalances WHERE VariantID = pv.VariantID), 999) AS AvailableStock
                FROM dbo.ProductVariants pv
                JOIN dbo.Products p ON p.ProductID = pv.ProductID
                WHERE pv.VariantID = ? AND pv.Status = 'ACTIVE'
                """;
            int availableStock = 999;
            try (PreparedStatement ps = conn.prepareStatement(checkVariantSql)) {
                ps.setInt(1, variantId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Biến thể sản phẩm không tồn tại hoặc đã ngừng kinh doanh.");
                    }
                    String prodStatus = rs.getString("ProductStatus");
                    if (!"ACTIVE".equalsIgnoreCase(prodStatus)) {
                        throw new SQLException("Sản phẩm đã ngừng kinh doanh.");
                    }
                    availableStock = rs.getInt("AvailableStock");
                }
            }

            // 2. Lấy hoặc tạo Cart ACTIVE của User
            long cartId = getOrCreateActiveCartId(conn, userId);

            // 3. Kiểm tra xem variantId đã có trong CartItems chưa
            String checkItemSql = "SELECT CartItemID, Quantity FROM dbo.CartItems WHERE CartID = ? AND VariantID = ?";
            Long existingCartItemId = null;
            int currentQty = 0;
            try (PreparedStatement ps = conn.prepareStatement(checkItemSql)) {
                ps.setLong(1, cartId);
                ps.setInt(2, variantId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        existingCartItemId = rs.getLong("CartItemID");
                        currentQty = rs.getInt("Quantity");
                    }
                }
            }

            int newQty = currentQty + quantity;
            if (newQty > availableStock && availableStock > 0) {
                newQty = Math.min(newQty, availableStock);
            }

            if (existingCartItemId != null) {
                String updateItemSql = "UPDATE dbo.CartItems SET Quantity = ?, UpdatedAt = SYSDATETIME() WHERE CartItemID = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateItemSql)) {
                    ps.setInt(1, newQty);
                    ps.setLong(2, existingCartItemId);
                    ps.executeUpdate();
                }
            } else {
                String insertItemSql = "INSERT INTO dbo.CartItems (CartID, VariantID, Quantity, CreatedAt, UpdatedAt) VALUES (?, ?, ?, SYSDATETIME(), SYSDATETIME())";
                try (PreparedStatement ps = conn.prepareStatement(insertItemSql)) {
                    ps.setLong(1, cartId);
                    ps.setInt(2, variantId);
                    ps.setInt(3, newQty);
                    ps.executeUpdate();
                }
            }

            // 4. Cập nhật Cart UpdatedAt
            touchCart(conn, cartId);

            conn.commit();
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

    public void addProduct(int userId, int productId, int quantity) throws SQLException {
        Integer variant = defaultVariant(productId);
        if (variant == null) throw new SQLException("Sản phẩm không có biến thể đang bán.");
        add(userId, variant, quantity);
    }

    public void update(int userId, int variantId, int quantity) throws SQLException {
        if (userId <= 0) return;
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            Long cartId = getActiveCartId(conn, userId);
            if (cartId == null) {
                conn.commit();
                return;
            }

            if (quantity <= 0) {
                String deleteSql = "DELETE FROM dbo.CartItems WHERE CartID = ? AND VariantID = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setLong(1, cartId);
                    ps.setInt(2, variantId);
                    ps.executeUpdate();
                }
            } else {
                String updateSql = "UPDATE dbo.CartItems SET Quantity = ?, UpdatedAt = SYSDATETIME() WHERE CartID = ? AND VariantID = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, quantity);
                    ps.setLong(2, cartId);
                    ps.setInt(3, variantId);
                    ps.executeUpdate();
                }
            }

            touchCart(conn, cartId);
            conn.commit();
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

    public void remove(int userId, int variantId) throws SQLException {
        update(userId, variantId, 0);
    }

    public void clear(int userId) throws SQLException {
        if (userId <= 0) return;
        String sql = """
            DELETE ci
            FROM dbo.CartItems ci
            JOIN dbo.Carts c ON c.CartID = ci.CartID
            WHERE c.UserID = ? AND c.Status = 'ACTIVE'
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> items(int userId) throws SQLException {
        String sql = """
          SELECT ci.CartItemID, ci.VariantID, ci.Quantity, p.ProductID, p.ProductName, pv.VariantName, pv.SKU,
                 pv.SalePrice, pv.CompareAtPrice, b.BrandName,
                 (SELECT TOP 1 ImageUrl FROM dbo.ProductImages WHERE ProductID = p.ProductID ORDER BY IsPrimary DESC, DisplayOrder ASC) AS ImageUrl,
                 ISNULL((SELECT SUM(AvailableQuantity) FROM dbo.InventoryBalances WHERE VariantID = pv.VariantID), 999) AS Available
          FROM dbo.CartItems ci
          JOIN dbo.Carts c ON c.CartID = ci.CartID AND c.Status = 'ACTIVE'
          JOIN dbo.ProductVariants pv ON pv.VariantID = ci.VariantID
          JOIN dbo.Products p ON p.ProductID = pv.ProductID
          JOIN dbo.Brands b ON b.BrandID = p.BrandID
          WHERE c.UserID = ?
          ORDER BY ci.CartItemID DESC
          """;
        List<Map<String, Object>> out = new ArrayList<>();
        try (Connection c = DBContext.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("cartItemId", rs.getLong("CartItemID"));
                    m.put("variantId", rs.getInt("VariantID"));
                    m.put("productId", rs.getInt("ProductID"));
                    m.put("quantity", rs.getInt("Quantity"));
                    m.put("available", rs.getInt("Available"));
                    m.put("name", rs.getString("ProductName"));
                    m.put("variantName", rs.getString("VariantName"));
                    m.put("sku", rs.getString("SKU"));
                    m.put("price", rs.getBigDecimal("SalePrice"));
                    m.put("oldPrice", rs.getBigDecimal("CompareAtPrice"));
                    m.put("brand", rs.getString("BrandName"));
                    m.put("image", rs.getString("ImageUrl"));
                    BigDecimal salePrice = rs.getBigDecimal("SalePrice");
                    if (salePrice == null) salePrice = BigDecimal.ZERO;
                    m.put("lineTotal", salePrice.multiply(BigDecimal.valueOf(rs.getInt("Quantity"))));
                    out.add(m);
                }
            }
        }
        return out;
    }

    public int count(int userId) throws SQLException {
        String sql = "SELECT ISNULL(SUM(ci.Quantity),0) FROM dbo.CartItems ci JOIN dbo.Carts c ON c.CartID=ci.CartID WHERE c.UserID=? AND c.Status='ACTIVE'";
        try (Connection c = DBContext.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public BigDecimal subtotal(int userId) throws SQLException {
        String sql = """
            SELECT ISNULL(SUM(ci.Quantity * pv.SalePrice), 0)
            FROM dbo.CartItems ci
            JOIN dbo.Carts c ON c.CartID = ci.CartID
            JOIN dbo.ProductVariants pv ON pv.VariantID = ci.VariantID
            WHERE c.UserID = ? AND c.Status = 'ACTIVE'
            """;
        try (Connection c = DBContext.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    private Long getActiveCartId(Connection conn, int userId) throws SQLException {
        String sql = "SELECT TOP 1 CartID FROM dbo.Carts WHERE UserID = ? AND Status = 'ACTIVE' ORDER BY CartID DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("CartID");
            }
        }
        return null;
    }

    private long getOrCreateActiveCartId(Connection conn, int userId) throws SQLException {
        Long cartId = getActiveCartId(conn, userId);
        if (cartId != null) return cartId;

        String insertSql = "INSERT INTO dbo.Carts (UserID, Status, CreatedAt, UpdatedAt) VALUES (?, 'ACTIVE', SYSDATETIME(), SYSDATETIME())";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Không thể tạo giỏ hàng cho người dùng.");
    }

    private void touchCart(Connection conn, long cartId) throws SQLException {
        String sql = "UPDATE dbo.Carts SET UpdatedAt = SYSDATETIME() WHERE CartID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cartId);
            ps.executeUpdate();
        }
    }

    private Integer defaultVariant(int productId) throws SQLException {
        String sql = "SELECT TOP 1 VariantID FROM dbo.ProductVariants WHERE ProductID = ? AND Status = 'ACTIVE' ORDER BY VariantID";
        try (Connection c = DBContext.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }
}
