package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Order;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderRepository {

    static {
        try (Connection conn = DBContext.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = """
                IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[OrderHistory]') AND type in (N'U'))
                BEGIN
                    CREATE TABLE dbo.OrderHistory (
                        HistoryID           BIGINT IDENTITY(1,1) PRIMARY KEY,
                        OrderID             BIGINT NOT NULL,
                        ActionType          NVARCHAR(50) NOT NULL,
                        ActionDetail        NVARCHAR(1000) NOT NULL,
                        PerformedBy         NVARCHAR(150) NOT NULL,
                        CreatedAt           DATETIME2 NOT NULL DEFAULT SYSDATETIME()
                    );
                END
                """;
            stmt.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM Orders ORDER BY OrderID DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Order findById(int id) {
        String sql = "SELECT * FROM Orders WHERE OrderID = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Order findByCode(String code) {
        String sql = "SELECT * FROM Orders WHERE OrderCode = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Order> findByCustomerId(int customerId) {
        List<Order> list = new ArrayList<>();

        String sql = """
                SELECT *
                FROM Orders
                WHERE CustomerID = ?
                ORDER BY OrderID DESC
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Map<String, Object>> getOrderItems(int orderId) {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = """
                SELECT
                    pv.VariantName,
                    p.ProductName,
                    pv.SKU,
                    oi.Quantity,
                    oi.UnitPrice,
                    (oi.Quantity * oi.UnitPrice) AS LineTotal
                FROM OrderItems oi
                JOIN ProductVariants pv
                    ON oi.VariantID = pv.VariantID
                JOIN Products p
                    ON pv.ProductID = p.ProductID
                WHERE oi.OrderID = ?
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();

                    item.put("productName", rs.getString("ProductName"));
                    item.put("variantName", rs.getString("VariantName"));
                    item.put("sku", rs.getString("SKU"));
                    item.put("quantity", rs.getInt("Quantity"));
                    item.put("price", rs.getBigDecimal("UnitPrice"));
                    item.put("lineTotal", rs.getBigDecimal("LineTotal"));

                    list.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public long createFromCart(int userId, int addressId, String voucher, String payment, String note) throws SQLException {
        try(Connection c=DBContext.getConnection();java.sql.CallableStatement cs=c.prepareCall("{call dbo.sp_CreateOrderFromCart(?,?,?,?,?,?)}")){
            cs.setInt(1,userId);cs.setInt(2,addressId);
            if(voucher==null||voucher.isBlank())cs.setNull(3,Types.VARCHAR);else cs.setString(3,voucher.trim());
            cs.setString(4,payment==null||payment.isBlank()?"COD":payment);
            if(note==null)cs.setNull(5,Types.NVARCHAR);else cs.setString(5,note);
            cs.registerOutParameter(6,Types.BIGINT);cs.execute();return cs.getLong(6);
        }
    }

    public void cancel(long orderId, int userId, String reason) throws SQLException {
        try(Connection c=DBContext.getConnection();java.sql.CallableStatement cs=c.prepareCall("{call dbo.sp_CancelOrder(?,?,?)}")){cs.setLong(1,orderId);cs.setInt(2,userId);cs.setString(3,reason);cs.execute();}
    }

    public List<Order> search(String keyword, String status) {
        return search(keyword, status, null, null);
    }

    public List<Order> search(
            String keyword,
            String status,
            String fromDate,
            String toDate
    ) {
        return search(keyword, status, fromDate, toDate, null, null);
    }

    public List<Order> search(
            String keyword,
            String status,
            String fromDate,
            String toDate,
            BigDecimal minAmount,
            BigDecimal maxAmount
    ) {
        List<Order> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT * FROM Orders WHERE 1=1 "
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("""
                    AND (
                        LOWER(RecipientName) LIKE ?
                        OR RecipientPhone LIKE ?
                        OR LOWER(OrderCode) LIKE ?
                    )
                    """);
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND OrderStatus = ? ");
        }

        if (fromDate != null && !fromDate.trim().isEmpty()) {
            sql.append("AND CreatedAt >= ? ");
        }

        if (toDate != null && !toDate.trim().isEmpty()) {
            sql.append("AND CreatedAt <= ? ");
        }

        if (minAmount != null) {
            sql.append("AND TotalAmount >= ? ");
        }

        if (maxAmount != null) {
            sql.append("AND TotalAmount <= ? ");
        }

        sql.append("ORDER BY OrderID DESC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (keyword != null && !keyword.trim().isEmpty()) {
                String keyPattern =
                        "%" + keyword.trim().toLowerCase() + "%";

                ps.setString(paramIndex++, keyPattern);
                ps.setString(paramIndex++, keyPattern);
                ps.setString(paramIndex++, keyPattern);
            }

            if (status != null && !status.trim().isEmpty()) {
                ps.setString(paramIndex++, status.trim());
            }

            if (fromDate != null && !fromDate.trim().isEmpty()) {
                ps.setString(
                        paramIndex++,
                        fromDate.trim() + " 00:00:00"
                );
            }

            if (toDate != null && !toDate.trim().isEmpty()) {
                ps.setString(
                        paramIndex++,
                        toDate.trim() + " 23:59:59"
                );
            }

            if (minAmount != null) {
                ps.setBigDecimal(paramIndex++, minAmount);
            }

            if (maxAmount != null) {
                ps.setBigDecimal(paramIndex++, maxAmount);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void add(Order order) {
        String sql = """
                INSERT INTO Orders
                (
                    OrderCode,
                    CustomerID,
                    RecipientName,
                    RecipientPhone,
                    ShippingAddress,
                    SubtotalAmount,
                    DiscountAmount,
                    ShippingFee,
                    TotalAmount,
                    OrderStatus,
                    PaymentStatus
                )
                VALUES (?, ?, ?, ?, ?, ?, 0, 0, ?, ?, ?)
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, order.getCode());

            ps.setInt(
                    2,
                    order.getUserId() > 0
                            ? order.getUserId()
                            : 4
            );

            ps.setString(
                    3,
                    order.getCustomerName() != null
                            ? order.getCustomerName()
                            : "Khách hàng WatchStore"
            );

            ps.setString(
                    4,
                    order.getPhone() != null
                            ? order.getPhone()
                            : "0988 686 868"
            );

            ps.setString(
                    5,
                    order.getShippingAddress() != null
                            ? order.getShippingAddress()
                            : "Hà Nội"
            );

            BigDecimal totalPrice =
                    order.getTotalPrice() != null
                            ? order.getTotalPrice()
                            : BigDecimal.ZERO;

            ps.setBigDecimal(6, totalPrice);
            ps.setBigDecimal(7, totalPrice);

            ps.setString(
                    8,
                    order.getStatus() != null
                            ? order.getStatus().name()
                            : "PENDING"
            );

            ps.setString(
                    9,
                    order.getPaymentStatus() != null
                            ? order.getPaymentStatus()
                            : "UNPAID"
            );

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateStatus(int id, String status) {
        String sql = """
                UPDATE Orders
                SET OrderStatus = ?
                WHERE OrderID = ?
                """;

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

    public boolean updatePaymentStatus(int id, String paymentStatus) {
        String sql = "UPDATE Orders SET PaymentStatus = ? WHERE OrderID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentStatus);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Order order) {
        String sql = """
                UPDATE Orders
                SET RecipientName = ?,
                    RecipientPhone = ?,
                    ShippingAddress = ?,
                    TotalAmount = ?,
                    OrderStatus = ?,
                    PaymentStatus = ?
                WHERE OrderID = ?
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, order.getCustomerName());
            ps.setString(2, order.getPhone());
            ps.setString(3, order.getShippingAddress());

            BigDecimal totalPrice =
                    order.getTotalPrice() != null
                            ? order.getTotalPrice()
                            : BigDecimal.ZERO;

            ps.setBigDecimal(4, totalPrice);

            ps.setString(
                    5,
                    order.getStatus() != null
                            ? order.getStatus().name()
                            : "PENDING"
            );

            ps.setString(
                    6,
                    order.getPaymentStatus() != null
                            ? order.getPaymentStatus()
                            : "UNPAID"
            );

            ps.setInt(7, order.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String deleteItemsSql =
                "DELETE FROM OrderItems WHERE OrderID = ?";

        String deleteOrderSql =
                "DELETE FROM Orders WHERE OrderID = ?";

        Connection conn = null;

        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psItems =
                         conn.prepareStatement(deleteItemsSql);
                 PreparedStatement psOrder =
                         conn.prepareStatement(deleteOrderSql)) {

                psItems.setInt(1, id);
                psItems.executeUpdate();

                psOrder.setInt(1, id);

                int affectedRows =
                        psOrder.executeUpdate();

                conn.commit();

                return affectedRows > 0;

            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }

                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;

        } finally {
            closeQuietly(conn);
        }
    }

    private Order mapResultSetToOrder(ResultSet rs)
            throws SQLException {

        Order order = new Order();

        order.setId(
                rs.getInt("OrderID")
        );

        order.setCode(
                rs.getString("OrderCode")
        );

        order.setUserId(
                rs.getInt("CustomerID")
        );

        order.setCustomerName(
                rs.getString("RecipientName")
        );

        order.setPhone(
                rs.getString("RecipientPhone")
        );

        order.setShippingAddress(
                rs.getString("ShippingAddress")
        );

        BigDecimal totalAmount =
                rs.getBigDecimal("TotalAmount");

        order.setTotalPrice(
                totalAmount != null
                        ? totalAmount
                        : BigDecimal.ZERO
        );

        order.setStatus(
                rs.getString("OrderStatus")
        );

        order.setPaymentStatus(
                rs.getString("PaymentStatus")
        );

        BigDecimal discountAmount =
                rs.getBigDecimal("DiscountAmount");

        order.setDiscountAmount(
                discountAmount != null
                        ? discountAmount
                        : BigDecimal.ZERO
        );

        Timestamp time =
                rs.getTimestamp("CreatedAt");

        if (time != null) {
            order.setCreatedAt(
                    new java.util.Date(
                            time.getTime()
                    )
            );
        }

        return order;
    }

    private void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception ignored) {
            }
        }
    }

    public boolean cancelOrderAndRestoreStock(int orderId) {
        String updateOrderSql = """
                UPDATE Orders
                SET OrderStatus = 'CANCELLED',
                    CancelledAt = GETDATE()
                WHERE OrderID = ?
                  AND OrderStatus != 'CANCELLED'
                """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateOrderSql)) {
            ps.setInt(1, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void logHistory(long orderId, String actionType, String detail, String performedBy) {
        String sql = """
            INSERT INTO dbo.OrderHistory (OrderID, ActionType, ActionDetail, PerformedBy)
            VALUES (?, ?, ?, ?)
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setString(2, actionType);
            ps.setString(3, detail);
            ps.setString(4, performedBy);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getHistory(long orderId) {
        List<Map<String, Object>> historyList = new ArrayList<>();
        String sql = """
            SELECT HistoryID, ActionType, ActionDetail, PerformedBy, CreatedAt 
            FROM dbo.OrderHistory 
            WHERE OrderID = ? 
            ORDER BY HistoryID DESC
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getLong("HistoryID"));
                    map.put("actionType", rs.getString("ActionType"));
                    map.put("detail", rs.getString("ActionDetail"));
                    map.put("performedBy", rs.getString("PerformedBy"));
                    map.put("createdAt", rs.getTimestamp("CreatedAt"));
                    historyList.add(map);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return historyList;
    }
}
