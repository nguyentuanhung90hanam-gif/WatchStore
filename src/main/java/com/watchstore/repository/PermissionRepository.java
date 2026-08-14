package com.watchstore.repository;

import com.watchstore.model.Permission;
import java.util.List;

public interface PermissionRepository {
    List<Permission> findAll();
    List<Permission> search(String keyword);
    List<Integer> getPermissionIdsByRoleId(int roleId);
    boolean addRolePermission(int roleId, int permissionId);
    boolean removeRolePermission(int roleId, int permissionId);
}
