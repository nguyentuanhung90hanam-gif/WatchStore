package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Banner;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BannerRepositoryImpl implements BannerRepository {

    private Connection getConnection() throws SQLException {
        return DBContext.getConnection();
    }

    private Banner mapRow(ResultSet rs) throws SQLException {
        Banner b = new Banner();
        b.setBannerId(rs.getInt("BannerID"));
        b.setBannerName(rs.getString("BannerName"));
        b.setTitle(rs.getString("Title"));
        b.setSubtitle(rs.getString("Subtitle"));
        b.setImageUrl(rs.getString("ImageUrl"));
        b.setTargetUrl(rs.getString("TargetUrl"));
        b.setPositionCode(rs.getString("PositionCode"));
        b.setDisplayOrder(rs.getInt("DisplayOrder"));
        b.setStatus(rs.getString("Status"));

        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) b.setCreatedAt(ts.toLocalDateTime());

        return b;
    }

    @Override
    public List<Banner> findAll() {
        List<Banner> list = new ArrayList<>();
        String sql = "SELECT * FROM Banners ORDER BY DisplayOrder ASC, BannerID DESC";

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
    public Banner findById(int id) {
        String sql = "SELECT * FROM Banners WHERE BannerID = ?";

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

    @Override
    public List<Banner> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return findAll();
        List<Banner> list = new ArrayList<>();
        String sql = """
            SELECT * FROM Banners
            WHERE BannerName LIKE ? OR Title LIKE ? OR PositionCode LIKE ?
            ORDER BY DisplayOrder ASC, BannerID DESC
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
    public boolean insert(Banner banner) {
        String sql = """
            INSERT INTO Banners (BannerName, Title, Subtitle, ImageUrl, TargetUrl, PositionCode, DisplayOrder, Status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, banner.getBannerName());
            ps.setString(2, banner.getTitle());
            ps.setString(3, banner.getSubtitle());
            ps.setString(4, banner.getImageUrl());
            ps.setString(5, banner.getTargetUrl());
            ps.setString(6, banner.getPositionCode() != null ? banner.getPositionCode() : "HOME_HERO");
            ps.setInt(7, banner.getDisplayOrder());
            ps.setString(8, banner.getStatus() != null ? banner.getStatus() : "ACTIVE");

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Banner banner) {
        String sql = """
            UPDATE Banners
            SET BannerName = ?, Title = ?, Subtitle = ?, ImageUrl = ?, TargetUrl = ?, PositionCode = ?, DisplayOrder = ?, Status = ?
            WHERE BannerID = ?
            """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, banner.getBannerName());
            ps.setString(2, banner.getTitle());
            ps.setString(3, banner.getSubtitle());
            ps.setString(4, banner.getImageUrl());
            ps.setString(5, banner.getTargetUrl());
            ps.setString(6, banner.getPositionCode() != null ? banner.getPositionCode() : "HOME_HERO");
            ps.setInt(7, banner.getDisplayOrder());
            ps.setString(8, banner.getStatus() != null ? banner.getStatus() : "ACTIVE");
            ps.setInt(9, banner.getBannerId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Banners WHERE BannerID = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
