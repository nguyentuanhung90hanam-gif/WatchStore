package com.watchstore.model;

import java.math.BigDecimal;
import java.util.Date;

public class Review {
    private long reviewId;
    private int productId;
    private Long orderItemId;
    private int userId;
    private int rating;
    private String reviewTitle;
    private String reviewContent;
    private boolean isVerifiedPurchase;
    private String status; // PENDING, APPROVED, REJECTED, HIDDEN
    private Date createdAt;
    private Date updatedAt;

    // View helper fields
    private String customerName;
    private String productName;
    private String productSku;
    
    // Reply detail
    private String replyContent;
    private Date replyCreatedAt;
    private String replierName;

    public Review() {
        this.status = "PENDING";
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isVerifiedPurchase = true;
    }

    public long getReviewId() { return reviewId; }
    public void setReviewId(long reviewId) { this.reviewId = reviewId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getReviewTitle() { return reviewTitle; }
    public void setReviewTitle(String reviewTitle) { this.reviewTitle = reviewTitle; }

    public String getReviewContent() { return reviewContent; }
    public void setReviewContent(String reviewContent) { this.reviewContent = reviewContent; }

    public boolean isVerifiedPurchase() { return isVerifiedPurchase; }
    public void setVerifiedPurchase(boolean verifiedPurchase) { isVerifiedPurchase = verifiedPurchase; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public String getReplyContent() { return replyContent; }
    public void setReplyContent(String replyContent) { this.replyContent = replyContent; }

    public Date getReplyCreatedAt() { return replyCreatedAt; }
    public void setReplyCreatedAt(Date replyCreatedAt) { this.replyCreatedAt = replyCreatedAt; }

    public String getReplierName() { return replierName; }
    public void setReplierName(String replierName) { this.replierName = replierName; }
}
