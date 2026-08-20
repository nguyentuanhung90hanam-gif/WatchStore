package com.watchstore.repository;

import com.watchstore.model.Review;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface ReviewRepository {
    boolean createReview(Review review) throws SQLException;
    List<Review> findByProductId(int productId);
    List<Review> findByUserId(int userId);
    List<Map<String, Object>> findPendingReviewItems(int userId);
    boolean hasUserReviewedProductInOrder(int userId, long orderId, int productId);
    List<Review> findAll();
    List<Review> search(String keyword, String status, Integer rating);
    boolean updateStatus(long reviewId, String status);
    Review findById(long reviewId);
}
