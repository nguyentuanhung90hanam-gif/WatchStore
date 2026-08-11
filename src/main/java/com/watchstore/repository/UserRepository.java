package com.watchstore.repository;

import com.watchstore.model.User;
import java.util.List;

public interface UserRepository {

    List<User> findAll();

    List<User> search(String keyword);

    User findById(int id);

    boolean insert(User user, List<Integer> roleIds);

    boolean update(User user, List<Integer> roleIds);

    boolean delete(int id);

    boolean existsByEmail(String email, Integer excludeId);

    boolean existsByPhone(String phone, Integer excludeId);

    boolean isUserInUse(int userId);
}