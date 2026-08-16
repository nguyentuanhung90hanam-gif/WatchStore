package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CategoryRepositoryImpl implements CategoryRepository {

    private Connection getConnection() throws SQLException {
        return DBContext.getConnection();
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setCategoryId(rs.getInt("CategoryID"));

        if (rs.getObject("ParentCategoryID") != null) {
            c.setParentCategoryId(rs.getInt("ParentCategoryID"));
        } else {
            c.setParentCategoryId(null);
        }

        c.setCategoryCode(rs.getString("CategoryCode"));
        c.setCategoryName(rs.getString("CategoryName"));
        c.setSlug(rs.getString("CategorySlug"));
        c.setDescription(rs.getString("Description"));
        c.setImageUrl(rs.getString("ImageUrl"));
        c.setDisplayOrder(rs.getInt("DisplayOrder"));
        c.setStatus(rs.getString("Status"));

        return c;
    }

    @Override
    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        String sql = """
                SELECT *
                FROM Categories
                ORDER BY DisplayOrder ASC, CategoryID ASC
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToCategory(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public Category findById(Integer id) {
        if (id == null) return null;
        String sql = "SELECT * FROM Categories WHERE CategoryID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToCategory(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean existsByCode(String code, Integer excludeId) {
        if (code == null || code.isBlank()) return false;
        String sql;
        if (excludeId != null && excludeId > 0) {
            sql = "SELECT COUNT(*) FROM Categories WHERE LOWER(CategoryCode) = LOWER(?) AND CategoryID <> ?";
        } else {
            sql = "SELECT COUNT(*) FROM Categories WHERE LOWER(CategoryCode) = LOWER(?)";
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code.trim());
            if (excludeId != null && excludeId > 0) {
                ps.setInt(2, excludeId);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean existsBySlug(String slug, Integer excludeId) {
        if (slug == null || slug.isBlank()) return false;
        String sql;
        if (excludeId != null && excludeId > 0) {
            sql = "SELECT COUNT(*) FROM Categories WHERE LOWER(CategorySlug) = LOWER(?) AND CategoryID <> ?";
        } else {
            sql = "SELECT COUNT(*) FROM Categories WHERE LOWER(CategorySlug) = LOWER(?)";
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, slug.trim());
            if (excludeId != null && excludeId > 0) {
                ps.setInt(2, excludeId);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private int getNextDisplayOrder() {
        String sql = "SELECT COALESCE(MAX(DisplayOrder), 0) + 1 FROM Categories";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    public void save(Category c) {
        String sql = """
                INSERT INTO Categories
                (
                    ParentCategoryID,
                    CategoryCode,
                    CategoryName,
                    CategorySlug,
                    Description,
                    ImageUrl,
                    DisplayOrder,
                    Status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (c.getParentCategoryId() != null && c.getParentCategoryId() > 0) {
                ps.setInt(1, c.getParentCategoryId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }

            ps.setString(2, c.getCategoryCode());
            ps.setString(3, c.getCategoryName());
            ps.setString(4, c.getSlug());
            ps.setString(5, c.getDescription());
            ps.setString(6, c.getImageUrl());

            int displayOrder = (c.getDisplayOrder() != null && c.getDisplayOrder() > 0)
                    ? c.getDisplayOrder()
                    : getNextDisplayOrder();

            ps.setInt(7, displayOrder);
            ps.setString(8, c.getStatus() != null ? c.getStatus() : "ACTIVE");

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Category c) {
        String sql = """
                UPDATE Categories
                SET 
                    ParentCategoryID = ?,
                    CategoryCode = ?,
                    CategoryName = ?,
                    CategorySlug = ?,
                    Description = ?,
                    ImageUrl = ?,
                    DisplayOrder = ?,
                    Status = ?
                WHERE CategoryID = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (c.getParentCategoryId() != null && c.getParentCategoryId() > 0) {
                ps.setInt(1, c.getParentCategoryId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }

            ps.setString(2, c.getCategoryCode());
            ps.setString(3, c.getCategoryName());
            ps.setString(4, c.getSlug());
            ps.setString(5, c.getDescription());
            ps.setString(6, c.getImageUrl());
            ps.setInt(7, (c.getDisplayOrder() != null && c.getDisplayOrder() > 0) ? c.getDisplayOrder() : getNextDisplayOrder());
            ps.setString(8, c.getStatus() != null ? c.getStatus() : "ACTIVE");
            ps.setInt(9, c.getCategoryId());

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Integer id) {
        if (id == null) return;
        String sql = "DELETE FROM Categories WHERE CategoryID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Category> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }

        List<Category> list = new ArrayList<>();
        String sql = """
                SELECT *
                FROM Categories
                WHERE CategoryCode LIKE ?
                   OR CategoryName LIKE ?
                   OR CategorySlug LIKE ?
                ORDER BY DisplayOrder ASC, CategoryID ASC
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String key = "%" + keyword.trim() + "%";
            ps.setString(1, key);
            ps.setString(2, key);
            ps.setString(3, key);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToCategory(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public boolean isCategoryInUse(Integer id) {
        if (id == null || id <= 0) return false;
        String sql = """
                SELECT (
                    (SELECT COUNT(*) FROM Categories WHERE ParentCategoryID = ?) +
                    (SELECT COUNT(*) FROM Products WHERE CategoryID = ?) +
                    (SELECT COUNT(*) FROM VoucherCategories WHERE CategoryID = ?)
                ) AS TotalRefs
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, id);
            ps.setInt(3, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}