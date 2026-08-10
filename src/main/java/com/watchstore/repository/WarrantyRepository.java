package com.watchstore.repository;

import com.watchstore.config.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository quản lý phiếu bảo hành.
 * Sử dụng bảng Warranties (tự tạo nếu chưa có).
 */
public class WarrantyRepository {

    /** Tự tạo bảng Warranties nếu chưa tồn tại */
    public void ensureTable() {
        String sql = """
            IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Warranties' AND xtype='U')
            CREATE TABLE Warranties (
                WarrantyID     INT IDENTITY(1,1) PRIMARY KEY,
                OrderID        INT NOT NULL,
                ProductName    NVARCHAR(255) NOT NULL,
                SerialNumber   NVARCHAR(100),
                WarrantyMonths INT NOT NULL DEFAULT 12,
                StartDate      DATE NOT NULL DEFAULT GETDATE(),
                EndDate        AS DATEADD(MONTH, WarrantyMonths, StartDate) PERSISTED,
                Status         NVARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                Note           NVARCHAR(500),
                CreatedAt      DATETIME NOT NULL DEFAULT GETDATE()
            )
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> findAll() {
        return search(null, null);
    }

    public List<Map<String, Object>> search(String keyword, String status) {
        List<Map<String, Object>> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT w.*, o.OrderCode
            FROM Warranties w
            LEFT JOIN Orders o ON w.OrderID = o.OrderID
            WHERE 1=1
            """);

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(w.ProductName) LIKE ? OR LOWER(w.SerialNumber) LIKE ? OR LOWER(o.OrderCode) LIKE ?)");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND w.Status = ?");
        }
        sql.append(" ORDER BY w.WarrantyID DESC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword.toLowerCase() + "%";
                ps.setString(idx++, k);
                ps.setString(idx++, k);
                ps.setString(idx++, k);
            }
            if (status != null && !status.isBlank()) {
                ps.setString(idx++, status);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Map<String, Object> findById(int id) {
        String sql = """
            SELECT w.*, o.OrderCode
            FROM Warranties w
            LEFT JOIN Orders o ON w.OrderID = o.OrderID
            WHERE w.WarrantyID = ?
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM Warranties";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Warranties WHERE Status = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean insert(int orderId, String productName, String serial, int months, String note) {
        String sql = """
            INSERT INTO Warranties (OrderID, ProductName, SerialNumber, WarrantyMonths, StartDate, Status, Note)
            VALUES (?, ?, ?, ?, GETDATE(), 'ACTIVE', ?)
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setString(2, productName);
            ps.setString(3, serial);
            ps.setInt(4, months);
            ps.setString(5, note);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE Warranties SET Status = ? WHERE WarrantyID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Map<String, Object> mapRow(ResultSet rs) throws SQLException {
        Map<String, Object> m = new HashMap<>();
        m.put("id",           rs.getInt("WarrantyID"));
        m.put("orderId",      rs.getInt("OrderID"));
        m.put("orderCode",    rs.getString("OrderCode"));
        m.put("productName",  rs.getString("ProductName"));
        m.put("serial",       rs.getString("SerialNumber"));
        m.put("months",       rs.getInt("WarrantyMonths"));
        m.put("startDate",    rs.getDate("StartDate"));
        m.put("endDate",      rs.getDate("EndDate"));
        m.put("status",       rs.getString("Status"));
        m.put("note",         rs.getString("Note"));
        m.put("createdAt",    rs.getTimestamp("CreatedAt"));
        return m;
    }
}
