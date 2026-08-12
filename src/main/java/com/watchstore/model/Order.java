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
    private String paymentStatus;
    private Date createdAt;
    private BigDecimal discountAmount;

    public Order() {
        this.createdAt = new Date();
        this.paymentStatus = "UNPAID";
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

    public OrderStatus getStatusEnum() {
        if (status == null) return OrderStatus.PENDING;
        try {
            return OrderStatus.valueOf(status.toUpperCase().trim());
        } catch (Exception e) {
            String s = status.trim().toUpperCase();
            if (s.contains("CHỜ") || s.contains("XỬ LÝ") || s.contains("PENDING")) return OrderStatus.PENDING;
            if (s.contains("XÁC NHẬN") || s.contains("CONFIRMED")) return OrderStatus.CONFIRMED;
            if (s.contains("GIAO") || s.contains("SHIPPING") || s.contains("DELIVERED")) return OrderStatus.SHIPPING;
            if (s.contains("HOÀN THÀNH") || s.contains("COMPLETED")) return OrderStatus.COMPLETED;
            if (s.contains("HỦY") || s.contains("CANCELLED")) return OrderStatus.CANCELLED;
            if (s.contains("ĐỔI") || s.contains("TRẢ") || s.contains("RETURNED")) return OrderStatus.RETURNED;
            return OrderStatus.PENDING;
        }
    }

    public String getPaymentStatus() {
        return paymentStatus != null ? paymentStatus : "UNPAID";
    }
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getOrderDate() { return createdAt; }

    public BigDecimal getDiscountAmount() {
        return discountAmount != null ? discountAmount : BigDecimal.ZERO;
    }
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
}