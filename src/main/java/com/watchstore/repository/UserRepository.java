package com.watchstore.repository;

import com.watchstore.model.User;
import java.util.List;

public interface UserRepository {

    List<User> findAll();

    List<User> search(String keyword);

    List<User> searchByName(String keyword);

    User findById(int id);

    User findByEmail(String email);

    User findByPhone(String phone);

    boolean insert(User user, List<Integer> roleIds);

    boolean update(User user);

    boolean update(User user, List<Integer> roleIds);

    boolean updatePassword(int userId, String oldPassword, String newPassword);

    String hashPassword(String password);

    User login(String email, String password) throws Exception;

    User register(String fullName, String email, String phone, String password) throws Exception;

    boolean delete(int id);

    boolean existsByEmail(String email, Integer excludeId);

    boolean existsByPhone(String phone, Integer excludeId);

    boolean isUserInUse(int userId);
}