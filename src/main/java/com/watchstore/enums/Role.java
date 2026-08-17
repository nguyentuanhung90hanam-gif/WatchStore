package com.watchstore.enums;

public enum Role {
    CUSTOMER("Khách hàng"), EMPLOYEE("Nhân viên bán hàng"), ADMIN("Quản trị viên");
    private final String label;
    Role(String label) { this.label = label; }
    public String getLabel() { return label; }
}
