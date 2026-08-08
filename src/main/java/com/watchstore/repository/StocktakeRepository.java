package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.Stocktake;
import com.watchstore.model.StocktakeItem;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StocktakeRepository {

    public List<Stocktake> findAll() {
        List<Stocktake> list = new ArrayList<>();
        String sql = "SELECT st.*, w.WarehouseName, u.FullName as CreatedByName " +
                "FROM dbo.Stocktakes st " +
                "INNER JOIN dbo.Warehouses w ON st.WarehouseID = w.WarehouseID " +
                "LEFT JOIN dbo.Users u ON st.CreatedBy = u.UserID " +
                "ORDER BY st.StocktakeDate DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapHeader(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Stocktake findById(long id) {
        Stocktake st = null;

        String sqlHeader = "SELECT st.*, w.WarehouseName, u.FullName as CreatedByName " +
                "FROM dbo.Stocktakes st " +
                "INNER JOIN dbo.Warehouses w ON st.WarehouseID = w.WarehouseID " +
                "LEFT JOIN dbo.Users u ON st.CreatedBy = u.UserID " +
                "WHERE st.StocktakeID = ?";

        String sqlItems = "SELECT sti.*, pv.SKU, " +
                "(SELECT STRING_AGG(pa.AttributeName + ': ' + pav.Value, ', ') " +
                " FROM dbo.VariantAttributeValues vav " +
                " INNER JOIN dbo.ProductAttributeValues pav ON vav.ValueID = pav.ValueID " +
                " INNER JOIN dbo.ProductAttributes pa ON pav.AttributeID = pa.AttributeID " +
                " WHERE vav.VariantID = pv.VariantID) as VariantName " +
                "FROM dbo.StocktakeItems sti " +
                "INNER JOIN dbo.ProductVariants pv ON sti.VariantID = pv.VariantID " +
                "WHERE sti.StocktakeID = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement psHeader = conn.prepareStatement(sqlHeader);
             PreparedStatement psItems = conn.prepareStatement(sqlItems)) {

            psHeader.setLong(1, id);

            try (ResultSet rs = psHeader.executeQuery()) {
                if (rs.next()) {
                    st = mapHeader(rs);
                }
            }

            if (st != null) {
                List<StocktakeItem> items = new ArrayList<>();

                psItems.setLong(1, id);

                try (ResultSet rs = psItems.executeQuery()) {
                    while (rs.next()) {
                        StocktakeItem item = new StocktakeItem();

                        item.setStocktakeItemId(rs.getLong("StocktakeItemID"));
                        item.setStocktakeId(rs.getLong("StocktakeID"));
                        item.setVariantId(rs.getInt("VariantID"));
                        item.setSystemQuantity(rs.getInt("SystemQuantity"));
                        item.setActualQuantity(rs.getInt("ActualQuantity"));
                        item.setDifferenceQuantity(rs.getInt("DifferenceQuantity"));
                        item.setNote(rs.getString("Note"));
                        item.setSku(rs.getString("SKU"));
                        item.setVariantName(rs.getString("VariantName"));

                        items.add(item);
                    }
                }

                st.setItems(items);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return st;
    }

    public long createDraft(Stocktake stocktake) throws Exception {
        if (stocktake.getItems() == null || stocktake.getItems().isEmpty()) {
            throw new Exception("Phiếu kiểm kê phải có ít nhất một sản phẩm.");
        }

        validateItems(stocktake.getItems());

        String insertStocktakeSql =
                "INSERT INTO dbo.Stocktakes " +
                        "(StocktakeCode, WarehouseID, Status, Note, CreatedBy) " +
                        "VALUES (?, ?, 'DRAFT', ?, ?)";

        String insertItemSql =
                "INSERT INTO dbo.StocktakeItems " +
                        "(StocktakeID, VariantID, SystemQuantity, ActualQuantity, Note) " +
                        "VALUES (?, ?, ?, ?, ?)";

        String getSysQtySql =
                "SELECT QuantityOnHand " +
                        "FROM dbo.InventoryBalances " +
                        "WHERE WarehouseID = ? AND VariantID = ?";

        Connection conn = null;

        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            long stocktakeId = 0;

            try (PreparedStatement psHeader =
                         conn.prepareStatement(insertStocktakeSql, Statement.RETURN_GENERATED_KEYS)) {

                psHeader.setString(1, stocktake.getStocktakeCode());
                psHeader.setInt(2, stocktake.getWarehouseId());
                psHeader.setString(3, stocktake.getNote());
                psHeader.setInt(4, stocktake.getCreatedBy());

                psHeader.executeUpdate();

                try (ResultSet rs = psHeader.getGeneratedKeys()) {
                    if (rs.next()) {
                        stocktakeId = rs.getLong(1);
                    } else {
                        throw new SQLException("Failed to retrieve StocktakeID.");
                    }
                }
            }

            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql);
                 PreparedStatement psSysQty = conn.prepareStatement(getSysQtySql)) {

                for (StocktakeItem item : stocktake.getItems()) {
                    int sysQty = 0;

                    psSysQty.setInt(1, stocktake.getWarehouseId());
                    psSysQty.setInt(2, item.getVariantId());

                    try (ResultSet rs = psSysQty.executeQuery()) {
                        if (rs.next()) {
                            sysQty = rs.getInt("QuantityOnHand");
                        }
                    }

                    psItem.setLong(1, stocktakeId);
                    psItem.setInt(2, item.getVariantId());
                    psItem.setInt(3, sysQty);
                    psItem.setInt(4, item.getActualQuantity());
                    psItem.setString(5, item.getNote());

                    psItem.executeUpdate();
                }
            }

            conn.commit();

            return stocktakeId;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Ignore rollback exception.
                }
            }

            throw e;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    // Ignore close exception.
                }
            }
        }
    }

    public void addItem(long stocktakeId, StocktakeItem item) throws Exception {
        assertStatus(stocktakeId, "DRAFT");

        if (item.getActualQuantity() < 0) {
            throw new Exception("Số lượng thực tế không được âm.");
        }

        if (isDuplicateVariant(stocktakeId, item.getVariantId(), 0)) {
            throw new Exception("Biến thể này đã tồn tại trong phiếu.");
        }

        String getSysQtySql =
                "SELECT ib.QuantityOnHand " +
                        "FROM dbo.InventoryBalances ib " +
                        "INNER JOIN dbo.Stocktakes st ON ib.WarehouseID = st.WarehouseID " +
                        "WHERE st.StocktakeID = ? AND ib.VariantID = ?";

        String insertSql =
                "INSERT INTO dbo.StocktakeItems " +
                        "(StocktakeID, VariantID, SystemQuantity, ActualQuantity) " +
                        "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement psSysQty = conn.prepareStatement(getSysQtySql);
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            psSysQty.setLong(1, stocktakeId);
            psSysQty.setInt(2, item.getVariantId());

            int sysQty = 0;

            try (ResultSet rs = psSysQty.executeQuery()) {
                if (rs.next()) {
                    sysQty = rs.getInt(1);
                }
            }

            ps.setLong(1, stocktakeId);
            ps.setInt(2, item.getVariantId());
            ps.setInt(3, sysQty);
            ps.setInt(4, item.getActualQuantity());

            ps.executeUpdate();
        }
    }

    public void updateItem(long itemId, int actualQuantity) throws Exception {
        if (actualQuantity < 0) {
            throw new Exception("Số lượng thực tế không được âm.");
        }

        long stocktakeId = getStocktakeIdByItem(itemId);
        assertStatus(stocktakeId, "DRAFT");

        String sql =
                "UPDATE dbo.StocktakeItems " +
                        "SET ActualQuantity = ? " +
                        "WHERE StocktakeItemID = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, actualQuantity);
            ps.setLong(2, itemId);

            ps.executeUpdate();
        }
    }

    public void deleteItem(long itemId) throws Exception {
        long stocktakeId = getStocktakeIdByItem(itemId);
        assertStatus(stocktakeId, "DRAFT");

        String sql =
                "DELETE FROM dbo.StocktakeItems " +
                        "WHERE StocktakeItemID = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, itemId);

            ps.executeUpdate();
        }
    }

    public void submitForApproval(long stocktakeId) throws Exception {
        assertStatus(stocktakeId, "DRAFT");
        assertHasItems(stocktakeId);
        updateStatus(stocktakeId, "COUNTING");
    }

    public void approve(long stocktakeId, int approvedBy) throws Exception {
        assertStatus(stocktakeId, "COUNTING");

        String sqlGetInfo =
                "SELECT Note, WarehouseID, CreatedBy " +
                        "FROM dbo.Stocktakes " +
                        "WHERE StocktakeID = ?";

        String sqlItems =
                "SELECT VariantID, ActualQuantity " +
                        "FROM dbo.StocktakeItems " +
                        "WHERE StocktakeID = ?";

        String sqlSysQty =
                "SELECT QuantityOnHand " +
                        "FROM dbo.InventoryBalances WITH (UPDLOCK) " +
                        "WHERE WarehouseID = ? AND VariantID = ?";

        String updateStatusSql =
                "UPDATE dbo.Stocktakes " +
                        "SET Status = 'COMPLETED', ApprovedBy = ?, ApprovedAt = SYSDATETIME() " +
                        "WHERE StocktakeID = ?";

        String updateItemSysQtySql =
                "UPDATE dbo.StocktakeItems " +
                        "SET SystemQuantity = ? " +
                        "WHERE StocktakeID = ? AND VariantID = ?";

        String callSp =
                "{CALL dbo.sp_RecordInventoryTransaction(?, ?, ?, ?, ?, ?, ?, ?)}";

        Connection conn = null;

        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            String note = "";
            int warehouseId = 0;
            int createdBy = 0;

            try (PreparedStatement ps = conn.prepareStatement(sqlGetInfo)) {
                ps.setLong(1, stocktakeId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        note = rs.getString("Note");
                        warehouseId = rs.getInt("WarehouseID");
                        createdBy = rs.getInt("CreatedBy");
                    }
                }
            }

            try (PreparedStatement psItems = conn.prepareStatement(sqlItems);
                 PreparedStatement psSysQty = conn.prepareStatement(sqlSysQty);
                 PreparedStatement psUpdateItem = conn.prepareStatement(updateItemSysQtySql);
                 CallableStatement cs = conn.prepareCall(callSp)) {

                psItems.setLong(1, stocktakeId);

                try (ResultSet rsItems = psItems.executeQuery()) {
                    while (rsItems.next()) {
                        int variantId = rsItems.getInt("VariantID");
                        int actualQty = rsItems.getInt("ActualQuantity");

                        psSysQty.setInt(1, warehouseId);
                        psSysQty.setInt(2, variantId);

                        int sysQty = 0;

                        try (ResultSet rsSys = psSysQty.executeQuery()) {
                            if (rsSys.next()) {
                                sysQty = rsSys.getInt(1);
                            }
                        }

                        psUpdateItem.setInt(1, sysQty);
                        psUpdateItem.setLong(2, stocktakeId);
                        psUpdateItem.setInt(3, variantId);
                        psUpdateItem.executeUpdate();

                        int diff = actualQty - sysQty;

                        if (diff != 0) {
                            String transactionType =
                                    diff > 0 ? "ADJUST_IN" : "ADJUST_OUT";

                            cs.setInt(1, warehouseId);
                            cs.setInt(2, variantId);
                            cs.setString(3, transactionType);
                            cs.setInt(4, diff);
                            cs.setString(5, "Stocktakes");
                            cs.setLong(6, stocktakeId);
                            cs.setString(7, note);
                            cs.setInt(8, createdBy);

                            cs.execute();
                        }
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(updateStatusSql)) {
                ps.setInt(1, approvedBy);
                ps.setLong(2, stocktakeId);

                ps.executeUpdate();
            }

            conn.commit();

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Ignore rollback exception.
                }
            }

            throw e;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    // Ignore close exception.
                }
            }
        }
    }

    public void cancel(long stocktakeId) throws Exception {
        Stocktake st = findById(stocktakeId);

        if (st == null) {
            throw new Exception("Phiếu không tồn tại.");
        }

        if ("COMPLETED".equals(st.getStatus())) {
            throw new Exception("Không thể hủy phiếu đã hoàn thành.");
        }

        updateStatus(stocktakeId, "CANCELLED");
    }

    private Stocktake mapHeader(ResultSet rs) throws SQLException {
        Stocktake st = new Stocktake();

        st.setStocktakeId(rs.getLong("StocktakeID"));
        st.setStocktakeCode(rs.getString("StocktakeCode"));
        st.setWarehouseId(rs.getInt("WarehouseID"));
        st.setWarehouseName(rs.getString("WarehouseName"));

        if (rs.getTimestamp("StocktakeDate") != null) {
            st.setStocktakeDate(
                    rs.getTimestamp("StocktakeDate").toLocalDateTime()
            );
        }

        st.setStatus(rs.getString("Status"));
        st.setNote(rs.getString("Note"));
        st.setCreatedBy(rs.getInt("CreatedBy"));
        st.setCreatedByName(rs.getString("CreatedByName"));
        st.setApprovedBy(
                rs.getObject("ApprovedBy") != null
                        ? rs.getInt("ApprovedBy")
                        : null
        );

        if (rs.getTimestamp("ApprovedAt") != null) {
            st.setApprovedAt(
                    rs.getTimestamp("ApprovedAt").toLocalDateTime()
            );
        }

        return st;
    }

    private void assertStatus(long stocktakeId, String expectedStatus) throws Exception {
        String sql =
                "SELECT Status " +
                        "FROM dbo.Stocktakes " +
                        "WHERE StocktakeID = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, stocktakeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("Phiếu không tồn tại.");
                }

                String status = rs.getString("Status");

                if (!expectedStatus.equals(status)) {
                    throw new Exception(
                            "Thao tác không hợp lệ. Trạng thái hiện tại: " + status
                    );
                }
            }
        }
    }

    private void assertHasItems(long stocktakeId) throws Exception {
        String sql =
                "SELECT COUNT(1) " +
                        "FROM dbo.StocktakeItems " +
                        "WHERE StocktakeID = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, stocktakeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new Exception("Phiếu phải có ít nhất một sản phẩm.");
                }
            }
        }
    }

    private void updateStatus(long stocktakeId, String newStatus) throws Exception {
        String sql =
                "UPDATE dbo.Stocktakes " +
                        "SET Status = ? " +
                        "WHERE StocktakeID = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setLong(2, stocktakeId);

            ps.executeUpdate();
        }
    }

    private long getStocktakeIdByItem(long itemId) throws Exception {
        String sql =
                "SELECT StocktakeID " +
                        "FROM dbo.StocktakeItems " +
                        "WHERE StocktakeItemID = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, itemId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("Không tìm thấy item.");
                }

                return rs.getLong(1);
            }
        }
    }

    private boolean isDuplicateVariant(
            long stocktakeId,
            int variantId,
            long excludeItemId) throws Exception {

        String sql =
                "SELECT COUNT(1) " +
                        "FROM dbo.StocktakeItems " +
                        "WHERE StocktakeID = ? " +
                        "AND VariantID = ? " +
                        "AND StocktakeItemID <> ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, stocktakeId);
            ps.setInt(2, variantId);
            ps.setLong(3, excludeItemId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void validateItems(List<StocktakeItem> items) throws Exception {
        Set<Integer> seen = new HashSet<>();

        for (StocktakeItem item : items) {
            if (item.getActualQuantity() < 0) {
                throw new Exception("Số lượng thực tế không được âm.");
            }

            if (!seen.add(item.getVariantId())) {
                throw new Exception(
                        "Không được chọn trùng biến thể trong cùng một phiếu."
                );
            }
        }
    }
}

