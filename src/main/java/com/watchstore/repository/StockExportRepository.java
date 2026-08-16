package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.StockExport;
import com.watchstore.model.StockExportItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StockExportRepository {

    // ─────────────────────────────────────────────────────────
    //  findAll
    // ─────────────────────────────────────────────────────────
    public List<StockExport> findAll() throws Exception {
        List<StockExport> list = new ArrayList<>();

        String sql =
                "SELECT se.*, " +
                        "w.WarehouseName, " +
                        "u.FullName as CreatedByName, " +
                        "au.FullName as ApprovedByName " +
                        "FROM dbo.StockExports se " +
                        "LEFT JOIN dbo.Warehouses w ON se.WarehouseID = w.WarehouseID " +
                        "LEFT JOIN dbo.Users u ON se.CreatedBy = u.UserID " +
                        "LEFT JOIN dbo.Users au ON se.ApprovedBy = au.UserID " +
                        "ORDER BY se.ExportDate DESC";

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                list.add(mapHeader(rs));
            }
        } catch (SQLException e) {
            throw new Exception("Không thể tải danh sách phiếu xuất.", e);
        }

        return list;
    }


    public List<StockExport> search(String keyword, String status, Integer warehouseId) throws Exception {
        List<StockExport> list=new ArrayList<>();
        StringBuilder sql=new StringBuilder(
                "SELECT se.*, w.WarehouseName, u.FullName AS CreatedByName, au.FullName AS ApprovedByName " +
                        "FROM dbo.StockExports se LEFT JOIN dbo.Warehouses w ON se.WarehouseID=w.WarehouseID " +
                        "LEFT JOIN dbo.Users u ON se.CreatedBy=u.UserID LEFT JOIN dbo.Users au ON se.ApprovedBy=au.UserID WHERE 1=1 "
        );
        List<Object> params=new ArrayList<>();
        if(keyword!=null&&!keyword.trim().isEmpty()){
            sql.append("AND (se.ExportCode LIKE ? OR se.ReceiverName LIKE ? OR w.WarehouseName LIKE ?) ");
            String v="%"+keyword.trim()+"%";params.add(v);params.add(v);params.add(v);
        }
        if(status!=null&&!status.isBlank()){sql.append("AND se.Status=? ");params.add(status.trim().toUpperCase());}
        if(warehouseId!=null&&warehouseId>0){sql.append("AND se.WarehouseID=? ");params.add(warehouseId);}
        sql.append("ORDER BY se.ExportDate DESC");
        try(Connection conn=DBContext.getConnection();PreparedStatement ps=conn.prepareStatement(sql.toString())){
            for(int i=0;i<params.size();i++)ps.setObject(i+1,params.get(i));
            try(ResultSet rs=ps.executeQuery()){while(rs.next())list.add(mapHeader(rs));}
        }
        return list;
    }

    public void updateDraft(StockExport export) throws Exception {
        if(export==null||export.getStockExportId()<=0)throw new Exception("Phiếu xuất không hợp lệ.");
        if(export.getExportCode()==null || export.getExportCode().isBlank())throw new Exception("Mã phiếu xuất không được để trống.");
        String type=export.getExportType()==null?"":export.getExportType().trim().toUpperCase();
        if(!List.of("SALE","TRANSFER","DAMAGED","OTHER").contains(type))throw new Exception("Loại xuất không hợp lệ.");
        Long orderId=export.getOrderId();
        if("SALE".equals(type)){ if(orderId==null||orderId<=0)throw new Exception("Phiếu xuất SALE phải có OrderID."); validateOrderExists(orderId); }
        else orderId=null;
        String sql="UPDATE dbo.StockExports SET ExportCode=?, WarehouseID=?, OrderID=?, ExportType=?, ReceiverName=?, Note=? WHERE StockExportID=? AND Status='DRAFT'";
        try(Connection conn=DBContext.getConnection();PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setString(1,export.getExportCode());ps.setInt(2,export.getWarehouseId());
            if(orderId==null)ps.setNull(3,Types.BIGINT);else ps.setLong(3,orderId);
            ps.setString(4,type);ps.setString(5,export.getReceiverName());ps.setString(6,export.getNote());ps.setLong(7,export.getStockExportId());
            if(ps.executeUpdate()!=1)throw new Exception("Chỉ có thể sửa phiếu xuất ở trạng thái DRAFT.");
        }
    }

    public void deleteDraft(long exportId) throws Exception {
        String sql="DELETE FROM dbo.StockExports WHERE StockExportID=? AND Status='DRAFT'";
        try(Connection conn=DBContext.getConnection();PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setLong(1,exportId);if(ps.executeUpdate()!=1)throw new Exception("Chỉ có thể xóa phiếu xuất ở trạng thái DRAFT.");
        }catch(SQLException e){throw new Exception("Không thể xóa phiếu xuất.",e);}
    }

    // ─────────────────────────────────────────────────────────
    //  findById (header + items)
    // ─────────────────────────────────────────────────────────
    public StockExport findById(long id) throws Exception {
        StockExport se = null;

        String sqlHeader =
                "SELECT se.*, " +
                        "w.WarehouseName, " +
                        "u.FullName as CreatedByName, " +
                        "au.FullName as ApprovedByName " +
                        "FROM dbo.StockExports se " +
                        "LEFT JOIN dbo.Warehouses w ON se.WarehouseID = w.WarehouseID " +
                        "LEFT JOIN dbo.Users u ON se.CreatedBy = u.UserID " +
                        "LEFT JOIN dbo.Users au ON se.ApprovedBy = au.UserID " +
                        "WHERE se.StockExportID = ?";

        String sqlItems =
                "SELECT sei.*, " +
                        "p.ProductName, " +
                        "pv.SKU, " +
                        "ISNULL((SELECT STRING_AGG(pa.AttributeName + ': ' + pav.ValueText, ', ') " +
                        " FROM dbo.VariantAttributeValues vav " +
                        " INNER JOIN dbo.ProductAttributeValues pav ON vav.AttributeValueID = pav.AttributeValueID " +
                        " INNER JOIN dbo.ProductAttributes pa ON pav.AttributeID = pa.AttributeID " +
                        " WHERE vav.VariantID = pv.VariantID), '') AS VariantName " +
                        "FROM dbo.StockExportItems sei " +
                        "INNER JOIN dbo.ProductVariants pv ON sei.VariantID = pv.VariantID " +
                        "INNER JOIN dbo.Products p ON pv.ProductID = p.ProductID " +
                        "WHERE sei.StockExportID = ?";

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement psHeader = conn.prepareStatement(sqlHeader);
                PreparedStatement psItems = conn.prepareStatement(sqlItems)
        ) {
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
            throw new Exception("Không thể tải chi tiết phiếu xuất.", e);
        }

        return se;
    }

    // ─────────────────────────────────────────────────────────
    //  createDraft — INSERT header + items, Status = DRAFT
    // ─────────────────────────────────────────────────────────
    public long createDraft(StockExport export) throws Exception {
        List<StockExportItem> items = export.getItems();
        if (items == null) {
            items = new ArrayList<>();
        }

        if (items.isEmpty()) {
            throw new Exception("Phiếu xuất phải có ít nhất một sản phẩm.");
        }

        validateItems(items);

        String exportType = export.getExportType() == null
                ? ""
                : export.getExportType().trim().toUpperCase();

        if (!"SALE".equals(exportType)
                && !"TRANSFER".equals(exportType)
                && !"DAMAGED".equals(exportType)
                && !"OTHER".equals(exportType)) {
            throw new Exception("Loại xuất không hợp lệ.");
        }

        export.setExportType(exportType);

        if ("SALE".equals(exportType)) {
            if (export.getOrderId() == null || export.getOrderId() <= 0) {
                throw new Exception("Phiếu xuất bán phải gắn với một OrderID hợp lệ.");
            }
            validateOrderExists(export.getOrderId());
        } else {
            // TRANSFER / DAMAGED / OTHER không liên kết Orders.
            // Luôn ghi NULL để không vi phạm FK_StockExports_Order.
            export.setOrderId(null);
        }

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

            validateDraftStock(conn, export.getWarehouseId(), items);

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
                for (StockExportItem item : items) {
                    psItem.setLong(1, exportId);
                    psItem.setInt(2, item.getVariantId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.executeUpdate();
                }
            }

            conn.commit();
            return exportId;

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

    private void validateDraftStock(
            Connection conn,
            int warehouseId,
            List<StockExportItem> items
    ) throws Exception {
        String sql =
                "SELECT (QuantityOnHand - QuantityReserved) AS AvailableQuantity " +
                        "FROM dbo.InventoryBalances WITH (UPDLOCK) " +
                        "WHERE WarehouseID = ? AND VariantID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (StockExportItem item : items) {
                ps.setInt(1, warehouseId);
                ps.setInt(2, item.getVariantId());

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new Exception(
                                "Sản phẩm chưa có tồn kho tại kho đã chọn: VariantID " +
                                        item.getVariantId() + "."
                        );
                    }

                    int available = rs.getInt("AvailableQuantity");
                    if (available <= 0) {
                        throw new Exception(
                                "Sản phẩm đã hết hàng tại kho đã chọn: VariantID " +
                                        item.getVariantId() + "."
                        );
                    }

                    if (item.getQuantity() > available) {
                        throw new Exception(
                                "Số lượng xuất của VariantID " + item.getVariantId() +
                                        " vượt tồn khả dụng. Tồn: " + available +
                                        ", yêu cầu: " + item.getQuantity() + "."
                        );
                    }
                }
            }
        }
    }

    private void validateOrderExists(long orderId) throws Exception {
        String sql =
                "SELECT COUNT(1) FROM dbo.Orders " +
                        "WHERE OrderID = ?";

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setLong(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt(1) != 1) {
                    throw new Exception(
                            "Đơn hàng #" + orderId + " không tồn tại trong hệ thống."
                    );
                }
            }
        } catch (SQLException e) {
            throw new Exception(
                    "Không thể kiểm tra đơn hàng: " + e.getMessage(),
                    e
            );
        }
    }

    // ─────────────────────────────────────────────────────────
    //  addItem — thêm item vào phiếu DRAFT
    // ─────────────────────────────────────────────────────────
    public void addItem(long exportId, StockExportItem item) throws Exception {
        if (item == null) {
            throw new Exception("Dữ liệu sản phẩm không hợp lệ.");
        }
        if (item.getVariantId() <= 0) {
            throw new Exception("Biến thể không hợp lệ.");
        }
        if (item.getQuantity() <= 0) {
            throw new Exception("Số lượng phải lớn hơn 0.");
        }

        String statusSql = "SELECT Status FROM dbo.StockExports WITH (UPDLOCK) WHERE StockExportID = ?";
        String insertSql = "INSERT INTO dbo.StockExportItems (StockExportID, VariantID, Quantity) VALUES (?, ?, ?)";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                assertStatus(conn, exportId, "DRAFT", statusSql);

                int warehouseId;
                try (PreparedStatement psWarehouse = conn.prepareStatement(
                        "SELECT WarehouseID FROM dbo.StockExports WHERE StockExportID = ?")) {
                    psWarehouse.setLong(1, exportId);
                    try (ResultSet rsWarehouse = psWarehouse.executeQuery()) {
                        if (!rsWarehouse.next()) {
                            throw new Exception("Không tìm thấy phiếu xuất.");
                        }
                        warehouseId = rsWarehouse.getInt("WarehouseID");
                    }
                }

                List<StockExportItem> oneItem = new ArrayList<>();
                oneItem.add(item);
                validateDraftStock(conn, warehouseId, oneItem);

                if (isDuplicateVariant(conn, exportId, item.getVariantId(), 0)) {
                    throw new Exception("Biến thể này đã tồn tại trong phiếu.");
                }

                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setLong(1, exportId);
                    ps.setInt(2, item.getVariantId());
                    ps.setInt(3, item.getQuantity());
                    ps.executeUpdate();
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
    //  updateItem — sửa quantity của 1 item trong DRAFT
    // ─────────────────────────────────────────────────────────
    public void updateItem(long exportItemId, int quantity) throws Exception {
        if (quantity <= 0) {
            throw new Exception("Số lượng phải lớn hơn 0.");
        }

        String sql =
                "SELECT sei.StockExportID " +
                        "FROM dbo.StockExportItems sei " +
                        "INNER JOIN dbo.StockExports se ON sei.StockExportID = se.StockExportID " +
                        "WHERE sei.StockExportItemID = ? AND se.Status = 'DRAFT'";

        String updateSql = "UPDATE dbo.StockExportItems SET Quantity = ? WHERE StockExportItemID = ?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long exportId;
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, exportItemId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new Exception("Không tìm thấy item hoặc phiếu không ở trạng thái DRAFT.");
                        }
                        exportId = rs.getLong(1);
                    }
                }

                assertStatus(conn, exportId, "DRAFT", "SELECT Status FROM dbo.StockExports WITH (UPDLOCK) WHERE StockExportID = ?");

                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, quantity);
                    ps.setLong(2, exportItemId);
                    if (ps.executeUpdate() != 1) {
                        throw new Exception("Không thể cập nhật sản phẩm trong phiếu xuất.");
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
    //  deleteItem — xóa item khỏi phiếu DRAFT
    // ─────────────────────────────────────────────────────────
    public void deleteItem(long exportItemId) throws Exception {
        String sql =
                "SELECT sei.StockExportID " +
                        "FROM dbo.StockExportItems sei " +
                        "INNER JOIN dbo.StockExports se ON sei.StockExportID = se.StockExportID " +
                        "WHERE sei.StockExportItemID = ? AND se.Status = 'DRAFT'";

        String deleteSql = "DELETE FROM dbo.StockExportItems WHERE StockExportItemID = ?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long exportId;
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, exportItemId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new Exception("Không tìm thấy item hoặc phiếu không ở trạng thái DRAFT.");
                        }
                        exportId = rs.getLong(1);
                    }
                }

                assertStatus(conn, exportId, "DRAFT", "SELECT Status FROM dbo.StockExports WITH (UPDLOCK) WHERE StockExportID = ?");

                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setLong(1, exportItemId);
                    if (ps.executeUpdate() != 1) {
                        throw new Exception("Không thể xóa sản phẩm khỏi phiếu xuất.");
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
    //  submitForApproval — DRAFT → PENDING
    // ─────────────────────────────────────────────────────────
    public void submitForApproval(long exportId) throws Exception {
        assertStatus(exportId, "DRAFT");
        assertHasItems(exportId);
        updateStatus(exportId, "PENDING");
    }

    // ─────────────────────────────────────────────────────────
    //  approve — PENDING → COMPLETED (kiểm tồn + trừ tồn)
    // ─────────────────────────────────────────────────────────
    public void approve(long exportId, int approvedBy) throws Exception {
        String sqlGetExport =
                "SELECT WarehouseID, CreatedBy, ExportType, Note " +
                        "FROM dbo.StockExports WITH (UPDLOCK) " +
                        "WHERE StockExportID = ? AND Status = 'PENDING'";

        String sqlItems =
                "SELECT VariantID, Quantity FROM dbo.StockExportItems WHERE StockExportID = ? ORDER BY StockExportItemID";

        String checkStockSql =
                "SELECT QuantityOnHand, QuantityReserved, (QuantityOnHand - QuantityReserved) AS AvailableQuantity " +
                        "FROM dbo.InventoryBalances WITH (UPDLOCK) " +
                        "WHERE WarehouseID = ? AND VariantID = ?";

        String updateStatusSql =
                "UPDATE dbo.StockExports SET Status = 'COMPLETED', ApprovedBy = ?, ApprovedAt = SYSDATETIME() " +
                        "WHERE StockExportID = ? AND Status = 'PENDING'";

        String callSp = "{CALL dbo.sp_RecordInventoryTransaction(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int warehouseId;
                int createdBy;
                String exportType;
                String note;

                try (PreparedStatement ps = conn.prepareStatement(sqlGetExport)) {
                    ps.setLong(1, exportId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new Exception("Phiếu không tồn tại hoặc không ở trạng thái PENDING.");
                        }
                        warehouseId = rs.getInt("WarehouseID");
                        createdBy = rs.getInt("CreatedBy");
                        exportType = rs.getString("ExportType");
                        note = rs.getString("Note");
                    }
                }

                List<int[]> items = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(sqlItems)) {
                    ps.setLong(1, exportId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int variantId = rs.getInt("VariantID");
                            int quantity = rs.getInt("Quantity");
                            if (quantity <= 0) {
                                throw new Exception("Số lượng xuất phải lớn hơn 0.");
                            }
                            items.add(new int[]{variantId, quantity});
                        }
                    }
                }

                if (items.isEmpty()) {
                    throw new Exception("Phiếu xuất phải có ít nhất một sản phẩm.");
                }

                // Kiểm tra tồn kho khả dụng
                try (PreparedStatement ps = conn.prepareStatement(checkStockSql)) {
                    for (int[] item : items) {
                        ps.setInt(1, warehouseId);
                        ps.setInt(2, item[0]);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                throw new Exception("Không có tồn kho cho VariantID " + item[0] + " tại kho đã chọn.");
                            }
                            int available = rs.getInt("AvailableQuantity");
                            if (available < item[1]) {
                                throw new Exception("Không đủ tồn kho cho VariantID " + item[0] +
                                        ". Khả dụng: " + available + ", yêu cầu: " + item[1] + ".");
                            }
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(updateStatusSql)) {
                    ps.setInt(1, approvedBy);
                    ps.setLong(2, exportId);
                    if (ps.executeUpdate() != 1) {
                        throw new Exception("Phiếu đã được xử lý bởi yêu cầu khác hoặc không còn ở trạng thái PENDING.");
                    }
                }

                String transactionType = resolveTransactionType(exportType);

                try (CallableStatement cs = conn.prepareCall(callSp)) {
                    for (int[] item : items) {
                        cs.setInt(1, warehouseId);
                        cs.setInt(2, item[0]);
                        cs.setString(3, transactionType);
                        cs.setInt(4, -item[1]); // Xuất kho -> số lượng âm
                        cs.setString(5, "StockExports");
                        cs.setLong(6, exportId);
                        cs.setString(7, note);
                        cs.setInt(8, createdBy);
                        cs.execute();
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
    public void cancel(long exportId) throws Exception {
        String sql =
                "UPDATE dbo.StockExports SET Status = 'CANCELLED' " +
                        "WHERE StockExportID = ? AND Status IN ('DRAFT', 'PENDING')";

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setLong(1, exportId);
            if (ps.executeUpdate() != 1) {
                throw new Exception("Phiếu không tồn tại hoặc không thể hủy ở trạng thái hiện tại.");
            }
        } catch (SQLException e) {
            throw new Exception("Không thể hủy phiếu xuất.", e);
        }
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
        se.setApprovedByName(rs.getString("ApprovedByName"));
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

    private void assertStatus(Connection conn, long exportId, String expectedStatus, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, exportId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("Phiếu không tồn tại.");
                }
                String status = rs.getString("Status");
                if (!expectedStatus.equalsIgnoreCase(status)) {
                    throw new Exception("Thao tác không hợp lệ. Trạng thái hiện tại: " + status);
                }
            }
        }
    }

    private void assertStatus(long exportId, String expectedStatus) throws Exception {
        String sql = "SELECT Status FROM dbo.StockExports WHERE StockExportID = ?";
        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setLong(1, exportId);
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

    private void assertHasItems(long exportId) throws Exception {
        String sql = "SELECT COUNT(1) FROM dbo.StockExportItems WHERE StockExportID = ?";
        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
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
        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, newStatus);
            ps.setLong(2, exportId);
            ps.executeUpdate();
        }
    }

    private boolean isDuplicateVariant(Connection conn, long exportId, int variantId, long excludeItemId) throws Exception {
        String sql =
                "SELECT COUNT(1) FROM dbo.StockExportItems " +
                        "WHERE StockExportID = ? AND VariantID = ? AND StockExportItemID <> ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, exportId);
            ps.setInt(2, variantId);
            ps.setLong(3, excludeItemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void validateItems(List<StockExportItem> items) throws Exception {
        Set<Integer> seen = new HashSet<>();
        for (StockExportItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new Exception("Số lượng phải lớn hơn 0.");
            }
            if (!seen.add(item.getVariantId())) {
                throw new Exception("Không được chọn trùng biến thể trong cùng một phiếu.");
            }
        }
    }

    private String resolveTransactionType(String exportType) {
        if ("TRANSFER".equals(exportType)) {
            return "TRANSFER_OUT";
        }
        if ("DAMAGED".equals(exportType)) {
            return "DAMAGED_OUT";
        }
        if ("OTHER".equals(exportType)) {
            return "ADJUST_OUT";
        }
        return "SALE";
    }
}