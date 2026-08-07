package com.watchstore.repository;

import com.watchstore.enums.Role;
import com.watchstore.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserRepositoryImpl implements UserRepository {

    private static final List<User> USERS = new ArrayList<>(List.of(

            new User(
                    1,
                    "Nguyễn Văn An",
                    "an@gmail.com",
                    "0988123456",
                    Role.ADMIN
            ),

            new User(
                    2,
                    "Trần Minh Tuấn",
                    "tuan@gmail.com",
                    "0988456789",
                    Role.SALES
            ),

            new User(
                    3,
                    "Lê Hoàng Nam",
                    "nam@gmail.com",
                    "0977665544",
                    Role.CUSTOMER
            )

    ));

    @Override
    public List<User> findAll() {
        return USERS;
    }

    @Override
    public List<User> search(String keyword) {

        List<User> result = new ArrayList<>();

        for (User u : USERS) {

            if (u.getFullName().toLowerCase().contains(keyword.toLowerCase())
                    || u.getEmail().toLowerCase().contains(keyword.toLowerCase())) {

                result.add(u);

            }

        }

        return result;
    }

    @Override
    public User findById(int id) {

        for (User u : USERS) {

            if (u.getId() == id) {
                return u;
            }

        }

        return null;
    }

    @Override
    public void save(User user) {

        USERS.add(user);

    }

    @Override
    public void update(User user) {

        for (int i = 0; i < USERS.size(); i++) {

            if (USERS.get(i).getId() == user.getId()) {

                USERS.set(i, user);
                return;

            }

        }

    }

    @Override
    public void delete(int id) {

        USERS.removeIf(u -> u.getId() == id);

    }

    public int generateId() {

        int max = 0;

        for (User u : USERS) {

            if (u.getId() > max) {
                max = u.getId();
            }

        }

        return max + 1;
    }

}