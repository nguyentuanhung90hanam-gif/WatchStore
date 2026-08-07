package com.watchstore.repository;

import com.watchstore.model.Category;

import java.util.List;

public interface CategoryRepository {

    List<Category> findAll();

    Category findById(Integer id);

    void save(Category category);

    void update(Category category);

    void delete(Integer id);

    List<Category> search(String keyword);
}