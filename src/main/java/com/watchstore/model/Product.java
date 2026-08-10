package com.watchstore.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Product {

    private int id;
    private String name;
    private BigDecimal price;
    private BigDecimal oldPrice;
    private int quantity;
    private String image;
    private String description;
    private String badge;
    private String brand;
    private double rating;

    public Product() {
        this.badge = "Hot";
        this.brand = "Chính hãng";
        this.rating = 5.0;
        this.oldPrice = BigDecimal.ZERO;
    }

    public Product(int id, String name, BigDecimal price, int quantity, String image, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.oldPrice = price.add(new BigDecimal("500000")); // Giá cũ giả lập
        this.quantity = quantity;
        this.image = image;
        this.description = description;
        this.badge = "Hot";
        this.brand = "Rolex";
        this.rating = 5.0;
    }

    public Product(int id, String name, BigDecimal price, BigDecimal oldPrice, int quantity,
                   String image, String description, String badge, String brand, double rating) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.oldPrice = oldPrice;
        this.quantity = quantity;
        this.image = image;
        this.description = description;
        this.badge = badge;
        this.brand = brand;
        this.rating = rating;
    }

    // Getter tính phần trăm giảm giá tự động cho JSP
    public int getDiscountPercent() {
        if (oldPrice == null || price == null || oldPrice.compareTo(price) <= 0 || oldPrice.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return oldPrice.subtract(price)
                .multiply(new BigDecimal(100))
                .divide(oldPrice, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getOldPrice() { return oldPrice; }
    public void setOldPrice(BigDecimal oldPrice) { this.oldPrice = oldPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getStock() { return quantity; }
    public void setStock(int stock) { this.quantity = stock; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}