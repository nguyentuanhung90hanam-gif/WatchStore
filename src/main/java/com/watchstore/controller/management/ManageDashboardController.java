package com.watchstore.controller.management;

import com.watchstore.config.DBContext;
import com.watchstore.model.User;
import com.watchstore.repository.*;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/manage/dashboard")
public class ManageDashboardController extends HttpServlet {
    private StatisticRepository statisticRepository;
    private InventoryRepository inventoryRepository;
    private WarrantyRepository warrantyRepository;
    private StockReceiptRepository stockReceiptRepository;
    private StockExportRepository stockExportRepository;
    private StocktakeRepository stocktakeRepository;

    @Override
    public void init() throws ServletException {
        statisticRepository = new StatisticRepositoryImpl();
        inventoryRepository = new InventoryRepository();
        warrantyRepository = new WarrantyRepository();
        stockReceiptRepository = new StockReceiptRepository();
        stockExportRepository = new StockExportRepository();
        stocktakeRepository = new StocktakeRepository();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        boolean hasDashboardView = user.hasPermission("DASHBOARD_VIEW") || user.getRole().name().equals("ADMIN");
        if (!hasDashboardView) {
            req.getSession().setAttribute("flash", "Bạn không có quyền xem Dashboard.");
            resp.sendRedirect(req.getContextPath() + "/page/home");
            return;
        }

        // Shared / ADMIN Data
        req.setAttribute("todayRevenue", getTodayRevenue());
        req.setAttribute("todayOrders", getTodayOrdersCount());
        req.setAttribute("pendingOrders", statisticRepository.getPendingOrdersCount());
        req.setAttribute("newCustomersToday", getNewCustomersToday());
        req.setAttribute("topProducts", statisticRepository.getTopSellingProducts());

        // SALES Data
        req.setAttribute("totalRevenue", statisticRepository.getTotalRevenue());
        req.setAttribute("totalCustomers", statisticRepository.getTotalCustomersCount());
        req.setAttribute("totalReturns", 0); // TODO: Return module not fully implemented
        req.setAttribute("activeWarranties", warrantyRepository.countByStatus("Đang bảo hành"));

        // WAREHOUSE Data
        try {
            req.setAttribute("totalInventory", inventoryRepository.getTotalQuantityOnHand());
            req.setAttribute("lowStockAlerts", inventoryRepository.getLowStockAlertCount());
            req.setAttribute("outOfStockCount", inventoryRepository.getOutOfStockCount());
            req.setAttribute("stockReceipts", stockReceiptRepository.findAll().size());
            req.setAttribute("stockExports", stockExportRepository.findAll().size());
            req.setAttribute("stocktakes", stocktakeRepository.findAll().size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        ViewRouter.admin(req, resp, "manage/dashboard", "Tổng quan", "dashboard");
    }

    private BigDecimal getTodayRevenue() {
        String sql = "SELECT ISNULL(SUM(TotalAmount), 0) FROM Orders WHERE CAST(CreatedAt AS DATE) = CAST(GETDATE() AS DATE) AND OrderStatus NOT IN ('CANCELLED', 'RETURNED')";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (Exception e) { e.printStackTrace(); }
        return BigDecimal.ZERO;
    }

    private int getTodayOrdersCount() {
        String sql = "SELECT COUNT(*) FROM Orders WHERE CAST(CreatedAt AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    private int getNewCustomersToday() {
        String sql = "SELECT COUNT(*) FROM Users WHERE CAST(CreatedAt AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }
}
