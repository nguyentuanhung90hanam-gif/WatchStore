package com.watchstore.model;

import com.watchstore.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private final String code;
    private final String customerName;
    private final LocalDateTime createdAt;
    private final BigDecimal total;
    private OrderStatus status;

    public Order(String code, String customerName, LocalDateTime createdAt, BigDecimal total, OrderStatus status) {
        this.code = code; this.customerName = customerName; this.createdAt = createdAt; this.total = total; this.status = status;
    }
    public String getCode() { return code; }
    public String getCustomerName() { return customerName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public BigDecimal getTotal() { return total; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
