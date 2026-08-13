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
        
        String sqlMigrate = """
            IF NOT EXISTS(SELECT * FROM sys.columns WHERE Name = N'ReceiveDate' AND Object_ID = Object_ID(N'Warranties'))
            ALTER TABLE Warranties ADD ReceiveDate DATE NULL;

            IF NOT EXISTS(SELECT * FROM sys.columns WHERE Name = N'ReceiveNote' AND Object_ID = Object_ID(N'Warranties'))
            ALTER TABLE Warranties ADD ReceiveNote NVARCHAR(500) NULL;

            IF NOT EXISTS(SELECT * FROM sys.columns WHERE Name = N'RepairContent' AND Object_ID = Object_ID(N'Warranties'))
            ALTER TABLE Warranties ADD RepairContent NVARCHAR(500) NULL;

            IF NOT EXISTS(SELECT * FROM sys.columns WHERE Name = N'ComponentReplaced' AND Object_ID = Object_ID(N'Warranties'))
            ALTER TABLE Warranties ADD ComponentReplaced NVARCHAR(255) NULL;

            IF NOT EXISTS(SELECT * FROM sys.columns WHERE Name = N'RepairNote' AND Object_ID = Object_ID(N'Warranties'))
            ALTER TABLE Warranties ADD RepairNote NVARCHAR(500) NULL;

            IF NOT EXISTS(SELECT * FROM sys.columns WHERE Name = N'CompleteDate' AND Object_ID = Object_ID(N'Warranties'))
            ALTER TABLE Warranties ADD CompleteDate DATE NULL;

            IF NOT EXISTS(SELECT * FROM sys.columns WHERE Name = N'ReturnDate' AND Object_ID = Object_ID(N'Warranties'))
            ALTER TABLE Warranties ADD ReturnDate DATE NULL;
            """;

        try (Connection conn = DBContext.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlMigrate)) {
                ps.executeUpdate();
            }
            
            // Dọn dẹp phục hồi các ký tự Unicode bị lỗi dấu hỏi do thiếu prefix N trước đó và chuẩn hóa dữ liệu thử nghiệm
            String sqlClean = """
                UPDATE Warranties SET Status = N'Đang sửa chữa' WHERE Status LIKE 'Đang s%a ch%a' OR Status = 'Đang s?a ch?a';
                UPDATE Warranties SET Status = N'Đang bảo hành' WHERE Status LIKE 'Đang b%o h%nh' OR Status = 'Đang b?o hành';
                UPDATE Warranties SET Status = N'Đã sửa xong' WHERE Status LIKE 'Đã s%a xong' OR Status = 'Đã s?a xong';
                UPDATE Warranties SET Status = N'Đã trả khách' WHERE Status LIKE 'Đã tr% kh%ch' OR Status = 'Đã tr? khách';
                UPDATE Warranties SET ProductName = N'Rolex Datejust 41' WHERE ProductName = 'Incorrect Watch Product';
                UPDATE Warranties SET ProductName = N'Seiko Presage Sharp Edged' WHERE ProductName = 'Invalid Watch 123';
                """;
            try (PreparedStatement ps = conn.prepareStatement(sqlClean)) {
                ps.executeUpdate();
            }
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
            SELECT w.*, o.OrderCode, o.RecipientName, o.RecipientPhone, o.CreatedAt AS BuyDate, u.Email
            FROM Warranties w
            LEFT JOIN Orders o ON w.OrderID = o.OrderID
            LEFT JOIN Users u ON o.CustomerID = u.UserID
            WHERE 1=1
            """);

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(w.ProductName) LIKE ? OR LOWER(w.SerialNumber) LIKE ? OR LOWER(o.OrderCode) LIKE ? OR LOWER(o.RecipientName) LIKE ? OR LOWER(o.RecipientPhone) LIKE ?)");
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
            SELECT w.*, o.OrderCode, o.RecipientName, o.RecipientPhone, o.CreatedAt AS BuyDate, u.Email
            FROM Warranties w
            LEFT JOIN Orders o ON w.OrderID = o.OrderID
            LEFT JOIN Users u ON o.CustomerID = u.UserID
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
            VALUES (?, ?, ?, ?, GETDATE(), N'Đang bảo hành', ?)
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

    public boolean updateReceive(int id, Date receiveDate, String receiveNote) {
        String sql = "UPDATE Warranties SET Status = N'Đang sửa chữa', ReceiveDate = ?, ReceiveNote = ? WHERE WarrantyID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, receiveDate);
            ps.setString(2, receiveNote);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRepair(int id, String repairContent, String componentReplaced, String repairNote, Date completeDate) {
        String sql = "UPDATE Warranties SET Status = N'Đã sửa xong', RepairContent = ?, ComponentReplaced = ?, RepairNote = ?, CompleteDate = ? WHERE WarrantyID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, repairContent);
            ps.setString(2, componentReplaced);
            ps.setString(3, repairNote);
            ps.setDate(4, completeDate);
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateReturn(int id, Date returnDate) {
        String sql = "UPDATE Warranties SET Status = N'Đã trả khách', ReturnDate = ? WHERE WarrantyID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, returnDate);
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

        m.put("customerName",   rs.getString("RecipientName"));
        m.put("customerPhone",  rs.getString("RecipientPhone"));
        m.put("customerEmail",  rs.getString("Email"));
        m.put("buyDate",        rs.getTimestamp("BuyDate"));

        m.put("receiveDate",       rs.getDate("ReceiveDate"));
        m.put("receiveNote",       rs.getString("ReceiveNote"));
        m.put("repairContent",     rs.getString("RepairContent"));
        m.put("componentReplaced", rs.getString("ComponentReplaced"));
        m.put("repairNote",        rs.getString("RepairNote"));
        m.put("completeDate",      rs.getDate("CompleteDate"));
        m.put("returnDate",        rs.getDate("ReturnDate"));
        return m;
    }

    public List<Map<String, Object>> getWarrantiesFromReturnRequests() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT r.ReturnRequestID, r.ReturnCode, r.OrderID, o.OrderCode, " +
                     "u.FullName as CustomerName, r.Status, r.Reason, r.CreatedAt " +
                     "FROM dbo.ReturnRequests r " +
                     "JOIN dbo.Orders o ON r.OrderID = o.OrderID " +
                     "JOIN dbo.Users u ON r.CustomerID = u.UserID " +
                     "WHERE r.RequestType = 'WARRANTY' " +
                     "ORDER BY r.CreatedAt DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("returnRequestId", rs.getLong("ReturnRequestID"));
                map.put("returnCode", rs.getString("ReturnCode"));
                map.put("orderId", rs.getLong("OrderID"));
                map.put("orderCode", rs.getString("OrderCode"));
                map.put("customerName", rs.getString("CustomerName"));
                map.put("status", rs.getString("Status"));
                map.put("reason", rs.getString("Reason"));
                map.put("createdAt", rs.getTimestamp("CreatedAt"));
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
