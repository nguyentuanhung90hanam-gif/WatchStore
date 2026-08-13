package com.watchstore.repository;

import com.watchstore.model.Product;
import java.util.List;

public class ProductPage {
    private final List<Product> items;
    private final int page;
    private final int size;
    private final int totalItems;

    public ProductPage(List<Product> items, int page, int size, int totalItems) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalItems = totalItems;
    }

    public List<Product> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public int getTotalItems() { return totalItems; }
    public int getTotalPages() { return Math.max(1, (int) Math.ceil(totalItems / (double) size)); }
    public boolean isHasPrevious() { return page > 1; }
    public boolean isHasNext() { return page < getTotalPages(); }
}
