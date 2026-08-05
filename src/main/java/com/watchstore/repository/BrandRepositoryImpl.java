package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Brand;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BrandRepositoryImpl implements BrandRepository {

    @Override
    public List<Brand> findAll() {

        List<Brand> list = new ArrayList<>();

        String sql = """
                SELECT *
                FROM Brands
                ORDER BY BrandID DESC
                """;

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                Brand b = new Brand();

                b.setBrandID(rs.getInt("BrandID"));
                b.setBrandCode(rs.getString("BrandCode"));
                b.setBrandName(rs.getString("BrandName"));
                b.setSlug(rs.getString("Slug"));
                b.setOriginCountry(rs.getString("OriginCountry"));
                b.setLogoUrl(rs.getString("LogoUrl"));
                b.setDescription(rs.getString("Description"));
                b.setStatus(rs.getString("Status"));

                Timestamp time = rs.getTimestamp("CreatedAt");
                if (time != null) {
                    b.setCreatedAt(time.toLocalDateTime());
                }

                list.add(b);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    @Override
    public Brand findById(int id) {

        String sql = """
            SELECT *
            FROM Brands
            WHERE BrandID = ?
            """;

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Brand b = new Brand();

                b.setBrandID(rs.getInt("BrandID"));
                b.setBrandCode(rs.getString("BrandCode"));
                b.setBrandName(rs.getString("BrandName"));
                b.setSlug(rs.getString("Slug"));
                b.setOriginCountry(rs.getString("OriginCountry"));
                b.setLogoUrl(rs.getString("LogoUrl"));
                b.setDescription(rs.getString("Description"));
                b.setStatus(rs.getString("Status"));

                Timestamp time = rs.getTimestamp("CreatedAt");

                if (time != null) {
                    b.setCreatedAt(time.toLocalDateTime());
                }

                return b;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Brand> search(String keyword) {

        List<Brand> list = new ArrayList<>();

        String sql = """
            SELECT *
            FROM Brands
            WHERE BrandCode LIKE ?
               OR BrandName LIKE ?
            ORDER BY BrandID DESC
            """;

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            String value = "%" + keyword + "%";

            ps.setString(1, value);
            ps.setString(2, value);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Brand b = new Brand();

                b.setBrandID(rs.getInt("BrandID"));
                b.setBrandCode(rs.getString("BrandCode"));
                b.setBrandName(rs.getString("BrandName"));
                b.setSlug(rs.getString("Slug"));
                b.setOriginCountry(rs.getString("OriginCountry"));
                b.setLogoUrl(rs.getString("LogoUrl"));
                b.setDescription(rs.getString("Description"));
                b.setStatus(rs.getString("Status"));

                Timestamp time = rs.getTimestamp("CreatedAt");

                if (time != null) {
                    b.setCreatedAt(time.toLocalDateTime());
                }

                list.add(b);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public boolean insert(Brand brand) {

        String sql = """
            INSERT INTO Brands
            (
                BrandCode,
                BrandName,
                Slug,
                OriginCountry,
                LogoUrl,
                Description,
                Status
            )
            VALUES
            (?, ?, ?, ?, ?, ?, ?)
            """;

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, brand.getBrandCode());
            ps.setString(2, brand.getBrandName());
            ps.setString(3, brand.getSlug());
            ps.setString(4, brand.getOriginCountry());
            ps.setString(5, brand.getLogoUrl());
            ps.setString(6, brand.getDescription());
            ps.setString(7, brand.getStatus());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean update(Brand brand) {

        String sql = """
            UPDATE Brands
            SET
                BrandCode=?,
                BrandName=?,
                Slug=?,
                OriginCountry=?,
                LogoUrl=?,
                Description=?,
                Status=?
            WHERE BrandID=?
            """;

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, brand.getBrandCode());
            ps.setString(2, brand.getBrandName());
            ps.setString(3, brand.getSlug());
            ps.setString(4, brand.getOriginCountry());
            ps.setString(5, brand.getLogoUrl());
            ps.setString(6, brand.getDescription());
            ps.setString(7, brand.getStatus());
            ps.setInt(8, brand.getBrandID());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    @Override
    public boolean delete(int id) {

        String sql = """
            DELETE FROM Brands
            WHERE BrandID=?
            """;

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}