package com.watchstore.model;

import java.time.LocalDateTime;

public class Banner {
    private int bannerId;
    private String bannerName;
    private String title;
    private String subtitle;
    private String imageUrl;
    private String targetUrl;
    private String positionCode;
    private int displayOrder;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String status;
    private Integer createdBy;
    private LocalDateTime createdAt;

    public Banner() {
    }

    public int getBannerId() { return bannerId; }
    public void setBannerId(int bannerId) { this.bannerId = bannerId; }

    public String getBannerName() { return bannerName; }
    public void setBannerName(String bannerName) { this.bannerName = bannerName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public String getPositionCode() { return positionCode; }
    public void setPositionCode(String positionCode) { this.positionCode = positionCode; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
