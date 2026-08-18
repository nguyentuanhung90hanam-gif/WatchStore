package com.watchstore.repository;

import com.watchstore.model.Role;
import java.util.List;

public interface RoleRepository {

    List<Role> findAll();

    List<Role> search(String keyword);

    Role findById(int id);

    boolean insert(Role role);

    boolean update(Role role);

    boolean delete(int id);

    boolean deleteById(int roleId);

    boolean isRoleInUse(int roleId);

    boolean existsByCode(String code, Integer excludeId);

    Role findByCode(String code);
}