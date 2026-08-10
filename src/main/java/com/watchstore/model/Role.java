package com.watchstore.model;

import java.time.LocalDateTime;

/**
 * Model khớp 100% bảng dbo.Roles.
 * userCount là trường hiển thị (transient), tính qua COUNT(UserRoles).
 */
public class Role {

    // ─── Columns from dbo.Roles ──────────────────────────────────────────────
    private int roleId;                 // INT IDENTITY(1,1) PRIMARY KEY
    private String roleCode;            // VARCHAR(30) NOT NULL, UNIQUE
    private String roleName;            // NVARCHAR(100) NOT NULL
    private String description;         // NVARCHAR(500) NULL
    private boolean isSystem;           // BIT NOT NULL DEFAULT 0
    private LocalDateTime createdAt;    // DATETIME2 NOT NULL DEFAULT SYSDATETIME()

    // ─── Transient: số user đang gán role này ────────────────────────────────
    private int userCount;

    // ─── Constructors ────────────────────────────────────────────────────────

    public Role() {
    }

    // ─── Getters / Setters ───────────────────────────────────────────────────

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(boolean isSystem) {
        this.isSystem = isSystem;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }
}