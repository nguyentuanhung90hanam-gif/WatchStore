package com.watchstore.repository;
import com.watchstore.config.DBContext;
import com.watchstore.model.Product;
import java.sql.*;
import java.util.*;

public class WishlistRepository {
    public boolean toggle(int userId,int productId) throws SQLException {
        try(Connection c=DBContext.getConnection();CallableStatement cs=c.prepareCall("{call dbo.sp_ToggleWishlist(?,?)}")){
            cs.setInt(1,userId);cs.setInt(2,productId);
            try(ResultSet rs=cs.executeQuery()){return rs.next() && rs.getBoolean("IsFavorite");}
        }
    }
    public List<Product> findAll(int userId) throws SQLException {
        String sql="""
        SELECT p.ProductID,p.ProductName,p.ShortDescription,p.RatingAverage,b.BrandName,
          (SELECT TOP 1 ImageUrl FROM ProductImages WHERE ProductID=p.ProductID ORDER BY IsPrimary DESC,DisplayOrder ASC) ImageUrl,
          (SELECT MIN(SalePrice) FROM ProductVariants WHERE ProductID=p.ProductID AND Status='ACTIVE') Price,
          (SELECT MIN(CompareAtPrice) FROM ProductVariants WHERE ProductID=p.ProductID AND Status='ACTIVE') CompareAtPrice,
          10 AS Quantity
        FROM Wishlists w JOIN WishlistItems wi ON wi.WishlistID=w.WishlistID JOIN Products p ON p.ProductID=wi.ProductID
        JOIN Brands b ON b.BrandID=p.BrandID WHERE w.UserID=? ORDER BY wi.AddedAt DESC""";
        List<Product> out=new ArrayList<>();
        try(Connection c=DBContext.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,userId);try(ResultSet rs=ps.executeQuery()){while(rs.next()){
            Product p=new Product();p.setProductId(rs.getInt("ProductID"));p.setProductName(rs.getString("ProductName"));p.setShortDescription(rs.getString("ShortDescription"));p.setBrandName(rs.getString("BrandName"));p.setImageUrl(rs.getString("ImageUrl"));p.setPrice(rs.getBigDecimal("Price"));p.setCompareAtPrice(rs.getBigDecimal("CompareAtPrice"));p.setStock(rs.getInt("Quantity"));p.setRatingAverage(rs.getDouble("RatingAverage"));out.add(p);
        }}}return out;
    }
}
