package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderRepository {

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
        String sql = "SELECT * FROM Orders WHERE CustomerID = ? ORDER BY OrderID DESC";
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
            SELECT pv.VariantName, p.ProductName, oi.Quantity, oi.Price, (oi.Quantity * oi.Price) AS LineTotal
            FROM OrderItems oi
            JOIN ProductVariants pv ON oi.VariantID = pv.VariantID
            JOIN Products p ON pv.ProductID = p.ProductID
            WHERE oi.OrderID = ?
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("productName", rs.getString("ProductName"));
                    item.put("variantName", rs.getString("VariantName"));
                    item.put("quantity", rs.getInt("Quantity"));
                    item.put("price", rs.getBigDecimal("Price"));
                    item.put("lineTotal", rs.getBigDecimal("LineTotal"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Order> search(String keyword, String status) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM Orders WHERE 1=1 ");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(RecipientName) LIKE ? OR RecipientPhone LIKE ? OR LOWER(OrderCode) LIKE ?) ");
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND OrderStatus = ? ");
        }
        sql.append("ORDER BY OrderID DESC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String keyPattern = "%" + keyword.trim().toLowerCase() + "%";
                ps.setString(paramIndex++, keyPattern);
                ps.setString(paramIndex++, keyPattern);
                ps.setString(paramIndex++, keyPattern);
            }
            if (status != null && !status.trim().isEmpty()) {
                ps.setString(paramIndex++, status.trim());
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
            (OrderCode, CustomerID, RecipientName, RecipientPhone, ShippingAddress, SubtotalAmount, DiscountAmount, ShippingFee, TaxAmount, TotalAmount, OrderStatus, PaymentStatus) 
            VALUES (?, ?, ?, ?, ?, ?, 0, 0, 0, ?, ?, 'UNPAID')
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, order.getCode());
            ps.setInt(2, order.getUserId() > 0 ? order.getUserId() : 4); // mặc định user 4 (customer@watchstore.vn)
            ps.setString(3, order.getCustomerName() != null ? order.getCustomerName() : "Khách hàng WatchStore");
            ps.setString(4, order.getPhone() != null ? order.getPhone() : "0988 686 868");
            ps.setString(5, order.getShippingAddress() != null ? order.getShippingAddress() : "Hà Nội");
            ps.setBigDecimal(6, order.getTotalPrice());
            ps.setBigDecimal(7, order.getTotalPrice());
            ps.setString(8, order.getStatus() != null ? order.getStatus() : "PENDING");

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE Orders SET OrderStatus = ? WHERE OrderID = ?";
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

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("OrderID"));
        order.setCode(rs.getString("OrderCode"));
        order.setUserId(rs.getInt("CustomerID"));
        order.setCustomerName(rs.getString("RecipientName"));
        order.setPhone(rs.getString("RecipientPhone"));
        order.setShippingAddress(rs.getString("ShippingAddress"));
        order.setTotalPrice(rs.getBigDecimal("TotalAmount"));
        order.setStatus(rs.getString("OrderStatus"));

        Timestamp time = rs.getTimestamp("CreatedAt");
        if (time != null) {
            order.setCreatedAt(new java.util.Date(time.getTime()));
        }
        return order;
    }
}