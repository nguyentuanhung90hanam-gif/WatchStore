package com.watchstore.repository;

import com.watchstore.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    List<Product> findAll();
    List<Product> findFeatured();
    Optional<Product> findById(int id);
    List<Product> search(String keyword);

    boolean insert(Product product);
    boolean update(Product product);
    boolean delete(int id);
    boolean existsByCode(String code, Integer excludeId);
    boolean existsBySlug(String slug, Integer excludeId);
    boolean existsBySku(String sku, Integer excludeId);
    boolean isProductInUse(int productId);

    default ProductPage search(ProductSearchCriteria criteria) {
        List<Product> filtered = findAll().stream()
            .filter(p -> contains(p.getName(), criteria.getKeyword()) || contains(p.getBrand(), criteria.getKeyword()) || criteria.getKeyword() == null)
            .filter(p -> criteria.getBrand() == null || equalsIgnoreCase(p.getBrand(), criteria.getBrand()))
            .filter(p -> criteria.getMinPrice() == null || safePrice(p).compareTo(criteria.getMinPrice()) >= 0)
            .filter(p -> criteria.getMaxPrice() == null || safePrice(p).compareTo(criteria.getMaxPrice()) <= 0)
            .filter(p -> !criteria.isInStockOnly() || p.getStock() > 0)
            .sorted(comparator(criteria.getSort()))
            .toList();
        int from = Math.min(criteria.getOffset(), filtered.size());
        int to = Math.min(from + criteria.getSize(), filtered.size());
        return new ProductPage(filtered.subList(from, to), criteria.getPage(), criteria.getSize(), filtered.size());
    }

    default List<String> findBrands() {
        return findAll().stream().map(Product::getBrand).filter(v -> v != null && !v.isBlank()).distinct().sorted().toList();
    }

    default List<String> findCategories() {
        return List.of();
    }

    private static boolean contains(String source, String keyword) {
        if (keyword == null || keyword.isBlank()) return true;
        return source != null && source.toLowerCase(java.util.Locale.ROOT).contains(keyword.toLowerCase(java.util.Locale.ROOT));
    }

    private static boolean equalsIgnoreCase(String source, String expected) {
        return source != null && source.equalsIgnoreCase(expected);
    }

    private static java.math.BigDecimal safePrice(Product product) {
        return product.getPrice() == null ? java.math.BigDecimal.ZERO : product.getPrice();
    }

    private static java.util.Comparator<Product> comparator(String sort) {
        java.util.Comparator<Product> newest = java.util.Comparator.comparingInt(Product::getId).reversed();
        return switch (sort) {
            case "price_asc" -> java.util.Comparator.comparing(ProductRepository::safePrice);
            case "price_desc" -> java.util.Comparator.comparing(ProductRepository::safePrice).reversed();
            case "newest" -> newest;
            default -> newest;
        };
    }
}
