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
    private BigDecimal totalPrice;
    private String status;
    private Date createdAt;

    public Order() {
        this.createdAt = new Date();
    }

    public Order(String code, String customerName, LocalDateTime createdAt, BigDecimal totalPrice, OrderStatus status) {
        this.code = code;
        this.customerName = customerName;
        this.createdAt = createdAt != null ? Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant()) : new Date();
        this.totalPrice = totalPrice;
        this.status = status != null ? status.name() : "PENDING";
        this.shippingAddress = "Hà Nội";
    }

    public Order(int id, int userId, String customerName, String phone, double totalPrice, String status, Date createdAt) {
        this.id = id;
        this.code = "ORD" + id;
        this.userId = userId;
        this.customerName = customerName;
        this.phone = phone;
        this.shippingAddress = "Hà Nội";
        this.totalPrice = BigDecimal.valueOf(totalPrice);
        this.status = status;
        this.createdAt = createdAt != null ? createdAt : new Date();
    }

    public Order(int id, String code, int userId, String customerName, String phone, String shippingAddress, BigDecimal totalPrice, String status, Date createdAt) {
        this.id = id;
        this.code = code != null ? code : "ORD" + id;
        this.userId = userId;
        this.customerName = customerName;
        this.phone = phone;
        this.shippingAddress = shippingAddress;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt != null ? createdAt : new Date();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code != null ? code : "ORD" + id; }
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

    public String getShippingAddress() {
        return shippingAddress != null ? shippingAddress : "Chưa cập nhật địa chỉ";
    }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public BigDecimal getTotal() { return totalPrice; }

    public double getTotalAsDouble() {
        return totalPrice != null ? totalPrice.doubleValue() : 0.0;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getOrderDate() { return createdAt; }
}