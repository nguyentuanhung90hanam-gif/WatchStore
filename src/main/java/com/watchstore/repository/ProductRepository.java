package com.watchstore.repository;

import com.watchstore.model.Product;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
        ProductSearchCriteria searchCriteria =
                criteria == null
                        ? new ProductSearchCriteria()
                        : criteria;

        List<Product> filtered = findAll().stream()
                .filter(product ->
                        contains(
                                product.getName(),
                                searchCriteria.getKeyword()
                        )
                                || contains(
                                product.getBrand(),
                                searchCriteria.getKeyword()
                        )
                )
                .filter(product ->
                        searchCriteria.getBrand() == null
                                || searchCriteria.getBrand().isBlank()
                                || equalsIgnoreCase(
                                product.getBrand(),
                                searchCriteria.getBrand()
                        )
                )
                .filter(product ->
                        searchCriteria.getCategory() == null
                                || searchCriteria.getCategory().isBlank()
                                || equalsIgnoreCase(
                                product.getCategoryName(),
                                searchCriteria.getCategory()
                        )
                )
                .filter(product ->
                        searchCriteria.getMinPrice() == null
                                || safePrice(product)
                                .compareTo(
                                        searchCriteria.getMinPrice()
                                ) >= 0
                )
                .filter(product ->
                        searchCriteria.getMaxPrice() == null
                                || safePrice(product)
                                .compareTo(
                                        searchCriteria.getMaxPrice()
                                ) <= 0
                )
                .filter(product ->
                        !searchCriteria.isInStockOnly()
                                || product.getStock() > 0
                )
                .sorted(
                        comparator(
                                searchCriteria.getSort()
                        )
                )
                .toList();

        int page = Math.max(
                searchCriteria.getPage(),
                1
        );

        int size = Math.max(
                searchCriteria.getSize(),
                1
        );

        int from = Math.min(
                searchCriteria.getOffset(),
                filtered.size()
        );

        int to = Math.min(
                from + size,
                filtered.size()
        );

        return new ProductPage(
                filtered.subList(from, to),
                page,
                size,
                filtered.size()
        );
    }

    default List<String> findBrands() {
        return findAll().stream()
                .map(Product::getBrand)
                .filter(value ->
                        value != null
                                && !value.isBlank()
                )
                .distinct()
                .sorted()
                .toList();
    }

    default List<String> findCategories() {
        return findAll().stream()
                .map(Product::getCategoryName)
                .filter(value ->
                        value != null
                                && !value.isBlank()
                )
                .distinct()
                .sorted()
                .toList();
    }

    private static boolean contains(
            String source,
            String keyword
    ) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        return source != null
                && source.toLowerCase(Locale.ROOT)
                .contains(
                        keyword.toLowerCase(Locale.ROOT)
                );
    }

    private static boolean equalsIgnoreCase(
            String source,
            String expected
    ) {
        return source != null
                && expected != null
                && source.equalsIgnoreCase(expected);
    }

    private static BigDecimal safePrice(
            Product product
    ) {
        if (product == null || product.getPrice() == null) {
            return BigDecimal.ZERO;
        }

        return product.getPrice();
    }

    private static Comparator<Product> comparator(
            String sort
    ) {
        Comparator<Product> newest =
                Comparator.comparingInt(
                        Product::getId
                ).reversed();

        if (sort == null || sort.isBlank()) {
            return newest;
        }

        return switch (sort) {
            case "price_asc" ->
                    Comparator.comparing(
                            ProductRepository::safePrice
                    );

            case "price_desc" ->
                    Comparator.comparing(
                            ProductRepository::safePrice
                    ).reversed();

            case "newest" ->
                    newest;

            default ->
                    newest;
        };
    }
}
