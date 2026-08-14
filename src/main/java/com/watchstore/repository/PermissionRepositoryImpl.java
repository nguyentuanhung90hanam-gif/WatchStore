package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Permission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermissionRepositoryImpl implements PermissionRepository {

    private Connection getConnection() throws SQLException {
        return DBContext.getConnection();
    }

    private Permission mapRow(ResultSet rs) throws SQLException {
        Permission p = new Permission();
        p.setPermissionId(rs.getInt("PermissionID"));
        p.setPermissionCode(rs.getString("PermissionCode"));
        p.setPermissionName(rs.getString("PermissionName"));
        p.setModuleCode(rs.getString("ModuleCode"));
        p.setDescription(rs.getString("Description"));
        return p;
    }

    @Override
    public List<Permission> findAll() {
        List<Permission> list = new ArrayList<>();
        String sql = "SELECT * FROM Permissions ORDER BY ModuleCode ASC, PermissionID ASC";

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
            SELECT * FROM Permissions
            WHERE PermissionCode LIKE ? OR PermissionName LIKE ? OR ModuleCode LIKE ?
            ORDER BY ModuleCode ASC, PermissionID ASC
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
    public List<Integer> getPermissionIdsByRoleId(int roleId) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT PermissionID FROM RolePermissions WHERE RoleID = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean addRolePermission(int roleId, int permissionId) {
        String sql = "INSERT INTO RolePermissions (RoleID, PermissionID) " +
                     "SELECT ?, ? WHERE NOT EXISTS (" +
                     "SELECT 1 FROM RolePermissions WHERE RoleID = ? AND PermissionID = ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            ps.setInt(2, permissionId);
            ps.setInt(3, roleId);
            ps.setInt(4, permissionId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeRolePermission(int roleId, int permissionId) {
        String sql = "DELETE FROM RolePermissions WHERE RoleID = ? AND PermissionID = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            ps.setInt(2, permissionId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
