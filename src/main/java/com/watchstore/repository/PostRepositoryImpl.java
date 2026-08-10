package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Post;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostRepositoryImpl implements PostRepository {

    private Connection getConnection() throws SQLException {
        return DBContext.getConnection();
    }

    private Post mapRow(ResultSet rs) throws SQLException {
        Post p = new Post();
        p.setPostId(rs.getInt("PostID"));
        p.setPostType(rs.getString("PostType"));
        p.setTitle(rs.getString("Title"));
        p.setSlug(rs.getString("Slug"));
        p.setSummary(rs.getString("Summary"));
        p.setContent(rs.getString("Content"));
        p.setThumbnailUrl(rs.getString("ThumbnailUrl"));
        p.setStatus(rs.getString("Status"));
        p.setAuthorId(rs.getInt("AuthorID"));

        try {
            p.setAuthorName(rs.getString("AuthorName"));
        } catch (SQLException ignored) {}

        Timestamp ts = rs.getTimestamp("PublishedAt");
        if (ts != null) p.setPublishedAt(ts.toLocalDateTime());
        ts = rs.getTimestamp("CreatedAt");
        if (ts != null) p.setCreatedAt(ts.toLocalDateTime());
        ts = rs.getTimestamp("UpdatedAt");
        if (ts != null) p.setUpdatedAt(ts.toLocalDateTime());

        return p;
    }

    private String getSelectSql() {
        return """
            SELECT p.*, u.FullName AS AuthorName
            FROM Posts p
            LEFT JOIN Users u ON p.AuthorID = u.UserID
            """;
    }

    @Override
    public List<Post> findAll() {
        List<Post> list = new ArrayList<>();
        String sql = getSelectSql() + " ORDER BY p.PostID DESC";

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
    public Post findById(int id) {
        String sql = getSelectSql() + " WHERE p.PostID = ?";

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
    public List<Post> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return findAll();
        List<Post> list = new ArrayList<>();
        String sql = getSelectSql() + """
            WHERE p.Title LIKE ? OR p.PostType LIKE ? OR p.Summary LIKE ?
            ORDER BY p.PostID DESC
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
    public boolean insert(Post post) {
        String sql = """
            INSERT INTO Posts (PostType, Title, Slug, Summary, Content, ThumbnailUrl, Status, AuthorID, PublishedAt)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME())
            """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, post.getPostType() != null ? post.getPostType() : "NEWS");
            ps.setString(2, post.getTitle());
            ps.setString(3, post.getSlug());
            ps.setString(4, post.getSummary());
            ps.setString(5, post.getContent());
            ps.setString(6, post.getThumbnailUrl());
            ps.setString(7, post.getStatus() != null ? post.getStatus() : "DRAFT");
            ps.setInt(8, post.getAuthorId() > 0 ? post.getAuthorId() : 1);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Post post) {
        String sql = """
            UPDATE Posts
            SET PostType = ?, Title = ?, Slug = ?, Summary = ?, Content = ?, ThumbnailUrl = ?, Status = ?, UpdatedAt = SYSDATETIME()
            WHERE PostID = ?
            """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, post.getPostType() != null ? post.getPostType() : "NEWS");
            ps.setString(2, post.getTitle());
            ps.setString(3, post.getSlug());
            ps.setString(4, post.getSummary());
            ps.setString(5, post.getContent());
            ps.setString(6, post.getThumbnailUrl());
            ps.setString(7, post.getStatus() != null ? post.getStatus() : "DRAFT");
            ps.setInt(8, post.getPostId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM Posts WHERE PostID = ?";

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
    public boolean existsBySlug(String slug, Integer excludeId) {
        if (slug == null || slug.isBlank()) return false;
        String sql = (excludeId != null && excludeId > 0)
                ? "SELECT COUNT(*) FROM Posts WHERE LOWER(Slug) = LOWER(?) AND PostID <> ?"
                : "SELECT COUNT(*) FROM Posts WHERE LOWER(Slug) = LOWER(?)";

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
}
