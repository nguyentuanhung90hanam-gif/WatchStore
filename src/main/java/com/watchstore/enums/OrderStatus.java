package com.watchstore.enums;

public enum OrderStatus {
    PENDING("Chờ xác nhận", "warning"), CONFIRMED("Đã xác nhận", "info"), SHIPPING("Đang giao", "primary"),
    COMPLETED("Hoàn thành", "success"), CANCELLED("Đã hủy", "danger"), RETURNED("Đổi trả", "orange");
    private final String label;
    private final String cssClass;
    OrderStatus(String label, String cssClass) { this.label = label; this.cssClass = cssClass; }
    public String getLabel() { return label; }
    public String getCssClass() { return cssClass; }
}
