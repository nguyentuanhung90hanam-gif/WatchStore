package com.watchstore.repository;

import com.watchstore.config.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressRepository {

    public List<Map<String, Object>> findAll(int userId) throws SQLException {
        String sql = """
            SELECT AddressID, RecipientName, RecipientPhone, Province, District, Ward, AddressLine, AddressType, IsDefault
            FROM dbo.UserAddresses
            WHERE UserID = ?
            ORDER BY IsDefault DESC, AddressID DESC
            """;
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", rs.getInt("AddressID"));
                    m.put("name", rs.getString("RecipientName"));
                    m.put("phone", rs.getString("RecipientPhone"));
                    m.put("province", rs.getString("Province"));
                    m.put("district", rs.getString("District"));
                    m.put("ward", rs.getString("Ward"));
                    m.put("line", rs.getString("AddressLine"));
                    m.put("type", rs.getString("AddressType"));
                    m.put("default", rs.getBoolean("IsDefault"));
                    list.add(m);
                }
            }
        }
        return list;
    }

    public void save(int userId, Integer id, String name, String phone, String province, String district, String ward, String line, String type, boolean isDefault) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Khách hàng không hợp lệ.");
        }
        if (name == null || name.trim().length() < 2 || name.trim().length() > 100) {
            throw new IllegalArgumentException("Họ và tên người nhận phải từ 2 đến 100 ký tự.");
        }
        if (phone == null || !phone.matches("\\d{9,11}")) {
            throw new IllegalArgumentException("Số điện thoại phải từ 9 đến 11 chữ số.");
        }
        if (province == null || province.isBlank() || district == null || district.isBlank() || ward == null || ward.isBlank() || line == null || line.isBlank()) {
            throw new IllegalArgumentException("Vui lòng điền đầy đủ thông tin địa chỉ.");
        }

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            // Nếu đặt là mặc định, reset các địa chỉ cũ của user
            if (isDefault) {
                String resetDefaultSql = "UPDATE dbo.UserAddresses SET IsDefault = 0 WHERE UserID = ?";
                try (PreparedStatement ps = conn.prepareStatement(resetDefaultSql)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
            } else if (id == null) {
                // Nếu là địa chỉ đầu tiên của user thì tự động là default
                String countSql = "SELECT COUNT(*) FROM dbo.UserAddresses WHERE UserID = ?";
                try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            isDefault = true;
                        }
                    }
                }
            }

            if (id == null || id <= 0) {
                String insertSql = """
                    INSERT INTO dbo.UserAddresses (UserID, RecipientName, RecipientPhone, Province, District, Ward, AddressLine, AddressType, IsDefault, CreatedAt)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME())
                    """;
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, name.trim());
                    ps.setString(3, phone.trim());
                    ps.setString(4, province.trim());
                    ps.setString(5, district.trim());
                    ps.setString(6, ward.trim());
                    ps.setString(7, line.trim());
                    ps.setString(8, type != null && !type.isBlank() ? type.trim() : "HOME");
                    ps.setBoolean(9, isDefault);
                    ps.executeUpdate();
                }
            } else {
                String updateSql = """
                    UPDATE dbo.UserAddresses
                    SET RecipientName = ?, RecipientPhone = ?, Province = ?, District = ?, Ward = ?, AddressLine = ?, AddressType = ?, IsDefault = ?
                    WHERE AddressID = ? AND UserID = ?
                    """;
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, name.trim());
                    ps.setString(2, phone.trim());
                    ps.setString(3, province.trim());
                    ps.setString(4, district.trim());
                    ps.setString(5, ward.trim());
                    ps.setString(6, line.trim());
                    ps.setString(7, type != null && !type.isBlank() ? type.trim() : "HOME");
                    ps.setBoolean(8, isDefault);
                    ps.setInt(9, id);
                    ps.setInt(10, userId);
                    ps.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public void delete(int userId, int id) throws SQLException {
        if (userId <= 0 || id <= 0) return;
        String sql = "DELETE FROM dbo.UserAddresses WHERE UserID = ? AND AddressID = ?";
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
}
