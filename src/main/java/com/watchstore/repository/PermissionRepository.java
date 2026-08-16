package com.watchstore.repository;

import com.watchstore.model.Permission;
import com.watchstore.model.User;
import java.util.List;
import java.util.Set;

public interface PermissionRepository {
    List<Permission> findAll();
    List<Permission> search(String keyword);
    List<User> findAllEmployees();
    Set<Integer> getUserPermissionIds(int userId);
    Set<String> getUserPermissionCodes(int userId);
    void updateUserPermissions(int userId, List<Integer> permissionIds);
}
