package com.watchstore.repository;

import com.watchstore.config.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReturnRepository {

    public boolean createReturn(int orderId, Integer orderItemId, int customerId, String productName, String reason) {
        String sql = """
            INSERT INTO Returns (OrderID, OrderItemID, CustomerID, ProductName, Reason, Status, RefundStatus, CreatedAt)
            VALUES (?, ?, ?, ?, ?, 'PENDING', 'UNREFUNDED', GETDATE())
            """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            if (orderItemId != null) {
                ps.setInt(2, orderItemId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, customerId);
            ps.setString(4, productName);
            ps.setString(5, reason);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> search(String keyword, String status) {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT r.*, o.OrderCode, u.FullName AS CustomerName, u.Phone AS CustomerPhone, u.Email AS CustomerEmail
            FROM Returns r
            JOIN Orders o ON r.OrderID = o.OrderID
            JOIN Users u ON r.CustomerID = u.UserID
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(u.FullName) LIKE ? OR LOWER(r.ProductName) LIKE ? OR LOWER(r.Reason) LIKE ? OR o.OrderCode LIKE ?)");
            String pat = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
            params.add("%" + keyword.trim() + "%");
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND r.Status = ?");
            params.add(status.trim());
        }
        sql.append(" ORDER BY r.ReturnID DESC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getInt("ReturnID"));
                    map.put("orderId", rs.getInt("OrderID"));
                    map.put("orderCode", rs.getString("OrderCode"));
                    map.put("customerId", rs.getInt("CustomerID"));
                    map.put("customerName", rs.getString("CustomerName"));
                    map.put("customerPhone", rs.getString("CustomerPhone"));
                    map.put("customerEmail", rs.getString("CustomerEmail"));
                    map.put("productName", rs.getString("ProductName"));
                    map.put("reason", rs.getString("Reason"));
                    map.put("status", rs.getString("Status"));
                    map.put("employeeNote", rs.getString("EmployeeNote"));
                    map.put("refundStatus", rs.getString("RefundStatus"));
                    map.put("createdAt", rs.getTimestamp("CreatedAt"));
                    list.add(map);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateStatus(int returnId, String status, String employeeNote) {
        String sql = "UPDATE Returns SET Status = ?, EmployeeNote = ?, UpdatedAt = GETDATE() WHERE ReturnID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, employeeNote);
            ps.setInt(3, returnId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRefundStatus(int returnId, String refundStatus) {
        String sql = "UPDATE Returns SET RefundStatus = ?, UpdatedAt = GETDATE() WHERE ReturnID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, refundStatus);
            ps.setInt(2, returnId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
