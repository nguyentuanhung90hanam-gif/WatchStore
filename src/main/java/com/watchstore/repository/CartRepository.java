package com.watchstore.repository;

import com.watchstore.config.DBContext;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class CartRepository {

    public void add(int userId, int variantId, int quantity) throws SQLException {
        try (Connection c=DBContext.getConnection();
             CallableStatement cs=c.prepareCall("{call dbo.sp_AddToCart(?,?,?)}")) {
            cs.setInt(1,userId); cs.setInt(2,variantId); cs.setInt(3,quantity); cs.execute();
        }
    }

    public void addProduct(int userId, int productId, int quantity) throws SQLException {
        Integer variant = defaultVariant(productId);
        if (variant == null) throw new SQLException("Sản phẩm không có biến thể đang bán.");
        add(userId,variant,quantity);
    }

    public void update(int userId, int variantId, int quantity) throws SQLException {
        try(Connection c=DBContext.getConnection(); CallableStatement cs=c.prepareCall("{call dbo.sp_UpdateCartItem(?,?,?)}")){
            cs.setInt(1,userId); cs.setInt(2,variantId); cs.setInt(3,quantity); cs.execute();
        }
    }

    public void remove(int userId, int variantId) throws SQLException {
        try(Connection c=DBContext.getConnection(); CallableStatement cs=c.prepareCall("{call dbo.sp_RemoveCartItem(?,?)}")){
            cs.setInt(1,userId); cs.setInt(2,variantId); cs.execute();
        }
    }

    public List<Map<String,Object>> items(int userId) throws SQLException {
        String sql="""
          SELECT ci.VariantID,ci.Quantity,p.ProductID,p.ProductName,pv.VariantName,pv.SKU,
                 pv.SalePrice,pv.CompareAtPrice,b.BrandName,
                 (SELECT TOP 1 ImageUrl FROM ProductImages WHERE ProductID=p.ProductID ORDER BY IsPrimary DESC,DisplayOrder ASC) ImageUrl,
                 ISNULL((SELECT SUM(AvailableQuantity) FROM InventoryBalances WHERE VariantID=pv.VariantID),0) Available
          FROM CartItems ci
          JOIN Carts c ON c.CartID=ci.CartID AND c.Status='ACTIVE'
          JOIN ProductVariants pv ON pv.VariantID=ci.VariantID
          JOIN Products p ON p.ProductID=pv.ProductID
          JOIN Brands b ON b.BrandID=p.BrandID
          WHERE c.UserID=?
          ORDER BY ci.CartItemID DESC""";
        List<Map<String,Object>> out=new ArrayList<>();
        try(Connection c=DBContext.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,userId);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()){
                    Map<String,Object> m=new HashMap<>();
                    m.put("variantId",rs.getInt("VariantID"));
                    m.put("productId",rs.getInt("ProductID"));
                    m.put("quantity",rs.getInt("Quantity"));
                    m.put("available",rs.getInt("Available"));
                    m.put("name",rs.getString("ProductName"));
                    m.put("variantName",rs.getString("VariantName"));
                    m.put("sku",rs.getString("SKU"));
                    m.put("price",rs.getBigDecimal("SalePrice"));
                    m.put("oldPrice",rs.getBigDecimal("CompareAtPrice"));
                    m.put("brand",rs.getString("BrandName"));
                    m.put("image",rs.getString("ImageUrl"));
                    m.put("lineTotal",rs.getBigDecimal("SalePrice").multiply(BigDecimal.valueOf(rs.getInt("Quantity"))));
                    out.add(m);
                }
            }
        }
        return out;
    }

    public int count(int userId) throws SQLException {
        String sql="SELECT ISNULL(SUM(ci.Quantity),0) FROM CartItems ci JOIN Carts c ON c.CartID=ci.CartID WHERE c.UserID=? AND c.Status='ACTIVE'";
        try(Connection c=DBContext.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,userId);try(ResultSet rs=ps.executeQuery()){rs.next();return rs.getInt(1);}}
    }

    public BigDecimal subtotal(int userId) throws SQLException {
        String sql="SELECT ISNULL(SUM(ci.Quantity*pv.SalePrice),0) FROM CartItems ci JOIN Carts c ON c.CartID=ci.CartID JOIN ProductVariants pv ON pv.VariantID=ci.VariantID WHERE c.UserID=? AND c.Status='ACTIVE'";
        try(Connection c=DBContext.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,userId);try(ResultSet rs=ps.executeQuery()){rs.next();return rs.getBigDecimal(1);}}
    }

    private Integer defaultVariant(int productId) throws SQLException {
        String sql="SELECT TOP 1 VariantID FROM ProductVariants WHERE ProductID=? AND Status='ACTIVE' ORDER BY VariantID";
        try(Connection c=DBContext.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,productId);try(ResultSet rs=ps.executeQuery()){return rs.next()?rs.getInt(1):null;}}
    }
}
