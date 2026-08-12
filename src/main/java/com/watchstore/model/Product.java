package com.watchstore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private int productId;
    private String productCode;
    private String productName;
    private String slug;
    private int brandId;
    private int categoryId;
    private String brandName;
    private String categoryName;
    private String movementType;
    private String gender;
    private String shortDescription;
    private String description;
    private String caseMaterial;
    private String glassMaterial;
    private String strapMaterial;
    private String waterResistance;
    private String originCountry;
    private int warrantyMonths;
    private String status;
    private boolean isFeatured;
    private double ratingAverage;
    private int ratingCount;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient fields for list/display compatibility
    private String sku;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private String imageUrl;
    private String badge;
    private int stock;

    public Product() {
    }

    public Product(int id, String brand, String name, String sku, BigDecimal price, BigDecimal oldPrice,
                   String image, String badge, int stock, double rating) {
        this.productId = id;
        this.brandName = brand;
        this.productName = name;
        this.productCode = sku;
        this.sku = sku;
        this.price = price;
        this.compareAtPrice = oldPrice;
        this.imageUrl = image;
        this.badge = badge;
        this.stock = stock;
        this.ratingAverage = rating;
        this.status = "ACTIVE";
    }

    // ─── Legacy Getters for Guest/Cart Compatibility ─────────────────────────
    public int getId() { return productId; }
    public String getBrand() { return brandName != null ? brandName : ""; }
    public String getName() { return productName != null ? productName : ""; }
    public String getSku() { return sku != null ? sku : (productCode != null ? productCode : ""); }
    public BigDecimal getPrice() { return price != null ? price : BigDecimal.ZERO; }
    public BigDecimal getOldPrice() { return compareAtPrice; }
    public String getImage() { return imageUrl != null ? imageUrl : "watch-1.png"; }
    public String getBadge() { return badge != null ? badge : (isFeatured ? "NỔI BẬT" : ""); }
    public int getStock() { return stock; }
    public double getRating() { return ratingAverage; }
    public int getDiscountPercent() {
        if (compareAtPrice == null || compareAtPrice.signum() == 0 || price == null) return 0;
        return compareAtPrice.subtract(price).multiply(BigDecimal.valueOf(100)).divide(compareAtPrice, 0, java.math.RoundingMode.HALF_UP).intValue();
    }

    // ─── Full Getters & Setters for Admin & Database ─────────────────────────
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public int getBrandId() { return brandId; }
    public void setBrandId(int brandId) { this.brandId = brandId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCaseMaterial() { return caseMaterial; }
    public void setCaseMaterial(String caseMaterial) { this.caseMaterial = caseMaterial; }

    public String getGlassMaterial() { return glassMaterial; }
    public void setGlassMaterial(String glassMaterial) { this.glassMaterial = glassMaterial; }

    public String getStrapMaterial() { return strapMaterial; }
    public void setStrapMaterial(String strapMaterial) { this.strapMaterial = strapMaterial; }

    public String getWaterResistance() { return waterResistance; }
    public void setWaterResistance(String waterResistance) { this.waterResistance = waterResistance; }

    public String getOriginCountry() { return originCountry; }
    public void setOriginCountry(String originCountry) { this.originCountry = originCountry; }

    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isFeatured() { return isFeatured; }
    public boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(boolean isFeatured) { this.isFeatured = isFeatured; }

    public double getRatingAverage() { return ratingAverage; }
    public void setRatingAverage(double ratingAverage) { this.ratingAverage = ratingAverage; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void setSku(String sku) { this.sku = sku; }

    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getCompareAtPrice() { return compareAtPrice; }
    public void setCompareAtPrice(BigDecimal compareAtPrice) { this.compareAtPrice = compareAtPrice; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public void setBadge(String badge) { this.badge = badge; }

    public void setStock(int stock) { this.stock = stock; }
}
