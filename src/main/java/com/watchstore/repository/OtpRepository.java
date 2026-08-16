package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.OtpRecord;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OtpRepository {
    private final Map<String, OtpRecord> memoryStore = new ConcurrentHashMap<>();

    public String hashOtp(String otp) {
        if (otp == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(otp.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            return otp;
        }
    }

    private String key(String email, String purpose) {
        return (email == null ? "" : email.trim().toLowerCase()) + ":" + (purpose == null ? "" : purpose.trim().toUpperCase());
    }

    public boolean save(OtpRecord record) {
        String storeKey = key(record.getEmail(), record.getPurpose());
        memoryStore.put(storeKey, record);

        String sql = """
            INSERT INTO dbo.OtpVerifications (Email, OtpHash, Purpose, ExpiresAt, Attempts, Verified, LastSentAt, ResendCount)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, record.getEmail());
            ps.setString(2, record.getOtpHash());
            ps.setString(3, record.getPurpose());
            ps.setTimestamp(4, Timestamp.valueOf(record.getExpiresAt()));
            ps.setInt(5, record.getAttempts());
            ps.setBoolean(6, record.isVerified());
            ps.setTimestamp(7, Timestamp.valueOf(record.getLastSentAt()));
            ps.setInt(8, record.getResendCount());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    record.setId(rs.getLong(1));
                }
            }
            return true;
        } catch (Exception ex) {
            // DB table might not exist yet, memoryStore is active
            return true;
        }
    }

    public OtpRecord findLatest(String email, String purpose) {
        String storeKey = key(email, purpose);
        String sql = """
            SELECT TOP 1 OtpID, Email, OtpHash, Purpose, ExpiresAt, Attempts, Verified, LastSentAt, ResendCount
            FROM dbo.OtpVerifications
            WHERE LOWER(Email) = LOWER(?) AND UPPER(Purpose) = UPPER(?) AND Verified = 0
            ORDER BY OtpID DESC
            """;
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, purpose);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OtpRecord r = new OtpRecord();
                    r.setId(rs.getLong("OtpID"));
                    r.setEmail(rs.getString("Email"));
                    r.setOtpHash(rs.getString("OtpHash"));
                    r.setPurpose(rs.getString("Purpose"));
                    Timestamp exp = rs.getTimestamp("ExpiresAt");
                    if (exp != null) r.setExpiresAt(exp.toLocalDateTime());
                    r.setAttempts(rs.getInt("Attempts"));
                    r.setVerified(rs.getBoolean("Verified"));
                    Timestamp last = rs.getTimestamp("LastSentAt");
                    if (last != null) r.setLastSentAt(last.toLocalDateTime());
                    r.setResendCount(rs.getInt("ResendCount"));
                    return r;
                }
            }
        } catch (Exception ignored) {
        }
        return memoryStore.get(storeKey);
    }

    public void update(OtpRecord record) {
        String storeKey = key(record.getEmail(), record.getPurpose());
        memoryStore.put(storeKey, record);

        String sql = "UPDATE dbo.OtpVerifications SET Attempts = ?, Verified = ?, ResendCount = ?, LastSentAt = ? WHERE OtpID = ?";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, record.getAttempts());
            ps.setBoolean(2, record.isVerified());
            ps.setInt(3, record.getResendCount());
            ps.setTimestamp(4, Timestamp.valueOf(record.getLastSentAt()));
            ps.setLong(5, record.getId());
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    public void invalidate(String email, String purpose) {
        String storeKey = key(email, purpose);
        memoryStore.remove(storeKey);

        String sql = "UPDATE dbo.OtpVerifications SET Verified = 1 WHERE LOWER(Email) = LOWER(?) AND UPPER(Purpose) = UPPER(?)";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, purpose);
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }
}
