package com.watchstore.controller.warehouse;

import com.watchstore.model.InventoryTransaction;
import com.watchstore.model.StockExport;
import com.watchstore.model.StockReceipt;
import com.watchstore.repository.InventoryRepository;
import com.watchstore.repository.StockExportRepository;
import com.watchstore.repository.StockReceiptRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/manage/warehouse/reports")
public class WarehouseReportController extends HttpServlet {
    private InventoryRepository inventoryRepo;
    private StockReceiptRepository receiptRepo;
    private StockExportRepository exportRepo;

    @Override
    public void init() {
        inventoryRepo = new InventoryRepository();
        receiptRepo = new StockReceiptRepository();
        exportRepo = new StockExportRepository();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String type = req.getParameter("type");
            if (type == null || type.isBlank()) type = "inventory";
            LocalDate from = parseDate(req.getParameter("from"));
            LocalDate to = parseDate(req.getParameter("to"));
            Integer warehouseId = parsePositiveInt(req.getParameter("warehouseId"));

            req.setAttribute("warehouses", inventoryRepo.findAllWarehouses());
            req.setAttribute("from", req.getParameter("from"));
            req.setAttribute("to", req.getParameter("to"));
            req.setAttribute("selectedType", type);
            req.setAttribute("selectedWarehouseId", warehouseId);

            switch (type) {
                case "receipts":
                    req.setAttribute("reportReceipts", filterReceipts(receiptRepo.findAll(), from, to, warehouseId));
                    break;
                case "exports":
                    req.setAttribute("reportExports", filterExports(exportRepo.findAll(), from, to, warehouseId));
                    break;
                case "damaged":
                    req.setAttribute("reportDamaged", filterTransactions(inventoryRepo.findAllTransactions(), from, to, warehouseId));
                    break;
                default:
                    req.setAttribute("reportInventory", inventoryRepo.findInventoryForReport());
                    type = "inventory";
                    req.setAttribute("selectedType", type);
                    break;
            }

            req.setAttribute("cp", req.getContextPath());
            ViewRouter.admin(req, resp, "warehouse/reports", "Báo cáo kho", "warehouse");
        } catch (Exception e) {
            req.getSession().setAttribute("errorMsg", getErrorMessage(e));
            resp.sendRedirect(req.getContextPath() + "/manage/warehouse/dashboard");
        }
    }

    private List<StockReceipt> filterReceipts(List<StockReceipt> source, LocalDate from, LocalDate to, Integer warehouseId) {
        return source.stream().filter(r -> "COMPLETED".equalsIgnoreCase(r.getStatus()))
                .filter(r -> warehouseId == null || r.getWarehouseId() == warehouseId)
                .filter(r -> inRange(r.getReceiptDate(), from, to)).collect(Collectors.toList());
    }

    private List<StockExport> filterExports(List<StockExport> source, LocalDate from, LocalDate to, Integer warehouseId) {
        return source.stream().filter(r -> "COMPLETED".equalsIgnoreCase(r.getStatus()))
                .filter(r -> warehouseId == null || r.getWarehouseId() == warehouseId)
                .filter(r -> inRange(r.getExportDate(), from, to)).collect(Collectors.toList());
    }

    private List<InventoryTransaction> filterTransactions(List<InventoryTransaction> source, LocalDate from, LocalDate to, Integer warehouseId) {
        return source.stream().filter(r -> "DAMAGED_OUT".equalsIgnoreCase(r.getTransactionType()))
                .filter(r -> warehouseId == null || r.getWarehouseId() == warehouseId)
                .filter(r -> inRange(r.getCreatedAt(), from, to)).collect(Collectors.toList());
    }

    private boolean inRange(LocalDateTime value, LocalDate from, LocalDate to) {
        if (value == null) return false;
        if (from != null && value.toLocalDate().isBefore(from)) return false;
        return to == null || !value.toLocalDate().isAfter(to);
    }

    private LocalDate parseDate(String value) {
        try { return value == null || value.isBlank() ? null : LocalDate.parse(value); }
        catch (Exception e) { return null; }
    }

    private Integer parsePositiveInt(String value) {
        try { int n = Integer.parseInt(value); return n > 0 ? n : null; }
        catch (Exception e) { return null; }
    }

    private String getErrorMessage(Exception e) {
        return e.getMessage() == null || e.getMessage().isBlank() ? "Không thể tải báo cáo kho." : e.getMessage();
    }
}
