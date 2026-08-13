package com.watchstore.model;

import com.watchstore.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Order {
    private int id;
    private String code;
    private int userId;
    private String customerName;
    private String phone;
    private String shippingAddress;
    private Date createdAt;
    private BigDecimal total;
    private OrderStatus status;

    public Order() {
        this.createdAt = new Date();
        this.status = OrderStatus.PENDING;
    }

    public Order(String code, String customerName, LocalDateTime createdAt, BigDecimal total, OrderStatus status) {
        this.code = code; this.customerName = customerName;
        this.createdAt = createdAt == null ? new Date() : Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant());
        this.total = total; this.status = status;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getCustomerId() { return userId; }
    public void setCustomerId(int customerId) { this.userId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCustomerPhone() { return phone; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getOrderDate() { return createdAt; }
    public BigDecimal getTotal() { return total; }
    public BigDecimal getTotalPrice() { return total; }
    public void setTotalPrice(BigDecimal total) { this.total = total; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setStatus(String status) {
        try {
            this.status = status == null || status.isBlank() ? OrderStatus.PENDING : OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            this.status = OrderStatus.PENDING;
        }
    }
    public String getStatusCode() { return status == null ? OrderStatus.PENDING.name() : status.name(); }
    public String getStatusLabel() { return status == null ? OrderStatus.PENDING.getLabel() : status.getLabel(); }
    public String getStatusCssClass() { return status == null ? OrderStatus.PENDING.getCssClass() : status.getCssClass(); }
}
