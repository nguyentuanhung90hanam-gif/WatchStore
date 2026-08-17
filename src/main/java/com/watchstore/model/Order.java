package com.watchstore.model;

import com.watchstore.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Order {

    private int id;
    private String code;
    private int userId;
    private String customerName;
    private String phone;
    private String shippingAddress;

    private BigDecimal totalPrice;
    private BigDecimal discountAmount;

    private OrderStatus status;
    private String paymentStatus;
    private String customerNote;

    private Date createdAt;

    private java.util.List<java.util.Map<String, Object>> items;
    private java.util.List<java.util.Map<String, Object>> orderDetails;

    public java.util.List<java.util.Map<String, Object>> getItems() {
        return items != null ? items : orderDetails;
    }

    public void setItems(java.util.List<java.util.Map<String, Object>> items) {
        this.items = items;
        this.orderDetails = items;
    }

    public java.util.List<java.util.Map<String, Object>> getOrderDetails() {
        return orderDetails != null ? orderDetails : items;
    }

    public void setOrderDetails(java.util.List<java.util.Map<String, Object>> orderDetails) {
        this.orderDetails = orderDetails;
        this.items = orderDetails;
    }

    public String getCreatedDate() {
        return getFormattedCreatedAt();
    }

    public BigDecimal getTotalAmount() {
        return getTotalPrice();
    }

    public String getOrderStatus() {
        return getStatus().name();
    }

    public Order() {
        this.createdAt = new Date();
        this.status = OrderStatus.PENDING;
        this.paymentStatus = "UNPAID";
        this.discountAmount = BigDecimal.ZERO;
    }

    public Order(
            String code,
            String customerName,
            LocalDateTime createdAt,
            BigDecimal totalPrice,
            OrderStatus status
    ) {
        this.code = code;
        this.customerName = customerName;
        this.createdAt = createdAt != null
                ? Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant())
                : new Date();
        this.totalPrice = totalPrice;
        this.status = status != null ? status : OrderStatus.PENDING;
        this.paymentStatus = "UNPAID";
        this.discountAmount = BigDecimal.ZERO;
    }

    public Order(
            int id,
            int userId,
            String customerName,
            String phone,
            double totalPrice,
            String status,
            Date createdAt
    ) {
        this.id = id;
        this.code = "ORD" + id;
        this.userId = userId;
        this.customerName = customerName;
        this.phone = phone;
        this.shippingAddress = "Hà Nội";
        this.totalPrice = BigDecimal.valueOf(totalPrice);
        this.status = parseStatus(status);
        this.createdAt = createdAt != null ? createdAt : new Date();
        this.paymentStatus = "UNPAID";
        this.discountAmount = BigDecimal.ZERO;
    }

    public Order(
            int id,
            String code,
            int userId,
            String customerName,
            String phone,
            String shippingAddress,
            BigDecimal totalPrice,
            String status,
            Date createdAt
    ) {
        this.id = id;
        this.code = code != null ? code : "ORD" + id;
        this.userId = userId;
        this.customerName = customerName;
        this.phone = phone;
        this.shippingAddress = shippingAddress;
        this.totalPrice = totalPrice;
        this.status = parseStatus(status);
        this.createdAt = createdAt != null ? createdAt : new Date();
        this.paymentStatus = "UNPAID";
        this.discountAmount = BigDecimal.ZERO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code != null ? code : "ORD" + id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCustomerId() {
        return userId;
    }

    public void setCustomerId(int customerId) {
        this.userId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCustomerPhone() {
        return phone;
    }

    public String getShippingAddress() {
        return shippingAddress != null
                ? shippingAddress
                : "Chưa cập nhật địa chỉ";
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice != null ? totalPrice : BigDecimal.ZERO;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getTotal() {
        return getTotalPrice();
    }

    public double getTotalAsDouble() {
        return getTotalPrice().doubleValue();
    }

    public OrderStatus getStatus() {
        return status != null ? status : OrderStatus.PENDING;
    }

    public void setStatus(OrderStatus status) {
        this.status = status != null ? status : OrderStatus.PENDING;
    }

    public void setStatus(String status) {
        this.status = parseStatus(status);
    }

    public OrderStatus getStatusEnum() {
        return getStatus();
    }

    public String getStatusCode() {
        return getStatus().name();
    }

    public String getStatusLabel() {
        return getStatus().getLabel();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt != null ? createdAt : new Date();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt != null
                ? Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant())
                : new Date();
    }

    public LocalDateTime getCreatedAtLocalDateTime() {
        if (createdAt == null) {
            return null;
        }

        return createdAt.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) {
            return "";
        }

        return createdAt.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public Date getOrderDate() {
        return createdAt;
    }

    public String getPaymentStatus() {
        return paymentStatus != null ? paymentStatus : "UNPAID";
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getCustomerNote() {
        return customerNote;
    }

    public void setCustomerNote(String customerNote) {
        this.customerNote = customerNote;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount != null
                ? discountAmount
                : BigDecimal.ZERO;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    private OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return OrderStatus.PENDING;
        }

        String normalized = status.trim().toUpperCase();

        try {
            return OrderStatus.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            if (normalized.contains("CONFIRM")) {
                return OrderStatus.CONFIRMED;
            }

            if (normalized.contains("SHIP")
                    || normalized.contains("DELIVER")) {
                return OrderStatus.SHIPPING;
            }

            if (normalized.contains("COMPLETE")
                    || normalized.contains("HOÀN THÀNH")) {
                return OrderStatus.COMPLETED;
            }

            if (normalized.contains("CANCEL")
                    || normalized.contains("HỦY")) {
                return OrderStatus.CANCELLED;
            }

            if (normalized.contains("RETURN")
                    || normalized.contains("TRẢ")) {
                return OrderStatus.RETURNED;
            }

            return OrderStatus.PENDING;
        }
    }
}