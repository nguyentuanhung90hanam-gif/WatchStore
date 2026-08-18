package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.enums.Role;
import com.watchstore.model.Permission;
import com.watchstore.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PermissionRepositoryImpl implements PermissionRepository {

    private Connection getConnection() throws SQLException {
        return DBContext.getConnection();
    }

    private Permission mapRow(ResultSet rs) throws SQLException {
        Permission p = new Permission();
        p.setPermissionId(rs.getInt("PermissionID"));
        p.setPermissionCode(rs.getString("PermissionCode"));
        p.setPermissionName(rs.getString("PermissionName"));
        p.setModuleCode(rs.getString("ModuleGroup"));
        p.setDescription(rs.getString("Description"));
        return p;
    }

    @Override
    public List<Permission> findAll() {
        List<Permission> list = new ArrayList<>();
        String sql = "SELECT * FROM dbo.Permissions ORDER BY ModuleGroup ASC, PermissionID ASC";

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

    @Override
    public List<Permission> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return findAll();
        List<Permission> list = new ArrayList<>();
        String sql = """
            SELECT * FROM dbo.Permissions
            WHERE PermissionCode LIKE ? OR PermissionName LIKE ? OR ModuleGroup LIKE ?
            ORDER BY ModuleGroup ASC, PermissionID ASC
            """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String val = "%" + keyword.trim() + "%";
            ps.setString(1, val);
            ps.setString(2, val);
            ps.setString(3, val);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<User> findAllEmployees() {
        List<User> list = new ArrayList<>();
        String sql = """
            SELECT u.UserID, u.Email, u.FullName, u.Phone
            FROM dbo.Users u
            JOIN dbo.UserRoles ur ON u.UserID = ur.UserID
            JOIN dbo.Roles r ON ur.RoleID = r.RoleID
            WHERE r.RoleCode = 'EMPLOYEE'
            ORDER BY u.FullName ASC
            """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("UserID"));
                u.setEmail(rs.getString("Email"));
                u.setFullName(rs.getString("FullName"));
                u.setPhone(rs.getString("Phone"));
                u.setRole(Role.EMPLOYEE);
                list.add(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Set<Integer> getUserPermissionIds(int userId) {
        Set<Integer> set = new HashSet<>();
        String sql = "SELECT PermissionID FROM dbo.UserPermissions WHERE UserID = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    set.add(rs.getInt("PermissionID"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return set;
    }

    @Override
    public Set<String> getUserPermissionCodes(int userId) {
        Set<String> set = new HashSet<>();
        String sql = """
            SELECT p.PermissionCode
            FROM dbo.Permissions p
            JOIN dbo.UserPermissions up ON p.PermissionID = up.PermissionID
            WHERE up.UserID = ?
            """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    set.add(rs.getString("PermissionCode"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return set;
    }

    @Override
    public void updateUserPermissions(int userId, List<Integer> permissionIds) {
        String deleteSql = "DELETE FROM dbo.UserPermissions WHERE UserID = ?";
        String insertSql = "INSERT INTO dbo.UserPermissions (UserID, PermissionID) VALUES (?, ?)";

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement psDel = con.prepareStatement(deleteSql)) {
                    psDel.setInt(1, userId);
                    psDel.executeUpdate();
                }

                if (permissionIds != null && !permissionIds.isEmpty()) {
                    try (PreparedStatement psIns = con.prepareStatement(insertSql)) {
                        for (Integer pid : permissionIds) {
                            if (pid != null && pid > 0) {
                                psIns.setInt(1, userId);
                                psIns.setInt(2, pid);
                                psIns.addBatch();
                            }
                        }
                        psIns.executeBatch();
                    }
                }
                con.commit();
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
