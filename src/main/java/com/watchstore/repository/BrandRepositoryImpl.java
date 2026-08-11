package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Brand;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BrandRepositoryImpl implements BrandRepository {

    private Connection getConnection() throws SQLException {
        return DBContext.getConnection();
    }

    // ─── Reusable mapper ─────────────────────────────────────────────────────

    private Brand mapRow(ResultSet rs) throws SQLException {
        Brand b = new Brand();
        b.setBrandID(rs.getInt("BrandID"));
        b.setBrandCode(rs.getString("BrandCode"));
        b.setBrandName(rs.getString("BrandName"));
        b.setSlug(rs.getString("Slug"));
        b.setOriginCountry(rs.getString("OriginCountry"));
        b.setLogoUrl(rs.getString("LogoUrl"));
        b.setDescription(rs.getString("Description"));
        b.setStatus(rs.getString("Status"));
        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) {
            b.setCreatedAt(ts.toLocalDateTime());
        }
        return b;
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Override
    public List<Brand> findAll() {
        List<Brand> list = new ArrayList<>();
        String sql = "SELECT * FROM Brands ORDER BY BrandID DESC";

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
    public Brand findById(int id) {
        String sql = "SELECT * FROM Brands WHERE BrandID = ?";

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
    public List<Brand> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }

        List<Brand> list = new ArrayList<>();
        String sql = """
                SELECT * FROM Brands
                WHERE BrandCode LIKE ?
                   OR BrandName LIKE ?
                   OR Slug LIKE ?
                ORDER BY BrandID DESC
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

    // ─── existsByCode ────────────────────────────────────────────────────────

    @Override
    public boolean existsByCode(String code, Integer excludeId) {
        if (code == null || code.isBlank()) return false;
        String sql = (excludeId != null && excludeId > 0)
                ? "SELECT COUNT(*) FROM Brands WHERE LOWER(BrandCode) = LOWER(?) AND BrandID <> ?"
                : "SELECT COUNT(*) FROM Brands WHERE LOWER(BrandCode) = LOWER(?)";

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

    // ─── existsBySlug ────────────────────────────────────────────────────────

    @Override
    public boolean existsBySlug(String slug, Integer excludeId) {
        if (slug == null || slug.isBlank()) return false;
        String sql = (excludeId != null && excludeId > 0)
                ? "SELECT COUNT(*) FROM Brands WHERE LOWER(Slug) = LOWER(?) AND BrandID <> ?"
                : "SELECT COUNT(*) FROM Brands WHERE LOWER(Slug) = LOWER(?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, slug.trim());
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

    // ─── insert ──────────────────────────────────────────────────────────────

    @Override
    public boolean insert(Brand brand) {
        String sql = """
                INSERT INTO Brands (BrandCode, BrandName, Slug, OriginCountry, LogoUrl, Description, Status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, brand.getBrandCode());
            ps.setString(2, brand.getBrandName());
            ps.setString(3, brand.getSlug());
            ps.setString(4, nullIfBlank(brand.getOriginCountry()));
            ps.setString(5, nullIfBlank(brand.getLogoUrl()));
            ps.setString(6, nullIfBlank(brand.getDescription()));
            ps.setString(7, brand.getStatus() != null ? brand.getStatus() : "ACTIVE");

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Override
    public boolean update(Brand brand) {
        String sql = """
                UPDATE Brands
                SET BrandCode=?, BrandName=?, Slug=?, OriginCountry=?, LogoUrl=?, Description=?, Status=?
                WHERE BrandID=?
                """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, brand.getBrandCode());
            ps.setString(2, brand.getBrandName());
            ps.setString(3, brand.getSlug());
            ps.setString(4, nullIfBlank(brand.getOriginCountry()));
            ps.setString(5, nullIfBlank(brand.getLogoUrl()));
            ps.setString(6, nullIfBlank(brand.getDescription()));
            ps.setString(7, brand.getStatus() != null ? brand.getStatus() : "ACTIVE");
            ps.setInt(8, brand.getBrandID());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Brands WHERE BrandID = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isBrandInUse(int brandId) {
        String sql = "SELECT COUNT(*) FROM Products WHERE BrandID = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, brandId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ─── helper ──────────────────────────────────────────────────────────────

    private String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}