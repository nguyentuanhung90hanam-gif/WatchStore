package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Variant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VariantRepository {

    /**
     * Lấy danh sách ProductVariants cùng với Attributes string từ JOIN
     * Không có cột Color, Size. Attribute được sinh ra thông qua bảng ProductAttributeValues.
     */
    public List<Variant> findAll() {
        List<Variant> variants = new ArrayList<>();
        // Query tối ưu ghép các attribute thành chuỗi "Tên_Att: Giá_trị" (Dùng STRING_AGG trên SQL Server 2017+)
        // Nếu bản SQL Server cũ hơn, có thể dùng FOR XML PATH. Ở đây dùng STRING_AGG theo chuẩn hiện đại.
        String sql = "SELECT pv.*, p.ProductName, b.BrandName, " +
                     "(SELECT STRING_AGG(pa.AttributeName + ': ' + pav.ValueName, ' | ') " +
                     " FROM dbo.VariantAttributeValues vav " +
                     " INNER JOIN dbo.ProductAttributeValues pav ON vav.AttributeValueID = pav.AttributeValueID " +
                     " INNER JOIN dbo.ProductAttributes pa ON pav.AttributeID = pa.AttributeID " +
                     " WHERE vav.VariantID = pv.VariantID) AS AttributesStr " +
                     "FROM dbo.ProductVariants pv " +
                     "INNER JOIN dbo.Products p ON pv.ProductID = p.ProductID " +
                     "INNER JOIN dbo.Brands b ON p.BrandID = b.BrandID " +
                     "ORDER BY p.ProductName, pv.VariantName";
                     
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                variants.add(mapVariant(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return variants;
    }

    private Variant mapVariant(ResultSet rs) throws SQLException {
        Variant v = new Variant();
        v.setVariantId(rs.getInt("VariantID"));
        v.setProductId(rs.getInt("ProductID"));
        v.setProductName(rs.getString("ProductName"));
        v.setBrandName(rs.getString("BrandName"));
        v.setSku(rs.getString("SKU"));
        v.setBarcode(rs.getString("Barcode"));
        v.setVariantName(rs.getString("VariantName"));
        v.setCostPrice(rs.getBigDecimal("CostPrice"));
        v.setSalePrice(rs.getBigDecimal("SalePrice"));
        v.setCompareAtPrice(rs.getBigDecimal("CompareAtPrice"));
        v.setWeightGram(rs.getObject("WeightGram") != null ? rs.getInt("WeightGram") : null);
        v.setStatus(rs.getString("Status"));
        v.setAttributes(rs.getString("AttributesStr"));
        return v;
    }
}
