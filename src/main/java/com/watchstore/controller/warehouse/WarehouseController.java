package com.watchstore.controller.warehouse;

import com.watchstore.model.StockExport;
import com.watchstore.model.StockExportItem;
import com.watchstore.model.StockReceipt;
import com.watchstore.model.StockReceiptItem;
import com.watchstore.model.Stocktake;
import com.watchstore.model.StocktakeItem;
import com.watchstore.model.User;
import com.watchstore.repository.InventoryRepository;
import com.watchstore.repository.StockExportRepository;
import com.watchstore.repository.StockReceiptRepository;
import com.watchstore.repository.StocktakeRepository;
import com.watchstore.repository.VariantRepository;
import com.watchstore.util.PdfGenerator;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/manage/warehouse/*")
public class WarehouseController extends HttpServlet {

    private InventoryRepository inventoryRepo;
    private VariantRepository variantRepo;
    private StockReceiptRepository receiptRepo;
    private StockExportRepository exportRepo;
    private StocktakeRepository stocktakeRepo;

    private static final Map<String, String[]> PAGES = Map.ofEntries(
        Map.entry("/dashboard",      new String[]{"dashboard",         "Tổng quan kho"}),
        Map.entry("/receipts",       new String[]{"receipt-list",      "Phiếu nhập kho"}),
        Map.entry("/receipt-create", new String[]{"receipt-create",    "Tạo phiếu nhập"}),
        Map.entry("/receipt-detail", new String[]{"receipt-detail",    "Chi tiết phiếu nhập"}),
        Map.entry("/exports",        new String[]{"export-list",       "Phiếu xuất kho"}),
        Map.entry("/export-create",  new String[]{"export-create",     "Tạo phiếu xuất"}),
        Map.entry("/export-detail",  new String[]{"export-detail",     "Chi tiết phiếu xuất"}),
        Map.entry("/inventory",      new String[]{"inventory",         "Tồn kho"}),
        Map.entry("/transactions",   new String[]{"transaction-list",  "Lịch sử biến động"}),
        Map.entry("/stocktake",      new String[]{"stocktake",         "Kiểm kê"}),
        Map.entry("/stocktake-create",new String[]{"stocktake-create", "Tạo phiếu kiểm kê"}),
        Map.entry("/stocktake-detail",new String[]{"stocktake-detail", "Chi tiết kiểm kê"}),
        Map.entry("/variants",       new String[]{"variant",           "Biến thể sản phẩm"}),
        Map.entry("/alerts",         new String[]{"stock-alert",       "Cảnh báo tồn kho"})
    );

    @Override
    public void init() {
        inventoryRepo  = new InventoryRepository();
        variantRepo    = new VariantRepository();
        receiptRepo    = new StockReceiptRepository();
        exportRepo     = new StockExportRepository();
        stocktakeRepo  = new StocktakeRepository();
    }

    // ─────────────────────────────────────────────────────────
    //  doGet
    // ─────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo() == null ? "/dashboard" : req.getPathInfo();
        String[] page = PAGES.getOrDefault(path, PAGES.get("/dashboard"));

        switch (path) {
            case "/inventory":
                req.setAttribute("inventoryItems", inventoryRepo.findAll());
                break;

            case "/transactions":
                req.setAttribute("transactions", inventoryRepo.findAllTransactions());
                break;

            case "/variants":
                req.setAttribute("variants", variantRepo.findAll());
                break;

            case "/alerts":
                req.setAttribute("lowStockItems", inventoryRepo.findLowStock());
                break;

            case "/receipts":
                req.setAttribute("receipts", receiptRepo.findAll());
                break;

            case "/receipt-create":
                req.setAttribute("warehouses", inventoryRepo.findAllWarehouses());
                req.setAttribute("variants", variantRepo.findAll());
                break;

            case "/receipt-detail":
                if (req.getParameter("id") != null) {
                    long id = Long.parseLong(req.getParameter("id"));
                    req.setAttribute("receipt", receiptRepo.findById(id));
                    req.setAttribute("variants", variantRepo.findAll());
                }
                break;

            case "/receipt-pdf":
                if (req.getParameter("id") != null) {
                    long id = Long.parseLong(req.getParameter("id"));
                    StockReceipt receipt = receiptRepo.findById(id);
                    if (receipt != null) {
                        resp.setContentType("application/pdf");
                        resp.setHeader("Content-Disposition", "attachment; filename=\"Receipt_" + receipt.getReceiptCode() + ".pdf\"");
                        try {
                            PdfGenerator.generateReceiptPdf(receipt, resp.getOutputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }
                resp.sendRedirect(req.getContextPath() + "/manage/warehouse/receipts");
                return;

            case "/exports":
                req.setAttribute("exports", exportRepo.findAll());
                break;

            case "/export-create":
                req.setAttribute("warehouses", inventoryRepo.findAllWarehouses());
                req.setAttribute("variants", variantRepo.findAll());
                break;

            case "/export-detail":
                if (req.getParameter("id") != null) {
                    long id = Long.parseLong(req.getParameter("id"));
                    req.setAttribute("export", exportRepo.findById(id));
                    req.setAttribute("variants", variantRepo.findAll());
                }
                break;

            case "/export-pdf":
                if (req.getParameter("id") != null) {
                    long id = Long.parseLong(req.getParameter("id"));
                    StockExport export = exportRepo.findById(id);
                    if (export != null) {
                        resp.setContentType("application/pdf");
                        resp.setHeader("Content-Disposition", "attachment; filename=\"Export_" + export.getExportCode() + ".pdf\"");
                        try {
                            PdfGenerator.generateExportPdf(export, resp.getOutputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }
                resp.sendRedirect(req.getContextPath() + "/manage/warehouse/exports");
                return;

            case "/stocktake":
                req.setAttribute("stocktakes", stocktakeRepo.findAll());
                break;

            case "/stocktake-create":
                req.setAttribute("warehouses", inventoryRepo.findAllWarehouses());
                req.setAttribute("variants", variantRepo.findAll());
                break;

            case "/stocktake-detail":
                if (req.getParameter("id") != null) {
                    long id = Long.parseLong(req.getParameter("id"));
                    req.setAttribute("stocktake", stocktakeRepo.findById(id));
                    req.setAttribute("variants", variantRepo.findAll());
                }
                break;

            case "/dashboard":
            default:
                req.setAttribute("totalQuantity",  inventoryRepo.getTotalQuantityOnHand());
                req.setAttribute("lowStockCount",  inventoryRepo.getLowStockAlertCount());
                break;
        }

        req.setAttribute("cp", req.getContextPath());
        req.setAttribute("moduleTitle", page[1]);
        ViewRouter.admin(req, resp, "warehouse/" + page[0], page[1], "warehouse");
    }

    // ─────────────────────────────────────────────────────────
    //  doPost
    // ─────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo() == null ? "" : req.getPathInfo();
        int userId = getCurrentUserId(req);

        try {
            switch (path) {

                // ── RECEIPT ──────────────────────────────────
                case "/receipt-create":      handleReceiptCreate(req, userId, resp); return;
                case "/receipt-add-item":    handleReceiptAddItem(req, userId, resp); return;
                case "/receipt-update-item": handleReceiptUpdateItem(req, resp); return;
                case "/receipt-delete-item": handleReceiptDeleteItem(req, resp); return;
                case "/receipt-submit":      handleReceiptSubmit(req, resp); return;
                case "/receipt-approve":     handleReceiptApprove(req, userId, resp); return;
                case "/receipt-cancel":      handleReceiptCancel(req, resp); return;

                // ── EXPORT ───────────────────────────────────
                case "/export-create":       handleExportCreate(req, userId, resp); return;
                case "/export-add-item":     handleExportAddItem(req, resp); return;
                case "/export-update-item":  handleExportUpdateItem(req, resp); return;
                case "/export-delete-item":  handleExportDeleteItem(req, resp); return;
                case "/export-submit":       handleExportSubmit(req, resp); return;
                case "/export-approve":      handleExportApprove(req, userId, resp); return;
                case "/export-cancel":       handleExportCancel(req, resp); return;

                // ── STOCKTAKE ────────────────────────────────
                case "/stocktake-create":      handleStocktakeCreate(req, userId, resp); return;
                case "/stocktake-add-item":    handleStocktakeAddItem(req, resp); return;
                case "/stocktake-update-item": handleStocktakeUpdateItem(req, resp); return;
                case "/stocktake-delete-item": handleStocktakeDeleteItem(req, resp); return;
                case "/stocktake-submit":      handleStocktakeSubmit(req, resp); return;
                case "/stocktake-approve":     handleStocktakeApprove(req, userId, resp); return;
                case "/stocktake-cancel":      handleStocktakeCancel(req, resp); return;

                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("errorMsg", e.getMessage());
            String redirect = req.getContextPath() + "/manage/warehouse" + path;
            
            String idParam = req.getParameter("receiptId");
            if (idParam == null) idParam = req.getParameter("exportId");
            if (idParam == null) idParam = req.getParameter("stocktakeId");

            if (idParam != null && !idParam.isEmpty()) {
                if (path.startsWith("/receipt")) {
                    redirect = req.getContextPath() + "/manage/warehouse/receipt-detail?id=" + idParam;
                } else if (path.startsWith("/export")) {
                    redirect = req.getContextPath() + "/manage/warehouse/export-detail?id=" + idParam;
                } else if (path.startsWith("/stocktake")) {
                    redirect = req.getContextPath() + "/manage/warehouse/stocktake-detail?id=" + idParam;
                }
            }
            resp.sendRedirect(redirect);
        }
    }

    // ─────────────────────────────────────────────────────────
    //  RECEIPT handlers
    // ─────────────────────────────────────────────────────────

    private void handleReceiptCreate(HttpServletRequest req, int userId, HttpServletResponse resp) throws Exception {
        int warehouseId      = Integer.parseInt(req.getParameter("warehouseId"));
        String supplierName  = req.getParameter("supplierName");
        String supplierPhone = req.getParameter("supplierPhone");
        String note          = req.getParameter("note");
        String[] variantIds  = req.getParameterValues("variantIds");
        String[] quantities  = req.getParameterValues("quantities");
        String[] unitCosts   = req.getParameterValues("unitCosts");

        if (variantIds == null || variantIds.length == 0) throw new Exception("Phiếu nhập phải có ít nhất một sản phẩm.");

        StockReceipt receipt = new StockReceipt();
        receipt.setReceiptCode("REC-" + System.currentTimeMillis());
        receipt.setWarehouseId(warehouseId);
        receipt.setSupplierName(supplierName);
        receipt.setSupplierPhone(supplierPhone);
        receipt.setNote(note);
        receipt.setCreatedBy(userId);

        BigDecimal totalCost = BigDecimal.ZERO;
        List<StockReceiptItem> items = new ArrayList<>();
        for (int i = 0; i < variantIds.length; i++) {
            if (variantIds[i] == null || variantIds[i].trim().isEmpty()) continue;
            StockReceiptItem item = new StockReceiptItem();
            item.setVariantId(Integer.parseInt(variantIds[i]));
            int qty = Integer.parseInt(quantities[i]);
            item.setQuantity(qty);
            BigDecimal cost = new BigDecimal(unitCosts[i]);
            item.setUnitCost(cost);
            totalCost = totalCost.add(cost.multiply(new BigDecimal(qty)));
            items.add(item);
        }
        receipt.setTotalCost(totalCost);
        receipt.setItems(items);

        long receiptId = receiptRepo.createDraft(receipt);
        req.getSession().setAttribute("successMsg", "Tạo phiếu nhập nháp thành công!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/receipt-detail?id=" + receiptId);
    }

    private void handleReceiptAddItem(HttpServletRequest req, int userId, HttpServletResponse resp) throws Exception {
        long receiptId  = Long.parseLong(req.getParameter("receiptId"));
        StockReceiptItem item = new StockReceiptItem();
        item.setVariantId(Integer.parseInt(req.getParameter("variantId")));
        item.setQuantity(Integer.parseInt(req.getParameter("quantity")));
        item.setUnitCost(new BigDecimal(req.getParameter("unitCost")));
        receiptRepo.addItem(receiptId, item);
        req.getSession().setAttribute("successMsg", "Thêm sản phẩm thành công.");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/receipt-detail?id=" + receiptId);
    }

    private void handleReceiptUpdateItem(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        long itemId = Long.parseLong(req.getParameter("itemId"));
        long receiptId = Long.parseLong(req.getParameter("receiptId"));
        receiptRepo.updateItem(itemId, Integer.parseInt(req.getParameter("quantity")), new BigDecimal(req.getParameter("unitCost")));
        req.getSession().setAttribute("successMsg", "Cập nhật thành công.");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/receipt-detail?id=" + receiptId);
    }

    private void handleReceiptDeleteItem(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        long receiptId = Long.parseLong(req.getParameter("receiptId"));
        receiptRepo.deleteItem(Long.parseLong(req.getParameter("itemId")));
        req.getSession().setAttribute("successMsg", "Đã xóa sản phẩm.");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/receipt-detail?id=" + receiptId);
    }

    private void handleReceiptSubmit(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        long receiptId = Long.parseLong(req.getParameter("receiptId"));
        receiptRepo.submitForApproval(receiptId);
        req.getSession().setAttribute("successMsg", "Đã gửi phiếu chờ duyệt.");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/receipt-detail?id=" + receiptId);
    }

    private void handleReceiptApprove(HttpServletRequest req, int userId, HttpServletResponse resp) throws Exception {
        long receiptId = Long.parseLong(req.getParameter("receiptId"));
        receiptRepo.approve(receiptId, userId);
        req.getSession().setAttribute("successMsg", "Phiếu nhập đã được duyệt và tồn kho đã cập nhật!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/receipt-detail?id=" + receiptId);
    }

    private void handleReceiptCancel(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        long receiptId = Long.parseLong(req.getParameter("receiptId"));
        receiptRepo.cancel(receiptId);
        req.getSession().setAttribute("successMsg", "Phiếu đã bị hủy.");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/receipt-detail?id=" + receiptId);
    }

    // ─────────────────────────────────────────────────────────
    //  EXPORT handlers
    // ─────────────────────────────────────────────────────────

    private void handleExportCreate(HttpServletRequest req, int userId, HttpServletResponse resp) throws Exception {
        int warehouseId      = Integer.parseInt(req.getParameter("warehouseId"));
        String[] variantIds  = req.getParameterValues("variantIds");
        String[] quantities  = req.getParameterValues("quantities");

        if (variantIds == null || variantIds.length == 0) throw new Exception("Phiếu xuất phải có ít nhất một sản phẩm.");

        StockExport export = new StockExport();
        export.setExportCode("EXP-" + System.currentTimeMillis());
        export.setWarehouseId(warehouseId);
        export.setExportType(req.getParameter("exportType"));
        export.setReceiverName(req.getParameter("receiverName"));
        export.setNote(req.getParameter("note"));
        export.setCreatedBy(userId);
        
        String orderIdStr = req.getParameter("orderId");
        if (orderIdStr != null && !orderIdStr.trim().isEmpty()) {
            export.setOrderId(Long.parseLong(orderIdStr));
        }

        List<StockExportItem> items = new ArrayList<>();
        for (int i = 0; i < variantIds.length; i++) {
            if (variantIds[i] == null || variantIds[i].trim().isEmpty()) continue;
            StockExportItem item = new StockExportItem();
            item.setVariantId(Integer.parseInt(variantIds[i]));
            item.setQuantity(Integer.parseInt(quantities[i]));
            items.add(item);
        }
        export.setItems(items);

        long exportId = exportRepo.createDraft(export);
        req.getSession().setAttribute("successMsg", "Tạo phiếu xuất nháp thành công!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/export-detail?id=" + exportId);
    }

    private void handleExportAddItem(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        long exportId = Long.parseLong(req.getParameter("exportId"));
        StockExportItem item = new StockExportItem();
        item.setVariantId(Integer.parseInt(req.getParameter("variantId")));
        item.setQuantity(Integer.parseInt(req.getParameter("quantity")));
        exportRepo.addItem(exportId, item);
        req.getSession().setAttribute("successMsg", "Thêm sản phẩm thành công.");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/export-detail?id=" + exportId);
    }

    private void handleExportUpdateItem(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        long exportId = Long.parseLong(req.getParameter("exportId"));
        exportRepo.updateItem(Long.parseLong(req.getParameter("itemId")), Integer.parseInt(req.getParameter("quantity")));
        req.getSession().setAttribute("successMsg", "Cập nhật thành công.");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/export-detail?id=" + exportId);
    }

    private void handleExportDeleteItem(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        long exportId = Long.parseLong(req.getParameter("exportId"));
        exportRepo.deleteItem(Long.parseLong(req.getParameter("itemId")));
        req.getSession().setAttribute("successMsg", "Đã xóa sản phẩm.");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/export-detail?id=" + exportId);
    }

    private void handleExportSubmit(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        long exportId = Long.parseLong(req.getParameter("exportId"));
        exportRepo.submitForApproval(exportId);
        req.getSession().setAttribute("successMsg", "Đã gửi phiếu chờ duyệt.");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/export-detail?id=" + exportId);
    }

    private void handleExportApprove(HttpServletRequest req, int userId, HttpServletResponse resp) throws Exception {
        long exportId = Long.parseLong(req.getParameter("exportId"));
        exportRepo.approve(exportId, userId);
        req.getSession().setAttribute("successMsg", "Phiếu xuất đã được duyệt và tồn kho đã cập nhật!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/export-detail?id=" + exportId);
    }

    private void handleExportCancel(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        long exportId = Long.parseLong(req.getParameter("exportId"));
        exportRepo.cancel(exportId);
        req.getSession().setAttribute("successMsg", "Phiếu đã bị hủy.");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/export-detail?id=" + exportId);
    }

    // ─────────────────────────────────────────────────────────
    //  STOCKTAKE handlers
    // ─────────────────────────────────────────────────────────

    private void handleStocktakeCreate(HttpServletRequest req, int userId, HttpServletResponse resp) throws Exception {
        int warehouseId         = Integer.parseInt(req.getParameter("warehouseId"));
        String note             = req.getParameter("note");
        String[] variantIds     = req.getParameterValues("variantIds");
        String[] actualQuantities = req.getParameterValues("actualQuantities");

        if (variantIds == null || variantIds.length == 0) throw new Exception("Phiếu kiểm kê phải có ít nhất một sản phẩm.");

        Stocktake stocktake = new Stocktake();
        stocktake.setStocktakeCode("STK-" + System.currentTimeMillis());
        stocktake.setWarehouseId(warehouseId);
        stocktake.setNote(note);
        stocktake.setCreatedBy(userId);

        List<StocktakeItem> items = new ArrayList<>();
        for (int i = 0; i < variantIds.length; i++) {
            if (variantIds[i] == null || variantIds[i].trim().isEmpty()) continue;
            StocktakeItem item = new StocktakeItem();
            item.setVariantId(Integer.parseInt(variantIds[i]));
            item.setActualQuantity(Integer.parseInt(actualQuantities[i]));
            items.add(item);
        }
        stocktake.setItems(items);

        long stocktakeId = stocktakeRepo.createDraft(stocktake);
        req.getSession().setAttribute("successMsg", "Tạo phiếu kiểm kê nháp thành công!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/stocktake-detail?id=" + stocktakeId);
    }

    private void handleStocktakeAddItem(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        long stocktakeId = Long.parseLong(req.getParameter("stocktakeId"));
        StocktakeItem item = new StocktakeItem();
        item.setVariantId(Integer.parseInt(req.getParameter("variantId")));
        item.setActualQuantity(Integer.parseInt(req.getParameter("actualQuantity")));
        stocktakeRepo.addItem(stocktakeId, item);
        req.getSession().setAttribute("successMsg", "Thêm sản phẩm thành công.");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/stocktake-detail?id=" + stocktakeId);
    }

    private void handleStocktakeUpdateItem(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        long stocktakeId = Long.parseLong(req.getParameter("stocktakeId"));
        stocktakeRepo.updateItem(Long.parseLong(req.getParameter("itemId")), Integer.parseInt(req.getParameter("actualQuantity")));
        req.getSession().setAttribute("successMsg", "Cập nhật số lượng kiểm kê thành công.");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/stocktake-detail?id=" + stocktakeId);
    }

    private void handleStocktakeDeleteItem(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        long stocktakeId = Long.parseLong(req.getParameter("stocktakeId"));
        stocktakeRepo.deleteItem(Long.parseLong(req.getParameter("itemId")));
        req.getSession().setAttribute("successMsg", "Đã xóa sản phẩm khỏi phiếu.");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/stocktake-detail?id=" + stocktakeId);
    }

    private void handleStocktakeSubmit(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        long stocktakeId = Long.parseLong(req.getParameter("stocktakeId"));
        stocktakeRepo.submitForApproval(stocktakeId);
        req.getSession().setAttribute("successMsg", "Đã gửi phiếu kiểm kê chờ duyệt.");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/stocktake-detail?id=" + stocktakeId);
    }

    private void handleStocktakeApprove(HttpServletRequest req, int userId, HttpServletResponse resp) throws Exception {
        long stocktakeId = Long.parseLong(req.getParameter("stocktakeId"));
        stocktakeRepo.approve(stocktakeId, userId);
        req.getSession().setAttribute("successMsg", "Phiếu kiểm kê đã hoàn tất và tồn kho đã được điều chỉnh!");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/stocktake-detail?id=" + stocktakeId);
    }

    private void handleStocktakeCancel(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        long stocktakeId = Long.parseLong(req.getParameter("stocktakeId"));
        stocktakeRepo.cancel(stocktakeId);
        req.getSession().setAttribute("successMsg", "Phiếu kiểm kê đã bị hủy.");
        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/stocktake-detail?id=" + stocktakeId);
    }

    // ─────────────────────────────────────────────────────────
    //  Helper
    // ─────────────────────────────────────────────────────────
    private int getCurrentUserId(HttpServletRequest req) {
        Object userObj = req.getSession().getAttribute("user");
        if (userObj instanceof User) {
            return ((User) userObj).getId();
        }
        return 1;
    }
}
