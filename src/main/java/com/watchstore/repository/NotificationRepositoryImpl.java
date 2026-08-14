package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepositoryImpl implements NotificationRepository {

    private Connection getConnection() throws SQLException {
        return DBContext.getConnection();
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getLong("NotificationID"));
        n.setNotificationType(rs.getString("NotificationType"));
        n.setTitle(rs.getString("Title"));
        n.setMessage(rs.getString("Message"));
        n.setTargetUrl(rs.getString("TargetUrl"));

        if (rs.getObject("CreatedBy") != null) {
            n.setCreatedBy(rs.getInt("CreatedBy"));
        }
        try {
            n.setCreatedByName(rs.getString("CreatedByName"));
        } catch (SQLException ignored) {}

        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) n.setCreatedAt(ts.toLocalDateTime());
        ts = rs.getTimestamp("ExpiresAt");
        if (ts != null) n.setExpiresAt(ts.toLocalDateTime());

        return n;
    }

    private String getSelectSql() {
        return """
            SELECT n.*, u.FullName AS CreatedByName
            FROM Notifications n
            LEFT JOIN Users u ON n.CreatedBy = u.UserID
            """;
    }

    @Override
    public List<Notification> findAll() {
        List<Notification> list = new ArrayList<>();
        String sql = getSelectSql() + " ORDER BY n.NotificationID DESC";

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
    public Notification findById(long id) {
        String sql = getSelectSql() + " WHERE n.NotificationID = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
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
    public List<Notification> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return findAll();
        List<Notification> list = new ArrayList<>();
        String sql = getSelectSql() + """
            WHERE n.Title LIKE ? OR n.Message LIKE ? OR n.NotificationType LIKE ?
            ORDER BY n.NotificationID DESC
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
    public boolean insert(Notification notification) {
        String sql = """
            INSERT INTO Notifications (NotificationType, Title, Message, TargetUrl, CreatedBy)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, notification.getNotificationType() != null ? notification.getNotificationType() : "SYSTEM");
            ps.setString(2, notification.getTitle());
            ps.setString(3, notification.getMessage());
            ps.setString(4, notification.getTargetUrl());

            if (notification.getCreatedBy() != null && notification.getCreatedBy() > 0) {
                ps.setInt(5, notification.getCreatedBy());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Notification notification) {
        String sql = """
            UPDATE Notifications
            SET NotificationType = ?, Title = ?, Message = ?, TargetUrl = ?
            WHERE NotificationID = ?
            """;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, notification.getNotificationType() != null ? notification.getNotificationType() : "SYSTEM");
            ps.setString(2, notification.getTitle());
            ps.setString(3, notification.getMessage());
            ps.setString(4, notification.getTargetUrl());
            ps.setLong(5, notification.getNotificationId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(long id) {
        String sql = "DELETE FROM Notifications WHERE NotificationID = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
