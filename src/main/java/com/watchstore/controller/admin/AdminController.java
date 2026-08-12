package com.watchstore.controller.admin;

import com.watchstore.model.Order;
import com.watchstore.repository.*;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * AdminController — xử lý trang tổng quan (Dashboard) cho hệ thống WatchStore.
 */
@WebServlet("/manage/admin/*")
public class AdminController extends HttpServlet {

    private ProductRepository productRepository;
    private BrandRepository brandRepository;
    private VoucherRepository voucherRepository;
    private UserRepository userRepository;
    private CategoryRepository categoryRepository;
    private StatisticRepository statisticRepository;

    private static final Map<String, String[]> PAGES = Map.ofEntries(
        Map.entry("/dashboard", new String[]{"dashboard", "Bảng điều khiển"}),
        Map.entry("/statistics", new String[]{"statistic", "Thống kê"}),
        Map.entry("/reports", new String[]{"report", "Báo cáo"})
    );

    @Override
    public void init() {
        productRepository = (ProductRepository) getServletContext().getAttribute("productRepository");
        brandRepository = (BrandRepository) getServletContext().getAttribute("brandRepository");
        voucherRepository = (VoucherRepository) getServletContext().getAttribute("voucherRepository");
        userRepository = (UserRepository) getServletContext().getAttribute("userRepository");
        categoryRepository = (CategoryRepository) getServletContext().getAttribute("categoryRepository");
        statisticRepository = (StatisticRepository) getServletContext().getAttribute("statisticRepository");
        if (statisticRepository == null) {
            statisticRepository = new StatisticRepositoryImpl();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        if (path != null && !path.equals("/") && !path.equals("/dashboard")) {
            resp.sendRedirect(req.getContextPath() + "/manage/admin" + path);
            return;
        }

        path = "/dashboard";
        String[] page = PAGES.get(path);

        // Thống kê thực tế 100% từ Database
        int totalOrders = statisticRepository.getTotalOrdersCount();
        int pendingOrders = statisticRepository.getPendingOrdersCount();
        BigDecimal totalRevenue = statisticRepository.getTotalRevenue();
        int totalProducts = statisticRepository.getTotalProductsCount();
        int activeProducts = statisticRepository.getActiveProductsCount();
        int totalUsers = statisticRepository.getTotalCustomersCount();
        int activeUsers = statisticRepository.getActiveCustomersCount();
        int lowStockCount = statisticRepository.getLowStockCount();
        int expiringVouchersCount = statisticRepository.getExpiringVouchersCount();
        List<Map<String, Object>> last7DaysSales = statisticRepository.getLast7DaysSales();
        List<Order> recentOrders = statisticRepository.getRecentOrders(8);

        req.setAttribute("totalOrders", totalOrders);
        req.setAttribute("pendingOrders", pendingOrders);
        req.setAttribute("totalRevenue", totalRevenue);
        req.setAttribute("totalProducts", totalProducts);
        req.setAttribute("activeProducts", activeProducts);
        req.setAttribute("totalUsers", totalUsers);
        req.setAttribute("activeUsers", activeUsers);
        req.setAttribute("lowStockCount", lowStockCount);
        req.setAttribute("expiringVouchersCount", expiringVouchersCount);
        req.setAttribute("last7DaysSales", last7DaysSales);
        req.setAttribute("orders", recentOrders);

        int totalBrands = brandRepository != null ? brandRepository.findAll().size() : statisticRepository.getTotalBrandsCount();
        int totalVouchers = voucherRepository != null ? voucherRepository.findAll().size() : statisticRepository.getTotalVouchersCount();
        int totalCategories = categoryRepository != null ? categoryRepository.findAll().size() : statisticRepository.getTotalCategoriesCount();

        req.setAttribute("totalBrands", totalBrands);
        req.setAttribute("totalVouchers", totalVouchers);
        req.setAttribute("totalCategories", totalCategories);

        req.setAttribute("moduleTitle", page[1]);
        req.setAttribute("moduleKicker", "WATCHSTORE ADMIN");
        req.setAttribute("moduleDescription", "Quản lý dữ liệu trong hệ thống");
        req.setAttribute("primaryAction", "");

        ViewRouter.admin(
                req,
                resp,
                "admin/" + page[0],
                page[1],
                "admin"
        );
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/manage/admin/dashboard");
    }
}

