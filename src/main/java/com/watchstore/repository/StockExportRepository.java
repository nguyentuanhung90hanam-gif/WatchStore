package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.StockExport;
import com.watchstore.model.StockExportItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockExportRepository {

    // ─────────────────────────────────────────────────────────
    //  findAll
    // ─────────────────────────────────────────────────────────
    public List<StockExport> findAll() {
        List<StockExport> list = new ArrayList<>();
        String sql = "SELECT se.*, w.WarehouseName, u.FullName as CreatedByName " +
                     "FROM dbo.StockExports se " +
                     "INNER JOIN dbo.Warehouses w ON se.WarehouseID = w.WarehouseID " +
                     "LEFT JOIN dbo.Users u ON se.CreatedBy = u.UserID " +
                     "ORDER BY se.ExportDate DESC";

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

    // ─────────────────────────────────────────────────────────
    //  findById (header + items)
    // ─────────────────────────────────────────────────────────
    public StockExport findById(long id) {
        StockExport se = null;
        String sqlHeader = "SELECT se.*, w.WarehouseName, u.FullName as CreatedByName " +
                           "FROM dbo.StockExports se " +
                           "INNER JOIN dbo.Warehouses w ON se.WarehouseID = w.WarehouseID " +
                           "LEFT JOIN dbo.Users u ON se.CreatedBy = u.UserID " +
                           "WHERE se.StockExportID = ?";

        String sqlItems = "SELECT sei.*, p.ProductName, p.SKU, " +
                          "(SELECT STRING_AGG(pa.AttributeName + ': ' + pav.Value, ', ') " +
                          " FROM dbo.VariantAttributeValues vav " +
                          " INNER JOIN dbo.ProductAttributeValues pav ON vav.ValueID = pav.ValueID " +
                          " INNER JOIN dbo.ProductAttributes pa ON pav.AttributeID = pa.AttributeID " +
                          " WHERE vav.VariantID = pv.VariantID) as VariantName " +
                          "FROM dbo.StockExportItems sei " +
                          "INNER JOIN dbo.ProductVariants pv ON sei.VariantID = pv.VariantID " +
                          "INNER JOIN dbo.Products p ON pv.ProductID = p.ProductID " +
                          "WHERE sei.StockExportID = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement psHeader = conn.prepareStatement(sqlHeader);
             PreparedStatement psItems = conn.prepareStatement(sqlItems)) {

            psHeader.setLong(1, id);
            try (ResultSet rs = psHeader.executeQuery()) {
                if (rs.next()) {
                    se = mapHeader(rs);
                }
            }

            if (se != null) {
                List<StockExportItem> items = new ArrayList<>();
                psItems.setLong(1, id);
                try (ResultSet rs = psItems.executeQuery()) {
                    while (rs.next()) {
                        items.add(mapItem(rs));
                    }
                }
                se.setItems(items);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return se;
    }

    // ─────────────────────────────────────────────────────────
    //  createDraft — INSERT header + items, Status = DRAFT, no SP call
    // ─────────────────────────────────────────────────────────
    public long createDraft(StockExport export) throws Exception {
        if (export.getItems() == null || export.getItems().isEmpty()) {
            throw new Exception("Phiếu xuất phải có ít nhất một sản phẩm.");
        }
        validateItems(export.getItems());

        String insertExportSql =
            "INSERT INTO dbo.StockExports " +
            "(ExportCode, WarehouseID, OrderID, ExportType, Status, ReceiverName, Note, CreatedBy) " +
            "VALUES (?, ?, ?, ?, 'DRAFT', ?, ?, ?)";
        String insertItemSql =
            "INSERT INTO dbo.StockExportItems (StockExportID, VariantID, Quantity) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            long exportId;
            try (PreparedStatement psHeader = conn.prepareStatement(insertExportSql, Statement.RETURN_GENERATED_KEYS)) {
                psHeader.setString(1, export.getExportCode());
                psHeader.setInt(2, export.getWarehouseId());
                if (export.getOrderId() != null && export.getOrderId() > 0) {
                    psHeader.setLong(3, export.getOrderId());
                } else {
                    psHeader.setNull(3, Types.BIGINT);
                }
                psHeader.setString(4, export.getExportType());
                psHeader.setString(5, export.getReceiverName());
                psHeader.setString(6, export.getNote());
                psHeader.setInt(7, export.getCreatedBy());
                psHeader.executeUpdate();

                try (ResultSet rs = psHeader.getGeneratedKeys()) {
                    if (rs.next()) {
                        exportId = rs.getLong(1);
                    } else {
                        throw new SQLException("Không lấy được StockExportID.");
                    }
                }
            }

            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql)) {
                for (StockExportItem item : export.getItems()) {
                    psItem.setLong(1, exportId);
                    psItem.setInt(2, item.getVariantId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.executeUpdate();
                }
            }

            conn.commit();
            return exportId;
        } catch (Exception e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { /* ignored */ } }
            throw e;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { /* ignored */ } }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  addItem — thêm item vào phiếu DRAFT
    // ─────────────────────────────────────────────────────────
    public void addItem(long exportId, StockExportItem item) throws Exception {
        assertStatus(exportId, "DRAFT");
        if (item.getQuantity() <= 0) throw new Exception("Số lượng phải lớn hơn 0.");
        if (isDuplicateVariant(exportId, item.getVariantId(), 0)) {
            throw new Exception("Biến thể này đã tồn tại trong phiếu.");
        }
        String sql = "INSERT INTO dbo.StockExportItems (StockExportID, VariantID, Quantity) VALUES (?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, exportId);
            ps.setInt(2, item.getVariantId());
            ps.setInt(3, item.getQuantity());
            ps.executeUpdate();
        }
    }

    // ─────────────────────────────────────────────────────────
    //  updateItem — sửa quantity của 1 item trong DRAFT
    // ─────────────────────────────────────────────────────────
    public void updateItem(long exportItemId, int quantity) throws Exception {
        if (quantity <= 0) throw new Exception("Số lượng phải lớn hơn 0.");
        long exportId = getExportIdByItem(exportItemId);
        assertStatus(exportId, "DRAFT");

        String sql = "UPDATE dbo.StockExportItems SET Quantity = ? WHERE StockExportItemID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, exportItemId);
            ps.executeUpdate();
        }
    }

    // ─────────────────────────────────────────────────────────
    //  deleteItem — xóa item khỏi phiếu DRAFT
    // ─────────────────────────────────────────────────────────
    public void deleteItem(long exportItemId) throws Exception {
        long exportId = getExportIdByItem(exportItemId);
        assertStatus(exportId, "DRAFT");

        String sql = "DELETE FROM dbo.StockExportItems WHERE StockExportItemID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, exportItemId);
            ps.executeUpdate();
        }
    }

    // ─────────────────────────────────────────────────────────
    //  submitForApproval — DRAFT → PENDING
    // ─────────────────────────────────────────────────────────
    public void submitForApproval(long exportId) throws Exception {
        assertStatus(exportId, "DRAFT");
        assertHasItems(exportId);
        updateStatus(exportId, "PENDING");
    }

    // ─────────────────────────────────────────────────────────
    //  approve — PENDING → COMPLETED + kiểm tồn kho + cập nhật qua SP
    // ─────────────────────────────────────────────────────────
    public void approve(long exportId, int approvedBy) throws Exception {
        assertStatus(exportId, "PENDING");

        String sqlGetExport = "SELECT Note, WarehouseID, CreatedBy, ExportType FROM dbo.StockExports WHERE StockExportID = ?";
        String checkStockSql =
            "SELECT (QuantityOnHand - QuantityReserved) AS AvailableQuantity " +
            "FROM dbo.InventoryBalances WITH (UPDLOCK) WHERE WarehouseID = ? AND VariantID = ?";
        String sqlItems = "SELECT VariantID, Quantity FROM dbo.StockExportItems WHERE StockExportID = ?";
        String updateStatusSql =
            "UPDATE dbo.StockExports SET Status = 'COMPLETED', ApprovedBy = ?, ApprovedAt = SYSDATETIME() " +
            "WHERE StockExportID = ?";
        String callSp = "{CALL dbo.sp_RecordInventoryTransaction(?, ?, ?, ?, ?, ?, ?, ?)}";

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            String note = "";
            int warehouseId = 0;
            int createdBy = 0;
            String exportType = "SALE";
            try (PreparedStatement ps = conn.prepareStatement(sqlGetExport)) {
                ps.setLong(1, exportId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        note = rs.getString("Note");
                        warehouseId = rs.getInt("WarehouseID");
                        createdBy = rs.getInt("CreatedBy");
                        exportType = rs.getString("ExportType");
                    }
                }
            }

            // 1. Kiểm tồn kho trước khi approve
            List<int[]> itemList = new ArrayList<>();
            try (PreparedStatement psItems = conn.prepareStatement(sqlItems)) {
                psItems.setLong(1, exportId);
                try (ResultSet rs = psItems.executeQuery()) {
                    while (rs.next()) {
                        itemList.add(new int[]{rs.getInt("VariantID"), rs.getInt("Quantity")});
                    }
                }
            }

            try (PreparedStatement psCheck = conn.prepareStatement(checkStockSql)) {
                for (int[] entry : itemList) {
                    psCheck.setInt(1, warehouseId);
                    psCheck.setInt(2, entry[0]);
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            int available = rs.getInt("AvailableQuantity");
                            if (available < entry[1]) {
                                throw new Exception(
                                    "Không đủ tồn kho cho VariantID " + entry[0] +
                                    ". Khả dụng: " + available + ", yêu cầu: " + entry[1]
                                );
                            }
                        } else {
                            throw new Exception("VariantID " + entry[0] + " không tồn tại trong kho này.");
                        }
                    }
                }
            }

            // 2. Cập nhật status
            try (PreparedStatement ps = conn.prepareStatement(updateStatusSql)) {
                ps.setInt(1, approvedBy);
                ps.setLong(2, exportId);
                ps.executeUpdate();
            }

            // 3. Gọi SP cho từng item
            String transactionType = resolveTransactionType(exportType);
            try (CallableStatement cs = conn.prepareCall(callSp)) {
                for (int[] entry : itemList) {
                    cs.setInt(1, warehouseId);
                    cs.setInt(2, entry[0]);
                    cs.setString(3, transactionType);
                    cs.setInt(4, -entry[1]); // âm = xuất
                    cs.setString(5, "StockExports");
                    cs.setLong(6, exportId);
                    cs.setString(7, note);
                    cs.setInt(8, createdBy);
                    cs.execute();
                }
            }

            conn.commit();
        } catch (Exception e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { /* ignored */ } }
            throw e;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { /* ignored */ } }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  cancel — DRAFT|PENDING → CANCELLED
    // ─────────────────────────────────────────────────────────
    public void cancel(long exportId) throws Exception {
        StockExport se = findById(exportId);
        if (se == null) throw new Exception("Phiếu không tồn tại.");
        if ("COMPLETED".equals(se.getStatus())) {
            throw new Exception("Không thể hủy phiếu đã hoàn thành.");
        }
        updateStatus(exportId, "CANCELLED");
    }

    // ─────────────────────────────────────────────────────────
    //  Private helpers
    // ─────────────────────────────────────────────────────────
    private StockExport mapHeader(ResultSet rs) throws SQLException {
        StockExport se = new StockExport();
        se.setStockExportId(rs.getLong("StockExportID"));
        se.setExportCode(rs.getString("ExportCode"));
        se.setWarehouseId(rs.getInt("WarehouseID"));
        se.setWarehouseName(rs.getString("WarehouseName"));
        se.setOrderId(rs.getObject("OrderID") != null ? rs.getLong("OrderID") : null);
        se.setExportType(rs.getString("ExportType"));
        if (rs.getTimestamp("ExportDate") != null) {
            se.setExportDate(rs.getTimestamp("ExportDate").toLocalDateTime());
        }
        se.setStatus(rs.getString("Status"));
        se.setReceiverName(rs.getString("ReceiverName"));
        se.setNote(rs.getString("Note"));
        se.setCreatedBy(rs.getInt("CreatedBy"));
        se.setCreatedByName(rs.getString("CreatedByName"));
        se.setApprovedBy(rs.getObject("ApprovedBy") != null ? rs.getInt("ApprovedBy") : null);
        if (rs.getTimestamp("ApprovedAt") != null) {
            se.setApprovedAt(rs.getTimestamp("ApprovedAt").toLocalDateTime());
        }
        return se;
    }

    private StockExportItem mapItem(ResultSet rs) throws SQLException {
        StockExportItem item = new StockExportItem();
        item.setStockExportItemId(rs.getLong("StockExportItemID"));
        item.setStockExportId(rs.getLong("StockExportID"));
        item.setVariantId(rs.getInt("VariantID"));
        item.setQuantity(rs.getInt("Quantity"));
        item.setProductName(rs.getString("ProductName"));
        item.setSku(rs.getString("SKU"));
        item.setVariantName(rs.getString("VariantName"));
        return item;
    }

    private void assertStatus(long exportId, String expectedStatus) throws Exception {
        String sql = "SELECT Status FROM dbo.StockExports WHERE StockExportID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, exportId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Phiếu không tồn tại.");
                String status = rs.getString("Status");
                if (!expectedStatus.equals(status)) {
                    throw new Exception("Thao tác không hợp lệ. Trạng thái hiện tại: " + status);
                }
            }
        }
    }

    private void assertHasItems(long exportId) throws Exception {
        String sql = "SELECT COUNT(1) FROM dbo.StockExportItems WHERE StockExportID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, exportId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new Exception("Phiếu phải có ít nhất một sản phẩm.");
                }
            }
        }
    }

    private void updateStatus(long exportId, String newStatus) throws Exception {
        String sql = "UPDATE dbo.StockExports SET Status = ? WHERE StockExportID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, exportId);
            ps.executeUpdate();
        }
    }

    private long getExportIdByItem(long exportItemId) throws Exception {
        String sql = "SELECT StockExportID FROM dbo.StockExportItems WHERE StockExportItemID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, exportItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Không tìm thấy item.");
                return rs.getLong(1);
            }
        }
    }

    private boolean isDuplicateVariant(long exportId, int variantId, long excludeItemId) throws Exception {
        String sql = "SELECT COUNT(1) FROM dbo.StockExportItems " +
                     "WHERE StockExportID = ? AND VariantID = ? AND StockExportItemID <> ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, exportId);
            ps.setInt(2, variantId);
            ps.setLong(3, excludeItemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void validateItems(List<StockExportItem> items) throws Exception {
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        for (StockExportItem item : items) {
            if (item.getQuantity() <= 0) throw new Exception("Số lượng phải lớn hơn 0.");
            if (!seen.add(item.getVariantId())) {
                throw new Exception("Không được chọn trùng biến thể trong cùng một phiếu.");
            }
        }
    }

    private String resolveTransactionType(String exportType) {
        if ("TRANSFER".equals(exportType)) return "TRANSFER_OUT";
        if ("DAMAGED".equals(exportType)) return "DAMAGED_OUT";
        if ("OTHER".equals(exportType)) return "ADJUST_OUT";
        return "SALE"; // default SALE
    }
}