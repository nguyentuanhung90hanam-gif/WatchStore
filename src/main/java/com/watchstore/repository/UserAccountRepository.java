package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.enums.Role;
import com.watchstore.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class UserAccountRepository {

    public User findById(int userId) throws Exception {
        if (userId <= 0) throw new Exception("Tài khoản không hợp lệ.");

        String sql = "SELECT TOP 1 u.UserID, u.FullName, u.Email, u.Phone, u.Gender, " +
                "u.DateOfBirth, u.AvatarUrl, u.Status, r.RoleCode " +
                "FROM dbo.Users u " +
                "LEFT JOIN dbo.UserRoles ur ON ur.UserID = u.UserID " +
                "LEFT JOIN dbo.Roles r ON r.RoleID = ur.RoleID " +
                "WHERE u.UserID = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Tài khoản không tồn tại.");
                Date dob = rs.getDate("DateOfBirth");
                return new User(
                        rs.getInt("UserID"),
                        rs.getString("FullName"),
                        rs.getString("Email"),
                        rs.getString("Phone"),
                        parseRole(rs.getString("RoleCode")),
                        rs.getString("Gender"),
                        dob == null ? null : dob.toLocalDate(),
                        rs.getString("AvatarUrl"),
                        rs.getString("Status")
                );
            }
        } catch (SQLException e) {
            throw new Exception("Không thể tải thông tin tài khoản.", e);
        }
    }

    public void updateProfile(int userId, String fullName, String phone) throws Exception {
        if (userId <= 0) throw new Exception("Tài khoản không hợp lệ.");
        if (fullName == null || fullName.isBlank()) throw new Exception("Họ và tên không được để trống.");
        String normalizedName = fullName.trim();
        if (normalizedName.length() > 150) throw new Exception("Họ và tên không được vượt quá 150 ký tự.");

        String normalizedPhone = phone == null ? "" : phone.trim();
        if (!normalizedPhone.isEmpty() && !normalizedPhone.matches("0\\d{9,10}")) {
            throw new Exception("Số điện thoại phải gồm 10 hoặc 11 chữ số và bắt đầu bằng 0.");
        }

        String sql = "UPDATE dbo.Users SET FullName=?, Phone=?, UpdatedAt=SYSDATETIME() WHERE UserID=? AND Status='ACTIVE'";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedName);
            if (normalizedPhone.isEmpty()) ps.setNull(2, java.sql.Types.VARCHAR);
            else ps.setString(2, normalizedPhone);
            ps.setInt(3, userId);
            if (ps.executeUpdate() != 1) throw new Exception("Không thể cập nhật thông tin tài khoản.");
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("ux_users_phone")) {
                throw new Exception("Số điện thoại đã được sử dụng bởi tài khoản khác.");
            }
            throw new Exception("Không thể cập nhật thông tin tài khoản.", e);
        }
    }

    public void updateProfile(int userId, String fullName, String phone,
                              String gender, LocalDate dateOfBirth) throws Exception {
        if (userId <= 0) throw new Exception("Tài khoản không hợp lệ.");
        if (fullName == null || fullName.isBlank()) throw new Exception("Họ và tên không được để trống.");
        String normalizedName = fullName.trim();
        if (normalizedName.length() > 150) throw new Exception("Họ và tên không được vượt quá 150 ký tự.");

        String normalizedPhone = phone == null ? "" : phone.trim();
        if (!normalizedPhone.isEmpty() && !normalizedPhone.matches("0\\d{9,10}")) {
            throw new Exception("Số điện thoại phải gồm 10 hoặc 11 chữ số và bắt đầu bằng 0.");
        }

        String normalizedGender = gender == null ? null : gender.trim().toUpperCase();
        if (normalizedGender != null && !normalizedGender.isEmpty() &&
                !normalizedGender.matches("MALE|FEMALE|OTHER")) {
            throw new Exception("Giới tính không hợp lệ.");
        }

        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw new Exception("Ngày sinh không được lớn hơn ngày hiện tại.");
        }

        String sql = "UPDATE dbo.Users SET FullName=?, Phone=?, Gender=?, DateOfBirth=?, UpdatedAt=SYSDATETIME() " +
                "WHERE UserID=? AND Status='ACTIVE'";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizedName);
            if (normalizedPhone.isEmpty()) ps.setNull(2, java.sql.Types.VARCHAR);
            else ps.setString(2, normalizedPhone);
            if (normalizedGender == null || normalizedGender.isEmpty()) ps.setNull(3, java.sql.Types.VARCHAR);
            else ps.setString(3, normalizedGender);
            if (dateOfBirth == null) ps.setNull(4, java.sql.Types.DATE);
            else ps.setDate(4, Date.valueOf(dateOfBirth));
            ps.setInt(5, userId);
            if (ps.executeUpdate() != 1) throw new Exception("Không thể cập nhật thông tin tài khoản.");
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("ux_users_phone")) {
                throw new Exception("Số điện thoại đã được sử dụng bởi tài khoản khác.");
            }
            throw new Exception("Không thể cập nhật thông tin tài khoản.", e);
        }
    }

    public void changePassword(int userId, String currentPassword, String newPassword) throws Exception {
        changePassword(userId, currentPassword, newPassword, newPassword);
    }

    public void changePassword(int userId, String currentPassword, String newPassword, String confirmPassword) throws Exception {
        if (currentPassword == null || currentPassword.isBlank()) throw new Exception("Vui lòng nhập mật khẩu hiện tại.");
        if (newPassword == null || newPassword.length() < 6) throw new Exception("Mật khẩu mới phải có ít nhất 6 ký tự.");
        if (!newPassword.matches(".*[A-Z].*") || !newPassword.matches(".*[a-z].*") || !newPassword.matches(".*\\d.*")) {
            throw new Exception("Mật khẩu mới phải có chữ hoa, chữ thường và chữ số.");
        }
        if (confirmPassword == null || !newPassword.equals(confirmPassword)) throw new Exception("Xác nhận mật khẩu mới không khớp.");
        if (currentPassword.equals(newPassword)) throw new Exception("Mật khẩu mới phải khác mật khẩu hiện tại.");

        String select = "SELECT PasswordHash FROM dbo.Users WHERE UserID=? AND Status='ACTIVE'";
        String update = "UPDATE dbo.Users SET PasswordHash=?, UpdatedAt=SYSDATETIME() WHERE UserID=? AND Status='ACTIVE'";
        try (Connection conn = DBContext.getConnection()) {
            String hash;
            try (PreparedStatement ps = conn.prepareStatement(select)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new Exception("Tài khoản không tồn tại hoặc đã bị khóa.");
                    hash = rs.getString("PasswordHash");
                }
            }
            if (hash == null || !hash.equals(sha256(currentPassword))) throw new Exception("Mật khẩu hiện tại không đúng.");
            try (PreparedStatement ps = conn.prepareStatement(update)) {
                ps.setString(1, sha256(newPassword));
                ps.setInt(2, userId);
                if (ps.executeUpdate() != 1) throw new Exception("Không thể đổi mật khẩu.");
            }
        } catch (SQLException e) {
            throw new Exception("Không thể đổi mật khẩu.", e);
        }
    }

    private Role parseRole(String roleCode) {
        if (roleCode == null) return Role.CUSTOMER;
        try { return Role.valueOf(roleCode.toUpperCase()); }
        catch (IllegalArgumentException e) { return Role.CUSTOMER; }
    }

    private String sha256(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }
}
