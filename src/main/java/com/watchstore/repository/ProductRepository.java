package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Product;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    /**
     * Lấy toàn bộ sản phẩm
     */
    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql = """
            SELECT p.ProductID, p.ProductName, p.ShortDescription, p.IsFeatured, p.RatingAverage, 
                   b.BrandName, 
                   (SELECT TOP 1 ImageUrl FROM ProductImages WHERE ProductID = p.ProductID ORDER BY IsPrimary DESC, DisplayOrder ASC) AS ImageUrl,
                   (SELECT MIN(SalePrice) FROM ProductVariants WHERE ProductID = p.ProductID) AS Price,
                   (SELECT MIN(CompareAtPrice) FROM ProductVariants WHERE ProductID = p.ProductID) AS CompareAtPrice,
                   (SELECT SUM(QuantityOnHand) FROM InventoryBalances ib JOIN ProductVariants pv ON ib.VariantID = pv.VariantID WHERE pv.ProductID = p.ProductID) AS Quantity
            FROM Products p
            LEFT JOIN Brands b ON p.BrandID = b.BrandID
            ORDER BY p.ProductID DESC
            """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy danh sách sản phẩm nổi bật
     */
    public List<Product> findFeatured() {
        List<Product> list = new ArrayList<>();
        String sql = """
            SELECT p.ProductID, p.ProductName, p.ShortDescription, p.IsFeatured, p.RatingAverage, 
                   b.BrandName, 
                   (SELECT TOP 1 ImageUrl FROM ProductImages WHERE ProductID = p.ProductID ORDER BY IsPrimary DESC, DisplayOrder ASC) AS ImageUrl,
                   (SELECT MIN(SalePrice) FROM ProductVariants WHERE ProductID = p.ProductID) AS Price,
                   (SELECT MIN(CompareAtPrice) FROM ProductVariants WHERE ProductID = p.ProductID) AS CompareAtPrice,
                   (SELECT SUM(QuantityOnHand) FROM InventoryBalances ib JOIN ProductVariants pv ON ib.VariantID = pv.VariantID WHERE pv.ProductID = p.ProductID) AS Quantity
            FROM Products p
            LEFT JOIN Brands b ON p.BrandID = b.BrandID
            WHERE p.IsFeatured = 1
            ORDER BY p.ProductID DESC
            """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm sản phẩm theo ID
     */
    public Product findById(int id) {
        String sql = """
            SELECT p.ProductID, p.ProductName, p.ShortDescription, p.IsFeatured, p.RatingAverage, 
                   b.BrandName, 
                   (SELECT TOP 1 ImageUrl FROM ProductImages WHERE ProductID = p.ProductID ORDER BY IsPrimary DESC, DisplayOrder ASC) AS ImageUrl,
                   (SELECT MIN(SalePrice) FROM ProductVariants WHERE ProductID = p.ProductID) AS Price,
                   (SELECT MIN(CompareAtPrice) FROM ProductVariants WHERE ProductID = p.ProductID) AS CompareAtPrice,
                   (SELECT SUM(QuantityOnHand) FROM InventoryBalances ib JOIN ProductVariants pv ON ib.VariantID = pv.VariantID WHERE pv.ProductID = p.ProductID) AS Quantity
            FROM Products p
            LEFT JOIN Brands b ON p.BrandID = b.BrandID
            WHERE p.ProductID = ?
            """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm kiếm sản phẩm theo tên
     */
    public List<Product> searchByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        List<Product> list = new ArrayList<>();
        String sql = """
            SELECT p.ProductID, p.ProductName, p.ShortDescription, p.IsFeatured, p.RatingAverage, 
                   b.BrandName, 
                   (SELECT TOP 1 ImageUrl FROM ProductImages WHERE ProductID = p.ProductID ORDER BY IsPrimary DESC, DisplayOrder ASC) AS ImageUrl,
                   (SELECT MIN(SalePrice) FROM ProductVariants WHERE ProductID = p.ProductID) AS Price,
                   (SELECT MIN(CompareAtPrice) FROM ProductVariants WHERE ProductID = p.ProductID) AS CompareAtPrice,
                   (SELECT SUM(QuantityOnHand) FROM InventoryBalances ib JOIN ProductVariants pv ON ib.VariantID = pv.VariantID WHERE pv.ProductID = p.ProductID) AS Quantity
            FROM Products p
            LEFT JOIN Brands b ON p.BrandID = b.BrandID
            WHERE LOWER(p.ProductName) LIKE ?
            ORDER BY p.ProductID DESC
            """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword.trim().toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Wrapper tương thích với Controller
     */
    public List<Product> search(String keyword) {
        return searchByName(keyword);
    }

    /**
     * Thêm sản phẩm
     */
    public void add(Product product) {
        if (product == null) return;
        String sqlProd = """
            INSERT INTO Products 
            (ProductCode, ProductName, Slug, BrandID, CategoryID, MovementType, ShortDescription, Description, Status, IsFeatured) 
            VALUES (?, ?, ?, ?, ?, 'QUARTZ', ?, ?, 'ACTIVE', ?)
            """;
        String sqlVar = """
            INSERT INTO ProductVariants (ProductID, SKU, VariantName, CostPrice, SalePrice, CompareAtPrice, Status) 
            VALUES (?, ?, 'Standard', ?, ?, ?, 'ACTIVE')
            """;
        String sqlInv = """
            INSERT INTO InventoryBalances (WarehouseID, VariantID, QuantityOnHand, QuantityReserved, ReorderLevel) 
            VALUES (1, ?, ?, 0, 5)
            """;

        try (Connection con = DBContext.getConnection()) {
            con.setAutoCommit(false);
            try {
                int pId = -1;
                String code = "PRO-" + System.currentTimeMillis();
                String slug = product.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-");

                // 1. Thêm vào Products
                try (PreparedStatement psProd = con.prepareStatement(sqlProd, Statement.RETURN_GENERATED_KEYS)) {
                    psProd.setString(1, code);
                    psProd.setString(2, product.getName());
                    psProd.setString(3, slug);
                    psProd.setInt(4, 1); // BrandID mặc định
                    psProd.setInt(5, 1); // CategoryID mặc định
                    psProd.setString(6, product.getDescription() != null ? product.getDescription() : "");
                    psProd.setString(7, product.getDescription() != null ? product.getDescription() : "");
                    psProd.setInt(8, "Featured".equals(product.getBadge()) ? 1 : 0);
                    psProd.executeUpdate();

                    try (ResultSet rsKeys = psProd.getGeneratedKeys()) {
                        if (rsKeys.next()) {
                            pId = rsKeys.getInt(1);
                        }
                    }
                }

                if (pId == -1) {
                    throw new SQLException("Failed to retrieve generated ProductID.");
                }

                // 2. Thêm vào ProductVariants
                int vId = -1;
                try (PreparedStatement psVar = con.prepareStatement(sqlVar, Statement.RETURN_GENERATED_KEYS)) {
                    psVar.setInt(1, pId);
                    psVar.setString(2, code + "-VAR");
                    psVar.setBigDecimal(3, product.getPrice().multiply(new BigDecimal("0.7")));
                    psVar.setBigDecimal(4, product.getPrice());
                    psVar.setBigDecimal(5, product.getOldPrice() != null ? product.getOldPrice() : product.getPrice().multiply(new BigDecimal("1.15")));
                    psVar.executeUpdate();

                    try (ResultSet rsKeys = psVar.getGeneratedKeys()) {
                        if (rsKeys.next()) {
                            vId = rsKeys.getInt(1);
                        }
                    }
                }

                if (vId == -1) {
                    throw new SQLException("Failed to retrieve generated VariantID.");
                }

                // 3. Thêm vào InventoryBalances
                try (PreparedStatement psInv = con.prepareStatement(sqlInv)) {
                    psInv.setInt(1, vId);
                    psInv.setInt(2, product.getQuantity());
                    psInv.executeUpdate();
                }

                // 4. Thêm hình ảnh nếu có
                if (product.getImage() != null && !product.getImage().isBlank()) {
                    String sqlImg = "INSERT INTO ProductImages (ProductID, ImageUrl, AltText, IsPrimary, DisplayOrder) VALUES (?, ?, ?, 1, 1)";
                    try (PreparedStatement psImg = con.prepareStatement(sqlImg)) {
                        psImg.setInt(1, pId);
                        psImg.setString(2, product.getImage());
                        psImg.setString(3, product.getName());
                        psImg.executeUpdate();
                    }
                }

                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật sản phẩm
     */
    public boolean update(Product product) {
        if (product == null) return false;
        String sqlProd = "UPDATE Products SET ProductName = ?, ShortDescription = ? WHERE ProductID = ?";
        String sqlVar = "UPDATE ProductVariants SET SalePrice = ?, CompareAtPrice = ? WHERE ProductID = ?";
        String sqlInv = """
            UPDATE InventoryBalances 
            SET QuantityOnHand = ? 
            WHERE VariantID IN (SELECT VariantID FROM ProductVariants WHERE ProductID = ?)
            """;

        try (Connection con = DBContext.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Cập nhật Products
                try (PreparedStatement psProd = con.prepareStatement(sqlProd)) {
                    psProd.setString(1, product.getName());
                    psProd.setString(2, product.getDescription());
                    psProd.setInt(3, product.getId());
                    psProd.executeUpdate();
                }

                // 2. Cập nhật ProductVariants
                try (PreparedStatement psVar = con.prepareStatement(sqlVar)) {
                    psVar.setBigDecimal(1, product.getPrice());
                    psVar.setBigDecimal(2, product.getOldPrice() != null ? product.getOldPrice() : product.getPrice().multiply(new BigDecimal("1.15")));
                    psVar.setInt(3, product.getId());
                    psVar.executeUpdate();
                }

                // 3. Cập nhật Tồn kho
                try (PreparedStatement psInv = con.prepareStatement(sqlInv)) {
                    psInv.setInt(1, product.getQuantity());
                    psInv.setInt(2, product.getId());
                    psInv.executeUpdate();
                }

                con.commit();
                return true;
            } catch (SQLException ex) {
                con.rollback();
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa sản phẩm
     */
    public boolean delete(int id) {
        String sqlDelInv = "DELETE FROM InventoryBalances WHERE VariantID IN (SELECT VariantID FROM ProductVariants WHERE ProductID = ?)";
        String sqlDelVar = "DELETE FROM ProductVariants WHERE ProductID = ?";
        String sqlDelImg = "DELETE FROM ProductImages WHERE ProductID = ?";
        String sqlDelProd = "DELETE FROM Products WHERE ProductID = ?";

        try (Connection con = DBContext.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlDelInv)) { ps.setInt(1, id); ps.executeUpdate(); }
                try (PreparedStatement ps = con.prepareStatement(sqlDelVar)) { ps.setInt(1, id); ps.executeUpdate(); }
                try (PreparedStatement ps = con.prepareStatement(sqlDelImg)) { ps.setInt(1, id); ps.executeUpdate(); }
                try (PreparedStatement ps = con.prepareStatement(sqlDelProd)) { ps.setInt(1, id); ps.executeUpdate(); }

                con.commit();
                return true;
            } catch (SQLException ex) {
                con.rollback();
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("ProductID"));
        p.setName(rs.getString("ProductName"));
        p.setDescription(rs.getString("ShortDescription") != null ? rs.getString("ShortDescription") : "");
        p.setBadge(rs.getInt("IsFeatured") == 1 ? "Featured" : "Hot");
        p.setRating(rs.getDouble("RatingAverage"));
        p.setBrand(rs.getString("BrandName") != null ? rs.getString("BrandName") : "WatchStore");
        p.setImage(rs.getString("ImageUrl") != null ? rs.getString("ImageUrl") : "default.jpg");

        BigDecimal price = rs.getBigDecimal("Price");
        p.setPrice(price != null ? price : BigDecimal.ZERO);

        BigDecimal oldPrice = rs.getBigDecimal("CompareAtPrice");
        if (oldPrice == null && price != null) {
            oldPrice = price.multiply(new BigDecimal("1.15"));
        }
        p.setOldPrice(oldPrice != null ? oldPrice : BigDecimal.ZERO);

        p.setQuantity(rs.getInt("Quantity"));
        return p;
    }
}