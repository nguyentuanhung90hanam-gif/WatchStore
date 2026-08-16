package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation — kết nối trực tiếp SQL Server qua DBContext.
 * Đọc/ghi bảng Roles + COUNT UserRoles để tính userCount.
 */
public class RoleRepositoryImpl implements RoleRepository {

    private Connection getConnection() throws SQLException {
        return DBContext.getConnection();
    }

    // ─── Reusable mapper ─────────────────────────────────────────────────────

    private Role mapRow(ResultSet rs) throws SQLException {
        Role r = new Role();
        r.setRoleId(rs.getInt("RoleID"));
        r.setRoleCode(rs.getString("RoleCode"));
        r.setRoleName(rs.getString("RoleName"));
        r.setDescription(rs.getString("Description"));

        String code = r.getRoleCode();
        r.setIsSystem("ADMIN".equalsIgnoreCase(code) || "EMPLOYEE".equalsIgnoreCase(code) || "CUSTOMER".equalsIgnoreCase(code));

        // UserCount nếu có trong ResultSet
        try {
            r.setUserCount(rs.getInt("UserCount"));
        } catch (SQLException ignored) {
            // Cột UserCount không có trong mọi query
        }

        return r;
    }

    // ─── findAll (có đếm số user gán role) ───────────────────────────────────

    @Override
    public List<Role> findAll() {
        List<Role> list = new ArrayList<>();
        String sql = """
                SELECT r.*,
                       (SELECT COUNT(*) FROM UserRoles ur WHERE ur.RoleID = r.RoleID) AS UserCount
                FROM Roles r
                ORDER BY r.RoleID ASC
                """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─── findById ────────────────────────────────────────────────────────────

    @Override
    public Role findById(int id) {
        String sql = """
                SELECT r.*,
                       (SELECT COUNT(*) FROM UserRoles ur WHERE ur.RoleID = r.RoleID) AS UserCount
                FROM Roles r
                WHERE r.RoleID = ?
                """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ─── search ──────────────────────────────────────────────────────────────

    @Override
    public List<Role> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }

        List<Role> list = new ArrayList<>();
        String sql = """
                SELECT r.*,
                       (SELECT COUNT(*) FROM UserRoles ur WHERE ur.RoleID = r.RoleID) AS UserCount
                FROM Roles r
                WHERE r.RoleCode LIKE ?
                   OR r.RoleName LIKE ?
                   OR r.Description LIKE ?
                ORDER BY r.RoleID ASC
                """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String key = "%" + keyword.trim() + "%";
            ps.setString(1, key);
            ps.setString(2, key);
            ps.setString(3, key);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─── insert ──────────────────────────────────────────────────────────────

    @Override
    public boolean insert(Role role) {
        String sql = """
                INSERT INTO Roles (RoleCode, RoleName, Description)
                VALUES (?, ?, ?)
                """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, role.getRoleCode());
            ps.setString(2, role.getRoleName());
            if (role.getDescription() != null && !role.getDescription().isBlank()) {
                ps.setString(3, role.getDescription().trim());
            } else {
                ps.setNull(3, Types.NVARCHAR);
            }

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Override
    public boolean update(Role role) {
        String sql = """
                UPDATE Roles
                SET RoleCode = ?, RoleName = ?, Description = ?
                WHERE RoleID = ?
                """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, role.getRoleCode());
            ps.setString(2, role.getRoleName());
            if (role.getDescription() != null && !role.getDescription().isBlank()) {
                ps.setString(3, role.getDescription().trim());
            } else {
                ps.setNull(3, Types.NVARCHAR);
            }
            ps.setInt(4, role.getRoleId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ─── delete (chặn xóa System Role và Role đang sử dụng) ───────────────────

    @Override
    public boolean delete(int id) {
        return deleteById(id);
    }

    @Override
    public boolean deleteById(int roleId) {
        Role role = findById(roleId);
        if (role == null) return false;
        if (role.getIsSystem()) return false; // Không cho xóa System Role
        if (isRoleInUse(roleId)) return false; // Không cho xóa nếu đang được sử dụng bởi User

        String sql = "DELETE FROM Roles WHERE RoleID = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RoleRepositoryImpl.deleteById] SQLException for roleId=" + roleId + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[RoleRepositoryImpl.deleteById] Exception for roleId=" + roleId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isRoleInUse(int roleId) {
        String sql = "SELECT COUNT(*) FROM UserRoles WHERE RoleID = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ─── existsByCode ────────────────────────────────────────────────────────

    @Override
    public boolean existsByCode(String code, Integer excludeId) {
        if (code == null || code.isBlank()) return false;

        String sql = (excludeId != null && excludeId > 0)
                ? "SELECT COUNT(*) FROM Roles WHERE LOWER(RoleCode) = LOWER(?) AND RoleID <> ?"
                : "SELECT COUNT(*) FROM Roles WHERE LOWER(RoleCode) = LOWER(?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, code.trim());
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
}