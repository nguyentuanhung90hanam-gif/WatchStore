package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    /**
     * Mã hóa mật khẩu sử dụng SHA-256 tương thích với SQL Server HASHBYTES
     */
    public String hashPassword(String password) {
        if (password == null) return "";
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            return password;
        }
    }

    /**
     * Lấy tất cả người dùng và vai trò
     */
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = """
            SELECT u.UserID, u.Email, u.PasswordHash, u.FullName, u.Phone, 
                   (SELECT TOP 1 AddressLine + ', ' + Ward + ', ' + District + ', ' + Province FROM UserAddresses WHERE UserID = u.UserID ORDER BY IsDefault DESC) AS Address,
                   r.RoleCode
            FROM Users u
            LEFT JOIN UserRoles ur ON u.UserID = ur.UserID
            LEFT JOIN Roles r ON ur.RoleID = r.RoleID
            ORDER BY u.UserID DESC
            """;
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm người dùng theo ID
     */
    public User findById(int id) {
        String sql = """
            SELECT u.UserID, u.Email, u.PasswordHash, u.FullName, u.Phone, 
                   (SELECT TOP 1 AddressLine + ', ' + Ward + ', ' + District + ', ' + Province FROM UserAddresses WHERE UserID = u.UserID ORDER BY IsDefault DESC) AS Address,
                   r.RoleCode
            FROM Users u
            LEFT JOIN UserRoles ur ON u.UserID = ur.UserID
            LEFT JOIN Roles r ON ur.RoleID = r.RoleID
            WHERE u.UserID = ?
            """;
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm người dùng theo Email (Phục vụ đăng nhập)
     */
    public User findByEmail(String email) {
        String sql = """
            SELECT u.UserID, u.Email, u.PasswordHash, u.FullName, u.Phone, 
                   (SELECT TOP 1 AddressLine + ', ' + Ward + ', ' + District + ', ' + Province FROM UserAddresses WHERE UserID = u.UserID ORDER BY IsDefault DESC) AS Address,
                   r.RoleCode
            FROM Users u
            LEFT JOIN UserRoles ur ON u.UserID = ur.UserID
            LEFT JOIN Roles r ON ur.RoleID = r.RoleID
            WHERE LOWER(u.Email) = LOWER(?)
            """;
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm người dùng theo tên
     */
    public List<User> searchByName(String keyword) {
        List<User> list = new ArrayList<>();
        String sql = """
            SELECT u.UserID, u.Email, u.PasswordHash, u.FullName, u.Phone, 
                   (SELECT TOP 1 AddressLine + ', ' + Ward + ', ' + District + ', ' + Province FROM UserAddresses WHERE UserID = u.UserID ORDER BY IsDefault DESC) AS Address,
                   r.RoleCode
            FROM Users u
            LEFT JOIN UserRoles ur ON u.UserID = ur.UserID
            LEFT JOIN Roles r ON ur.RoleID = r.RoleID
            WHERE LOWER(u.FullName) LIKE ?
            ORDER BY u.UserID DESC
            """;
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tìm khách hàng theo số điện thoại
     */
    public User findByPhone(String phone) {
        String sql = """
            SELECT u.UserID, u.Email, u.PasswordHash, u.FullName, u.Phone, 
                   (SELECT TOP 1 AddressLine + ', ' + Ward + ', ' + District + ', ' + Province FROM UserAddresses WHERE UserID = u.UserID ORDER BY IsDefault DESC) AS Address,
                   r.RoleCode
            FROM Users u
            LEFT JOIN UserRoles ur ON u.UserID = ur.UserID
            LEFT JOIN Roles r ON ur.RoleID = r.RoleID
            WHERE u.Phone = ?
            """;
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cập nhật thông tin khách hàng
     */
    public boolean update(User user) {
        if(user==null)return false;
        if(user.getPhone()!=null && !user.getPhone().isBlank() && !user.getPhone().matches("\\d{9,11}")) return false;
        String sql="UPDATE Users SET FullName=?,Email=?,Phone=?,UpdatedAt=SYSDATETIME() WHERE UserID=?";
        try(Connection con=DBContext.getConnection();PreparedStatement ps=con.prepareStatement(sql)){
            ps.setString(1,user.getFullName());ps.setString(2,user.getEmail());ps.setString(3,user.getPhone());ps.setInt(4,user.getId());
            return ps.executeUpdate()>0;
        }catch(SQLException e){e.printStackTrace();return false;}
    }

    /**
     * Cập nhật mật khẩu người dùng
     */
    public boolean updatePassword(int userId, String oldPassword, String newPassword) {
        User user = findById(userId);
        if (user == null) return false;

        // Nếu người dùng có PasswordHash, kiểm tra mật khẩu cũ
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            String oldHash = hashPassword(oldPassword);
            if (!oldHash.equalsIgnoreCase(user.getPassword())) {
                return false; // Mật khẩu cũ không chính xác
            }
        }

        String newHash = hashPassword(newPassword);
        String sql = "UPDATE Users SET PasswordHash = ? WHERE UserID = ?";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                user.setPassword(newHash);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("UserID"));
        u.setUsername(rs.getString("Email"));
        u.setPassword(rs.getString("PasswordHash"));
        u.setFullName(rs.getString("FullName"));
        u.setEmail(rs.getString("Email"));
        u.setPhone(rs.getString("Phone"));
        u.setAddress(rs.getString("Address") != null ? rs.getString("Address") : "Chưa cập nhật địa chỉ");
        String role = rs.getString("RoleCode");
        u.setRole(role != null ? role : "CUSTOMER");
        return u;
    }
    public User login(String email, String password) throws SQLException {
        try (Connection con=DBContext.getConnection();
             CallableStatement cs=con.prepareCall("{call dbo.sp_Login(?,?)}")) {
            cs.setString(1,email); cs.setString(2,password);
            try(ResultSet rs=cs.executeQuery()){ return rs.next()?mapLogin(rs):null; }
        }
    }

    public User register(String fullName,String email,String phone,String password) throws SQLException {
        try (Connection con=DBContext.getConnection();
             CallableStatement cs=con.prepareCall("{call dbo.sp_RegisterCustomer(?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setString(1,email); cs.setString(2,password); cs.setString(3,fullName);
            if(phone==null||phone.isBlank()) cs.setNull(4,java.sql.Types.VARCHAR); else cs.setString(4,phone);
            cs.setNull(5,java.sql.Types.VARCHAR); cs.setNull(6,java.sql.Types.DATE);
            for(int i=7;i<=12;i++) cs.setNull(i,java.sql.Types.NVARCHAR);
            try(ResultSet rs=cs.executeQuery()){ return rs.next()?mapLogin(rs):null; }
        }
    }

    private User mapLogin(ResultSet rs) throws SQLException {
        User u=new User();
        u.setId(rs.getInt("UserID")); u.setEmail(rs.getString("Email"));
        u.setFullName(rs.getString("FullName")); u.setPhone(rs.getString("Phone"));
        u.setRole(rs.getString("RoleCode"));
        return u;
    }

}