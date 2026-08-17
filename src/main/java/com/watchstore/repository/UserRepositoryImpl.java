package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Role;
import com.watchstore.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation — kết nối trực tiếp SQL Server qua DBContext.
 * Đọc/ghi bảng Users + UserRoles (M:N) + Roles.
 */
public class UserRepositoryImpl implements UserRepository {

    private Connection getConnection() throws SQLException {
        return DBContext.getConnection();
    }

    // ─── Reusable mapper (Users row → User object) ───────────────────────────

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("UserID"));
        u.setEmail(rs.getString("Email"));
        u.setPasswordHash(rs.getString("PasswordHash"));
        u.setFullName(rs.getString("FullName"));
        u.setPhone(rs.getString("Phone"));
        u.setGender(rs.getString("Gender"));

        Date dob = rs.getDate("DateOfBirth");
        if (dob != null) {
            u.setDateOfBirth(dob.toLocalDate());
        }

        u.setAvatarUrl(rs.getString("AvatarUrl"));
        u.setStatus(rs.getString("Status"));

        Timestamp ts;
        ts = rs.getTimestamp("EmailVerifiedAt");
        if (ts != null) u.setEmailVerifiedAt(ts.toLocalDateTime());

        ts = rs.getTimestamp("LastLoginAt");
        if (ts != null) u.setLastLoginAt(ts.toLocalDateTime());

        ts = rs.getTimestamp("CreatedAt");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());

        ts = rs.getTimestamp("UpdatedAt");
        if (ts != null) u.setUpdatedAt(ts.toLocalDateTime());

        return u;
    }

    // ─── Load roles cho 1 user ───────────────────────────────────────────────

    private List<Role> loadRoles(Connection con, int userId) throws SQLException {
        List<Role> roles = new ArrayList<>();
        String sql = """
                SELECT r.RoleID, r.RoleCode, r.RoleName
                FROM UserRoles ur
                INNER JOIN Roles r ON ur.RoleID = r.RoleID
                WHERE ur.UserID = ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Role role = new Role();
                role.setRoleId(rs.getInt("RoleID"));
                role.setRoleCode(rs.getString("RoleCode"));
                role.setRoleName(rs.getString("RoleName"));
                roles.add(role);
            }
        }
        return roles;
    }

    // ─── Build tên roles dạng chuỗi ─────────────────────────────────────────

    private String buildRoleNames(List<Role> roles) {
        if (roles == null || roles.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < roles.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(roles.get(i).getRoleName());
        }
        return sb.toString();
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users ORDER BY UserID DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User u = mapRow(rs);
                List<Role> roles = loadRoles(con, u.getUserId());
                u.setRoles(roles);
                u.setRoleNames(buildRoleNames(roles));
                list.add(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─── findById ────────────────────────────────────────────────────────────

    @Override
    public User findById(int id) {
        String sql = "SELECT * FROM Users WHERE UserID = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = mapRow(rs);
                List<Role> roles = loadRoles(con, u.getUserId());
                u.setRoles(roles);
                u.setRoleNames(buildRoleNames(roles));
                return u;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ─── search (Email, FullName, Phone) ─────────────────────────────────────

    @Override
    public List<User> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }

        List<User> list = new ArrayList<>();
        String sql = """
                SELECT * FROM Users
                WHERE Email LIKE ?
                   OR FullName LIKE ?
                   OR Phone LIKE ?
                ORDER BY UserID DESC
                """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String key = "%" + keyword.trim() + "%";
            ps.setString(1, key);
            ps.setString(2, key);
            ps.setString(3, key);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User u = mapRow(rs);
                List<Role> roles = loadRoles(con, u.getUserId());
                u.setRoles(roles);
                u.setRoleNames(buildRoleNames(roles));
                list.add(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─── insert (User + UserRoles trong transaction) ─────────────────────────

    @Override
    public boolean insert(User user, List<Integer> roleIds) {
        String sqlUser = """
                INSERT INTO Users (Email, PasswordHash, FullName, Phone, Gender, DateOfBirth,
                                   AvatarUrl, Status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                int userId;
                // Insert user
                try (PreparedStatement ps = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, user.getEmail());
                    ps.setString(2, user.getPasswordHash());
                    ps.setString(3, user.getFullName());
                    setNullableString(ps, 4, user.getPhone());
                    setNullableString(ps, 5, user.getGender());
                    if (user.getDateOfBirth() != null) {
                        ps.setDate(6, Date.valueOf(user.getDateOfBirth()));
                    } else {
                        ps.setNull(6, Types.DATE);
                    }
                    setNullableString(ps, 7, user.getAvatarUrl());
                    ps.setString(8, user.getStatus() != null ? user.getStatus() : "ACTIVE");

                    ps.executeUpdate();

                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys.next()) {
                        userId = keys.getInt(1);
                        user.setUserId(userId);
                    } else {
                        throw new SQLException("Không lấy được UserID sau INSERT.");
                    }
                }

                // Insert UserRoles
                insertUserRoles(con, userId, roleIds);

                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ─── update (User + xóa UserRoles cũ + insert lại — transaction) ────────

    @Override
    public boolean update(User user, List<Integer> roleIds) {
        String sqlUser = """
                UPDATE Users
                SET Email = ?, PasswordHash = ?, FullName = ?, Phone = ?,
                    Gender = ?, DateOfBirth = ?, AvatarUrl = ?, Status = ?,
                    UpdatedAt = SYSDATETIME()
                WHERE UserID = ?
                """;

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                // Update user
                try (PreparedStatement ps = con.prepareStatement(sqlUser)) {
                    ps.setString(1, user.getEmail());
                    ps.setString(2, user.getPasswordHash());
                    ps.setString(3, user.getFullName());
                    setNullableString(ps, 4, user.getPhone());
                    setNullableString(ps, 5, user.getGender());
                    if (user.getDateOfBirth() != null) {
                        ps.setDate(6, Date.valueOf(user.getDateOfBirth()));
                    } else {
                        ps.setNull(6, Types.DATE);
                    }
                    setNullableString(ps, 7, user.getAvatarUrl());
                    ps.setString(8, user.getStatus() != null ? user.getStatus() : "ACTIVE");
                    ps.setInt(9, user.getUserId());

                    ps.executeUpdate();
                }

                // Xóa UserRoles cũ
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM UserRoles WHERE UserID = ?")) {
                    ps.setInt(1, user.getUserId());
                    ps.executeUpdate();
                }

                // Insert UserRoles mới
                insertUserRoles(con, user.getUserId(), roleIds);

                con.commit();
                return true;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Override
    public boolean delete(int id) {
        // UserRoles sẽ tự xóa theo ON DELETE CASCADE
        String sql = "DELETE FROM Users WHERE UserID = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ─── existsByEmail ───────────────────────────────────────────────────────

    @Override
    public boolean existsByEmail(String email, Integer excludeId) {
        if (email == null || email.isBlank()) return false;

        String sql = (excludeId != null && excludeId > 0)
                ? "SELECT COUNT(*) FROM Users WHERE LOWER(Email) = LOWER(?) AND UserID <> ?"
                : "SELECT COUNT(*) FROM Users WHERE LOWER(Email) = LOWER(?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            if (excludeId != null && excludeId > 0) {
                ps.setInt(2, excludeId);
            }
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ─── existsByPhone ───────────────────────────────────────────────────────

    @Override
    public boolean existsByPhone(String phone, Integer excludeId) {
        if (phone == null || phone.isBlank()) return false;

        String sql = (excludeId != null && excludeId > 0)
                ? "SELECT COUNT(*) FROM Users WHERE Phone = ? AND UserID <> ?"
                : "SELECT COUNT(*) FROM Users WHERE Phone = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, phone.trim());
            if (excludeId != null && excludeId > 0) {
                ps.setInt(2, excludeId);
            }
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isUserInUse(int userId) {
        String sql = """
                SELECT (
                    (SELECT COUNT(*) FROM Orders WHERE CustomerID = ?) +
                    (SELECT COUNT(*) FROM Reviews WHERE UserID = ?) +
                    (SELECT COUNT(*) FROM Posts WHERE AuthorID = ?) +
                    (SELECT COUNT(*) FROM CustomerNotes WHERE CustomerID = ? OR StaffID = ?) +
                    (SELECT COUNT(*) FROM VoucherUsages WHERE UserID = ?)
                ) AS TotalRefs
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.setInt(4, userId);
            ps.setInt(5, userId);
            ps.setInt(6, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private void insertUserRoles(Connection con, int userId, List<Integer> roleIds) throws SQLException {
        if (roleIds == null || roleIds.isEmpty()) return;

        String sql = "INSERT INTO UserRoles (UserID, RoleID) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int roleId : roleIds) {
                ps.setInt(1, userId);
                ps.setInt(2, roleId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }
}