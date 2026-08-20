package com.watchstore.repository;

import com.watchstore.config.DBContext;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarrantyRepository {

    public List<Map<String, Object>> findAll() {
        return search(null, null);
    }

    public List<Map<String, Object>> search(String keyword, String status) {
        List<Map<String, Object>> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT w.*,
                   COALESCE(o.OrderCode, 'OFFLINE-' + CAST(w.WarrantyID AS VARCHAR)) AS DisplayOrderCode,
                   COALESCE(w.CustomerName, o.RecipientName, u.FullName, N'Khách mua tại quầy') AS DisplayCustomerName,
                   COALESCE(w.CustomerPhone, o.RecipientPhone, u.Phone, '—') AS DisplayCustomerPhone,
                   COALESCE(w.CustomerEmail, u.Email, '—') AS DisplayCustomerEmail,
                   COALESCE(w.StartDate, CAST(o.CreatedAt AS DATE), CAST(w.CreatedAt AS DATE)) AS DisplayBuyDate
            FROM dbo.Warranties w
            LEFT JOIN dbo.Orders o ON w.OrderID = o.OrderID
            LEFT JOIN dbo.Users u ON o.CustomerID = u.UserID
            WHERE 1=1
            """);

        if (keyword != null && !keyword.isBlank()) {
            sql.append("""
                AND (LOWER(w.ProductName) LIKE ?
                     OR LOWER(ISNULL(w.SerialNumber, '')) LIKE ?
                     OR LOWER(ISNULL(o.OrderCode, '')) LIKE ?
                     OR LOWER(ISNULL(w.CustomerName, '')) LIKE ?
                     OR LOWER(ISNULL(o.RecipientName, '')) LIKE ?
                     OR LOWER(ISNULL(w.CustomerPhone, '')) LIKE ?
                     OR LOWER(ISNULL(o.RecipientPhone, '')) LIKE ?)
            """);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND w.Status = ?");
        }
        sql.append(" ORDER BY w.WarrantyID DESC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (keyword != null && !keyword.isBlank()) {
                String k = "%" + keyword.trim().toLowerCase() + "%";
                ps.setString(idx++, k);
                ps.setString(idx++, k);
                ps.setString(idx++, k);
                ps.setString(idx++, k);
                ps.setString(idx++, k);
                ps.setString(idx++, k);
                ps.setString(idx++, k);
            }
            if (status != null && !status.isBlank()) {
                ps.setString(idx++, status.trim());
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
            SELECT w.*,
                   COALESCE(o.OrderCode, 'OFFLINE-' + CAST(w.WarrantyID AS VARCHAR)) AS DisplayOrderCode,
                   COALESCE(w.CustomerName, o.RecipientName, u.FullName, N'Khách mua tại quầy') AS DisplayCustomerName,
                   COALESCE(w.CustomerPhone, o.RecipientPhone, u.Phone, '—') AS DisplayCustomerPhone,
                   COALESCE(w.CustomerEmail, u.Email, '—') AS DisplayCustomerEmail,
                   COALESCE(w.StartDate, CAST(o.CreatedAt AS DATE), CAST(w.CreatedAt AS DATE)) AS DisplayBuyDate
            FROM dbo.Warranties w
            LEFT JOIN dbo.Orders o ON w.OrderID = o.OrderID
            LEFT JOIN dbo.Users u ON o.CustomerID = u.UserID
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

    public List<Map<String, Object>> findByOrderId(long orderId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT w.*,
                   o.OrderCode AS DisplayOrderCode,
                   o.RecipientName AS DisplayCustomerName,
                   o.RecipientPhone AS DisplayCustomerPhone,
                   u.Email AS DisplayCustomerEmail,
                   COALESCE(w.StartDate, CAST(o.CreatedAt AS DATE)) AS DisplayBuyDate
            FROM dbo.Warranties w
            JOIN dbo.Orders o ON w.OrderID = o.OrderID
            LEFT JOIN dbo.Users u ON o.CustomerID = u.UserID
            WHERE w.OrderID = ?
            ORDER BY w.WarrantyID DESC
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
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

    public List<Map<String, Object>> findByCustomerId(int customerId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT w.*,
                   o.OrderCode AS DisplayOrderCode,
                   o.RecipientName AS DisplayCustomerName,
                   o.RecipientPhone AS DisplayCustomerPhone,
                   u.Email AS DisplayCustomerEmail,
                   COALESCE(w.StartDate, CAST(o.CreatedAt AS DATE)) AS DisplayBuyDate
            FROM dbo.Warranties w
            JOIN dbo.Orders o ON w.OrderID = o.OrderID
            JOIN dbo.Users u ON o.CustomerID = u.UserID
            WHERE o.CustomerID = ?
            ORDER BY w.WarrantyID DESC
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
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

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM dbo.Warranties";
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
        String sql = "SELECT COUNT(*) FROM dbo.Warranties WHERE Status = ?";
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

    /**
     * A. LUỒNG ONLINE: Khách hàng gửi yêu cầu bảo hành cho đơn hàng của chính mình.
     */
    public boolean insertOnlineWarranty(long orderId, String productName, String reason, String imageUrl) throws SQLException {
        if (orderId <= 0 || productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Thông tin đơn hàng hoặc sản phẩm không hợp lệ.");
        }
        if (reason == null || reason.trim().length() < 3) {
            throw new IllegalArgumentException("Vui lòng mô tả chi tiết lỗi gặp phải (từ 3 ký tự trở lên).");
        }

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            // 1. Kiểm tra đơn hàng & trạng thái COMPLETED
            String checkOrderSql = """
                SELECT o.OrderID, o.CustomerID, o.OrderStatus, o.CreatedAt, o.RecipientName, o.RecipientPhone
                FROM dbo.Orders o
                WHERE o.OrderID = ?
                """;
            Timestamp orderCreatedAt = null;
            String orderStatus = "";
            String customerName = "";
            String customerPhone = "";

            try (PreparedStatement ps = conn.prepareStatement(checkOrderSql)) {
                ps.setLong(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Đơn hàng #" + orderId + " không tồn tại.");
                    }
                    orderStatus = rs.getString("OrderStatus");
                    orderCreatedAt = rs.getTimestamp("CreatedAt");
                    customerName = rs.getString("RecipientName");
                    customerPhone = rs.getString("RecipientPhone");
                }
            }

            if (!"COMPLETED".equalsIgnoreCase(orderStatus)) {
                throw new SQLException("Chỉ có thể gửi yêu cầu bảo hành đối với đơn hàng đã hoàn thành (COMPLETED).");
            }

            // 2. Lấy thông tin sản phẩm và thời hạn bảo hành snapshot từ OrderItems
            String checkItemSql = """
                SELECT TOP 1 oi.OrderItemID, oi.ProductName, oi.SKU, ISNULL(oi.WarrantyMonths, 12) AS WarrantyMonths,
                       DATEADD(month, ISNULL(oi.WarrantyMonths, 12), o.CreatedAt) AS WarrantyEndDate
                FROM dbo.OrderItems oi
                JOIN dbo.Orders o ON o.OrderID = oi.OrderID
                WHERE oi.OrderID = ? AND (oi.ProductName = ? OR oi.ProductName LIKE ?)
                """;

            int warrantyMonths = 12;
            String matchedProductName = productName.trim();
            Date startDate = new Date(orderCreatedAt != null ? orderCreatedAt.getTime() : System.currentTimeMillis());
            Date endDate = null;

            try (PreparedStatement ps = conn.prepareStatement(checkItemSql)) {
                ps.setLong(1, orderId);
                ps.setString(2, productName.trim());
                ps.setString(3, "%" + productName.trim() + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Sản phẩm '" + productName + "' không có trong đơn hàng #" + orderId + ".");
                    }
                    matchedProductName = rs.getString("ProductName");
                    warrantyMonths = rs.getInt("WarrantyMonths");
                    Timestamp endTs = rs.getTimestamp("WarrantyEndDate");
                    if (endTs != null) {
                        endDate = new Date(endTs.getTime());
                    }
                }
            }

            if (endDate == null) {
                LocalDate startLocal = startDate.toLocalDate();
                endDate = Date.valueOf(startLocal.plusMonths(warrantyMonths));
            }

            // 3. KIỂM TRA HẾT HẠN BẢO HÀNH (Backend validation)
            Date today = Date.valueOf(LocalDate.now());
            if (today.after(endDate)) {
                throw new SQLException("Sản phẩm đã hết thời hạn bảo hành (" + warrantyMonths + " tháng tính từ ngày mua).");
            }

            // 4. Kiểm tra xem sản phẩm đang có yêu cầu bảo hành chưa hoàn tất không
            if (hasActiveWarranty(orderId, matchedProductName)) {
                throw new SQLException("Sản phẩm này trong đơn hàng đang có yêu cầu bảo hành đang được xử lý.");
            }

            // 5. Thêm phiếu bảo hành mới (Trạng thái ban đầu của Online: 'Chờ tiếp nhận')
            String insertSql = """
                INSERT INTO dbo.Warranties
                (OrderID, ProductName, SerialNumber, WarrantyMonths, StartDate, EndDate, Status, Note, ImageUrl, CustomerName, CustomerPhone, WarrantyType, CreatedAt)
                VALUES (?, ?, '', ?, ?, ?, N'Chờ tiếp nhận', ?, ?, ?, ?, 'ONLINE', SYSDATETIME())
                """;

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setLong(1, orderId);
                ps.setString(2, matchedProductName);
                ps.setInt(3, warrantyMonths);
                ps.setDate(4, startDate);
                ps.setDate(5, endDate);
                ps.setString(6, reason.trim());
                if (imageUrl != null && !imageUrl.isBlank()) {
                    ps.setString(7, imageUrl.trim());
                } else {
                    ps.setNull(7, Types.NVARCHAR);
                }
                ps.setString(8, customerName);
                ps.setString(9, customerPhone);
                ps.executeUpdate();
            }

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

    /**
     * B. LUỒNG OFFLINE: Nhân viên lập phiếu bảo hành cho khách mua trực tiếp tại cửa hàng.
     * Trạng thái phiếu chuyển thẳng sang: 'Đang xử lý' (Không có bước xác nhận thừa).
     */
    public boolean insertOfflineWarranty(String customerName, String customerPhone, String customerEmail,
                                         String productName, String serial, int months, Date buyDate,
                                         String note, String imageUrl) throws SQLException {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tên sản phẩm bảo hành.");
        }
        if (months <= 0) {
            months = 12;
        }

        Date startDate = buyDate != null ? buyDate : Date.valueOf(LocalDate.now());
        LocalDate startLocal = startDate.toLocalDate();
        Date endDate = Date.valueOf(startLocal.plusMonths(months));
        Date receiveDate = Date.valueOf(LocalDate.now());

        String insertSql = """
            INSERT INTO dbo.Warranties
            (OrderID, CustomerName, CustomerPhone, CustomerEmail, ProductName, SerialNumber, WarrantyMonths, StartDate, EndDate, Status, Note, ImageUrl, ReceiveDate, ReceiveNote, WarrantyType, CreatedAt)
            VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?, N'Đang xử lý', ?, ?, ?, ?, 'OFFLINE', SYSDATETIME())
            """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, customerName != null ? customerName.trim() : "Khách mua tại quầy");
            ps.setString(2, customerPhone != null ? customerPhone.trim() : "");
            ps.setString(3, customerEmail != null ? customerEmail.trim() : "");
            ps.setString(4, productName.trim());
            ps.setString(5, serial != null ? serial.trim() : "");
            ps.setInt(6, months);
            ps.setDate(7, startDate);
            ps.setDate(8, endDate);
            ps.setString(9, note != null ? note.trim() : "");
            if (imageUrl != null && !imageUrl.isBlank()) {
                ps.setString(10, imageUrl.trim());
            } else {
                ps.setNull(10, Types.NVARCHAR);
            }
            ps.setDate(11, receiveDate);
            ps.setString(12, note != null ? "Tiếp nhận trực tiếp tại quầy: " + note.trim() : "Tiếp nhận tại quầy");

            return ps.executeUpdate() > 0;
        }
    }

    public boolean insert(int orderId, String productName, String serial, int months, String note) {
        try {
            return insertOnlineWarranty(orderId, productName, note, null);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE dbo.Warranties SET Status = ? WHERE WarrantyID = ?";
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

    public boolean updateStatusAndNote(int id, String status, String note) {
        String sql = "UPDATE dbo.Warranties SET Status = ?, Note = ? WHERE WarrantyID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, note);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateReceive(int id, Date receiveDate, String receiveNote) {
        String sql = "UPDATE dbo.Warranties SET Status = N'Đã tiếp nhận', ReceiveDate = ?, ReceiveNote = ? WHERE WarrantyID = ?";
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

    public boolean updateProcessing(int id) {
        String sql = "UPDATE dbo.Warranties SET Status = N'Đang xử lý' WHERE WarrantyID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRepair(int id, String repairContent, String componentReplaced, String repairNote, Date completeDate) {
        String sql = "UPDATE dbo.Warranties SET Status = N'Hoàn tất', RepairContent = ?, ComponentReplaced = ?, RepairNote = ?, CompleteDate = ? WHERE WarrantyID = ?";
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
        String sql = "UPDATE dbo.Warranties SET Status = N'Đã trả khách', ReturnDate = ? WHERE WarrantyID = ?";
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

    public boolean hasActiveWarranty(long orderId, String productName) {
        String sql = """
            SELECT COUNT(*) FROM dbo.Warranties 
            WHERE OrderID = ? AND (ProductName = ? OR ProductName LIKE ?)
              AND Status IN (N'Chờ tiếp nhận', N'Đã tiếp nhận', N'Đang xử lý')
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setString(2, productName);
            ps.setString(3, "%" + productName + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Map<String, Object> mapRow(ResultSet rs) throws SQLException {
        Map<String, Object> m = new HashMap<>();
        int id = rs.getInt("WarrantyID");
        m.put("id", id);

        long orderId = rs.getLong("OrderID");
        if (rs.wasNull()) {
            m.put("orderId", null);
            m.put("orderCode", "Mua tại quầy");
        } else {
            m.put("orderId", orderId);
            m.put("orderCode", rs.getString("DisplayOrderCode"));
        }

        m.put("productName", rs.getString("ProductName"));
        m.put("serial", rs.getString("SerialNumber"));
        int months = rs.getInt("WarrantyMonths");
        m.put("months", months > 0 ? months : 12);

        Date startDate = rs.getDate("StartDate");
        Date endDate = rs.getDate("EndDate");
        if (startDate == null) {
            startDate = rs.getDate("DisplayBuyDate");
        }
        if (endDate == null && startDate != null) {
            endDate = Date.valueOf(startDate.toLocalDate().plusMonths(months > 0 ? months : 12));
        }

        m.put("startDate", startDate);
        m.put("endDate", endDate);

        // Tính số ngày còn lại & trạng thái hết hạn
        Date today = Date.valueOf(LocalDate.now());
        boolean isExpired = endDate != null && today.after(endDate);
        long remainingDays = 0;
        if (endDate != null && !isExpired) {
            remainingDays = (endDate.getTime() - today.getTime()) / (1000L * 60 * 60 * 24);
        }
        m.put("isExpired", isExpired);
        m.put("remainingDays", remainingDays);

        m.put("status", rs.getString("Status"));
        m.put("note", rs.getString("Note"));
        m.put("imageUrl", rs.getString("ImageUrl"));
        m.put("warrantyType", rs.getString("WarrantyType"));
        m.put("createdAt", rs.getTimestamp("CreatedAt"));

        m.put("customerName", rs.getString("DisplayCustomerName"));
        m.put("customerPhone", rs.getString("DisplayCustomerPhone"));
        m.put("customerEmail", rs.getString("DisplayCustomerEmail"));
        m.put("buyDate", rs.getDate("DisplayBuyDate"));

        m.put("receiveDate", rs.getDate("ReceiveDate"));
        m.put("receiveNote", rs.getString("ReceiveNote"));
        m.put("repairContent", rs.getString("RepairContent"));
        m.put("componentReplaced", rs.getString("ComponentReplaced"));
        m.put("repairNote", rs.getString("RepairNote"));
        m.put("completeDate", rs.getDate("CompleteDate"));
        m.put("returnDate", rs.getDate("ReturnDate"));

        return m;
    }
}
