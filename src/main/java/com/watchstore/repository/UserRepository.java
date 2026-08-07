package com.watchstore.repository;

import com.watchstore.model.User;
import java.util.List;

public interface UserRepository {

    List<User> findAll();

    List<User> search(String keyword);

    User findById(int id);

    void save(User user);

    void update(User user);

    void delete(int id);

}