package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.StockReceipt;
import com.watchstore.model.StockReceiptItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StockReceiptRepository {

    // ─────────────────────────────────────────────────────────
    //  findAll
    // ─────────────────────────────────────────────────────────
    public List<StockReceipt> findAll() {
        List<StockReceipt> list = new ArrayList<>();

        String sql =
                "SELECT sr.*, w.WarehouseName, u.FullName as CreatedByName, " +
                        "au.FullName as ApprovedByName " +
                        "FROM dbo.StockReceipts sr " +
                        "LEFT JOIN dbo.Warehouses w ON sr.WarehouseID = w.WarehouseID " +
                        "LEFT JOIN dbo.Users u ON sr.CreatedBy = u.UserID " +
                        "LEFT JOIN dbo.Users au ON sr.ApprovedBy = au.UserID " +
                        "ORDER BY sr.ReceiptDate DESC";

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                list.add(mapHeader(rs));
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách phiếu nhập.", e);
        }

        return list;
    }

    // ─────────────────────────────────────────────────────────
    //  findById (header + items)
    // ─────────────────────────────────────────────────────────
    public StockReceipt findById(long id) {

        StockReceipt sr = null;

        String sqlHeader =
                "SELECT sr.*, " +
                        "w.WarehouseName, " +
                        "u.FullName as CreatedByName, " +
                        "au.FullName as ApprovedByName " +
                        "FROM dbo.StockReceipts sr " +
                        "LEFT JOIN dbo.Warehouses w ON sr.WarehouseID = w.WarehouseID " +
                        "LEFT JOIN dbo.Users u ON sr.CreatedBy = u.UserID " +
                        "LEFT JOIN dbo.Users au ON sr.ApprovedBy = au.UserID " +
                        "WHERE sr.StockReceiptID = ?";

        String sqlItems =
                "SELECT sri.*, " +
                        "p.ProductName, " +
                        "pv.SKU, " +
                        "ISNULL((SELECT STRING_AGG(pa.AttributeName + ': ' + pav.ValueName, ', ') " +
                        " FROM dbo.VariantAttributeValues vav " +
                        " INNER JOIN dbo.ProductAttributeValues pav ON vav.AttributeValueID = pav.AttributeValueID " +
                        " INNER JOIN dbo.ProductAttributes pa ON pav.AttributeID = pa.AttributeID " +
                        " WHERE vav.VariantID = pv.VariantID), '') AS VariantName " +
                        "FROM dbo.StockReceiptItems sri " +
                        "INNER JOIN dbo.ProductVariants pv ON sri.VariantID = pv.VariantID " +
                        "INNER JOIN dbo.Products p ON pv.ProductID = p.ProductID " +
                        "WHERE sri.StockReceiptID = ?";

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement psHeader = conn.prepareStatement(sqlHeader);
                PreparedStatement psItems = conn.prepareStatement(sqlItems)
        ) {

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
            throw new IllegalStateException("Không thể tải chi tiết phiếu nhập.", e);
        }

        return sr;
    }


    public List<StockReceipt> search(String keyword, String status, Integer warehouseId) {
        List<StockReceipt> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT sr.*, w.WarehouseName, u.FullName AS CreatedByName, au.FullName AS ApprovedByName " +
                        "FROM dbo.StockReceipts sr LEFT JOIN dbo.Warehouses w ON sr.WarehouseID=w.WarehouseID " +
                        "LEFT JOIN dbo.Users u ON sr.CreatedBy=u.UserID LEFT JOIN dbo.Users au ON sr.ApprovedBy=au.UserID WHERE 1=1 "
        );
        List<Object> params=new ArrayList<>();
        if(keyword!=null && !keyword.trim().isEmpty()){
            sql.append("AND (sr.ReceiptCode LIKE ? OR sr.SupplierName LIKE ? OR w.WarehouseName LIKE ?) ");
            String v="%"+keyword.trim()+"%"; params.add(v);params.add(v);params.add(v);
        }
        if(status!=null && !status.isBlank()){ sql.append("AND sr.Status=? "); params.add(status.trim().toUpperCase()); }
        if(warehouseId!=null && warehouseId>0){ sql.append("AND sr.WarehouseID=? "); params.add(warehouseId); }
        sql.append("ORDER BY sr.ReceiptDate DESC");
        try(Connection conn=DBContext.getConnection(); PreparedStatement ps=conn.prepareStatement(sql.toString())){
            for(int i=0;i<params.size();i++) ps.setObject(i+1,params.get(i));
            try(ResultSet rs=ps.executeQuery()){ while(rs.next()) list.add(mapHeader(rs)); }
        }catch(SQLException e){ throw new IllegalStateException("Không thể tra cứu phiếu nhập.",e); }
        return list;
    }

    public void updateDraft(StockReceipt receipt) throws Exception {
        if(receipt==null || receipt.getStockReceiptId()<=0) throw new Exception("Phiếu nhập không hợp lệ.");
        if(receipt.getWarehouseId()<=0) throw new Exception("Kho không hợp lệ.");
        if(receipt.getReceiptCode()==null || receipt.getReceiptCode().isBlank()) throw new Exception("Mã phiếu nhập không được để trống.");
        if(receipt.getSupplierName()==null || receipt.getSupplierName().isBlank()) throw new Exception("Nhà cung cấp không được để trống.");
        String sql="UPDATE dbo.StockReceipts SET ReceiptCode=?, WarehouseID=?, SupplierName=?, SupplierPhone=?, Note=? WHERE StockReceiptID=? AND Status='DRAFT'";
        try(Connection conn=DBContext.getConnection(); PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setString(1,receipt.getReceiptCode()); ps.setInt(2,receipt.getWarehouseId()); ps.setString(3,receipt.getSupplierName().trim());
            ps.setString(4,receipt.getSupplierPhone()); ps.setString(5,receipt.getNote()); ps.setLong(6,receipt.getStockReceiptId());
            if(ps.executeUpdate()!=1) throw new Exception("Chỉ có thể sửa phiếu nhập ở trạng thái DRAFT.");
        }catch(SQLException e){ throw new Exception("Không thể cập nhật phiếu nhập.",e); }
    }

    public void deleteDraft(long receiptId) throws Exception {
        String sql="DELETE FROM dbo.StockReceipts WHERE StockReceiptID=? AND Status='DRAFT'";
        try(Connection conn=DBContext.getConnection(); PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setLong(1,receiptId);
            if(ps.executeUpdate()!=1) throw new Exception("Chỉ có thể xóa phiếu nhập ở trạng thái DRAFT.");
        }catch(SQLException e){ throw new Exception("Không thể xóa phiếu nhập.",e); }
    }

    // ─────────────────────────────────────────────────────────
    //  createDraft — INSERT header + items, Status = DRAFT
    // ─────────────────────────────────────────────────────────
    public long createDraft(StockReceipt receipt) throws Exception {

        if (receipt.getItems() == null || receipt.getItems().isEmpty()) {
            throw new Exception("Phiếu nhập phải có ít nhất một sản phẩm.");
        }

        validateItems(receipt.getItems());

        String insertReceiptSql =
                "INSERT INTO dbo.StockReceipts " +
                        "(ReceiptCode, WarehouseID, SupplierName, " +
                        "SupplierPhone, Status, TotalCost, Note, CreatedBy) " +
                        "VALUES (?, ?, ?, ?, 'DRAFT', ?, ?, ?)";

        String insertItemSql =
                "INSERT INTO dbo.StockReceiptItems " +
                        "(StockReceiptID, VariantID, Quantity, UnitCost) " +
                        "VALUES (?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            long receiptId;

            try (
                    PreparedStatement psHeader = conn.prepareStatement(
                            insertReceiptSql,
                            Statement.RETURN_GENERATED_KEYS
                    )
            ) {
                psHeader.setString(1, receipt.getReceiptCode());
                psHeader.setInt(2, receipt.getWarehouseId());
                psHeader.setString(3, receipt.getSupplierName());
                psHeader.setString(4, receipt.getSupplierPhone());
                psHeader.setBigDecimal(5, BigDecimal.ZERO);
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

            recalculateTotalCost(conn, receiptId);
            conn.commit();

            return receiptId;

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  addItem — thêm 1 item vào phiếu DRAFT
    // ─────────────────────────────────────────────────────────
    public void addItem(long receiptId, StockReceiptItem item) throws Exception {

        if (item == null) {
            throw new Exception("Dữ liệu sản phẩm không hợp lệ.");
        }
        if (item.getVariantId() <= 0) {
            throw new Exception("Phải chọn biến thể.");
        }
        if (item.getQuantity() <= 0) {
            throw new Exception("Số lượng phải lớn hơn 0.");
        }
        if (item.getUnitCost() != null && item.getUnitCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new Exception("Đơn giá không được âm.");
        }

        String statusSql = "SELECT Status FROM dbo.StockReceipts WITH (UPDLOCK) WHERE StockReceiptID = ?";
        String duplicateSql = "SELECT COUNT(1) FROM dbo.StockReceiptItems WHERE StockReceiptID = ? AND VariantID = ?";
        String insertSql = "INSERT INTO dbo.StockReceiptItems (StockReceiptID, VariantID, Quantity, UnitCost) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                assertStatus(conn, receiptId, "DRAFT", statusSql);

                try (PreparedStatement ps = conn.prepareStatement(duplicateSql)) {
                    ps.setLong(1, receiptId);
                    ps.setInt(2, item.getVariantId());

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new Exception("Biến thể này đã tồn tại trong phiếu. Hãy chỉnh sửa dòng hiện có.");
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setLong(1, receiptId);
                    ps.setInt(2, item.getVariantId());
                    ps.setInt(3, item.getQuantity());
                    ps.setBigDecimal(4, item.getUnitCost() != null ? item.getUnitCost() : BigDecimal.ZERO);
                    ps.executeUpdate();
                }

                recalculateTotalCost(conn, receiptId);
                conn.commit();

            } catch (Exception e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                throw e;
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  updateItem — sửa item trong DRAFT
    // ─────────────────────────────────────────────────────────
    public void updateItem(long receiptItemId, int quantity, BigDecimal unitCost) throws Exception {

        if (quantity <= 0) {
            throw new Exception("Số lượng phải lớn hơn 0.");
        }
        if (unitCost != null && unitCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new Exception("Đơn giá không được âm.");
        }

        String findSql = "SELECT StockReceiptID FROM dbo.StockReceiptItems WHERE StockReceiptItemID = ?";
        String updateSql = "UPDATE dbo.StockReceiptItems SET Quantity = ?, UnitCost = ? WHERE StockReceiptItemID = ?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long receiptId;
                try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                    ps.setLong(1, receiptItemId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new Exception("Không tìm thấy sản phẩm trong phiếu.");
                        }
                        receiptId = rs.getLong("StockReceiptID");
                    }
                }

                assertStatus(conn, receiptId, "DRAFT", "SELECT Status FROM dbo.StockReceipts WITH (UPDLOCK) WHERE StockReceiptID = ?");

                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, quantity);
                    ps.setBigDecimal(2, unitCost != null ? unitCost : BigDecimal.ZERO);
                    ps.setLong(3, receiptItemId);

                    if (ps.executeUpdate() != 1) {
                        throw new Exception("Không thể cập nhật sản phẩm trong phiếu.");
                    }
                }

                recalculateTotalCost(conn, receiptId);
                conn.commit();

            } catch (Exception e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                throw e;
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  deleteItem — xóa item khỏi DRAFT
    // ─────────────────────────────────────────────────────────
    public void deleteItem(long receiptItemId) throws Exception {

        String findSql = "SELECT StockReceiptID FROM dbo.StockReceiptItems WHERE StockReceiptItemID = ?";
        String deleteSql = "DELETE FROM dbo.StockReceiptItems WHERE StockReceiptItemID = ?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long receiptId;
                try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                    ps.setLong(1, receiptItemId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new Exception("Không tìm thấy sản phẩm trong phiếu.");
                        }
                        receiptId = rs.getLong("StockReceiptID");
                    }
                }

                assertStatus(conn, receiptId, "DRAFT", "SELECT Status FROM dbo.StockReceipts WITH (UPDLOCK) WHERE StockReceiptID = ?");

                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setLong(1, receiptItemId);
                    if (ps.executeUpdate() != 1) {
                        throw new Exception("Không thể xóa sản phẩm khỏi phiếu.");
                    }
                }

                recalculateTotalCost(conn, receiptId);
                conn.commit();

            } catch (Exception e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                throw e;
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  submitForApproval — DRAFT → PENDING
    // ─────────────────────────────────────────────────────────
    public void submitForApproval(long receiptId) throws Exception {

        String sql =
                "UPDATE dbo.StockReceipts " +
                        "SET Status = 'PENDING' " +
                        "WHERE StockReceiptID = ? " +
                        "AND Status = 'DRAFT' " +
                        "AND EXISTS (SELECT 1 FROM dbo.StockReceiptItems WHERE StockReceiptID = ?)";

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setLong(1, receiptId);
            ps.setLong(2, receiptId);

            if (ps.executeUpdate() != 1) {
                throw new Exception("Phiếu không tồn tại, không ở trạng thái DRAFT hoặc chưa có sản phẩm.");
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  approve — PENDING → COMPLETED + cập nhật tồn kho
    // ─────────────────────────────────────────────────────────
    public void approve(long receiptId, int approvedBy) throws Exception {

        String sqlGetReceipt =
                "SELECT Note, WarehouseID, CreatedBy " +
                        "FROM dbo.StockReceipts WITH (UPDLOCK) " +
                        "WHERE StockReceiptID = ? AND Status = 'PENDING'";

        String sqlItems = "SELECT VariantID, Quantity FROM dbo.StockReceiptItems WHERE StockReceiptID = ?";

        String updateStatusSql =
                "UPDATE dbo.StockReceipts " +
                        "SET Status = 'COMPLETED', ApprovedBy = ?, ApprovedAt = SYSDATETIME() " +
                        "WHERE StockReceiptID = ? AND Status = 'PENDING'";

        String callSp = "{CALL dbo.sp_RecordInventoryTransaction(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String note;
                int warehouseId;
                int createdBy;

                try (PreparedStatement ps = conn.prepareStatement(sqlGetReceipt)) {
                    ps.setLong(1, receiptId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new Exception("Phiếu không tồn tại hoặc không ở trạng thái chờ duyệt.");
                        }
                        note = rs.getString("Note");
                        warehouseId = rs.getInt("WarehouseID");
                        createdBy = rs.getInt("CreatedBy");
                    }
                }

                if (!hasItems(conn, receiptId)) {
                    throw new Exception("Phiếu phải có ít nhất một sản phẩm.");
                }

                try (PreparedStatement ps = conn.prepareStatement(updateStatusSql)) {
                    ps.setInt(1, approvedBy);
                    ps.setLong(2, receiptId);
                    if (ps.executeUpdate() != 1) {
                        throw new Exception("Phiếu đã được xử lý bởi một thao tác khác.");
                    }
                }

                try (
                        PreparedStatement psItems = conn.prepareStatement(sqlItems);
                        CallableStatement cs = conn.prepareCall(callSp)
                ) {
                    psItems.setLong(1, receiptId);
                    try (ResultSet rs = psItems.executeQuery()) {
                        while (rs.next()) {
                            int quantity = rs.getInt("Quantity");
                            if (quantity <= 0) {
                                throw new Exception("Số lượng sản phẩm trong phiếu không hợp lệ.");
                            }

                            cs.setInt(1, warehouseId);
                            cs.setInt(2, rs.getInt("VariantID"));
                            cs.setString(3, "RECEIPT");
                            cs.setInt(4, quantity);
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
                try { conn.rollback(); } catch (SQLException ignored) {}
                throw e;
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    //  cancel — DRAFT|PENDING → CANCELLED
    // ─────────────────────────────────────────────────────────
    public void cancel(long receiptId) throws Exception {

        String sql =
                "UPDATE dbo.StockReceipts SET Status = 'CANCELLED' " +
                        "WHERE StockReceiptID = ? AND Status IN ('DRAFT', 'PENDING')";

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setLong(1, receiptId);
            if (ps.executeUpdate() != 1) {
                throw new Exception("Phiếu không tồn tại hoặc không thể hủy ở trạng thái hiện tại.");
            }
        }
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
        sr.setApprovedByName(rs.getString("ApprovedByName"));
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

    private void assertStatus(Connection conn, long receiptId, String expectedStatus, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, receiptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("Phiếu không tồn tại.");
                }
                String status = rs.getString("Status");
                if (!expectedStatus.equals(status)) {
                    throw new Exception("Thao tác không hợp lệ. Trạng thái hiện tại: " + status);
                }
            }
        }
    }

    private boolean hasItems(Connection conn, long receiptId) throws SQLException {
        String sql = "SELECT COUNT(1) FROM dbo.StockReceiptItems WHERE StockReceiptID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, receiptId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void recalculateTotalCost(Connection conn, long receiptId) throws SQLException {
        String sql =
                "UPDATE dbo.StockReceipts " +
                        "SET TotalCost = (SELECT ISNULL(SUM(Quantity * UnitCost), 0) FROM dbo.StockReceiptItems WHERE StockReceiptID = ?) " +
                        "WHERE StockReceiptID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, receiptId);
            ps.setLong(2, receiptId);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Không thể cập nhật tổng tiền phiếu nhập.");
            }
        }
    }

    private void validateItems(List<StockReceiptItem> items) throws Exception {
        Set<Integer> seen = new HashSet<>();
        for (StockReceiptItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new Exception("Số lượng phải lớn hơn 0.");
            }
            if (item.getUnitCost() != null && item.getUnitCost().compareTo(BigDecimal.ZERO) < 0) {
                throw new Exception("Đơn giá không được âm.");
            }
            if (!seen.add(item.getVariantId())) {
                throw new Exception("Không được chọn trùng biến thể trong cùng một phiếu.");
            }
        }
    }
}