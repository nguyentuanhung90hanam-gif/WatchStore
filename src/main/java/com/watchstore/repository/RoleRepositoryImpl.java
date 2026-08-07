package com.watchstore.repository;

import com.watchstore.model.Role;

import java.util.ArrayList;
import java.util.List;

public class RoleRepositoryImpl implements RoleRepository {

    private static final List<Role> ROLES = new ArrayList<>(
            List.of(
                    new Role(1, "ADMIN", "Quản trị viên", 1, true),
                    new Role(2, "SALES", "Nhân viên bán hàng", 3, true),
                    new Role(3, "WAREHOUSE", "Nhân viên kho", 2, true),
                    new Role(4, "CUSTOMER", "Khách hàng", 25, true)
            )
    );


    @Override
    public List<Role> findAll() {
        return ROLES;
    }


    @Override
    public List<Role> search(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return ROLES;
        }

        keyword = keyword.toLowerCase();

        List<Role> result = new ArrayList<>();

        for (Role role : ROLES) {

            if (role.getCode().toLowerCase().contains(keyword)
                    || role.getName().toLowerCase().contains(keyword)) {

                result.add(role);

            }

        }

        return result;
    }


    @Override
    public Role findById(int id) {

        for(Role role : ROLES){

            if(role.getId() == id){
                return role;
            }

        }

        return null;
    }


    @Override
    public void save(Role role) {

        ROLES.add(role);

    }


    @Override
    public void delete(int id) {

        ROLES.removeIf(role -> role.getId() == id);

    }


    @Override
    public void update(Role role) {

        for (int i = 0; i < ROLES.size(); i++) {

            if (ROLES.get(i).getId() == role.getId()) {

                ROLES.set(i, role);
                return;

            }

        }

    }


    // Tạo ID tự tăng
    public int generateId() {

        return ROLES.stream()
                .mapToInt(Role::getId)
                .max()
                .orElse(0) + 1;

    }

}