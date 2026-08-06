package com.watchstore.controller.warehouse;

import com.watchstore.model.StockExport;
import com.watchstore.model.StockReceipt;
import com.watchstore.model.User;
import com.watchstore.model.Variant;
import com.watchstore.repository.ProductRepository;
import com.watchstore.repository.WarehouseRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Controller cho Nhân viên kho — Hỗ trợ đầy đủ luồng Xem, Thêm, Chỉnh Sửa, Xóa (CRUD).
 * URL pattern: /manage/warehouse/*
 */
@WebServlet("/manage/warehouse/*")
public class WarehouseController extends HttpServlet {

    private WarehouseRepository repo;

    @Override
    public void init() {
        repo = new WarehouseRepository();
    }

    // =========================================================================
    // GET — Hiển thị trang & Thực hiện lệnh XÓA (Delete)
    // =========================================================================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        if (path == null) path = "/dashboard";

        switch (path) {

            case "/dashboard":
                showDashboard(req, resp);
                break;

            // --- PHIẾU NHẬP ---
            case "/receipts":
                showReceiptList(req, resp);
                break;

            case "/receipt-create":
                ViewRouter.admin(req, resp, "warehouse/receipt-create", "Tạo phiếu nhập", "warehouse");
                break;

            case "/receipt-edit":
                showReceiptEdit(req, resp);
                break;

            case "/receipt-delete":
                handleDeleteReceipt(req, resp);
                break;

            // --- PHIẾU XUẤT ---
            case "/exports":
                showExportList(req, resp);
                break;

            case "/export-create":
                ViewRouter.admin(req, resp, "warehouse/export-create", "Tạo phiếu xuất", "warehouse");
                break;

            case "/export-edit":
                showExportEdit(req, resp);
                break;

            case "/export-delete":
                handleDeleteExport(req, resp);
                break;

            // --- TỒN KHO ---
            case "/inventory":
                showInventory(req, resp);
                break;

            // --- KIỂM KÊ ---
            case "/stocktake":
                showStocktake(req, resp);
                break;

            case "/stocktake-delete":
                handleDeleteStocktake(req, resp);
                break;

            // --- CẢNH BÁO ---
            case "/alerts":
                showAlerts(req, resp);
                break;

            // --- BIẼN THỂ ---
            case "/variants":
                showVariants(req, resp);
                break;

            case "/variant-create":
                ViewRouter.admin(req, resp, "warehouse/variant-create", "Thêm biến thể", "warehouse");
                break;

            case "/variant-edit":
                showVariantEdit(req, resp);
                break;

            case "/variant-delete":
                handleDeleteVariant(req, resp);
                break;

            // --- THỐNG KÊ ---
            case "/statistics":
                showStatistics(req, resp);
                break;

            // --- IN PHIẼU ---
            case "/receipt-print":
                showReceiptPrint(req, resp);
                break;

            case "/export-print":
                showExportPrint(req, resp);
                break;

            default:
                resp.sendRedirect(req.getContextPath() + "/manage/warehouse/dashboard");
        }
    }

    // =========================================================================
    // POST — Xử lý gửi Form (Thêm mới & Cập nhật)
    // =========================================================================
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        if (path == null) path = "";

        switch (path) {

            case "/receipt-create":
                handleCreateReceipt(req, resp);
                break;

            case "/receipt-edit":
                handleUpdateReceipt(req, resp);
                break;

            case "/export-create":
                handleCreateExport(req, resp);
                break;

            case "/export-edit":
                handleUpdateExport(req, resp);
                break;

            case "/stocktake-create":
                handleCreateStocktake(req, resp);
                break;

            case "/inventory-edit":
                handleUpdateInventory(req, resp);
                break;

            case "/variant-create":
                handleCreateVariant(req, resp);
                break;

            case "/variant-edit":
                handleUpdateVariant(req, resp);
                break;

            default:
                resp.sendRedirect(req.getContextPath() + "/manage/warehouse/dashboard");
        }
    }

    // =========================================================================
    // GET HANDLERS (Hiển thị View)
    // =========================================================================

    private void showDashboard(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("isDbConnected",  repo.isDbConnected());
        req.setAttribute("totalVariants",  repo.getTotalVariants());
        req.setAttribute("totalInventory", repo.getTotalInventory());
        req.setAttribute("todayReceipts",  repo.getTodayReceipts());
        req.setAttribute("todayExports",   repo.getTodayExports());
        req.setAttribute("lowStockCount",  repo.getLowStockCount());
        ViewRouter.admin(req, resp, "warehouse/dashboard", "Tổng quan kho", "warehouse");
    }

    private void showReceiptList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("receipts", repo.findAllReceipts());
        ViewRouter.admin(req, resp, "warehouse/receipt-list", "Phiếu nhập kho", "warehouse");
    }

    private void showReceiptEdit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long id = parseLong(req.getParameter("id"));
        StockReceipt receipt = repo.findReceiptById(id);
        if (receipt == null) {
            resp.sendRedirect(req.getContextPath() + "/manage/warehouse/receipts");
            return;
        }
        req.setAttribute("receipt", receipt);
        ViewRouter.admin(req, resp, "warehouse/receipt-edit", "Chỉnh sửa phiếu nhập", "warehouse");
    }

    private void showExportList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("exports", repo.findAllExports());
        ViewRouter.admin(req, resp, "warehouse/export-list", "Phiếu xuất kho", "warehouse");
    }

    private void showExportEdit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long id = parseLong(req.getParameter("id"));
        StockExport export = repo.findExportById(id);
        if (export == null) {
            resp.sendRedirect(req.getContextPath() + "/manage/warehouse/exports");
            return;
        }
        req.setAttribute("export", export);
        ViewRouter.admin(req, resp, "warehouse/export-edit", "Chỉnh sửa phiếu xuất", "warehouse");
    }

    private void showInventory(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("inventoryItems", repo.findInventory());
        ViewRouter.admin(req, resp, "warehouse/inventory", "Tồn kho", "warehouse");
    }

    private void showStocktake(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("stocktakes", repo.findAllStocktakes());
        ViewRouter.admin(req, resp, "warehouse/stocktake", "Kiểm kê", "warehouse");
    }

    private void showAlerts(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("lowStockItems", repo.findLowStockItems());
        ViewRouter.admin(req, resp, "warehouse/stock-alert", "Cảnh báo tồn kho", "warehouse");
    }

    // =========================================================================
    // POST HANDLERS & DELETE ACTIONS
    // =========================================================================

    private void handleCreateReceipt(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String supplierName    = req.getParameter("supplierName");
        String supplierPhone   = req.getParameter("supplierPhone");
        String supplierEmail   = req.getParameter("supplierEmail");
        String supplierAddress = req.getParameter("supplierAddress");
        String receiptType     = req.getParameter("receiptType");  // NEW, RESTOCK, RETURN, TRANSFER, OTHER
        String receiptDate     = req.getParameter("receiptDate");   // yyyy-MM-dd
        String invoiceNo       = req.getParameter("invoiceNo");     // Số hóa đơn
        String note            = req.getParameter("note");
        BigDecimal totalCost   = parseBigDecimal(req.getParameter("totalCost"));
        int itemCount          = parseInt(req.getParameter("itemCount"));
        int totalQty           = parseInt(req.getParameter("totalQty"));

        // Ghép thêm thông tin vào note nếu có
        StringBuilder noteBuilder = new StringBuilder(note != null ? note : "");
        if (invoiceNo != null && !invoiceNo.isBlank())
            noteBuilder.insert(0, "[HĐ: " + invoiceNo + "] ");
        if (supplierEmail != null && !supplierEmail.isBlank())
            noteBuilder.append(" | Email: ").append(supplierEmail);
        if (supplierAddress != null && !supplierAddress.isBlank())
            noteBuilder.append(" | Địa chỉ: ").append(supplierAddress);

        User user = (User) req.getSession().getAttribute("user");
        int userId = (user != null) ? user.getId() : 1;

        boolean ok = repo.createReceipt(supplierName, supplierPhone, totalCost, noteBuilder.toString(), 1, userId);
        req.getSession().setAttribute("flashMessage",
            ok ? "✅ Tạo phiếu nhập thành công! (" + (receiptType != null ? receiptType : "NEW") + ")" : "❌ Tạo phiếu nhập thất bại!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/receipts");
    }

    private void handleUpdateReceipt(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long id              = parseLong(req.getParameter("id"));
        String supplierName  = req.getParameter("supplierName");
        String supplierPhone = req.getParameter("supplierPhone");
        String note          = req.getParameter("note");
        String status        = req.getParameter("status");
        BigDecimal totalCost = parseBigDecimal(req.getParameter("totalCost"));

        boolean ok = repo.updateReceipt(id, supplierName, supplierPhone, totalCost, note, status);
        req.getSession().setAttribute("flashMessage", ok ? "Cập nhật phiếu nhập thành công!" : "Cập nhật thất bại!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/receipts");
    }

    private void handleDeleteReceipt(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long id = parseLong(req.getParameter("id"));
        boolean ok = repo.deleteReceipt(id);
        req.getSession().setAttribute("flashMessage", ok ? "Đã xóa phiếu nhập!" : "Không thể xóa phiếu đã Hoàn Thành (COMPLETED)!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/receipts");
    }

    private void handleCreateExport(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String exportType    = req.getParameter("exportType");      // SALE, TRANSFER, DAMAGED, OTHER
        String receiverName  = req.getParameter("receiverName");
        String receiverPhone = req.getParameter("receiverPhone");   // SĐT người nhận
        String exportDate    = req.getParameter("exportDate");       // yyyy-MM-dd
        String transferTo    = req.getParameter("transferTo");       // Kho nhận (TRANSFER)
        String note          = req.getParameter("note");
        int itemCount        = parseInt(req.getParameter("itemCount"));
        int totalQty         = parseInt(req.getParameter("totalQty"));

        // Gắn thêm orderID nếu xuất theo đơn hàng
        Long orderIdVal = null;
        String orderIdStr = req.getParameter("orderID");
        if (orderIdStr != null && !orderIdStr.isBlank()) {
            try { orderIdVal = Long.parseLong(orderIdStr.trim()); } catch (NumberFormatException ignored) {}
        }

        // Ghép thêm thông tin vào note
        StringBuilder noteBuilder = new StringBuilder(note != null ? note : "");
        if (receiverPhone != null && !receiverPhone.isBlank())
            noteBuilder.insert(0, "[SĐT: " + receiverPhone + "] ");
        if (transferTo != null && !transferTo.isBlank())
            noteBuilder.append(" | Đến: ").append(transferTo);

        User user = (User) req.getSession().getAttribute("user");
        int userId = (user != null) ? user.getId() : 1;

        boolean ok = repo.createExport(exportType, receiverName, noteBuilder.toString(), orderIdVal, 1, userId);
        req.getSession().setAttribute("flashMessage",
            ok ? "✅ Tạo phiếu xuất thành công! (" + (exportType != null ? exportType : "") + ")" : "❌ Tạo phiếu xuất thất bại!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/exports");
    }

    private void handleUpdateExport(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long id             = parseLong(req.getParameter("id"));
        String exportType   = req.getParameter("exportType");
        String receiverName = req.getParameter("receiverName");
        String note         = req.getParameter("note");
        String status       = req.getParameter("status");

        boolean ok = repo.updateExport(id, exportType, receiverName, note, status);
        req.getSession().setAttribute("flashMessage", ok ? "Cập nhật phiếu xuất thành công!" : "Cập nhật thất bại!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/exports");
    }

    private void handleDeleteExport(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long id = parseLong(req.getParameter("id"));
        boolean ok = repo.deleteExport(id);
        req.getSession().setAttribute("flashMessage", ok ? "Đã xóa phiếu xuất!" : "Không thể xóa phiếu đã Hoàn Thành (COMPLETED)!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/exports");
    }

    private void handleCreateStocktake(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String note = req.getParameter("note");
        User user = (User) req.getSession().getAttribute("user");
        int userId = (user != null) ? user.getId() : 1;

        boolean ok = repo.createStocktake(note, 1, userId);
        req.getSession().setAttribute("flashMessage", ok ? "Tạo phiếu kiểm kê thành công!" : "Tạo thất bại!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/stocktake");
    }

    private void handleDeleteStocktake(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long id = parseLong(req.getParameter("id"));
        boolean ok = repo.deleteStocktake(id);
        req.getSession().setAttribute("flashMessage", ok ? "Đã xóa phiếu kiểm kê!" : "Xóa thất bại!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/stocktake");
    }

    private void handleUpdateInventory(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String sku          = req.getParameter("sku");
        int quantity        = parseInt(req.getParameter("quantityOnHand"));
        int reorderLevel    = parseInt(req.getParameter("reorderLevel"));

        boolean ok = repo.updateInventoryItem(sku, quantity, reorderLevel);
        req.getSession().setAttribute("flashMessage", ok ? "Cập nhật tồn kho thành công!" : "Cập nhật thất bại!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/inventory");
    }

    // =========================================================================
    // HANDLER: BIẼN THỂ
    // =========================================================================

    private void showVariants(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("variants", repo.findAllVariants());
        ViewRouter.admin(req, resp, "warehouse/variant-list", "Quản lý biến thể", "warehouse");
    }

    private void showVariantEdit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long id = parseLong(req.getParameter("id"));
        Variant variant = repo.findVariantById(id);
        if (variant == null) {
            resp.sendRedirect(req.getContextPath() + "/manage/warehouse/variants");
            return;
        }
        req.setAttribute("variant", variant);
        ViewRouter.admin(req, resp, "warehouse/variant-edit", "Sửa biến thể", "warehouse");
    }

    private void handleCreateVariant(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String sku         = req.getParameter("sku");
        String variantName = req.getParameter("variantName");
        String color       = req.getParameter("color");
        String material    = req.getParameter("material");
        BigDecimal price   = parseBigDecimal(req.getParameter("price"));
        int productId      = parseInt(req.getParameter("productId"));

        boolean ok = repo.createVariant(sku, variantName, color, material, price, productId);
        req.getSession().setAttribute("flashMessage",
            ok ? "✅ Thêm biến thể thành công!" : "❌ Thêm biến thể thất bại!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/variants");
    }

    private void handleUpdateVariant(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        long id            = parseLong(req.getParameter("id"));
        String variantName = req.getParameter("variantName");
        String color       = req.getParameter("color");
        String material    = req.getParameter("material");
        BigDecimal price   = parseBigDecimal(req.getParameter("price"));
        String status      = req.getParameter("status");

        boolean ok = repo.updateVariant(id, variantName, color, material, price, status);
        req.getSession().setAttribute("flashMessage",
            ok ? "Cập nhật biến thể thành công!" : "Cập nhật thất bại!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/variants");
    }

    private void handleDeleteVariant(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        long id = parseLong(req.getParameter("id"));
        boolean ok = repo.deleteVariant(id);
        req.getSession().setAttribute("flashMessage",
            ok ? "Đã xóa biến thể!" : "Không thể xóa biến thể còn hàng trong kho!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/variants");
    }

    // =========================================================================
    // HANDLER: THỐNG KÊ & IN PHIẼU
    // =========================================================================

    private void showStatistics(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("receiptsThisMonth", repo.getReceiptsThisMonth());
        req.setAttribute("exportsThisMonth",  repo.getExportsThisMonth());
        req.setAttribute("outOfStockCount",   repo.getOutOfStockCount());
        req.setAttribute("lowStockCount",     repo.getLowStockCount());
        req.setAttribute("recentReceipts",    repo.findRecentReceipts());
        req.setAttribute("recentExports",     repo.findRecentExports());
        ViewRouter.admin(req, resp, "warehouse/statistics", "Thống kê kho", "warehouse");
    }

    private void showReceiptPrint(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long id = parseLong(req.getParameter("id"));
        StockReceipt receipt = repo.findReceiptById(id);
        if (receipt == null) {
            resp.sendRedirect(req.getContextPath() + "/manage/warehouse/receipts");
            return;
        }
        req.setAttribute("receipt", receipt);
        // Forward thẳng JSP in, không qua layout
        req.getRequestDispatcher("/views/warehouse/receipt-print.jsp").forward(req, resp);
    }

    private void showExportPrint(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long id = parseLong(req.getParameter("id"));
        StockExport export = repo.findExportById(id);
        if (export == null) {
            resp.sendRedirect(req.getContextPath() + "/manage/warehouse/exports");
            return;
        }
        req.setAttribute("export", export);
        req.getRequestDispatcher("/views/warehouse/export-print.jsp").forward(req, resp);
    }

    // =========================================================================
    // HELPER
    // =========================================================================

    private long parseLong(String val) {
        try { return Long.parseLong(val); } catch (Exception e) { return 0; }
    }

    private int parseInt(String val) {
        try { return Integer.parseInt(val); } catch (Exception e) { return 0; }
    }

    private BigDecimal parseBigDecimal(String val) {
        try {
            if (val != null && !val.isBlank()) {
                return new BigDecimal(val.replaceAll("[^0-9.]", ""));
            }
        } catch (Exception ignored) {}
        return BigDecimal.ZERO;
    }
}
