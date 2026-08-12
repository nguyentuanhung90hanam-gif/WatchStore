package com.watchstore.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {

    private int userId;
    private String email;
    private String passwordHash;
    private String fullName;
    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private String status;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<Role> roles = new ArrayList<>();
    private String roleNames;

    private com.watchstore.enums.Role role;

    public User() {
    }

    public User(
            int id,
            String fullName,
            String email,
            String phone,
            com.watchstore.enums.Role role
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
            com.watchstore.enums.Role role,
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

    public int getId() {
        return userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public String getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(String roleNames) {
        this.roleNames = roleNames;
    }

    public com.watchstore.enums.Role getRole() {
        if (role != null) {
            return role;
        }

        if (roles != null && !roles.isEmpty()) {
            for (Role r : roles) {
                if (r != null && r.getRoleCode() != null) {
                    String code = r.getRoleCode().trim().toUpperCase();

                    if ("ADMIN".equals(code)) {
                        return com.watchstore.enums.Role.ADMIN;
                    }

                    if ("SALES".equals(code)) {
                        return com.watchstore.enums.Role.SALES;
                    }

                    if ("WAREHOUSE".equals(code)) {
                        return com.watchstore.enums.Role.WAREHOUSE;
                    }
                }
            }
        }

        return com.watchstore.enums.Role.ADMIN;
    }

    public void setRole(com.watchstore.enums.Role role) {
        this.role = role;
    }
}