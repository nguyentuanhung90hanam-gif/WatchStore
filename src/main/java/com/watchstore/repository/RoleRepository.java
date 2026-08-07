package com.watchstore.repository;

import com.watchstore.model.Role;
import java.util.List;

public interface RoleRepository {

    List<Role> findAll();

    List<Role> search(String keyword);

    Role findById(int id);

    void save(Role role);

    void update(Role role);

    void delete(int id);
}