package com.watchstore.repository;

import com.watchstore.model.Product;

import java.util.List;

public class MockProductRepository extends ProductRepository {

    private final ProductRepository productRepository;

    public MockProductRepository() {
        this.productRepository = new ProductRepository();
    }

    /**
     * Lấy tất cả sản phẩm
     */
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    /**
     * Tìm sản phẩm theo ID
     */
    public Product findById(int id) {
        return productRepository.findById(id);
    }

    /**
     * Tìm sản phẩm theo tên
     */
    public List<Product> search(String keyword) {
        return productRepository.searchByName(keyword);
    }
}