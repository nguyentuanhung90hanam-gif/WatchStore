package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.StockReceipt;
import com.watchstore.model.StockReceiptItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockReceiptRepository {

    // ─────────────────────────────────────────────────────────
    //  findAll
    // ─────────────────────────────────────────────────────────
    public List<StockReceipt> findAll() {
        List<StockReceipt> list = new ArrayList<>();
        String sql = "SELECT sr.*, w.WarehouseName, u.FullName as CreatedByName " +
                     "FROM dbo.StockReceipts sr " +
                     "INNER JOIN dbo.Warehouses w ON sr.WarehouseID = w.WarehouseID " +
                     "LEFT JOIN dbo.Users u ON sr.CreatedBy = u.UserID " +
                     "ORDER BY sr.ReceiptDate DESC";

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
    public StockReceipt findById(long id) {
        StockReceipt sr = null;
        String sqlHeader = "SELECT sr.*, w.WarehouseName, u.FullName as CreatedByName " +
                           "FROM dbo.StockReceipts sr " +
                           "INNER JOIN dbo.Warehouses w ON sr.WarehouseID = w.WarehouseID " +
                           "LEFT JOIN dbo.Users u ON sr.CreatedBy = u.UserID " +
                           "WHERE sr.StockReceiptID = ?";

        String sqlItems = "SELECT sri.*, p.ProductName, p.SKU, " +
                          "(SELECT STRING_AGG(pa.AttributeName + ': ' + pav.Value, ', ') " +
                          " FROM dbo.VariantAttributeValues vav " +
                          " INNER JOIN dbo.ProductAttributeValues pav ON vav.ValueID = pav.ValueID " +
                          " INNER JOIN dbo.ProductAttributes pa ON pav.AttributeID = pa.AttributeID " +
                          " WHERE vav.VariantID = pv.VariantID) as VariantName " +
                          "FROM dbo.StockReceiptItems sri " +
                          "INNER JOIN dbo.ProductVariants pv ON sri.VariantID = pv.VariantID " +
                          "INNER JOIN dbo.Products p ON pv.ProductID = p.ProductID " +
                          "WHERE sri.StockReceiptID = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement psHeader = conn.prepareStatement(sqlHeader);
             PreparedStatement psItems = conn.prepareStatement(sqlItems)) {

            psHeader.setLong(1, id);
            try (ResultSet rs = psHeader.executeQuery()) {
                if (rs.next()) {
                    sr = mapHeader(rs);
                }
            }

            if (sr != null) {
                List<StockReceiptItem> items = new ArrayList<>();
                psItems.setLong(1, id);
                try (ResultSet rs = psItems.executeQuery()) {
                    while (rs.next()) {
                        items.add(mapItem(rs));
                    }
                }
                sr.setItems(items);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sr;
    }

    // ─────────────────────────────────────────────────────────
    //  createDraft — INSERT header + items, Status = DRAFT, no SP call
    // ─────────────────────────────────────────────────────────
    public long createDraft(StockReceipt receipt) throws Exception {
        if (receipt.getItems() == null || receipt.getItems().isEmpty()) {
            throw new Exception("Phiếu nhập phải có ít nhất một sản phẩm.");
        }
        validateItems(receipt.getItems());

        String insertReceiptSql =
            "INSERT INTO dbo.StockReceipts " +
            "(ReceiptCode, WarehouseID, SupplierName, SupplierPhone, Status, TotalCost, Note, CreatedBy) " +
            "VALUES (?, ?, ?, ?, 'DRAFT', ?, ?, ?)";
        String insertItemSql =
            "INSERT INTO dbo.StockReceiptItems (StockReceiptID, VariantID, Quantity, UnitCost) " +
            "VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            long receiptId;
            try (PreparedStatement psHeader = conn.prepareStatement(insertReceiptSql, Statement.RETURN_GENERATED_KEYS)) {
                psHeader.setString(1, receipt.getReceiptCode());
                psHeader.setInt(2, receipt.getWarehouseId());
                psHeader.setString(3, receipt.getSupplierName());
                psHeader.setString(4, receipt.getSupplierPhone());
                psHeader.setBigDecimal(5, receipt.getTotalCost());
                psHeader.setString(6, receipt.getNote());
                psHeader.setInt(7, receipt.getCreatedBy());
                psHeader.executeUpdate();
                try (ResultSet rs = psHeader.getGeneratedKeys()) {
                    if (rs.next()) {
                        receiptId = rs.getLong(1);
                    } else {
                        throw new SQLException("Không lấy được StockReceiptID.");
                    }
                }
            }

            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql)) {
                for (StockReceiptItem item : receipt.getItems()) {
                    psItem.setLong(1, receiptId);
                    psItem.setInt(2, item.getVariantId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.setBigDecimal(4, item.getUnitCost() != null ? item.getUnitCost() : BigDecimal.ZERO);
                    psItem.executeUpdate();
                }
            }

            conn.commit();
            return receiptId;
        } catch (Exception e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { /* ignored */ } }
            throw e;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { /* ignored */ } }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  addItem — thêm 1 item vào phiếu DRAFT
    // ─────────────────────────────────────────────────────────
    public void addItem(long receiptId, StockReceiptItem item) throws Exception {
        assertStatus(receiptId, "DRAFT");
        if (item.getQuantity() <= 0) throw new Exception("Số lượng phải lớn hơn 0.");
        if (isDuplicateVariant(receiptId, item.getVariantId(), 0)) {
            throw new Exception("Biến thể này đã tồn tại trong phiếu. Hãy chỉnh sửa dòng hiện có.");
        }
        String sql = "INSERT INTO dbo.StockReceiptItems (StockReceiptID, VariantID, Quantity, UnitCost) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, receiptId);
            ps.setInt(2, item.getVariantId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitCost() != null ? item.getUnitCost() : BigDecimal.ZERO);
            ps.executeUpdate();
        }
        recalculateTotalCost(receiptId);
    }

    // ─────────────────────────────────────────────────────────
    //  updateItem — sửa quantity/unitCost của 1 item trong DRAFT
    // ─────────────────────────────────────────────────────────
    public void updateItem(long receiptItemId, int quantity, BigDecimal unitCost) throws Exception {
        if (quantity <= 0) throw new Exception("Số lượng phải lớn hơn 0.");
        String getReceiptIdSql = "SELECT StockReceiptID FROM dbo.StockReceiptItems WHERE StockReceiptItemID = ?";
        long receiptId;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(getReceiptIdSql)) {
            ps.setLong(1, receiptItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Không tìm thấy item.");
                receiptId = rs.getLong(1);
            }
        }
        assertStatus(receiptId, "DRAFT");

        String sql = "UPDATE dbo.StockReceiptItems SET Quantity = ?, UnitCost = ? WHERE StockReceiptItemID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setBigDecimal(2, unitCost != null ? unitCost : BigDecimal.ZERO);
            ps.setLong(3, receiptItemId);
            ps.executeUpdate();
        }
        recalculateTotalCost(receiptId);
    }

    // ─────────────────────────────────────────────────────────
    //  deleteItem — xóa 1 item khỏi phiếu DRAFT
    // ─────────────────────────────────────────────────────────
    public void deleteItem(long receiptItemId) throws Exception {
        String getReceiptIdSql = "SELECT StockReceiptID FROM dbo.StockReceiptItems WHERE StockReceiptItemID = ?";
        long receiptId;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(getReceiptIdSql)) {
            ps.setLong(1, receiptItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Không tìm thấy item.");
                receiptId = rs.getLong(1);
            }
        }
        assertStatus(receiptId, "DRAFT");

        String sql = "DELETE FROM dbo.StockReceiptItems WHERE StockReceiptItemID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, receiptItemId);
            ps.executeUpdate();
        }
        recalculateTotalCost(receiptId);
    }

    // ─────────────────────────────────────────────────────────
    //  submitForApproval — DRAFT → PENDING
    // ─────────────────────────────────────────────────────────
    public void submitForApproval(long receiptId) throws Exception {
        assertStatus(receiptId, "DRAFT");
        assertHasItems(receiptId);
        updateStatus(receiptId, "PENDING");
    }

    // ─────────────────────────────────────────────────────────
    //  approve — PENDING → COMPLETED + cập nhật tồn kho (qua SP)
    // ─────────────────────────────────────────────────────────
    public void approve(long receiptId, int approvedBy) throws Exception {
        assertStatus(receiptId, "PENDING");

        String sqlItems = "SELECT VariantID, Quantity FROM dbo.StockReceiptItems WHERE StockReceiptID = ?";
        String sqlGetNote = "SELECT Note, WarehouseID, CreatedBy FROM dbo.StockReceipts WHERE StockReceiptID = ?";
        String updateStatusSql =
            "UPDATE dbo.StockReceipts SET Status = 'COMPLETED', ApprovedBy = ?, ApprovedAt = SYSDATETIME() " +
            "WHERE StockReceiptID = ?";
        String callSp = "{CALL dbo.sp_RecordInventoryTransaction(?, ?, ?, ?, ?, ?, ?, ?)}";

        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            String note = "";
            int warehouseId = 0;
            int createdBy = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlGetNote)) {
                ps.setLong(1, receiptId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        note = rs.getString("Note");
                        warehouseId = rs.getInt("WarehouseID");
                        createdBy = rs.getInt("CreatedBy");
                    }
                }
            }

            // 1. Cập nhật status
            try (PreparedStatement ps = conn.prepareStatement(updateStatusSql)) {
                ps.setInt(1, approvedBy);
                ps.setLong(2, receiptId);
                ps.executeUpdate();
            }

            // 2. Gọi SP cho từng item
            try (PreparedStatement psItems = conn.prepareStatement(sqlItems);
                 CallableStatement cs = conn.prepareCall(callSp)) {
                psItems.setLong(1, receiptId);
                try (ResultSet rs = psItems.executeQuery()) {
                    while (rs.next()) {
                        cs.setInt(1, warehouseId);
                        cs.setInt(2, rs.getInt("VariantID"));
                        cs.setString(3, "RECEIPT");
                        cs.setInt(4, rs.getInt("Quantity"));
                        cs.setString(5, "StockReceipts");
                        cs.setLong(6, receiptId);
                        cs.setString(7, note);
                        cs.setInt(8, createdBy);
                        cs.execute();
                    }
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
    //  cancel — DRAFT|PENDING → CANCELLED (không cập nhật tồn kho)
    // ─────────────────────────────────────────────────────────
    public void cancel(long receiptId) throws Exception {
        StockReceipt sr = findById(receiptId);
        if (sr == null) throw new Exception("Phiếu không tồn tại.");
        if ("COMPLETED".equals(sr.getStatus())) {
            throw new Exception("Không thể hủy phiếu đã hoàn thành.");
        }
        updateStatus(receiptId, "CANCELLED");
    }

    // ─────────────────────────────────────────────────────────
    //  Private helpers
    // ─────────────────────────────────────────────────────────
    private StockReceipt mapHeader(ResultSet rs) throws SQLException {
        StockReceipt sr = new StockReceipt();
        sr.setStockReceiptId(rs.getLong("StockReceiptID"));
        sr.setReceiptCode(rs.getString("ReceiptCode"));
        sr.setWarehouseId(rs.getInt("WarehouseID"));
        sr.setWarehouseName(rs.getString("WarehouseName"));
        sr.setSupplierName(rs.getString("SupplierName"));
        sr.setSupplierPhone(rs.getString("SupplierPhone"));
        if (rs.getTimestamp("ReceiptDate") != null) {
            sr.setReceiptDate(rs.getTimestamp("ReceiptDate").toLocalDateTime());
        }
        sr.setStatus(rs.getString("Status"));
        sr.setTotalCost(rs.getBigDecimal("TotalCost"));
        sr.setNote(rs.getString("Note"));
        sr.setCreatedBy(rs.getInt("CreatedBy"));
        sr.setCreatedByName(rs.getString("CreatedByName"));
        sr.setApprovedBy(rs.getObject("ApprovedBy") != null ? rs.getInt("ApprovedBy") : null);
        if (rs.getTimestamp("ApprovedAt") != null) {
            sr.setApprovedAt(rs.getTimestamp("ApprovedAt").toLocalDateTime());
        }
        return sr;
    }

    private StockReceiptItem mapItem(ResultSet rs) throws SQLException {
        StockReceiptItem item = new StockReceiptItem();
        item.setStockReceiptItemId(rs.getLong("StockReceiptItemID"));
        item.setStockReceiptId(rs.getLong("StockReceiptID"));
        item.setVariantId(rs.getInt("VariantID"));
        item.setQuantity(rs.getInt("Quantity"));
        item.setUnitCost(rs.getBigDecimal("UnitCost"));
        item.setLineTotal(rs.getBigDecimal("LineTotal"));
        item.setProductName(rs.getString("ProductName"));
        item.setSku(rs.getString("SKU"));
        item.setVariantName(rs.getString("VariantName"));
        return item;
    }

    private void assertStatus(long receiptId, String expectedStatus) throws Exception {
        String sql = "SELECT Status FROM dbo.StockReceipts WHERE StockReceiptID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, receiptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Phiếu không tồn tại.");
                String status = rs.getString("Status");
                if (!expectedStatus.equals(status)) {
                    throw new Exception("Thao tác không hợp lệ. Trạng thái hiện tại: " + status);
                }
            }
        }
    }

    private void assertHasItems(long receiptId) throws Exception {
        String sql = "SELECT COUNT(1) FROM dbo.StockReceiptItems WHERE StockReceiptID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, receiptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new Exception("Phiếu phải có ít nhất một sản phẩm.");
                }
            }
        }
    }

    private void updateStatus(long receiptId, String newStatus) throws Exception {
        String sql = "UPDATE dbo.StockReceipts SET Status = ? WHERE StockReceiptID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, receiptId);
            ps.executeUpdate();
        }
    }

    private void recalculateTotalCost(long receiptId) {
        String sql = "UPDATE dbo.StockReceipts SET TotalCost = (" +
                     "  SELECT ISNULL(SUM(Quantity * UnitCost), 0) FROM dbo.StockReceiptItems WHERE StockReceiptID = ?" +
                     ") WHERE StockReceiptID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, receiptId);
            ps.setLong(2, receiptId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isDuplicateVariant(long receiptId, int variantId, long excludeItemId) throws Exception {
        String sql = "SELECT COUNT(1) FROM dbo.StockReceiptItems " +
                     "WHERE StockReceiptID = ? AND VariantID = ? AND StockReceiptItemID <> ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, receiptId);
            ps.setInt(2, variantId);
            ps.setLong(3, excludeItemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void validateItems(List<StockReceiptItem> items) throws Exception {
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        for (StockReceiptItem item : items) {
            if (item.getQuantity() <= 0) throw new Exception("Số lượng phải lớn hơn 0.");
            if (item.getUnitCost() != null && item.getUnitCost().compareTo(BigDecimal.ZERO) < 0) {
                throw new Exception("Đơn giá không được âm.");
            }
            if (!seen.add(item.getVariantId())) {
                throw new Exception("Không được chọn trùng biến thể trong cùng một phiếu.");
            }
        }
    }
}
