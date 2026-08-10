package com.watchstore.repository;

import com.watchstore.model.Permission;
import java.util.List;

public interface PermissionRepository {
    List<Permission> findAll();
    List<Permission> search(String keyword);
}
