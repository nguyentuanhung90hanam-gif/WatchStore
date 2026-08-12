package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.enums.OrderStatus;
import com.watchstore.model.Order;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepositoryImpl {

    public List<Order> findAll() {
        return fetchOrders("SELECT OrderCode, RecipientName AS CustomerName, CreatedAt, TotalAmount, OrderStatus FROM dbo.Orders ORDER BY CreatedAt DESC");
    }

    public Optional<Order> findByCode(String code) {
        List<Order> orders = fetchOrders("SELECT OrderCode, RecipientName AS CustomerName, CreatedAt, TotalAmount, OrderStatus FROM dbo.Orders WHERE OrderCode = '" + code + "'");
        return orders.isEmpty() ? Optional.empty() : Optional.of(orders.get(0));
    }

    private List<Order> fetchOrders(String sql) {
        List<Order> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Order(
                        rs.getString("OrderCode"),
                        rs.getString("CustomerName"),
                        rs.getTimestamp("CreatedAt") != null ? rs.getTimestamp("CreatedAt").toLocalDateTime() : null,
                        rs.getBigDecimal("TotalAmount"),
                        parseStatus(rs.getString("OrderStatus"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private OrderStatus parseStatus(String statusStr) {
        if (statusStr == null) return OrderStatus.PENDING;
        try {
            return OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (Exception e) {
            return OrderStatus.PENDING;
        }
    }
}
