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
    private Role role;

    public User() {
        this.role = Role.CUSTOMER;
    }

    public User(int id, String fullName, String email, String phone, Role role) {
        this.id = id; this.fullName = fullName; this.email = email; this.phone = phone; this.role = role;
    }
    public User(int id, String username, String password, String fullName, String email, String phone, String address, String role) {
        this.id = id; this.username = username; this.password = password; this.fullName = fullName;
        this.email = email; this.phone = phone; this.address = address; setRole(role);
    }
    public int getId() { return id; }
    public int getUserId() { return id; }
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
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public void setRole(String role) {
        try {
            this.role = role == null || role.isBlank() ? Role.CUSTOMER : Role.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            this.role = Role.CUSTOMER;
        }
    }
    public String getRoleCode() { return role == null ? Role.CUSTOMER.name() : role.name(); }
    public String getRoleLabel() { return role == null ? Role.CUSTOMER.getLabel() : role.getLabel(); }
}
