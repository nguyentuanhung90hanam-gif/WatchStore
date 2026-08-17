package com.watchstore.model;

import com.watchstore.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {

    private int userId;
    private String username;

    private String email;
    private String passwordHash;
    private String fullName;
    private String phone;
    private String address;

    private String gender;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private String status;

    private LocalDateTime emailVerifiedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<com.watchstore.model.Role> roles = new ArrayList<>();
    private String roleNames;

    private Role role;

    public User() {
    }

    public User(
            int id,
            String fullName,
            String email,
            String phone,
            Role role
    ) {
        this.userId = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public User(
            int id,
            String fullName,
            String email,
            String phone,
            Role role,
            String gender,
            LocalDate dateOfBirth,
            String avatarUrl,
            String status
    ) {
        this(id, fullName, email, phone, role);
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.avatarUrl = avatarUrl;
        this.status = status;
    }

    public User(
            int id,
            String username,
            String password,
            String fullName,
            String email,
            String phone,
            String address,
            String role
    ) {
        this.userId = id;
        this.username = username;
        this.passwordHash = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = parseRole(role);
    }

    public User(
            int id,
            String username,
            String password,
            String fullName,
            String email,
            String phone,
            String address,
            Role role
    ) {
        this(
                id,
                username,
                password,
                fullName,
                email,
                phone,
                address,
                role == null ? "CUSTOMER" : role.name()
        );
    }

    public int getId() {
        return userId;
    }

    public void setId(int id) {
        this.userId = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPassword() {
        return passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<com.watchstore.model.Role> getRoles() {
        return roles;
    }

    public void setRoles(List<com.watchstore.model.Role> roles) {
        this.roles = roles;
    }

    public String getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(String roleNames) {
        this.roleNames = roleNames;
    }

    public Role getRole() {
        if (role != null) {
            return role;
        }

        if (roles != null && !roles.isEmpty()) {
            for (com.watchstore.model.Role item : roles) {
                if (item == null || item.getRoleCode() == null) {
                    continue;
                }

                String code = item.getRoleCode().trim().toUpperCase();

                if ("ADMIN".equals(code)) {
                    return Role.ADMIN;
                }

                if ("EMPLOYEE".equals(code)) {
                    return Role.EMPLOYEE;
                }

                if ("CUSTOMER".equals(code)) {
                    return Role.CUSTOMER;
                }
            }
        }

        return Role.CUSTOMER;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setRole(String role) {
        this.role = parseRole(role);
    }

    public String getRoleCode() {
        return getRole().name();
    }

    public String getRoleLabel() {
        Role currentRole = getRole();

        if (currentRole == null) {
            return "Khách hàng";
        }

        switch (currentRole) {
            case ADMIN:
                return "Quản trị viên";
            case EMPLOYEE:
                return "Nhân viên";
            case CUSTOMER:
            default:
                return "Khách hàng";
        }
    }

    private Role parseRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return Role.CUSTOMER;
        }

        try {
            return Role.valueOf(roleCode.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Role.CUSTOMER;
        }
    }
}