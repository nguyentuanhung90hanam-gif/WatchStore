package com.watchstore.controller.warehouse;

import com.watchstore.repository.InventoryRepository;
import com.watchstore.repository.StockExportRepository;
import com.watchstore.repository.StockReceiptRepository;
import com.watchstore.repository.StocktakeRepository;
import com.watchstore.repository.VariantRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/manage/warehouse/search")
public class WarehouseSearchController extends HttpServlet {
    private InventoryRepository inventoryRepo;
    private StockReceiptRepository receiptRepo;
    private StockExportRepository exportRepo;
    private StocktakeRepository stocktakeRepo;
    private VariantRepository variantRepo;

    @Override
    public void init() {
        inventoryRepo = new InventoryRepository();
        receiptRepo = new StockReceiptRepository();
        exportRepo = new StockExportRepository();
        stocktakeRepo = new StocktakeRepository();
        variantRepo = new VariantRepository();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String keyword = req.getParameter("keyword");
            keyword = keyword == null ? "" : keyword.trim();
            req.setAttribute("keyword", keyword);
            if (keyword.isEmpty()) {
                req.setAttribute("variants", java.util.Collections.emptyList());
                req.setAttribute("inventoryItems", java.util.Collections.emptyList());
                req.setAttribute("receipts", java.util.Collections.emptyList());
                req.setAttribute("exports", java.util.Collections.emptyList());
                req.setAttribute("stocktakes", java.util.Collections.emptyList());
                req.setAttribute("transactions", java.util.Collections.emptyList());
            } else {
                req.setAttribute("variants", variantRepo.search(keyword, null));
                req.setAttribute("inventoryItems", inventoryRepo.search(keyword, null));
                req.setAttribute("receipts", receiptRepo.search(keyword, null, null));
                req.setAttribute("exports", exportRepo.search(keyword, null, null));
                req.setAttribute("stocktakes", stocktakeRepo.search(keyword, null, null));
                req.setAttribute("transactions", inventoryRepo.searchTransactions(keyword));
            }
            req.setAttribute("cp", req.getContextPath());
            ViewRouter.admin(req, resp, "warehouse/search", "Tra cứu thông tin kho", "warehouse");
        } catch (Exception e) {
            req.getSession().setAttribute("errorMsg", getErrorMessage(e));
            resp.sendRedirect(req.getContextPath() + "/manage/warehouse/dashboard");
        }
    }

    private String getErrorMessage(Exception e) {
        return e.getMessage() == null || e.getMessage().isBlank()
                ? "Không thể thực hiện tra cứu kho."
                : e.getMessage();
    }
}
