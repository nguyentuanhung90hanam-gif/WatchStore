package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        if (user == null) return false;
        String sqlUser = "UPDATE Users SET FullName = ?, Email = ?, Phone = ? WHERE UserID = ?";
        try (Connection con = DBContext.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement psUser = con.prepareStatement(sqlUser)) {
                    psUser.setString(1, user.getFullName());
                    psUser.setString(2, user.getEmail());
                    psUser.setString(3, user.getPhone());
                    psUser.setInt(4, user.getId());
                    psUser.executeUpdate();
                }

                // Cập nhật địa chỉ
                if (user.getAddress() != null && !user.getAddress().isBlank() && !user.getAddress().contains("Chưa cập nhật")) {
                    String sqlCheckAddr = "SELECT COUNT(*) FROM UserAddresses WHERE UserID = ? AND IsDefault = 1";
                    boolean hasDefault = false;
                    try (PreparedStatement psCheck = con.prepareStatement(sqlCheckAddr)) {
                        psCheck.setInt(1, user.getId());
                        try (ResultSet rs = psCheck.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                hasDefault = true;
                            }
                        }
                    }

                    if (hasDefault) {
                        String sqlUpdateAddr = "UPDATE UserAddresses SET AddressLine = ?, RecipientName = ?, RecipientPhone = ? WHERE UserID = ? AND IsDefault = 1";
                        try (PreparedStatement psUp = con.prepareStatement(sqlUpdateAddr)) {
                            psUp.setString(1, user.getAddress());
                            psUp.setString(2, user.getFullName());
                            psUp.setString(3, user.getPhone() != null ? user.getPhone() : "");
                            psUp.setInt(4, user.getId());
                            psUp.executeUpdate();
                        }
                    } else {
                        String sqlInsertAddr = "INSERT INTO UserAddresses (UserID, RecipientName, RecipientPhone, Province, District, Ward, AddressLine, IsDefault) VALUES (?, ?, ?, ?, ?, ?, ?, 1)";
                        try (PreparedStatement psIns = con.prepareStatement(sqlInsertAddr)) {
                            psIns.setInt(1, user.getId());
                            psIns.setString(2, user.getFullName());
                            psIns.setString(3, user.getPhone() != null ? user.getPhone() : "");
                            psIns.setString(4, "Hà Nội"); // Tỉnh mặc định
                            psIns.setString(5, "Nam Từ Liêm"); // Huyện mặc định
                            psIns.setString(6, "Mỹ Đình"); // Xã mặc định
                            psIns.setString(7, user.getAddress());
                            psIns.executeUpdate();
                        }
                    }
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
}