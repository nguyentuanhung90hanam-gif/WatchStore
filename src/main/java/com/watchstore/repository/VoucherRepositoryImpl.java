package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Voucher;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class VoucherRepositoryImpl implements VoucherRepository {

    private Voucher mapResultSetToVoucher(ResultSet rs) throws SQLException {
        Voucher v = new Voucher();
        v.setVoucherId(rs.getInt("VoucherID"));
        v.setVoucherCode(rs.getString("VoucherCode"));
        v.setVoucherName(rs.getString("VoucherName"));
        v.setDescription(rs.getString("Description"));

        v.setDiscountType(rs.getString("DiscountType"));
        v.setDiscountValue(rs.getBigDecimal("DiscountValue"));
        v.setMaximumDiscount(rs.getBigDecimal("MaximumDiscount"));
        v.setMinimumOrderValue(rs.getBigDecimal("MinimumOrderValue"));

        if (rs.getObject("UsageLimit") != null) {
            v.setUsageLimit(rs.getInt("UsageLimit"));
        } else {
            v.setUsageLimit(null);
        }
        v.setUsageLimitPerUser(rs.getInt("UsageLimitPerUser"));
        v.setUsedCount(rs.getInt("UsedCount"));

        Timestamp start = rs.getTimestamp("StartAt");
        if (start != null) {
            v.setStartAt(start.toLocalDateTime());
        }

        Timestamp end = rs.getTimestamp("EndAt");
        if (end != null) {
            v.setEndAt(end.toLocalDateTime());
        }

        v.setIsPublic(rs.getBoolean("IsPublic"));
        v.setStatus(rs.getString("Status"));
        
        if (rs.getObject("CreatedBy") != null) {
            v.setCreatedBy(rs.getInt("CreatedBy"));
        } else {
            v.setCreatedBy(null);
        }

        Timestamp created = rs.getTimestamp("CreatedAt");
        if (created != null) {
            v.setCreatedAt(created.toLocalDateTime());
        }

        return v;
    }

    @Override
    public List<Voucher> findAll() {
        List<Voucher> list = new ArrayList<>();
        String sql = """
            SELECT *
            FROM Vouchers
            ORDER BY VoucherID DESC
            """;

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                list.add(mapResultSetToVoucher(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public Voucher findById(Integer id) {
        if (id == null) return null;
        String sql = """
            SELECT *
            FROM Vouchers
            WHERE VoucherID = ?
            """;

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToVoucher(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Voucher> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        List<Voucher> list = new ArrayList<>();
        String sql = """
            SELECT *
            FROM Vouchers
            WHERE VoucherCode LIKE ?
               OR VoucherName LIKE ?
            ORDER BY VoucherID DESC
            """;

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            String value = "%" + keyword.trim() + "%";
            ps.setString(1, value);
            ps.setString(2, value);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToVoucher(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public boolean existsByCode(String code, Integer excludeId) {
        if (code == null || code.isBlank()) return false;
        String sql;
        if (excludeId != null && excludeId > 0) {
            sql = "SELECT COUNT(*) FROM Vouchers WHERE LOWER(VoucherCode) = LOWER(?) AND VoucherID <> ?";
        } else {
            sql = "SELECT COUNT(*) FROM Vouchers WHERE LOWER(VoucherCode) = LOWER(?)";
        }

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
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
    public boolean save(Voucher voucher) {
        String sql = """
            INSERT INTO Vouchers
            (
                VoucherCode,
                VoucherName,
                Description,
                DiscountType,
                DiscountValue,
                MaximumDiscount,
                MinimumOrderValue,
                UsageLimit,
                UsageLimitPerUser,
                UsedCount,
                StartAt,
                EndAt,
                IsPublic,
                Status,
                CreatedBy
            )
            VALUES
            (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, voucher.getVoucherCode());
            ps.setString(2, voucher.getVoucherName());
            if (voucher.getDescription() != null && !voucher.getDescription().isBlank()) {
                ps.setString(3, voucher.getDescription().trim());
            } else {
                ps.setNull(3, Types.NVARCHAR);
            }
            ps.setString(4, voucher.getDiscountType());
            ps.setBigDecimal(5, voucher.getDiscountValue());

            if (voucher.getMaximumDiscount() != null) {
                ps.setBigDecimal(6, voucher.getMaximumDiscount());
            } else {
                ps.setNull(6, Types.DECIMAL);
            }

            ps.setBigDecimal(7, voucher.getMinimumOrderValue() != null ? voucher.getMinimumOrderValue() : BigDecimal.ZERO);

            if (voucher.getUsageLimit() != null) {
                ps.setInt(8, voucher.getUsageLimit());
            } else {
                ps.setNull(8, Types.INTEGER);
            }

            ps.setInt(9, voucher.getUsageLimitPerUser() != null ? voucher.getUsageLimitPerUser() : 1);
            ps.setInt(10, voucher.getUsedCount() != null ? voucher.getUsedCount() : 0);

            ps.setTimestamp(11, voucher.getStartAt() == null ? null : Timestamp.valueOf(voucher.getStartAt()));
            ps.setTimestamp(12, voucher.getEndAt() == null ? null : Timestamp.valueOf(voucher.getEndAt()));
            ps.setBoolean(13, voucher.getIsPublic() != null ? voucher.getIsPublic() : true);
            ps.setString(14, voucher.getStatus() != null ? voucher.getStatus() : "ACTIVE");

            if (voucher.getCreatedBy() != null) {
                ps.setInt(15, voucher.getCreatedBy());
            } else {
                ps.setNull(15, Types.INTEGER);
            }

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean update(Voucher voucher) {
        String sql = """
            UPDATE Vouchers
            SET
                VoucherCode = ?,
                VoucherName = ?,
                Description = ?,
                DiscountType = ?,
                DiscountValue = ?,
                MaximumDiscount = ?,
                MinimumOrderValue = ?,
                UsageLimit = ?,
                UsageLimitPerUser = ?,
                UsedCount = ?,
                StartAt = ?,
                EndAt = ?,
                IsPublic = ?,
                Status = ?,
                CreatedBy = ?
            WHERE VoucherID = ?
            """;

        try (
                Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, voucher.getVoucherCode());
            ps.setString(2, voucher.getVoucherName());
            if (voucher.getDescription() != null && !voucher.getDescription().isBlank()) {
                ps.setString(3, voucher.getDescription().trim());
            } else {
                ps.setNull(3, Types.NVARCHAR);
            }
            ps.setString(4, voucher.getDiscountType());
            ps.setBigDecimal(5, voucher.getDiscountValue());

            if (voucher.getMaximumDiscount() != null) {
                ps.setBigDecimal(6, voucher.getMaximumDiscount());
            } else {
                ps.setNull(6, Types.DECIMAL);
            }

            ps.setBigDecimal(7, voucher.getMinimumOrderValue() != null ? voucher.getMinimumOrderValue() : BigDecimal.ZERO);

            if (voucher.getUsageLimit() != null) {
                ps.setInt(8, voucher.getUsageLimit());
            } else {
                ps.setNull(8, Types.INTEGER);
            }

            ps.setInt(9, voucher.getUsageLimitPerUser() != null ? voucher.getUsageLimitPerUser() : 1);
            ps.setInt(10, voucher.getUsedCount() != null ? voucher.getUsedCount() : 0);

            ps.setTimestamp(11, voucher.getStartAt() == null ? null : Timestamp.valueOf(voucher.getStartAt()));
            ps.setTimestamp(12, voucher.getEndAt() == null ? null : Timestamp.valueOf(voucher.getEndAt()));
            ps.setBoolean(13, voucher.getIsPublic() != null ? voucher.getIsPublic() : true);
            ps.setString(14, voucher.getStatus() != null ? voucher.getStatus() : "ACTIVE");

            if (voucher.getCreatedBy() != null) {
                ps.setInt(15, voucher.getCreatedBy());
            } else {
                ps.setNull(15, Types.INTEGER);
            }

            ps.setInt(16, voucher.getVoucherId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean delete(Integer id) {
        if (id == null) return false;
        String sql = """
            DELETE FROM Vouchers
            WHERE VoucherID = ?
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

    @Override
    public boolean isVoucherInUse(Integer id) {
        if (id == null || id <= 0) return false;
        String sql = """
                SELECT (
                    (SELECT COUNT(*) FROM Orders WHERE VoucherID = ?) +
                    (SELECT COUNT(*) FROM VoucherUsages WHERE VoucherID = ?) +
                    (SELECT COUNT(*) FROM VoucherUsers WHERE VoucherID = ?) +
                    (SELECT COUNT(*) FROM VoucherProducts WHERE VoucherID = ?) +
                    (SELECT COUNT(*) FROM VoucherCategories WHERE VoucherID = ?)
                ) AS TotalRefs
                """;
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, id);
            ps.setInt(3, id);
            ps.setInt(4, id);
            ps.setInt(5, id);
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
