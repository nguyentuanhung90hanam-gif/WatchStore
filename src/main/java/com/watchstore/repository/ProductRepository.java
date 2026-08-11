package com.watchstore.repository;

import com.watchstore.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    List<Product> findAll();
    List<Product> findFeatured();
    Optional<Product> findById(int id);
    List<Product> search(String keyword);

    boolean insert(Product product);
    boolean update(Product product);
    boolean delete(int id);
    boolean existsByCode(String code, Integer excludeId);
    boolean existsBySlug(String slug, Integer excludeId);
    boolean existsBySku(String sku, Integer excludeId);
    boolean isProductInUse(int productId);
}
