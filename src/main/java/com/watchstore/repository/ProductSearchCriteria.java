package com.watchstore.repository;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ProductSearchCriteria {
    private String keyword;
    private String brand;
    private String category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private boolean inStockOnly;
    private String sort;
    private int page = 1;
    private int size = 8;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = clean(keyword); }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = clean(brand); }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = clean(category); }
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public boolean isInStockOnly() { return inStockOnly; }
    public void setInStockOnly(boolean inStockOnly) { this.inStockOnly = inStockOnly; }
    public String getSort() { return sort == null || sort.isBlank() ? "featured" : sort; }
    public void setSort(String sort) {
        String value = clean(sort);
        this.sort = switch (value == null ? "" : value) {
            case "price_asc", "price_desc", "newest" -> value;
            default -> "featured";
        };
    }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = Math.max(1, page); }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size < 1 || size > 48 ? 8 : size; }
    public int getOffset() { return (page - 1) * size; }

    public String toQueryStringWithoutPage() {
        StringBuilder query = new StringBuilder();
        append(query, "q", keyword);
        append(query, "brand", brand);
        append(query, "category", category);
        append(query, "minPrice", minPrice == null ? null : minPrice.toPlainString());
        append(query, "maxPrice", maxPrice == null ? null : maxPrice.toPlainString());
        if (inStockOnly) append(query, "inStock", "1");
        append(query, "sort", getSort());
        append(query, "size", String.valueOf(size));
        return query.toString();
    }

    private static String clean(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static void append(StringBuilder query, String key, String value) {
        if (value == null || value.isBlank()) return;
        if (!query.isEmpty()) query.append('&');
        query.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
        query.append('=');
        query.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}
