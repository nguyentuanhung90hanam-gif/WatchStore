package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.ProductComment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentRepositoryImpl implements CommentRepository {

    static {
        // Ensure ProductComments table exists
        try (Connection conn = DBContext.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = """
                IF OBJECT_ID(N'dbo.ProductComments', N'U') IS NULL
                BEGIN
                    CREATE TABLE dbo.ProductComments (
                        CommentID       BIGINT IDENTITY(1,1) PRIMARY KEY,
                        ProductID       INT NOT NULL,
                        UserID          INT NOT NULL,
                        Content         NVARCHAR(1000) NOT NULL,
                        Status          VARCHAR(20) NOT NULL CONSTRAINT DF_ProductComments_Status DEFAULT 'APPROVED',
                        CreatedAt       DATETIME2 NOT NULL CONSTRAINT DF_ProductComments_CreatedAt DEFAULT SYSDATETIME(),
                        CONSTRAINT FK_ProductComments_Product FOREIGN KEY (ProductID) REFERENCES dbo.Products(ProductID) ON DELETE CASCADE,
                        CONSTRAINT FK_ProductComments_User FOREIGN KEY (UserID) REFERENCES dbo.Users(UserID) ON DELETE CASCADE
                    );
                END
                """;
            stmt.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean insert(int productId, int userId, String content) throws SQLException {
        if (productId <= 0 || userId <= 0) {
            throw new IllegalArgumentException("Thông tin bình luận không hợp lệ.");
        }
        if (content == null || content.trim().length() < 2 || content.trim().length() > 1000) {
            throw new IllegalArgumentException("Nội dung bình luận phải từ 2 đến 1000 ký tự.");
        }

        String sql = """
            INSERT INTO dbo.ProductComments (ProductID, UserID, Content, Status, CreatedAt)
            VALUES (?, ?, ?, 'APPROVED', SYSDATETIME())
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, userId);
            ps.setString(3, content.trim());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<ProductComment> findByProductId(int productId) {
        List<ProductComment> list = new ArrayList<>();
        String sql = """
            SELECT c.*, u.FullName, u.AvatarUrl, p.ProductName
            FROM dbo.ProductComments c
            JOIN dbo.Users u ON u.UserID = c.UserID
            JOIN dbo.Products p ON p.ProductID = c.ProductID
            WHERE c.ProductID = ? AND c.Status = 'APPROVED'
            ORDER BY c.CommentID DESC
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
    public List<ProductComment> findAll() {
        return search(null, null);
    }

    @Override
    public List<ProductComment> search(String keyword, String status) {
        List<ProductComment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT c.*, u.FullName, u.Email, u.AvatarUrl, p.ProductName
            FROM dbo.ProductComments c
            JOIN dbo.Users u ON u.UserID = c.UserID
            JOIN dbo.Products p ON p.ProductID = c.ProductID
            WHERE 1=1
            """);

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(u.FullName) LIKE ? OR LOWER(p.ProductName) LIKE ? OR LOWER(c.Content) LIKE ?)");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND c.Status = ?");
        }
        sql.append(" ORDER BY c.CommentID DESC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword.trim().toLowerCase() + "%";
                ps.setString(idx++, k);
                ps.setString(idx++, k);
                ps.setString(idx++, k);
            }
            if (status != null && !status.isBlank()) {
                ps.setString(idx++, status.trim());
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
    public boolean updateStatus(long commentId, String status) {
        String sql = "UPDATE dbo.ProductComments SET Status = ? WHERE CommentID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, commentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public ProductComment findById(long commentId) {
        String sql = """
            SELECT c.*, u.FullName, u.AvatarUrl, p.ProductName
            FROM dbo.ProductComments c
            JOIN dbo.Users u ON u.UserID = c.UserID
            JOIN dbo.Products p ON p.ProductID = c.ProductID
            WHERE c.CommentID = ?
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, commentId);
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

    private ProductComment mapResultSet(ResultSet rs) throws SQLException {
        ProductComment c = new ProductComment();
        c.setCommentId(rs.getLong("CommentID"));
        c.setProductId(rs.getInt("ProductID"));
        c.setUserId(rs.getInt("UserID"));
        c.setContent(rs.getString("Content"));
        c.setStatus(rs.getString("Status"));
        c.setCreatedAt(rs.getTimestamp("CreatedAt"));

        try { c.setUserFullName(rs.getString("FullName")); } catch (SQLException ignored) {}
        try { c.setUserEmail(rs.getString("Email")); } catch (SQLException ignored) {}
        try { c.setUserAvatar(rs.getString("AvatarUrl")); } catch (SQLException ignored) {}
        try { c.setProductName(rs.getString("ProductName")); } catch (SQLException ignored) {}

        return c;
    }
}
