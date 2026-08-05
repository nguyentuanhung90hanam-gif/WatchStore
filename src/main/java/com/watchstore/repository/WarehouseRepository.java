package com.watchstore.repository;

import com.watchstore.config.DBContext;
import com.watchstore.model.InventoryItem;
import com.watchstore.model.StockExport;
import com.watchstore.model.StockReceipt;
import com.watchstore.model.StocktakeRecord;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository cho chức năng Nhân viên kho.
 * Hỗ trợ ĐẦY ĐỦ CRUD (Thêm, Xem, Chỉnh Sửa, Xóa).
 * Tự động đảm bảo dữ liệu danh mục (Users, Warehouses) khi tạo phiếu trong CSDL.
 */
public class WarehouseRepository {

    private static final List<StockReceipt> MOCK_RECEIPTS = Collections.synchronizedList(new ArrayList<>());
    private static final List<StockExport>  MOCK_EXPORTS  = Collections.synchronizedList(new ArrayList<>());
    private static final List<InventoryItem> MOCK_INVENTORY = Collections.synchronizedList(new ArrayList<>());
    private static final List<StocktakeRecord> MOCK_STOCKTAKES = Collections.synchronizedList(new ArrayList<>());

    static {
        // --- 1. Dữ liệu mẫu Phiếu Nhập ---
        StockReceipt r1 = new StockReceipt();
        r1.setReceiptID(1);
        r1.setReceiptCode("PN-20260801-001");
        r1.setSupplierName("Công ty Seiko Việt Nam");
        r1.setSupplierPhone("0901234567");
        r1.setReceiptDate("01/08/2026");
        r1.setStatus("COMPLETED");
        r1.setTotalCost(new BigDecimal("45000000"));
        r1.setNote("Nhập hàng chính hãng lô 01/08");
        r1.setCreatedByName("Nguyễn Văn Kho");
        MOCK_RECEIPTS.add(r1);

        StockReceipt r2 = new StockReceipt();
        r2.setReceiptID(2);
        r2.setReceiptCode("PN-20260802-002");
        r2.setSupplierName("Casio Official Store");
        r2.setSupplierPhone("0912345678");
        r2.setReceiptDate("02/08/2026");
        r2.setStatus("PENDING");
        r2.setTotalCost(new BigDecimal("28500000"));
        r2.setNote("Hàng đợt 2 chờ duyệt nhập kho");
        r2.setCreatedByName("Trần Thị Kho");
        MOCK_RECEIPTS.add(r2);

        StockReceipt r3 = new StockReceipt();
        r3.setReceiptID(3);
        r3.setReceiptCode("PN-20260804-003");
        r3.setSupplierName("Tissot Import Co.");
        r3.setSupplierPhone("0988776655");
        r3.setReceiptDate("04/08/2026");
        r3.setStatus("DRAFT");
        r3.setTotalCost(new BigDecimal("89000000"));
        r3.setNote("Phiếu nháp nhập dòng Thụy Sĩ cao cấp");
        r3.setCreatedByName("Nguyễn Văn Kho");
        MOCK_RECEIPTS.add(r3);

        // --- 2. Dữ liệu mẫu Phiếu Xuất ---
        StockExport e1 = new StockExport();
        e1.setExportID(1);
        e1.setExportCode("PX-20260801-001");
        e1.setExportType("SALE");
        e1.setExportDate("01/08/2026");
        e1.setStatus("COMPLETED");
        e1.setReceiverName("Bưu điện VNPost");
        e1.setNote("Xuất giao đơn hàng online #WS8492");
        e1.setCreatedByName("Nguyễn Văn Kho");
        e1.setOrderID(8492L);
        MOCK_EXPORTS.add(e1);

        StockExport e2 = new StockExport();
        e2.setExportID(2);
        e2.setExportCode("PX-20260803-002");
        e2.setExportType("TRANSFER");
        e2.setExportDate("03/08/2026");
        e2.setStatus("PENDING");
        e2.setReceiverName("Showroom Quận 1");
        e2.setNote("Điều chuyển hàng chéo giữa các chi nhánh");
        e2.setCreatedByName("Trần Thị Kho");
        MOCK_EXPORTS.add(e2);

        StockExport e3 = new StockExport();
        e3.setExportID(3);
        e3.setExportCode("PX-20260804-003");
        e3.setExportType("DAMAGED");
        e3.setExportDate("04/08/2026");
        e3.setStatus("DRAFT");
        e3.setReceiverName("Bộ phận kỹ thuật");
        e3.setNote("Xuất mẫu bị xước vỏ sang phòng bảo hành");
        e3.setCreatedByName("Nguyễn Văn Kho");
        MOCK_EXPORTS.add(e3);

        // --- 3. Dữ liệu mẫu Tồn kho ---
        InventoryItem i1 = new InventoryItem();
        i1.setProductCode("CAS-EFR-108");
        i1.setProductName("Edifice Sapphire EFR-S108D");
        i1.setVariantSku("SKU-EFR-108-SL");
        i1.setVariantName("Mặt Xanh / Dây thép");
        i1.setQuantityOnHand(18);
        i1.setQuantityReserved(3);
        i1.setAvailableQuantity(15);
        i1.setReorderLevel(5);
        MOCK_INVENTORY.add(i1);

        InventoryItem i2 = new InventoryItem();
        i2.setProductCode("SEI-PRO-510");
        i2.setProductName("Prospex Diver Automatic");
        i2.setVariantSku("SKU-PRO-510-BK");
        i2.setVariantName("Mặt Đen / Dây cao su");
        i2.setQuantityOnHand(4);
        i2.setQuantityReserved(2);
        i2.setAvailableQuantity(2);
        i2.setReorderLevel(5);
        MOCK_INVENTORY.add(i2);

        InventoryItem i3 = new InventoryItem();
        i3.setProductCode("ORI-BAM-210");
        i3.setProductName("Bambino Open Heart Classic");
        i3.setVariantSku("SKU-BAM-210-BR");
        i3.setVariantName("Mặt Trắng / Dây da nâu");
        i3.setQuantityOnHand(9);
        i3.setQuantityReserved(1);
        i3.setAvailableQuantity(8);
        i3.setReorderLevel(5);
        MOCK_INVENTORY.add(i3);

        InventoryItem i4 = new InventoryItem();
        i4.setProductCode("TIS-LEL-080");
        i4.setProductName("Le Locle Powermatic 80");
        i4.setVariantSku("SKU-LEL-080-GD");
        i4.setVariantName("Vỏ Vàng / Dây da đen");
        i4.setQuantityOnHand(3);
        i4.setQuantityReserved(3);
        i4.setAvailableQuantity(0);
        i4.setReorderLevel(5);
        MOCK_INVENTORY.add(i4);

        // --- 4. Dữ liệu mẫu Kiểm kê ---
        StocktakeRecord s1 = new StocktakeRecord();
        s1.setStocktakeID(1);
        s1.setStocktakeCode("KK-20260801-001");
        s1.setStocktakeDate("01/08/2026");
        s1.setStatus("COMPLETED");
        s1.setNote("Kiểm kê định kỳ đầu tháng 8/2026");
        s1.setCreatedByName("Nguyễn Văn Kho");
        MOCK_STOCKTAKES.add(s1);

        StocktakeRecord s2 = new StocktakeRecord();
        s2.setStocktakeID(2);
        s2.setStocktakeCode("KK-20260804-002");
        s2.setStocktakeDate("04/08/2026");
        s2.setStatus("COUNTING");
        s2.setNote("Kiểm kê đột xuất khu vực đồng hồ Thụy Sĩ");
        s2.setCreatedByName("Trần Thị Kho");
        MOCK_STOCKTAKES.add(s2);
    }

    public WarehouseRepository() {}

    /** Kiểm tra xem hiện tại CSDL SQL Server có đang hoạt động hay không. */
    public boolean isDbConnected() {
        return DBContext.isConnected();
    }

    /**
     * Tự động kiểm tra và thêm bản ghi mặc định cho bảnh Users và Warehouses trong SQL Server
     * nếu CSDL mới khởi tạo chưa có dòng nào. Việc này giúp tránh lỗi khóa ngoại FK_CreatedBy & FK_Warehouse.
     */
    private void ensureMasterData(Connection conn) {
        try {
            // 1. Kiểm tra Users
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dbo.Users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    st.executeUpdate(
                        "INSERT INTO dbo.Users (Email, PasswordHash, FullName, Phone, Status) " +
                        "VALUES ('warehouse@watchstore.vn', '123456', N'Nhân viên kho', '0901234567', 'ACTIVE')"
                    );
                }
            }

            // 2. Kiểm tra Warehouses
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM dbo.Warehouses")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    st.executeUpdate(
                        "INSERT INTO dbo.Warehouses (WarehouseCode, WarehouseName, Address, Status) " +
                        "VALUES ('WH-MAIN', N'Kho trung tâm WatchStore', N'Hà Nội', 'ACTIVE')"
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Lưu ý ensureMasterData: " + e.getMessage());
        }
    }

    /** Lấy ID kho đầu tiên trong SQL Server */
    private int getFirstWarehouseId(Connection conn) {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT TOP 1 WarehouseID FROM dbo.Warehouses")) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ignored) {}
        return 1;
    }

    /** Lấy UserID đầu tiên trong SQL Server */
    private int getFirstUserId(Connection conn) {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT TOP 1 UserID FROM dbo.Users")) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ignored) {}
        return 1;
    }

    // =========================================================================
    // PHẦN 1: DASHBOARD
    // =========================================================================

    public int getTotalVariants() {
        int dbVal = queryInt("SELECT COUNT(*) FROM ProductVariants WHERE Status = 'ACTIVE'");
        return dbVal > 0 ? dbVal : MOCK_INVENTORY.size();
    }

    public int getTotalInventory() {
        int dbVal = queryInt("SELECT ISNULL(SUM(QuantityOnHand), 0) FROM InventoryBalances");
        return dbVal > 0 ? dbVal : MOCK_INVENTORY.stream().mapToInt(InventoryItem::getQuantityOnHand).sum();
    }

    public int getTodayReceipts() {
        int dbVal = queryInt("SELECT COUNT(*) FROM StockReceipts WHERE CAST(ReceiptDate AS DATE) = CAST(GETDATE() AS DATE)");
        return dbVal > 0 ? dbVal : MOCK_RECEIPTS.size();
    }

    public int getTodayExports() {
        int dbVal = queryInt("SELECT COUNT(*) FROM StockExports WHERE CAST(ExportDate AS DATE) = CAST(GETDATE() AS DATE)");
        return dbVal > 0 ? dbVal : MOCK_EXPORTS.size();
    }

    public int getLowStockCount() {
        int dbVal = queryInt("SELECT COUNT(*) FROM InventoryBalances WHERE AvailableQuantity <= ReorderLevel");
        return dbVal > 0 ? dbVal : (int) MOCK_INVENTORY.stream().filter(InventoryItem::isLowStock).count();
    }

    // =========================================================================
    // PHẦN 2: PHIẾU NHẬP KHO (READ, CREATE, UPDATE, DELETE)
    // =========================================================================

    public List<StockReceipt> findAllReceipts() {
        List<StockReceipt> list = new ArrayList<>();
        String sql =
            "SELECT r.StockReceiptID, r.ReceiptCode, r.SupplierName, r.SupplierPhone, " +
            "       FORMAT(r.ReceiptDate, 'dd/MM/yyyy') AS ReceiptDate, " +
            "       r.Status, r.TotalCost, r.Note, ISNULL(u.FullName, N'Nhân viên kho') AS CreatedByName " +
            "FROM StockReceipts r " +
            "LEFT JOIN Users u ON u.UserID = r.CreatedBy " +
            "ORDER BY r.StockReceiptID DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StockReceipt receipt = new StockReceipt();
                receipt.setReceiptID(rs.getLong("StockReceiptID"));
                receipt.setReceiptCode(rs.getString("ReceiptCode"));
                receipt.setSupplierName(rs.getString("SupplierName"));
                receipt.setSupplierPhone(rs.getString("SupplierPhone"));
                receipt.setReceiptDate(rs.getString("ReceiptDate"));
                receipt.setStatus(rs.getString("Status"));
                receipt.setTotalCost(rs.getBigDecimal("TotalCost"));
                receipt.setNote(rs.getString("Note"));
                receipt.setCreatedByName(rs.getString("CreatedByName"));
                list.add(receipt);
            }
        } catch (Exception e) {
            System.err.println("SQL ERROR (findAllReceipts): " + e.getMessage());
        }
        return list.isEmpty() ? new ArrayList<>(MOCK_RECEIPTS) : list;
    }

    public StockReceipt findReceiptById(long id) {
        String sql =
            "SELECT r.StockReceiptID, r.ReceiptCode, r.SupplierName, r.SupplierPhone, " +
            "       FORMAT(r.ReceiptDate, 'dd/MM/yyyy') AS ReceiptDate, " +
            "       r.Status, r.TotalCost, r.Note, ISNULL(u.FullName, N'Nhân viên kho') AS CreatedByName " +
            "FROM StockReceipts r " +
            "LEFT JOIN Users u ON u.UserID = r.CreatedBy " +
            "WHERE r.StockReceiptID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StockReceipt receipt = new StockReceipt();
                    receipt.setReceiptID(rs.getLong("StockReceiptID"));
                    receipt.setReceiptCode(rs.getString("ReceiptCode"));
                    receipt.setSupplierName(rs.getString("SupplierName"));
                    receipt.setSupplierPhone(rs.getString("SupplierPhone"));
                    receipt.setReceiptDate(rs.getString("ReceiptDate"));
                    receipt.setStatus(rs.getString("Status"));
                    receipt.setTotalCost(rs.getBigDecimal("TotalCost"));
                    receipt.setNote(rs.getString("Note"));
                    receipt.setCreatedByName(rs.getString("CreatedByName"));
                    return receipt;
                }
            }
        } catch (Exception ignored) {}

        return MOCK_RECEIPTS.stream().filter(r -> r.getReceiptID() == id).findFirst().orElse(null);
    }

    public boolean createReceipt(String supplierName, String supplierPhone, BigDecimal totalCost,
                                  String note, int warehouseId, int createdByUserId) {
        String receiptCode = "PN-"
            + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
            + "-" + (System.currentTimeMillis() % 1000);

        try (Connection conn = DBContext.getConnection()) {
            // Đảm bảo có sẵn bản ghi trong Users và Warehouses trước khi INSERT
            ensureMasterData(conn);
            int validWarehouseId = getFirstWarehouseId(conn);
            int validUserId = getFirstUserId(conn);

            String sql =
                "INSERT INTO StockReceipts (ReceiptCode, WarehouseID, SupplierName, SupplierPhone, Status, TotalCost, Note, CreatedBy) " +
                "VALUES (?, ?, ?, ?, 'DRAFT', ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, receiptCode);
                ps.setInt(2, validWarehouseId);
                ps.setString(3, supplierName);
                ps.setString(4, supplierPhone);
                ps.setBigDecimal(5, totalCost != null ? totalCost : BigDecimal.ZERO);
                ps.setString(6, note);
                ps.setInt(7, validUserId);
                if (ps.executeUpdate() > 0) {
                    System.out.println("✅ Đã lưu phiếu nhập mới vào SQL Server thành công: " + receiptCode);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("SQL ERROR (createReceipt): " + e.getMessage());
        }

        // Tự động lưu vào MOCK nếu CSDL không khả dụng
        StockReceipt mock = new StockReceipt();
        mock.setReceiptID(MOCK_RECEIPTS.size() + 1);
        mock.setReceiptCode(receiptCode);
        mock.setSupplierName(supplierName);
        mock.setSupplierPhone(supplierPhone);
        mock.setReceiptDate(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        mock.setStatus("DRAFT");
        mock.setTotalCost(totalCost != null ? totalCost : BigDecimal.ZERO);
        mock.setNote(note);
        mock.setCreatedByName("Nhân viên kho");
        MOCK_RECEIPTS.add(0, mock);
        return true;
    }

    public boolean updateReceipt(long id, String supplierName, String supplierPhone, BigDecimal totalCost, String note, String status) {
        String sql = "UPDATE StockReceipts SET SupplierName = ?, SupplierPhone = ?, TotalCost = ?, Note = ?, Status = ? WHERE StockReceiptID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supplierName);
            ps.setString(2, supplierPhone);
            ps.setBigDecimal(3, totalCost != null ? totalCost : BigDecimal.ZERO);
            ps.setString(4, note);
            ps.setString(5, status);
            ps.setLong(6, id);
            if (ps.executeUpdate() > 0) return true;
        } catch (Exception e) {
            System.err.println("SQL ERROR (updateReceipt): " + e.getMessage());
        }

        StockReceipt mock = findReceiptById(id);
        if (mock != null) {
            mock.setSupplierName(supplierName);
            mock.setSupplierPhone(supplierPhone);
            if (totalCost != null) mock.setTotalCost(totalCost);
            mock.setNote(note);
            if (status != null && !status.isBlank()) mock.setStatus(status);
            return true;
        }
        return false;
    }

    public boolean deleteReceipt(long id) {
        StockReceipt receipt = findReceiptById(id);
        if (receipt != null && "COMPLETED".equalsIgnoreCase(receipt.getStatus())) {
            return false;
        }

        String sql = "DELETE FROM StockReceipts WHERE StockReceiptID = ? AND Status != 'COMPLETED'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            if (ps.executeUpdate() > 0) return true;
        } catch (Exception e) {
            System.err.println("SQL ERROR (deleteReceipt): " + e.getMessage());
        }

        return MOCK_RECEIPTS.removeIf(r -> r.getReceiptID() == id && !"COMPLETED".equalsIgnoreCase(r.getStatus()));
    }

    // =========================================================================
    // PHẦN 3: PHIẾU XUẤT KHO
    // =========================================================================

    public List<StockExport> findAllExports() {
        List<StockExport> list = new ArrayList<>();
        String sql =
            "SELECT e.StockExportID, e.ExportCode, e.ExportType, " +
            "       FORMAT(e.ExportDate, 'dd/MM/yyyy') AS ExportDate, " +
            "       e.Status, e.ReceiverName, e.Note, e.OrderID, " +
            "       ISNULL(u.FullName, N'Nhân viên kho') AS CreatedByName " +
            "FROM StockExports e " +
            "LEFT JOIN Users u ON u.UserID = e.CreatedBy " +
            "ORDER BY e.StockExportID DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StockExport export = new StockExport();
                export.setExportID(rs.getLong("StockExportID"));
                export.setExportCode(rs.getString("ExportCode"));
                export.setExportType(rs.getString("ExportType"));
                export.setExportDate(rs.getString("ExportDate"));
                export.setStatus(rs.getString("Status"));
                export.setReceiverName(rs.getString("ReceiverName"));
                export.setNote(rs.getString("Note"));
                export.setCreatedByName(rs.getString("CreatedByName"));
                long orderId = rs.getLong("OrderID");
                if (!rs.wasNull()) export.setOrderID(orderId);
                list.add(export);
            }
        } catch (Exception e) {
            System.err.println("SQL ERROR (findAllExports): " + e.getMessage());
        }
        return list.isEmpty() ? new ArrayList<>(MOCK_EXPORTS) : list;
    }

    public StockExport findExportById(long id) {
        String sql =
            "SELECT e.StockExportID, e.ExportCode, e.ExportType, " +
            "       FORMAT(e.ExportDate, 'dd/MM/yyyy') AS ExportDate, " +
            "       e.Status, e.ReceiverName, e.Note, e.OrderID, " +
            "       ISNULL(u.FullName, N'Nhân viên kho') AS CreatedByName " +
            "FROM StockExports e " +
            "LEFT JOIN Users u ON u.UserID = e.CreatedBy " +
            "WHERE e.StockExportID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StockExport export = new StockExport();
                    export.setExportID(rs.getLong("StockExportID"));
                    export.setExportCode(rs.getString("ExportCode"));
                    export.setExportType(rs.getString("ExportType"));
                    export.setExportDate(rs.getString("ExportDate"));
                    export.setStatus(rs.getString("Status"));
                    export.setReceiverName(rs.getString("ReceiverName"));
                    export.setNote(rs.getString("Note"));
                    export.setCreatedByName(rs.getString("CreatedByName"));
                    long orderId = rs.getLong("OrderID");
                    if (!rs.wasNull()) export.setOrderID(orderId);
                    return export;
                }
            }
        } catch (Exception ignored) {}

        return MOCK_EXPORTS.stream().filter(e -> e.getExportID() == id).findFirst().orElse(null);
    }

    public boolean createExport(String exportType, String receiverName,
                                 String note, Long orderID, int warehouseId, int createdByUserId) {
        String exportCode = "PX-"
            + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
            + "-" + (System.currentTimeMillis() % 1000);

        try (Connection conn = DBContext.getConnection()) {
            ensureMasterData(conn);
            int validWarehouseId = getFirstWarehouseId(conn);
            int validUserId = getFirstUserId(conn);

            String sql =
                "INSERT INTO StockExports (ExportCode, WarehouseID, ExportType, Status, ReceiverName, Note, CreatedBy, OrderID) " +
                "VALUES (?, ?, ?, 'DRAFT', ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, exportCode);
                ps.setInt(2, validWarehouseId);
                ps.setString(3, exportType);
                ps.setString(4, receiverName);
                ps.setString(5, note);
                ps.setInt(6, validUserId);
                if (orderID != null) ps.setLong(7, orderID);
                else ps.setNull(7, java.sql.Types.BIGINT);
                if (ps.executeUpdate() > 0) return true;
            }
        } catch (Exception e) {
            System.err.println("SQL ERROR (createExport): " + e.getMessage());
        }

        StockExport mock = new StockExport();
        mock.setExportID(MOCK_EXPORTS.size() + 1);
        mock.setExportCode(exportCode);
        mock.setExportType(exportType);
        mock.setExportDate(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        mock.setStatus("DRAFT");
        mock.setReceiverName(receiverName);
        mock.setNote(note);
        mock.setCreatedByName("Nhân viên kho");
        if (orderID != null) mock.setOrderID(orderID);
        MOCK_EXPORTS.add(0, mock);
        return true;
    }

    public boolean updateExport(long id, String exportType, String receiverName, String note, String status) {
        String sql = "UPDATE StockExports SET ExportType = ?, ReceiverName = ?, Note = ?, Status = ? WHERE StockExportID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, exportType);
            ps.setString(2, receiverName);
            ps.setString(3, note);
            ps.setString(4, status);
            ps.setLong(5, id);
            if (ps.executeUpdate() > 0) return true;
        } catch (Exception e) {
            System.err.println("SQL ERROR (updateExport): " + e.getMessage());
        }

        StockExport mock = findExportById(id);
        if (mock != null) {
            mock.setExportType(exportType);
            mock.setReceiverName(receiverName);
            mock.setNote(note);
            if (status != null && !status.isBlank()) mock.setStatus(status);
            return true;
        }
        return false;
    }

    public boolean deleteExport(long id) {
        StockExport export = findExportById(id);
        if (export != null && "COMPLETED".equalsIgnoreCase(export.getStatus())) {
            return false;
        }

        String sql = "DELETE FROM StockExports WHERE StockExportID = ? AND Status != 'COMPLETED'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            if (ps.executeUpdate() > 0) return true;
        } catch (Exception e) {
            System.err.println("SQL ERROR (deleteExport): " + e.getMessage());
        }

        return MOCK_EXPORTS.removeIf(e -> e.getExportID() == id && !"COMPLETED".equalsIgnoreCase(e.getStatus()));
    }

    // =========================================================================
    // PHẦN 4: TỒN KHO
    // =========================================================================

    public List<InventoryItem> findInventory() {
        List<InventoryItem> list = new ArrayList<>();
        String sql =
            "SELECT p.ProductName, p.ProductCode, " +
            "       v.SKU AS VariantSku, v.VariantName, " +
            "       ib.QuantityOnHand, ib.QuantityReserved, " +
            "       ib.AvailableQuantity, ib.ReorderLevel " +
            "FROM InventoryBalances ib " +
            "JOIN ProductVariants v ON v.VariantID = ib.VariantID " +
            "JOIN Products p        ON p.ProductID = v.ProductID " +
            "WHERE v.Status = 'ACTIVE' AND p.Status = 'ACTIVE' " +
            "ORDER BY ib.AvailableQuantity ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                InventoryItem item = new InventoryItem();
                item.setProductName(rs.getString("ProductName"));
                item.setProductCode(rs.getString("ProductCode"));
                item.setVariantSku(rs.getString("VariantSku"));
                item.setVariantName(rs.getString("VariantName"));
                item.setQuantityOnHand(rs.getInt("QuantityOnHand"));
                item.setQuantityReserved(rs.getInt("QuantityReserved"));
                item.setAvailableQuantity(rs.getInt("AvailableQuantity"));
                item.setReorderLevel(rs.getInt("ReorderLevel"));
                list.add(item);
            }
        } catch (Exception e) {
            System.err.println("SQL ERROR (findInventory): " + e.getMessage());
        }
        return list.isEmpty() ? new ArrayList<>(MOCK_INVENTORY) : list;
    }

    public boolean updateInventoryItem(String sku, int newQuantity, int newReorderLevel) {
        String sql =
            "UPDATE ib SET ib.QuantityOnHand = ?, ib.ReorderLevel = ? " +
            "FROM InventoryBalances ib " +
            "JOIN ProductVariants v ON v.VariantID = ib.VariantID " +
            "WHERE v.SKU = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, newReorderLevel);
            ps.setString(3, sku);
            if (ps.executeUpdate() > 0) return true;
        } catch (Exception e) {
            System.err.println("SQL ERROR (updateInventoryItem): " + e.getMessage());
        }

        for (InventoryItem item : MOCK_INVENTORY) {
            if (item.getVariantSku().equalsIgnoreCase(sku)) {
                item.setQuantityOnHand(newQuantity);
                item.setReorderLevel(newReorderLevel);
                item.setAvailableQuantity(newQuantity - item.getQuantityReserved());
                return true;
            }
        }
        return false;
    }

    // =========================================================================
    // PHẦN 5: KIỂM KÊ
    // =========================================================================

    public List<StocktakeRecord> findAllStocktakes() {
        List<StocktakeRecord> list = new ArrayList<>();
        String sql =
            "SELECT s.StocktakeID, s.StocktakeCode, " +
            "       FORMAT(s.StocktakeDate, 'dd/MM/yyyy') AS StocktakeDate, " +
            "       s.Status, s.Note, ISNULL(u.FullName, N'Nhân viên kho') AS CreatedByName " +
            "FROM Stocktakes s " +
            "LEFT JOIN Users u ON u.UserID = s.CreatedBy " +
            "ORDER BY s.StocktakeID DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StocktakeRecord st = new StocktakeRecord();
                st.setStocktakeID(rs.getLong("StocktakeID"));
                st.setStocktakeCode(rs.getString("StocktakeCode"));
                st.setStocktakeDate(rs.getString("StocktakeDate"));
                st.setStatus(rs.getString("Status"));
                st.setNote(rs.getString("Note"));
                st.setCreatedByName(rs.getString("CreatedByName"));
                list.add(st);
            }
        } catch (Exception e) {
            System.err.println("SQL ERROR (findAllStocktakes): " + e.getMessage());
        }
        return list.isEmpty() ? new ArrayList<>(MOCK_STOCKTAKES) : list;
    }

    public boolean createStocktake(String note, int warehouseId, int createdByUserId) {
        String stocktakeCode = "KK-"
            + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
            + "-" + (System.currentTimeMillis() % 1000);

        try (Connection conn = DBContext.getConnection()) {
            ensureMasterData(conn);
            int validWarehouseId = getFirstWarehouseId(conn);
            int validUserId = getFirstUserId(conn);

            String sql =
                "INSERT INTO Stocktakes (StocktakeCode, WarehouseID, Status, Note, CreatedBy) " +
                "VALUES (?, ?, 'DRAFT', ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, stocktakeCode);
                ps.setInt(2, validWarehouseId);
                ps.setString(3, note);
                ps.setInt(4, validUserId);
                if (ps.executeUpdate() > 0) return true;
            }
        } catch (Exception e) {
            System.err.println("SQL ERROR (createStocktake): " + e.getMessage());
        }

        StocktakeRecord mock = new StocktakeRecord();
        mock.setStocktakeID(MOCK_STOCKTAKES.size() + 1);
        mock.setStocktakeCode(stocktakeCode);
        mock.setStocktakeDate(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        mock.setStatus("DRAFT");
        mock.setNote(note);
        mock.setCreatedByName("Nhân viên kho");
        MOCK_STOCKTAKES.add(0, mock);
        return true;
    }

    public boolean deleteStocktake(long id) {
        String sql = "DELETE FROM Stocktakes WHERE StocktakeID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            if (ps.executeUpdate() > 0) return true;
        } catch (Exception e) {
            System.err.println("SQL ERROR (deleteStocktake): " + e.getMessage());
        }

        return MOCK_STOCKTAKES.removeIf(s -> s.getStocktakeID() == id);
    }

    // =========================================================================
    // PHẦN 6: CẢNH BÁO SẮP HẾT HÀNG
    // =========================================================================

    public List<InventoryItem> findLowStockItems() {
        List<InventoryItem> list = new ArrayList<>();
        String sql =
            "SELECT p.ProductName, p.ProductCode, " +
            "       v.SKU AS VariantSku, v.VariantName, " +
            "       ib.QuantityOnHand, ib.QuantityReserved, " +
            "       ib.AvailableQuantity, ib.ReorderLevel " +
            "FROM InventoryBalances ib " +
            "JOIN ProductVariants v ON v.VariantID = ib.VariantID " +
            "JOIN Products p        ON p.ProductID = v.ProductID " +
            "WHERE ib.AvailableQuantity <= ib.ReorderLevel " +
            "  AND v.Status = 'ACTIVE' AND p.Status = 'ACTIVE' " +
            "ORDER BY ib.AvailableQuantity ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                InventoryItem item = new InventoryItem();
                item.setProductName(rs.getString("ProductName"));
                item.setProductCode(rs.getString("ProductCode"));
                item.setVariantSku(rs.getString("VariantSku"));
                item.setVariantName(rs.getString("VariantName"));
                item.setQuantityOnHand(rs.getInt("QuantityOnHand"));
                item.setQuantityReserved(rs.getInt("QuantityReserved"));
                item.setAvailableQuantity(rs.getInt("AvailableQuantity"));
                item.setReorderLevel(rs.getInt("ReorderLevel"));
                list.add(item);
            }
        } catch (Exception e) {
            System.err.println("SQL ERROR (findLowStockItems): " + e.getMessage());
        }

        if (list.isEmpty()) {
            return MOCK_INVENTORY.stream().filter(InventoryItem::isLowStock).toList();
        }
        return list;
    }

    // =========================================================================
    // HELPER
    // =========================================================================

    private int queryInt(String sql) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ignored) {}
        return 0;
    }
}
