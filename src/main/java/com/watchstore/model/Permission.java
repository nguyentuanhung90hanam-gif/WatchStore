package com.watchstore.model;

public class Permission {
    private int permissionId;
    private String permissionCode;
    private String permissionName;
    private String moduleCode;
    private String description;

    public Permission() {
    }

    public int getPermissionId() { return permissionId; }
    public void setPermissionId(int permissionId) { this.permissionId = permissionId; }

    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }

    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }

    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
