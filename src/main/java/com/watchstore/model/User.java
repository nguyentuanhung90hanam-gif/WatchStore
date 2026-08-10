package com.watchstore.model;

import com.watchstore.enums.Role;

public class User {

    private int id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String role;

    public User() {
    }

    // Constructor đầy đủ 8 tham số (Role dạng String)
    public User(int id, String username, String password, String fullName, String email, String phone, String address, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
    }

    // Constructor đầy đủ 8 tham số (Role dạng Enum)
    public User(int id, String username, String password, String fullName, String email, String phone, String address, Role role) {
        this(id, username, password, fullName, email, phone, address, role != null ? role.name() : "CUSTOMER");
    }

    // Constructor rút gọn 5 tham số (Bổ sung để khắc phục lỗi)
    public User(int id, String fullName, String email, String phone, Role role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role != null ? role.name() : "CUSTOMER";
    }

    // GETTERS & SETTERS
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRole() { return role != null ? role : "CUSTOMER"; }
    public void setRole(String role) { this.role = role; }
    public void setRole(Role role) { this.role = role != null ? role.name() : "CUSTOMER"; }

    public String getRoleLabel() {
        if (role == null) return "Khách hàng";
        if ("ADMIN".equalsIgnoreCase(role)) return "Quản trị viên";
        if ("SALES".equalsIgnoreCase(role)) return "Nhân viên bán hàng";
        if ("WAREHOUSE".equalsIgnoreCase(role)) return "Nhân viên kho";
        if ("CUSTOMER".equalsIgnoreCase(role)) return "Khách hàng";
        try {
            return Role.valueOf(role.toUpperCase()).getLabel();
        } catch (Exception e) {
            return role;
        }
    }
}