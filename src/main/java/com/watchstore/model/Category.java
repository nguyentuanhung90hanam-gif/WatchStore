package com.watchstore.model;

public class Category {

    private Integer categoryId;
    private Integer parentCategoryId;
    private String categoryCode;
    private String categoryName;
    private String slug;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    private String status;

    public Category() {
    }

    public Category(Integer categoryId, Integer parentCategoryId, String categoryCode, String categoryName, String slug, String description, String imageUrl, Integer displayOrder, String status) {
        this.categoryId = categoryId;
        this.parentCategoryId = parentCategoryId;
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.slug = slug;
        this.description = description;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
        this.status = status;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Integer parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    // Alias for backward compatibility
    public Integer getParentId() {
        return parentCategoryId;
    }

    public void setParentId(Integer parentId) {
        this.parentCategoryId = parentId;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Alias for backward compatibility
    public String getImage() {
        return imageUrl;
    }

    public void setImage(String image) {
        this.imageUrl = image;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    // Alias for backward compatibility
    public Integer getSortOrder() {
        return displayOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.displayOrder = sortOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}