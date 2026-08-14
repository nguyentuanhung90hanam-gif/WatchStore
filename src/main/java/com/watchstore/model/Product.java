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
        this.badge = "Hot";
        this.brandName = "WatchStore";
        this.ratingAverage = 5.0;
        this.compareAtPrice = BigDecimal.ZERO;
        this.price = BigDecimal.ZERO;
        this.status = "ACTIVE";
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

    public Product(int id, String name, BigDecimal price, BigDecimal oldPrice, int quantity,
                   String image, String description, String badge, String brand, double rating) {
        this(id, brand, name, null, price, oldPrice, image, badge, quantity, rating);
        this.description = description;
    }

    // ─── Legacy Getters/Setters for Guest/Cart/Customer Compatibility ────────
    public int getId() { return productId; }
    public void setId(int id) { this.productId = id; }

    public String getBrand() { return brandName != null ? brandName : ""; }
    public void setBrand(String brand) { this.brandName = brand; }

    public String getName() { return productName != null ? productName : ""; }
    public void setName(String name) { this.productName = name; }

    public String getSku() { return sku != null ? sku : (productCode != null ? productCode : ""); }
    public void setSku(String sku) { this.sku = sku; this.productCode = sku; }

    public BigDecimal getPrice() { return price != null ? price : BigDecimal.ZERO; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getOldPrice() { return compareAtPrice; }
    public void setOldPrice(BigDecimal oldPrice) { this.compareAtPrice = oldPrice; }

    public String getImage() { return imageUrl != null ? imageUrl : "watch-1.png"; }
    public void setImage(String image) { this.imageUrl = image; }

    public String getBadge() { return badge != null ? badge : (isFeatured ? "NỔI BẬT" : ""); }
    public void setBadge(String badge) { this.badge = badge; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getQuantity() { return stock; }
    public void setQuantity(int quantity) { this.stock = quantity; }

    public double getRating() { return ratingAverage; }
    public void setRating(double rating) { this.ratingAverage = rating; }

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

    public BigDecimal getCompareAtPrice() { return compareAtPrice; }
    public void setCompareAtPrice(BigDecimal compareAtPrice) { this.compareAtPrice = compareAtPrice; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
