package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepositoryImpl implements ProductRepository {

    private Connection getConnection() throws SQLException {
        return DBContext.getConnection();
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("ProductID"));
        p.setProductCode(rs.getString("ProductCode"));
        p.setProductName(rs.getString("ProductName"));
        p.setSlug(rs.getString("ProductSlug"));
        p.setBrandId(rs.getInt("BrandID"));
        p.setCategoryId(rs.getInt("CategoryID"));

        try { p.setBrandName(rs.getString("BrandName")); } catch (SQLException ignored) {}
        try { p.setCategoryName(rs.getString("CategoryName")); } catch (SQLException ignored) {}

        p.setMovementType(rs.getString("MovementType"));
        p.setGender(rs.getString("Gender"));
        p.setShortDescription(rs.getString("ShortDescription"));
        p.setDescription(rs.getString("Description"));
        p.setCaseMaterial(rs.getString("CaseMaterial"));
        p.setGlassMaterial(rs.getString("GlassMaterial"));
        p.setStrapMaterial(rs.getString("StrapMaterial"));
        p.setWaterResistance(rs.getString("WaterResistance"));
        p.setOriginCountry(rs.getString("OriginCountry"));
        p.setWarrantyMonths(rs.getInt("WarrantyMonths"));
        p.setStatus(rs.getString("Status"));
        p.setIsFeatured(rs.getBoolean("IsFeatured"));
        p.setRatingAverage(rs.getDouble("RatingAverage"));
        p.setRatingCount(rs.getInt("RatingCount"));

        if (rs.getObject("CreatedBy") != null) {
            p.setCreatedBy(rs.getInt("CreatedBy"));
        }

        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) p.setCreatedAt(ts.toLocalDateTime());
        ts = rs.getTimestamp("UpdatedAt");
        if (ts != null) p.setUpdatedAt(ts.toLocalDateTime());

        try {
            BigDecimal price = rs.getBigDecimal("SalePrice");
            if (price != null) p.setPrice(price);
        } catch (SQLException ignored) {}

        try {
            BigDecimal compPrice = rs.getBigDecimal("CompareAtPrice");
            p.setCompareAtPrice(compPrice);
        } catch (SQLException ignored) {}

        try {
            p.setSku(rs.getString("SKU"));
        } catch (SQLException ignored) {}

        try {
            p.setStock(rs.getInt("TotalStock"));
        } catch (SQLException ignored) {}

        try {
            String img = rs.getString("PrimaryImageUrl");
            if (img != null && !img.isBlank()) {
                p.setImageUrl(img);
            } else {
                p.setImageUrl("watch-1.png");
            }
        } catch (SQLException ignored) {}

        return p;
    }

    private String getSelectSql() {
        return """
            SELECT p.*,
                   b.BrandName,
                   c.CategoryName,
                   pv.SKU,
                   pv.SalePrice,
                   pv.CompareAtPrice,
                   10 AS TotalStock,
                   (SELECT TOP 1 ImageUrl FROM ProductImages pi WHERE pi.ProductID = p.ProductID ORDER BY pi.IsPrimary DESC, pi.DisplayOrder ASC) AS PrimaryImageUrl
            FROM Products p
            LEFT JOIN Brands b ON p.BrandID = b.BrandID
            LEFT JOIN Categories c ON p.CategoryID = c.CategoryID
            LEFT JOIN ProductVariants pv ON p.ProductID = pv.ProductID
            """;
    }

    @Override
    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql = getSelectSql() + " ORDER BY p.ProductID DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Product> findFeatured() {
        List<Product> list = new ArrayList<>();
        String sql = getSelectSql() + " WHERE p.IsFeatured = 1 AND p.Status = 'ACTIVE' ORDER BY p.ProductID DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Optional<Product> findById(int id) {
        String sql = getSelectSql() + " WHERE p.ProductID = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Product> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        List<Product> list = new ArrayList<>();
        String sql = getSelectSql() + """
            WHERE p.ProductCode LIKE ?
               OR p.ProductName LIKE ?
               OR b.BrandName LIKE ?
               OR c.CategoryName LIKE ?
            ORDER BY p.ProductID DESC
            """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String val = "%" + keyword.trim() + "%";
            ps.setString(1, val);
            ps.setString(2, val);
            ps.setString(3, val);
            ps.setString(4, val);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean insert(Product product) {
        String sqlProduct = """
            INSERT INTO Products
            (ProductCode, ProductName, ProductSlug, BrandID, CategoryID, MovementType, Gender,
             ShortDescription, Description, CaseMaterial, GlassMaterial, StrapMaterial,
             WaterResistance, OriginCountry, WarrantyMonths, Status, IsFeatured, CreatedBy)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sqlProduct, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, product.getProductCode());
                ps.setString(2, product.getProductName());
                ps.setString(3, product.getSlug());
                ps.setInt(4, product.getBrandId());
                ps.setInt(5, product.getCategoryId());
                ps.setString(6, product.getMovementType() != null ? product.getMovementType() : "AUTOMATIC");
                ps.setString(7, product.getGender() != null ? product.getGender() : "MEN");
                ps.setString(8, product.getShortDescription());
                ps.setString(9, product.getDescription());
                ps.setString(10, product.getCaseMaterial());
                ps.setString(11, product.getGlassMaterial());
                ps.setString(12, product.getStrapMaterial());
                ps.setString(13, product.getWaterResistance());
                ps.setString(14, product.getOriginCountry());
                ps.setInt(15, product.getWarrantyMonths() > 0 ? product.getWarrantyMonths() : 24);
                ps.setString(16, product.getStatus() != null ? product.getStatus() : "ACTIVE");
                ps.setBoolean(17, product.isFeatured());

                if (product.getCreatedBy() != null) {
                    ps.setInt(18, product.getCreatedBy());
                } else {
                    ps.setNull(18, Types.INTEGER);
                }

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        int productId = rs.getInt(1);
                        product.setProductId(productId);

                        String sku = product.getSku() != null && !product.getSku().isBlank() ? product.getSku() : product.getProductCode() + "-STD";
                        String sqlVariant = """
                            INSERT INTO ProductVariants (ProductID, SKU, VariantName, CostPrice, SalePrice, CompareAtPrice, Status)
                            VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')
                            """;
                        try (PreparedStatement psVar = con.prepareStatement(sqlVariant)) {
                            psVar.setInt(1, productId);
                            psVar.setString(2, sku);
                            psVar.setString(3, product.getProductName());
                            psVar.setBigDecimal(4, product.getPrice() != null ? product.getPrice().multiply(new BigDecimal("0.7")) : BigDecimal.ZERO);
                            psVar.setBigDecimal(5, product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
                            if (product.getCompareAtPrice() != null) {
                                psVar.setBigDecimal(6, product.getCompareAtPrice());
                            } else {
                                psVar.setNull(6, Types.DECIMAL);
                            }
                            psVar.executeUpdate();
                        }
                    }
                    con.commit();
                    return true;
                }
                con.rollback();
            } catch (Exception e) {
                con.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Product product) {
        String sqlProduct = """
            UPDATE Products
            SET ProductCode = ?, ProductName = ?, ProductSlug = ?, BrandID = ?, CategoryID = ?,
                MovementType = ?, Gender = ?, ShortDescription = ?, Description = ?,
                CaseMaterial = ?, GlassMaterial = ?, StrapMaterial = ?, WaterResistance = ?,
                OriginCountry = ?, WarrantyMonths = ?, Status = ?, IsFeatured = ?, UpdatedAt = SYSDATETIME()
            WHERE ProductID = ?
            """;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sqlProduct)) {
                ps.setString(1, product.getProductCode());
                ps.setString(2, product.getProductName());
                ps.setString(3, product.getSlug());
                ps.setInt(4, product.getBrandId());
                ps.setInt(5, product.getCategoryId());
                ps.setString(6, product.getMovementType() != null ? product.getMovementType() : "AUTOMATIC");
                ps.setString(7, product.getGender() != null ? product.getGender() : "MEN");
                ps.setString(8, product.getShortDescription());
                ps.setString(9, product.getDescription());
                ps.setString(10, product.getCaseMaterial());
                ps.setString(11, product.getGlassMaterial());
                ps.setString(12, product.getStrapMaterial());
                ps.setString(13, product.getWaterResistance());
                ps.setString(14, product.getOriginCountry());
                ps.setInt(15, product.getWarrantyMonths() > 0 ? product.getWarrantyMonths() : 24);
                ps.setString(16, product.getStatus() != null ? product.getStatus() : "ACTIVE");
                ps.setBoolean(17, product.isFeatured());
                ps.setInt(18, product.getProductId());

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    String sku = product.getSku() != null && !product.getSku().isBlank() ? product.getSku() : product.getProductCode() + "-STD";
                    String sqlVariant = """
                        UPDATE ProductVariants
                        SET SKU = ?, SalePrice = ?, CompareAtPrice = ?
                        WHERE ProductID = ?
                        """;
                    try (PreparedStatement psVar = con.prepareStatement(sqlVariant)) {
                        psVar.setString(1, sku);
                        psVar.setBigDecimal(2, product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
                        if (product.getCompareAtPrice() != null) {
                            psVar.setBigDecimal(3, product.getCompareAtPrice());
                        } else {
                            psVar.setNull(3, Types.DECIMAL);
                        }
                        psVar.setInt(4, product.getProductId());
                        psVar.executeUpdate();
                    }
                    con.commit();
                    return true;
                }
                con.rollback();
            } catch (Exception e) {
                con.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Products WHERE ProductID = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean existsByCode(String code, Integer excludeId) {
        if (code == null || code.isBlank()) return false;
        String sql = (excludeId != null && excludeId > 0)
                ? "SELECT COUNT(*) FROM Products WHERE LOWER(ProductCode) = LOWER(?) AND ProductID <> ?"
                : "SELECT COUNT(*) FROM Products WHERE LOWER(ProductCode) = LOWER(?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, code.trim());
            if (excludeId != null && excludeId > 0) {
                ps.setInt(2, excludeId);
            }
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean existsBySlug(String slug, Integer excludeId) {
        if (slug == null || slug.isBlank()) return false;
        String sql = (excludeId != null && excludeId > 0)
                ? "SELECT COUNT(*) FROM Products WHERE LOWER(ProductSlug) = LOWER(?) AND ProductID <> ?"
                : "SELECT COUNT(*) FROM Products WHERE LOWER(ProductSlug) = LOWER(?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, slug.trim());
            if (excludeId != null && excludeId > 0) {
                ps.setInt(2, excludeId);
            }
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean existsBySku(String sku, Integer excludeId) {
        if (sku == null || sku.isBlank()) return false;
        String sql = (excludeId != null && excludeId > 0)
                ? "SELECT COUNT(*) FROM ProductVariants WHERE LOWER(SKU) = LOWER(?) AND ProductID <> ?"
                : "SELECT COUNT(*) FROM ProductVariants WHERE LOWER(SKU) = LOWER(?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, sku.trim());
            if (excludeId != null && excludeId > 0) {
                ps.setInt(2, excludeId);
            }
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isProductInUse(int productId) {
        String sql = """
                SELECT (
                    (SELECT COUNT(*) FROM OrderItems oi INNER JOIN ProductVariants pv ON oi.VariantID = pv.VariantID WHERE pv.ProductID = ?) +
                    (SELECT COUNT(*) FROM CartItems ci INNER JOIN ProductVariants pv ON ci.VariantID = pv.VariantID WHERE pv.ProductID = ?) +
                    (SELECT COUNT(*) FROM WishlistItems WHERE ProductID = ?) +
                    (SELECT COUNT(*) FROM Reviews WHERE ProductID = ?) +
                    (SELECT COUNT(*) FROM VoucherProducts WHERE ProductID = ?)
                ) AS TotalRefs
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, productId);
            ps.setInt(3, productId);
            ps.setInt(4, productId);
            ps.setInt(5, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
