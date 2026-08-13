package com.watchstore.repository;

import com.watchstore.config.DBContext;
import java.sql.*;
import java.util.*;

public class AddressRepository {
    public List<Map<String,Object>> findAll(int userId) throws SQLException {
        String sql="SELECT AddressID,RecipientName,RecipientPhone,Province,District,Ward,AddressLine,AddressType,IsDefault FROM UserAddresses WHERE UserID=? ORDER BY IsDefault DESC,AddressID DESC";
        List<Map<String,Object>> list=new ArrayList<>();
        try(Connection c=DBContext.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,userId);try(ResultSet rs=ps.executeQuery()){
            while(rs.next()){Map<String,Object> m=new HashMap<>();m.put("id",rs.getInt("AddressID"));m.put("name",rs.getString("RecipientName"));m.put("phone",rs.getString("RecipientPhone"));m.put("province",rs.getString("Province"));m.put("district",rs.getString("District"));m.put("ward",rs.getString("Ward"));m.put("line",rs.getString("AddressLine"));m.put("type",rs.getString("AddressType"));m.put("default",rs.getBoolean("IsDefault"));list.add(m);}
        }} return list;
    }
    public void save(int userId,Integer id,String name,String phone,String province,String district,String ward,String line,String type,boolean isDefault) throws SQLException {
        try(Connection c=DBContext.getConnection();CallableStatement cs=c.prepareCall("{call dbo.sp_SaveAddress(?,?,?,?,?,?,?,?,?,?)}")){
            cs.setInt(1,userId);if(id==null)cs.setNull(2,Types.INTEGER);else cs.setInt(2,id);
            cs.setString(3,name);cs.setString(4,phone);cs.setString(5,province);cs.setString(6,district);cs.setString(7,ward);cs.setString(8,line);cs.setString(9,type==null?"HOME":type);cs.setBoolean(10,isDefault);cs.execute();
        }
    }
    public void delete(int userId,int id) throws SQLException {
        try(Connection c=DBContext.getConnection();CallableStatement cs=c.prepareCall("{call dbo.sp_DeleteAddress(?,?)}")){cs.setInt(1,userId);cs.setInt(2,id);cs.execute();}
    }
}
