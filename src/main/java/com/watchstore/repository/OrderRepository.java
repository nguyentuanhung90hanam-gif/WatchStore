package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Order;
import java.sql.*;
import java.util.*;

public class OrderRepository {
    public List<Order> findAll(){return find(null);}
    public List<Order> findByCustomerId(int userId){return find(userId);}
    private List<Order> find(Integer uid){
        List<Order> list=new ArrayList<>();
        String sql="SELECT * FROM Orders "+(uid==null?"":"WHERE CustomerID=? ")+"ORDER BY OrderID DESC";
        try(Connection c=DBContext.getConnection();PreparedStatement ps=c.prepareStatement(sql)){if(uid!=null)ps.setInt(1,uid);try(ResultSet rs=ps.executeQuery()){while(rs.next())list.add(map(rs));}}
        catch(SQLException e){e.printStackTrace();}return list;
    }
    public Order findById(long id){return findOne("OrderID",String.valueOf(id));}
    public Order findByCode(String code){return findOne("OrderCode",code);}
    private Order findOne(String col,String val){
        String sql="SELECT * FROM Orders WHERE "+col+"=?";
        try(Connection c=DBContext.getConnection();PreparedStatement ps=c.prepareStatement(sql)){if("OrderID".equals(col))ps.setLong(1,Long.parseLong(val));else ps.setString(1,val);try(ResultSet rs=ps.executeQuery()){return rs.next()?map(rs):null;}}
        catch(Exception e){return null;}
    }
    public List<Map<String,Object>> getOrderItems(long orderId){
        String sql="SELECT oi.OrderItemID,oi.VariantID,oi.ProductName,oi.VariantName,oi.SKU,oi.ImageUrl,oi.UnitPrice,oi.Quantity,oi.DiscountAmount,oi.LineTotal FROM OrderItems oi WHERE oi.OrderID=? ORDER BY oi.OrderItemID";
        List<Map<String,Object>> out=new ArrayList<>();
        try(Connection c=DBContext.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setLong(1,orderId);try(ResultSet rs=ps.executeQuery()){while(rs.next()){Map<String,Object> m=new HashMap<>();m.put("id",rs.getLong("OrderItemID"));m.put("variantId",rs.getInt("VariantID"));m.put("productName",rs.getString("ProductName"));m.put("variantName",rs.getString("VariantName"));m.put("sku",rs.getString("SKU"));m.put("image",rs.getString("ImageUrl"));m.put("price",rs.getBigDecimal("UnitPrice"));m.put("quantity",rs.getInt("Quantity"));m.put("lineTotal",rs.getBigDecimal("LineTotal"));out.add(m);}}}catch(SQLException e){e.printStackTrace();}return out;
    }
    public long createFromCart(int userId,int addressId,String voucher,String payment,String note)throws SQLException{
        try(Connection c=DBContext.getConnection();CallableStatement cs=c.prepareCall("{call dbo.sp_CreateOrderFromCart(?,?,?,?,?,?)}")){
            cs.setInt(1,userId);cs.setInt(2,addressId);
            if(voucher==null||voucher.isBlank())cs.setNull(3,Types.VARCHAR);else cs.setString(3,voucher.trim());
            cs.setString(4,payment==null||payment.isBlank()?"COD":payment);
            if(note==null)cs.setNull(5,Types.NVARCHAR);else cs.setString(5,note);
            cs.registerOutParameter(6,Types.BIGINT);cs.execute();return cs.getLong(6);
        }
    }
    public void cancel(long orderId,int userId,String reason)throws SQLException{
        try(Connection c=DBContext.getConnection();CallableStatement cs=c.prepareCall("{call dbo.sp_CancelOrder(?,?,?)}")){cs.setLong(1,orderId);cs.setInt(2,userId);cs.setString(3,reason);cs.execute();}
    }
    public boolean updateStatus(long id,String status,int changedBy)throws SQLException{
        try(Connection c=DBContext.getConnection();CallableStatement cs=c.prepareCall("{call dbo.sp_UpdateOrderStatus(?,?,?,?)}")){cs.setLong(1,id);cs.setString(2,status);cs.setInt(3,changedBy);cs.setNull(4,Types.NVARCHAR);cs.execute();return true;}
    }
    private Order map(ResultSet rs)throws SQLException{
        Order o=new Order();o.setId(rs.getInt("OrderID"));o.setCode(rs.getString("OrderCode"));o.setUserId(rs.getInt("CustomerID"));o.setCustomerName(rs.getString("RecipientName"));o.setPhone(rs.getString("RecipientPhone"));o.setShippingAddress(rs.getString("ShippingAddress"));o.setTotalPrice(rs.getBigDecimal("TotalAmount"));o.setStatus(rs.getString("OrderStatus"));Timestamp t=rs.getTimestamp("CreatedAt");if(t!=null)o.setCreatedAt(new java.util.Date(t.getTime()));return o;
    }
    public List<Order> search(String keyword,String status){
        List<Order> all=findAll();List<Order> out=new ArrayList<>();
        String k=keyword==null?"":keyword.trim().toLowerCase();
        for(Order o:all)if((k.isEmpty()||o.getCode().toLowerCase().contains(k)||(o.getCustomerName()!=null&&o.getCustomerName().toLowerCase().contains(k))||(o.getPhone()!=null&&o.getPhone().contains(k)))&&(status==null||status.isBlank()||status.equalsIgnoreCase(o.getStatusCode())))out.add(o);
        return out;
    }
}
