package com.watchstore.model;

import com.watchstore.enums.Role;

public class User {
    private final int id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;

    public User(int id, String fullName, String email, String phone, Role role) {
        this.id = id; this.fullName = fullName; this.email = email; this.phone = phone; this.role = role;
    }
    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
