package com.watchstore.model;

import java.math.BigDecimal;

public class Product {
    private int id;
    private String brand;
    private String name;
    private String sku;
    private BigDecimal price;
    private BigDecimal oldPrice;
    private String image;
    private String badge;
    private int stock;
    private double rating;
    private String description;

    public Product() {
        this.badge = "Hot";
        this.brand = "WatchStore";
        this.rating = 5.0;
        this.oldPrice = BigDecimal.ZERO;
        this.price = BigDecimal.ZERO;
    }

    public Product(int id, String brand, String name, String sku, BigDecimal price, BigDecimal oldPrice,
                   String image, String badge, int stock, double rating) {
        this.id = id; this.brand = brand; this.name = name; this.sku = sku; this.price = price;
        this.oldPrice = oldPrice; this.image = image; this.badge = badge; this.stock = stock; this.rating = rating;
    }

    public Product(int id, String name, BigDecimal price, BigDecimal oldPrice, int quantity,
                   String image, String description, String badge, String brand, double rating) {
        this(id, brand, name, null, price, oldPrice, image, badge, quantity, rating);
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getOldPrice() { return oldPrice; }
    public void setOldPrice(BigDecimal oldPrice) { this.oldPrice = oldPrice; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public int getQuantity() { return stock; }
    public void setQuantity(int quantity) { this.stock = quantity; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getDiscountPercent() {
        if (oldPrice == null || oldPrice.signum() == 0) return 0;
        return oldPrice.subtract(price).multiply(BigDecimal.valueOf(100)).divide(oldPrice, 0, java.math.RoundingMode.HALF_UP).intValue();
    }
}
