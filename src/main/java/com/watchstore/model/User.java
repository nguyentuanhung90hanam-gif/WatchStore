package com.watchstore.model;

import com.watchstore.enums.Role;

import java.time.LocalDate;

public class User {
    private final int id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private String gender;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private String status;

    public User(int id, String fullName, String email, String phone, Role role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public User(int id, String fullName, String email, String phone, Role role,
                String gender, LocalDate dateOfBirth, String avatarUrl, String status) {
        this(id, fullName, email, phone, role);
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.avatarUrl = avatarUrl;
        this.status = status;
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
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
