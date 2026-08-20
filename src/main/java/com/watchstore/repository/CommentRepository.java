package com.watchstore.repository;

import com.watchstore.model.ProductComment;

import java.sql.SQLException;
import java.util.List;

public interface CommentRepository {
    boolean insert(int productId, int userId, String content) throws SQLException;
    List<ProductComment> findByProductId(int productId);
    List<ProductComment> findAll();
    List<ProductComment> search(String keyword, String status);
    boolean updateStatus(long commentId, String status);
    ProductComment findById(long commentId);
}
