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

public class ProductRepositoryImpl implements ProductRepository {

    @Override
    public List<Product> findAll() {
        return fetchProducts("SELECT p.ProductID, b.BrandName, p.ProductName, p.ProductCode AS SKU, p.Price, NULL AS OldPrice, " +
                "(SELECT TOP 1 ImageUrl FROM dbo.ProductImages pi WHERE pi.ProductID = p.ProductID) AS ImageUrl, " +
                "NULL AS Badge, (SELECT ISNULL(SUM(QuantityOnHand), 0) FROM dbo.InventoryBalances ib " +
                "INNER JOIN dbo.ProductVariants pv ON ib.VariantID = pv.VariantID WHERE pv.ProductID = p.ProductID) AS Stock, " +
                "5.0 AS Rating " +
                "FROM dbo.Products p LEFT JOIN dbo.Brands b ON p.BrandID = b.BrandID");
    }

    @Override
    public List<Product> findFeatured() {
        return fetchProducts("SELECT TOP 4 p.ProductID, b.BrandName, p.ProductName, p.ProductCode AS SKU, p.Price, NULL AS OldPrice, " +
                "(SELECT TOP 1 ImageUrl FROM dbo.ProductImages pi WHERE pi.ProductID = p.ProductID) AS ImageUrl, " +
                "NULL AS Badge, (SELECT ISNULL(SUM(QuantityOnHand), 0) FROM dbo.InventoryBalances ib " +
                "INNER JOIN dbo.ProductVariants pv ON ib.VariantID = pv.VariantID WHERE pv.ProductID = p.ProductID) AS Stock, " +
                "5.0 AS Rating " +
                "FROM dbo.Products p LEFT JOIN dbo.Brands b ON p.BrandID = b.BrandID");
    }

    @Override
    public Optional<Product> findById(int id) {
        List<Product> products = fetchProducts("SELECT p.ProductID, b.BrandName, p.ProductName, p.ProductCode AS SKU, p.Price, NULL AS OldPrice, " +
                "(SELECT TOP 1 ImageUrl FROM dbo.ProductImages pi WHERE pi.ProductID = p.ProductID) AS ImageUrl, " +
                "NULL AS Badge, (SELECT ISNULL(SUM(QuantityOnHand), 0) FROM dbo.InventoryBalances ib " +
                "INNER JOIN dbo.ProductVariants pv ON ib.VariantID = pv.VariantID WHERE pv.ProductID = p.ProductID) AS Stock, " +
                "5.0 AS Rating " +
                "FROM dbo.Products p LEFT JOIN dbo.Brands b ON p.BrandID = b.BrandID WHERE p.ProductID = " + id);
        return products.isEmpty() ? Optional.empty() : Optional.of(products.get(0));
    }

    @Override
    public List<Product> search(String keyword) {
        String query = "SELECT p.ProductID, b.BrandName, p.ProductName, p.ProductCode AS SKU, " +
                "(SELECT ISNULL(MIN(SalePrice), 0) FROM dbo.ProductVariants pv WHERE pv.ProductID = p.ProductID) AS Price, " +
                "(SELECT ISNULL(MAX(CompareAtPrice), 0) FROM dbo.ProductVariants pv WHERE pv.ProductID = p.ProductID) AS OldPrice, " +
                "(SELECT TOP 1 ImageUrl FROM dbo.ProductImages pi WHERE pi.ProductID = p.ProductID) AS ImageUrl, " +
                "NULL AS Badge, (SELECT ISNULL(SUM(QuantityOnHand), 0) FROM dbo.InventoryBalances ib " +
                "INNER JOIN dbo.ProductVariants pv ON ib.VariantID = pv.VariantID WHERE pv.ProductID = p.ProductID) AS Stock, " +
                "5.0 AS Rating " +
                "FROM dbo.Products p LEFT JOIN dbo.Brands b ON p.BrandID = b.BrandID " +
                "WHERE p.ProductName LIKE ? OR p.ProductCode LIKE ?";

        List<Product> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<Product> fetchProducts(String sql) {
        List<Product> list = new ArrayList<>();
        // Note: Replaced p.Price with subquery on ProductVariants
        String query = sql.replace("p.Price", "(SELECT ISNULL(MIN(SalePrice), 0) FROM dbo.ProductVariants pv WHERE pv.ProductID = p.ProductID) AS Price")
                          .replace("NULL AS OldPrice", "(SELECT ISNULL(MAX(CompareAtPrice), 0) FROM dbo.ProductVariants pv WHERE pv.ProductID = p.ProductID) AS OldPrice");
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("ProductID"),
                rs.getString("BrandName") != null ? rs.getString("BrandName") : "Unknown",
                rs.getString("ProductName"),
                rs.getString("SKU"),
                rs.getBigDecimal("Price"),
                rs.getBigDecimal("OldPrice") != null ? rs.getBigDecimal("OldPrice") : rs.getBigDecimal("Price"),
                rs.getString("ImageUrl") != null ? rs.getString("ImageUrl") : "default.png",
                rs.getString("Badge") != null ? rs.getString("Badge") : "",
                rs.getInt("Stock"),
                rs.getDouble("Rating")
        );
    }
}
