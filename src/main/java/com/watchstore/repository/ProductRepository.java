package com.watchstore.repository;

import com.watchstore.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    List<Product> findAll();
    List<Product> findFeatured();
    Optional<Product> findById(int id);
    List<Product> search(String keyword);
}
