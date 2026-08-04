package com.watchstore.repository;

import com.watchstore.model.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class MockProductRepository implements ProductRepository {
    private static final List<Product> PRODUCTS = List.of(
        new Product(1, "CASIO", "Edifice Sapphire EFR-S108D", "CAS-EFR-108", new BigDecimal("3450000"), new BigDecimal("4290000"), "watch-1.png", "BÁN CHẠY", 18, 4.8),
        new Product(2, "ORIENT", "Bambino Open Heart Classic", "ORI-BAM-210", new BigDecimal("6790000"), new BigDecimal("7990000"), "watch-2.png", "-15%", 9, 4.9),
        new Product(3, "SEIKO", "Prospex Diver Automatic", "SEI-PRO-510", new BigDecimal("9890000"), new BigDecimal("11200000"), "watch-3.png", "MỚI", 5, 4.7),
        new Product(4, "FOSSIL", "Minimalist Mesh Rose Gold", "FOS-MIN-330", new BigDecimal("4250000"), new BigDecimal("4990000"), "watch-4.png", "ĐỘC QUYỀN", 22, 4.8),
        new Product(5, "CITIZEN", "Tsuyosa Automatic Blue", "CIT-TSU-040", new BigDecimal("8250000"), new BigDecimal("9200000"), "watch-1.png", "AUTOMATIC", 7, 4.9),
        new Product(6, "TISSOT", "Le Locle Powermatic 80", "TIS-LEL-080", new BigDecimal("16800000"), new BigDecimal("18500000"), "watch-2.png", "CAO CẤP", 3, 5.0),
        new Product(7, "G-SHOCK", "GA-B2100 Carbon Core", "GSH-GAB-210", new BigDecimal("3990000"), new BigDecimal("4590000"), "watch-3.png", "THỂ THAO", 26, 4.8),
        new Product(8, "FOSSIL", "Machine Chronograph", "FOS-MAC-420", new BigDecimal("5190000"), new BigDecimal("5890000"), "watch-4.png", "ƯU ĐÃI", 14, 4.6)
    );

    @Override public List<Product> findAll() { return PRODUCTS; }
    @Override public List<Product> findFeatured() { return PRODUCTS.subList(0, 4); }
    @Override public Optional<Product> findById(int id) { return PRODUCTS.stream().filter(p -> p.getId() == id).findFirst(); }
    @Override public List<Product> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return PRODUCTS;
        String value = keyword.toLowerCase(Locale.ROOT);
        return PRODUCTS.stream().filter(p -> (p.getName() + " " + p.getBrand()).toLowerCase(Locale.ROOT).contains(value)).toList();
    }
}
