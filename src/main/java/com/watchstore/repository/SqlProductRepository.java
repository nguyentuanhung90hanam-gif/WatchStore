package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Product;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlProductRepository implements ProductRepository {
    private final ProductRepository fallback;

    public SqlProductRepository(ProductRepository fallback) {
        this.fallback = fallback;
    }

    @Override
    public List<Product> findAll() {
        return search(new ProductSearchCriteria()).getItems();
    }

    @Override
    public List<Product> findFeatured() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setSize(4);
        return search(criteria).getItems();
    }

    @Override
    public Optional<Product> findById(int id) {
        String sql = baseSelect() + " WHERE p.ProductID = ?";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            return fallback.findById(id);
        }
    }

    @Override
    public List<Product> search(String keyword) {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setKeyword(keyword);
        criteria.setSize(48);
        return search(criteria).getItems();
    }

    @Override
    public ProductPage search(ProductSearchCriteria criteria) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(criteria, params);
        String order = switch (criteria.getSort()) {
            case "price_asc" -> " ORDER BY Price ASC, p.ProductID DESC";
            case "price_desc" -> " ORDER BY Price DESC, p.ProductID DESC";
            case "newest" -> " ORDER BY p.ProductID DESC";
            default -> " ORDER BY p.IsFeatured DESC, p.ProductID DESC";
        };
        String sql = baseSelect() + where + order + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        String countSql = "SELECT COUNT(*) FROM Products p LEFT JOIN Brands b ON p.BrandID=b.BrandID LEFT JOIN Categories c ON p.CategoryID=c.CategoryID " + where;
        try (Connection con = DBContext.getConnection()) {
            int total;
            try (PreparedStatement ps = con.prepareStatement(countSql)) {
                bind(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    total = rs.getInt(1);
                }
            }
            List<Product> products = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                bind(ps, params);
                ps.setInt(params.size() + 1, criteria.getOffset());
                ps.setInt(params.size() + 2, criteria.getSize());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) products.add(map(rs));
                }
            }
            return new ProductPage(products, criteria.getPage(), criteria.getSize(), total);
        } catch (SQLException ex) {
            return fallback.search(criteria);
        }
    }

    @Override
    public List<String> findBrands() {
        return distinct("SELECT DISTINCT b.BrandName FROM Brands b JOIN Products p ON p.BrandID=b.BrandID WHERE b.BrandName IS NOT NULL ORDER BY b.BrandName", fallback.findBrands());
    }

    @Override
    public List<String> findCategories() {
        return distinct("SELECT DISTINCT c.CategoryName FROM Categories c JOIN Products p ON p.CategoryID=c.CategoryID WHERE c.CategoryName IS NOT NULL ORDER BY c.CategoryName", fallback.findCategories());
    }

    private static String baseSelect() {
        return """
            SELECT p.ProductID, p.ProductName, p.ProductCode, p.ShortDescription, p.IsFeatured, p.RatingAverage,
                   b.BrandName, c.CategoryName,
                   (SELECT TOP 1 ImageUrl FROM ProductImages WHERE ProductID=p.ProductID ORDER BY IsPrimary DESC, DisplayOrder ASC) AS ImageUrl,
                   (SELECT MIN(SalePrice) FROM ProductVariants WHERE ProductID=p.ProductID AND Status='ACTIVE') AS Price,
                   (SELECT MIN(CompareAtPrice) FROM ProductVariants WHERE ProductID=p.ProductID AND Status='ACTIVE') AS CompareAtPrice,
                   ISNULL((SELECT SUM(QuantityOnHand) FROM InventoryBalances ib JOIN ProductVariants pv ON pv.VariantID=ib.VariantID WHERE pv.ProductID=p.ProductID),0) AS Quantity
            FROM Products p
            LEFT JOIN Brands b ON p.BrandID=b.BrandID
            LEFT JOIN Categories c ON p.CategoryID=c.CategoryID
            """;
    }

    private static String buildWhere(ProductSearchCriteria criteria, List<Object> params) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (criteria.getKeyword() != null) {
            where.append(" AND (LOWER(p.ProductName) LIKE ? OR LOWER(ISNULL(b.BrandName,'')) LIKE ?)");
            String value = "%" + criteria.getKeyword().toLowerCase() + "%";
            params.add(value);
            params.add(value);
        }
        if (criteria.getBrand() != null) {
            where.append(" AND b.BrandName = ?");
            params.add(criteria.getBrand());
        }
        if (criteria.getCategory() != null) {
            where.append(" AND c.CategoryName = ?");
            params.add(criteria.getCategory());
        }
        if (criteria.getMinPrice() != null) {
            where.append(" AND (SELECT MIN(SalePrice) FROM ProductVariants WHERE ProductID=p.ProductID AND Status='ACTIVE') >= ?");
            params.add(criteria.getMinPrice());
        }
        if (criteria.getMaxPrice() != null) {
            where.append(" AND (SELECT MIN(SalePrice) FROM ProductVariants WHERE ProductID=p.ProductID AND Status='ACTIVE') <= ?");
            params.add(criteria.getMaxPrice());
        }
        if (criteria.isInStockOnly()) {
            where.append(" AND ISNULL((SELECT SUM(QuantityOnHand) FROM InventoryBalances ib JOIN ProductVariants pv ON pv.VariantID=ib.VariantID WHERE pv.ProductID=p.ProductID),0) > 0");
        }
        return where.toString();
    }

    private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value instanceof BigDecimal decimal) ps.setBigDecimal(i + 1, decimal);
            else ps.setString(i + 1, String.valueOf(value));
        }
    }

    private List<String> distinct(String sql, List<String> fallbackValues) {
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<String> values = new ArrayList<>();
            while (rs.next()) values.add(rs.getString(1));
            return values;
        } catch (SQLException ex) {
            return fallbackValues;
        }
    }

    private static Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("ProductID"));
        p.setName(rs.getString("ProductName"));
        p.setSku(rs.getString("ProductCode"));
        p.setDescription(rs.getString("ShortDescription"));
        p.setBadge(rs.getBoolean("IsFeatured") ? "Featured" : "Hot");
        p.setRating(rs.getDouble("RatingAverage"));
        p.setBrand(rs.getString("BrandName") == null ? "WatchStore" : rs.getString("BrandName"));
        p.setImage(rs.getString("ImageUrl") == null ? "watch-1.png" : rs.getString("ImageUrl"));
        p.setPrice(rs.getBigDecimal("Price") == null ? BigDecimal.ZERO : rs.getBigDecimal("Price"));
        p.setOldPrice(rs.getBigDecimal("CompareAtPrice"));
        p.setQuantity(rs.getInt("Quantity"));
        return p;
    }
}
