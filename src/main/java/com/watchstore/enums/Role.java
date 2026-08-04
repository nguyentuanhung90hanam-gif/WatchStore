package com.watchstore.enums;

public enum Role {
    CUSTOMER("Khách hàng"), SALES("Nhân viên bán hàng"), WAREHOUSE("Nhân viên kho"), ADMIN("Quản trị viên");
    private final String label;
    Role(String label) { this.label = label; }
    public String getLabel() { return label; }
}
