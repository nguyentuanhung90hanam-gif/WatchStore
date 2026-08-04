package com.watchstore.model;

import java.math.BigDecimal;

public class Product {
    private final int id;
    private final String brand;
    private final String name;
    private final String sku;
    private final BigDecimal price;
    private final BigDecimal oldPrice;
    private final String image;
    private final String badge;
    private final int stock;
    private final double rating;

    public Product(int id, String brand, String name, String sku, BigDecimal price, BigDecimal oldPrice,
                   String image, String badge, int stock, double rating) {
        this.id = id; this.brand = brand; this.name = name; this.sku = sku; this.price = price;
        this.oldPrice = oldPrice; this.image = image; this.badge = badge; this.stock = stock; this.rating = rating;
    }
    public int getId() { return id; }
    public String getBrand() { return brand; }
    public String getName() { return name; }
    public String getSku() { return sku; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getOldPrice() { return oldPrice; }
    public String getImage() { return image; }
    public String getBadge() { return badge; }
    public int getStock() { return stock; }
    public double getRating() { return rating; }
    public int getDiscountPercent() {
        if (oldPrice == null || oldPrice.signum() == 0) return 0;
        return oldPrice.subtract(price).multiply(BigDecimal.valueOf(100)).divide(oldPrice, 0, java.math.RoundingMode.HALF_UP).intValue();
    }
}
