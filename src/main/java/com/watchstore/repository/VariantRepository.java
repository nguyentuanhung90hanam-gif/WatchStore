package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Variant;
import com.watchstore.model.VariantAttributeOption;
import com.watchstore.model.ProductOption;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VariantRepository {

    private static final String BASE_SELECT =
            "SELECT pv.VariantID, pv.ProductID, pv.SKU, pv.Barcode, pv.VariantName, " +
                    "pv.CostPrice, pv.SalePrice, pv.CompareAtPrice, pv.WeightGram, pv.Status, " +
                    "pv.CreatedAt, " +
                    "p.ProductName, b.BrandName, " +
                    "(SELECT STRING_AGG(pa.AttributeName + ': ' + pav.ValueText, ' | ') " +
                    " FROM dbo.VariantAttributeValues vav " +
                    " INNER JOIN dbo.ProductAttributeValues pav " +
                    "     ON vav.AttributeValueID = pav.AttributeValueID " +
                    " INNER JOIN dbo.ProductAttributes pa " +
                    "     ON pav.AttributeID = pa.AttributeID " +
                    " WHERE vav.VariantID = pv.VariantID) AS AttributesStr " +
                    "FROM dbo.ProductVariants pv " +
                    "INNER JOIN dbo.Products p ON pv.ProductID = p.ProductID " +
                    "INNER JOIN dbo.Brands b ON p.BrandID = b.BrandID ";

    public List<Variant> findAll() {
        return query(
                BASE_SELECT +
                        "ORDER BY p.ProductName, pv.VariantName",
                new ArrayList<>()
        );
    }

    public List<Variant> search(String keyword, String status) {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append("WHERE 1 = 1 ");

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(
                    "AND (" +
                            "pv.SKU LIKE ? " +
                            "OR pv.Barcode LIKE ? " +
                            "OR pv.VariantName LIKE ? " +
                            "OR p.ProductName LIKE ?" +
                            ") "
            );

            String value = "%" + keyword.trim() + "%";

            params.add(value);
            params.add(value);
            params.add(value);
            params.add(value);
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND pv.Status = ? ");
            params.add(status.trim());
        }

        sql.append("ORDER BY p.ProductName, pv.VariantName");

        return query(sql.toString(), params);
    }

    public Variant findById(int variantId) throws Exception {
        if (variantId <= 0) {
            throw new Exception("ID biến thể không hợp lệ.");
        }

        List<Variant> result = query(
                BASE_SELECT + "WHERE pv.VariantID = ?",
                List.of(variantId)
        );

        return result.isEmpty() ? null : result.get(0);
    }

    public void create(Variant variant) throws Exception {
        create(variant, new ArrayList<>());
    }

    public void create(
            Variant variant,
            List<Integer> attributeValueIds
    ) throws Exception {

        validate(variant);
        ensureProductExists(variant.getProductId());
        ensureUnique(
                variant.getSku(),
                variant.getBarcode(),
                0
        );
        validateAttributeValueIds(attributeValueIds);

        String sql =
                "INSERT INTO dbo.ProductVariants " +
                        "(ProductID, SKU, Barcode, VariantName, CostPrice, " +
                        "SalePrice, CompareAtPrice, WeightGram, Status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection conn = DBContext.getConnection()
        ) {
            conn.setAutoCommit(false);

            try (
                    PreparedStatement ps =
                            conn.prepareStatement(
                                    sql,
                                    Statement.RETURN_GENERATED_KEYS
                            )
            ) {
                bindVariant(ps, variant);
                ps.executeUpdate();

                int variantId;

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new Exception(
                                "Không thể lấy ID biến thể vừa tạo."
                        );
                    }

                    variantId = keys.getInt(1);
                }

                insertVariantAttributeValues(
                        conn,
                        variantId,
                        attributeValueIds
                );

                conn.commit();

            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new Exception(
                    "Không thể thêm biến thể: " +
                            e.getMessage(),
                    e
            );
        }
    }

    public void update(Variant variant) throws Exception {
        update(variant, new ArrayList<>());
    }

    public void update(
            Variant variant,
            List<Integer> attributeValueIds
    ) throws Exception {

        if (variant == null || variant.getVariantId() <= 0) {
            throw new Exception("ID biến thể không hợp lệ.");
        }

        validate(variant);

        Variant existing = findById(variant.getVariantId());

        if (existing == null) {
            throw new Exception("Biến thể không tồn tại.");
        }

        ensureProductExists(variant.getProductId());

        ensureUnique(
                variant.getSku(),
                variant.getBarcode(),
                variant.getVariantId()
        );

        validateAttributeValueIds(attributeValueIds);

        String sql =
                "UPDATE dbo.ProductVariants SET " +
                        "ProductID = ?, " +
                        "SKU = ?, " +
                        "Barcode = ?, " +
                        "VariantName = ?, " +
                        "CostPrice = ?, " +
                        "SalePrice = ?, " +
                        "CompareAtPrice = ?, " +
                        "WeightGram = ?, " +
                        "Status = ? " +
                        "WHERE VariantID = ?";

        try (
                Connection conn = DBContext.getConnection()
        ) {
            conn.setAutoCommit(false);

            try (
                    PreparedStatement ps =
                            conn.prepareStatement(sql)
            ) {
                bindVariant(ps, variant);

                ps.setInt(
                        10,
                        variant.getVariantId()
                );

                if (ps.executeUpdate() != 1) {
                    throw new Exception(
                            "Không thể cập nhật biến thể."
                    );
                }

                deleteVariantAttributeValues(
                        conn,
                        variant.getVariantId()
                );

                insertVariantAttributeValues(
                        conn,
                        variant.getVariantId(),
                        attributeValueIds
                );

                conn.commit();

            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new Exception(
                    "Không thể cập nhật biến thể: " +
                            e.getMessage(),
                    e
            );
        }
    }

    public List<VariantAttributeOption> findAttributeOptions(
            int variantId
    ) throws Exception {

        String sql =
                "SELECT pa.AttributeID, " +
                        "pa.AttributeCode, " +
                        "pa.AttributeName, " +
                        "pav.AttributeValueID, " +
                        "pav.ValueText, " +
                        "CASE WHEN vav.VariantID IS NULL " +
                        "THEN 0 ELSE 1 END AS IsSelected " +
                        "FROM dbo.ProductAttributes pa " +
                        "INNER JOIN dbo.ProductAttributeValues pav " +
                        "ON pa.AttributeID = pav.AttributeID " +
                        "LEFT JOIN dbo.VariantAttributeValues vav " +
                        "ON vav.AttributeValueID = pav.AttributeValueID " +
                        "AND vav.VariantID = ? " +
                        "ORDER BY pa.AttributeName, " +
                        "pav.DisplayOrder, " +
                        "pav.ValueText";

        List<VariantAttributeOption> list =
                new ArrayList<>();

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {
            ps.setInt(1, variantId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VariantAttributeOption option =
                            new VariantAttributeOption();

                    option.setAttributeId(
                            rs.getInt("AttributeID")
                    );

                    option.setAttributeCode(
                            rs.getString("AttributeCode")
                    );

                    option.setAttributeName(
                            rs.getString("AttributeName")
                    );

                    option.setAttributeValueId(
                            rs.getInt("AttributeValueID")
                    );

                    option.setValueName(
                            rs.getString("ValueText")
                    );

                    option.setSelected(
                            rs.getInt("IsSelected") == 1
                    );

                    list.add(option);
                }
            }

        } catch (SQLException e) {
            throw new Exception(
                    "Không thể tải thuộc tính biến thể: " +
                            e.getMessage(),
                    e
            );
        }

        return list;
    }

    private void validateAttributeValueIds(
            List<Integer> attributeValueIds
    ) throws Exception {

        if (attributeValueIds == null ||
                attributeValueIds.isEmpty()) {
            return;
        }

        String sql =
                "SELECT pav.AttributeValueID, pav.AttributeID " +
                        "FROM dbo.ProductAttributeValues pav " +
                        "WHERE pav.AttributeValueID = ?";

        Set<Integer> selectedAttributes =
                new HashSet<>();

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {
            for (Integer valueId : attributeValueIds) {

                if (valueId == null || valueId <= 0) {
                    throw new Exception(
                            "Giá trị thuộc tính không hợp lệ."
                    );
                }

                ps.setInt(1, valueId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        throw new Exception(
                                "Giá trị thuộc tính không tồn tại."
                        );
                    }

                    int attributeId =
                            rs.getInt("AttributeID");

                    if (!selectedAttributes.add(attributeId)) {
                        throw new Exception(
                                "Mỗi thuộc tính chỉ được chọn một giá trị."
                        );
                    }
                }
            }

        } catch (SQLException e) {
            throw new Exception(
                    "Không thể kiểm tra thuộc tính biến thể: " +
                            e.getMessage(),
                    e
            );
        }
    }

    private void deleteVariantAttributeValues(
            Connection conn,
            int variantId
    ) throws SQLException {

        String sql =
                "DELETE FROM dbo.VariantAttributeValues " +
                        "WHERE VariantID = ?";

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, variantId);
            ps.executeUpdate();
        }
    }

    private void insertVariantAttributeValues(
            Connection conn,
            int variantId,
            List<Integer> attributeValueIds
    ) throws SQLException {

        if (attributeValueIds == null ||
                attributeValueIds.isEmpty()) {
            return;
        }

        String sql =
                "INSERT INTO dbo.VariantAttributeValues " +
                        "(VariantID, AttributeValueID) " +
                        "VALUES (?, ?)";

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            for (Integer valueId : attributeValueIds) {
                ps.setInt(1, variantId);
                ps.setInt(2, valueId);
                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    public void deactivate(int variantId) throws Exception {
        Variant variant = findById(variantId);

        if (variant == null) {
            throw new Exception(
                    "Biến thể không tồn tại."
            );
        }

        if (hasOperationalData(variantId)) {
            setStatus(
                    variantId,
                    "INACTIVE"
            );
            return;
        }

        String sql =
                "DELETE FROM dbo.ProductVariants " +
                        "WHERE VariantID = ?";

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, variantId);

            if (ps.executeUpdate() != 1) {
                throw new Exception(
                        "Không thể xóa biến thể."
                );
            }

        } catch (SQLException e) {
            throw new Exception(
                    "Không thể xóa biến thể: " +
                            e.getMessage(),
                    e
            );
        }
    }

    public void setStatus(
            int variantId,
            String status
    ) throws Exception {

        if (variantId <= 0) {
            throw new Exception(
                    "ID biến thể không hợp lệ."
            );
        }

        if (!isValidStatus(status)) {
            throw new Exception(
                    "Trạng thái biến thể không hợp lệ."
            );
        }

        String sql =
                "UPDATE dbo.ProductVariants " +
                        "SET Status = ? " +
                        "WHERE VariantID = ?";

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, status);
            ps.setInt(2, variantId);

            if (ps.executeUpdate() != 1) {
                throw new Exception(
                        "Biến thể không tồn tại."
                );
            }

        } catch (SQLException e) {
            throw new Exception(
                    "Không thể cập nhật trạng thái biến thể: " +
                            e.getMessage(),
                    e
            );
        }
    }

    public List<ProductOption> findProductOptions() {
        List<ProductOption> list =
                new ArrayList<>();

        String sql =
                "SELECT p.ProductID, " +
                        "p.ProductName, " +
                        "b.BrandName " +
                        "FROM dbo.Products p " +
                        "INNER JOIN dbo.Brands b " +
                        "ON p.BrandID = b.BrandID " +
                        "WHERE p.Status IN ('DRAFT', 'ACTIVE') " +
                        "ORDER BY p.ProductName";

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                ProductOption option =
                        new ProductOption();

                option.setProductId(
                        rs.getInt("ProductID")
                );

                option.setProductName(
                        rs.getString("ProductName")
                );

                option.setBrandName(
                        rs.getString("BrandName")
                );

                list.add(option);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<VariantAttributeOption> findAttributeOptionsForCreate()
            throws Exception {

        return findAttributeOptions(0);
    }

    private List<Variant> query(
            String sql,
            List<?> params
    ) {
        List<Variant> variants =
                new ArrayList<>();

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            for (int i = 0;
                 i < params.size();
                 i++) {

                ps.setObject(
                        i + 1,
                        params.get(i)
                );
            }

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                while (rs.next()) {
                    variants.add(
                            mapVariant(rs)
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return variants;
    }

    private Variant mapVariant(
            ResultSet rs
    ) throws SQLException {

        Variant v = new Variant();

        v.setVariantId(
                rs.getInt("VariantID")
        );

        v.setProductId(
                rs.getInt("ProductID")
        );

        v.setProductName(
                rs.getString("ProductName")
        );

        v.setBrandName(
                rs.getString("BrandName")
        );

        v.setSku(
                rs.getString("SKU")
        );

        v.setBarcode(
                rs.getString("Barcode")
        );

        v.setVariantName(
                rs.getString("VariantName")
        );

        v.setCostPrice(
                rs.getBigDecimal("CostPrice")
        );

        v.setSalePrice(
                rs.getBigDecimal("SalePrice")
        );

        v.setCompareAtPrice(
                rs.getBigDecimal("CompareAtPrice")
        );

        v.setWeightGram(
                rs.getObject("WeightGram") == null
                        ? null
                        : rs.getInt("WeightGram")
        );

        v.setStatus(
                rs.getString("Status")
        );

        v.setAttributes(
                rs.getString("AttributesStr")
        );

        return v;
    }

    private void bindVariant(
            PreparedStatement ps,
            Variant variant
    ) throws SQLException {

        ps.setInt(
                1,
                variant.getProductId()
        );

        ps.setString(
                2,
                variant.getSku().trim()
        );

        ps.setString(
                3,
                emptyToNull(
                        variant.getBarcode()
                )
        );

        ps.setString(
                4,
                variant.getVariantName().trim()
        );

        ps.setBigDecimal(
                5,
                variant.getCostPrice()
        );

        ps.setBigDecimal(
                6,
                variant.getSalePrice()
        );

        if (variant.getCompareAtPrice() == null) {
            ps.setNull(
                    7,
                    java.sql.Types.DECIMAL
            );
        } else {
            ps.setBigDecimal(
                    7,
                    variant.getCompareAtPrice()
            );
        }

        if (variant.getWeightGram() == null) {
            ps.setNull(
                    8,
                    java.sql.Types.INTEGER
            );
        } else {
            ps.setInt(
                    8,
                    variant.getWeightGram()
            );
        }

        String status =
                variant.getStatus();

        if (status == null ||
                status.trim().isEmpty()) {

            status = "ACTIVE";
        }

        ps.setString(
                9,
                status.trim()
        );
    }

    private void validate(
            Variant variant
    ) throws Exception {

        if (variant == null) {
            throw new Exception(
                    "Dữ liệu biến thể không hợp lệ."
            );
        }

        if (variant.getProductId() <= 0) {
            throw new Exception(
                    "Phải chọn sản phẩm."
            );
        }

        if (variant.getSku() == null ||
                variant.getSku().trim().isEmpty()) {

            throw new Exception(
                    "SKU không được để trống."
            );
        }

        if (variant.getSku().trim().length() > 80) {
            throw new Exception(
                    "SKU không được vượt quá 80 ký tự."
            );
        }

        if (variant.getBarcode() != null &&
                variant.getBarcode().trim().length() > 80) {

            throw new Exception(
                    "Barcode không được vượt quá 80 ký tự."
            );
        }

        if (variant.getVariantName() == null ||
                variant.getVariantName().trim().isEmpty()) {

            throw new Exception(
                    "Tên biến thể không được để trống."
            );
        }

        if (variant.getCostPrice() == null ||
                variant.getCostPrice()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new Exception(
                    "Giá nhập không được âm."
            );
        }

        if (variant.getSalePrice() == null ||
                variant.getSalePrice()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new Exception(
                    "Giá bán không được âm."
            );
        }

        if (variant.getCompareAtPrice() != null &&
                variant.getCompareAtPrice()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new Exception(
                    "Giá so sánh không được âm."
            );
        }

        if (variant.getWeightGram() != null &&
                variant.getWeightGram() < 0) {

            throw new Exception(
                    "Trọng lượng không được âm."
            );
        }

        if (variant.getSalePrice()
                .compareTo(variant.getCostPrice()) < 0) {

            throw new Exception(
                    "Giá bán không được thấp hơn giá nhập."
            );
        }

        if (variant.getCompareAtPrice() != null &&
                variant.getCompareAtPrice()
                        .compareTo(variant.getSalePrice()) < 0) {

            throw new Exception(
                    "Giá so sánh không được thấp hơn giá bán."
            );
        }

        if (variant.getStatus() != null &&
                !variant.getStatus().trim().isEmpty() &&
                !isValidStatus(
                        variant.getStatus().trim()
                )) {

            throw new Exception(
                    "Trạng thái biến thể không hợp lệ."
            );
        }
    }

    private boolean isValidStatus(
            String status
    ) {
        return "ACTIVE".equals(status)
                || "INACTIVE".equals(status)
                || "OUT_OF_STOCK".equals(status);
    }

    private void ensureProductExists(
            int productId
    ) throws Exception {

        String sql =
                "SELECT COUNT(1) " +
                        "FROM dbo.Products " +
                        "WHERE ProductID = ?";

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    productId
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (!rs.next() ||
                        rs.getInt(1) == 0) {

                    throw new Exception(
                            "Sản phẩm không tồn tại."
                    );
                }
            }

        } catch (SQLException e) {
            throw new Exception(
                    "Không thể kiểm tra sản phẩm: " +
                            e.getMessage(),
                    e
            );
        }
    }

    private void ensureUnique(
            String sku,
            String barcode,
            int excludeId
    ) throws Exception {

        String skuSql =
                "SELECT COUNT(1) " +
                        "FROM dbo.ProductVariants " +
                        "WHERE SKU = ? " +
                        "AND VariantID <> ?";

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(skuSql)
        ) {

            ps.setString(
                    1,
                    sku.trim()
            );

            ps.setInt(
                    2,
                    excludeId
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (rs.next() &&
                        rs.getInt(1) > 0) {

                    throw new Exception(
                            "SKU đã tồn tại."
                    );
                }
            }

        } catch (SQLException e) {
            throw new Exception(
                    "Không thể kiểm tra SKU: " +
                            e.getMessage(),
                    e
            );
        }

        if (barcode != null &&
                !barcode.trim().isEmpty()) {

            String barcodeSql =
                    "SELECT COUNT(1) " +
                            "FROM dbo.ProductVariants " +
                            "WHERE Barcode = ? " +
                            "AND VariantID <> ?";

            try (
                    Connection conn =
                            DBContext.getConnection();
                    PreparedStatement ps =
                            conn.prepareStatement(
                                    barcodeSql
                            )
            ) {

                ps.setString(
                        1,
                        barcode.trim()
                );

                ps.setInt(
                        2,
                        excludeId
                );

                try (
                        ResultSet rs =
                                ps.executeQuery()
                ) {

                    if (rs.next() &&
                            rs.getInt(1) > 0) {

                        throw new Exception(
                                "Barcode đã tồn tại."
                        );
                    }
                }

            } catch (SQLException e) {
                throw new Exception(
                        "Không thể kiểm tra Barcode: " +
                                e.getMessage(),
                        e
                );
            }
        }
    }

    private boolean hasOperationalData(
            int variantId
    ) throws Exception {

        String sql =
                "SELECT CASE WHEN " +
                        "EXISTS (" +
                        "SELECT 1 FROM dbo.OrderItems " +
                        "WHERE VariantID = ?" +
                        ") " +
                        "OR EXISTS (" +
                        "SELECT 1 FROM dbo.StockReceiptItems " +
                        "WHERE VariantID = ?" +
                        ") " +
                        "OR EXISTS (" +
                        "SELECT 1 FROM dbo.StockExportItems " +
                        "WHERE VariantID = ?" +
                        ") " +
                        "OR EXISTS (" +
                        "SELECT 1 FROM dbo.InventoryBalances " +
                        "WHERE VariantID = ?" +
                        ") " +
                        "OR EXISTS (" +
                        "SELECT 1 FROM dbo.StocktakeItems " +
                        "WHERE VariantID = ?" +
                        ") " +
                        "OR EXISTS (" +
                        "SELECT 1 FROM dbo.InventoryTransactions " +
                        "WHERE VariantID = ?" +
                        ") " +
                        "OR EXISTS (" +
                        "SELECT 1 FROM dbo.VariantAttributeValues " +
                        "WHERE VariantID = ?" +
                        ") " +
                        "THEN 1 ELSE 0 END";

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            for (int i = 1; i <= 7; i++) {
                ps.setInt(i, variantId);
            }

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                return rs.next() &&
                        rs.getInt(1) == 1;
            }

        } catch (SQLException e) {
            throw new Exception(
                    "Không thể kiểm tra dữ liệu nghiệp vụ của biến thể: " +
                            e.getMessage(),
                    e
            );
        }
    }

    private String emptyToNull(
            String value
    ) {
        if (value == null ||
                value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}