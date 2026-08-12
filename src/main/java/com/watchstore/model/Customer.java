package com.watchstore.model;

import com.watchstore.enums.Role;

public class Customer extends User {

    public Customer() {
        super();
        setRole(Role.CUSTOMER);
    }

    // Overload 8 tham số
    public Customer(int id, String username, String password, String fullName, String email, String phone, String address) {
        super(id, username, password, fullName, email, phone, address, Role.CUSTOMER);
    }

    // Overload 5 tham số (Fix lỗi khi khởi tạo Customer rút gọn)
    public Customer(int id, String fullName, String email, String phone, Role role) {
        super(id, fullName, email, phone, role);
    }

    // Copy Constructor từ User
    public Customer(User user) {
        if (user != null) {
            setId(user.getId());
            setUsername(user.getUsername());
            setPassword(user.getPassword());
            setFullName(user.getFullName());
            setEmail(user.getEmail());
            setPhone(user.getPhone());
            setAddress(user.getAddress());
            setRole(Role.CUSTOMER);
        }
    }
}