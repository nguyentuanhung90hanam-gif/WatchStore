package com.watchstore.controller.admin;

import com.watchstore.repository.StatisticRepository;
import com.watchstore.repository.StatisticRepositoryImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet(urlPatterns = {"/manage/admin/statistics", "/manage/admin/statistics/*"})
public class StatisticController extends HttpServlet {

    private StatisticRepository statisticRepository;

    @Override
    public void init() {
        statisticRepository = (StatisticRepository) getServletContext().getAttribute("statisticRepository");
        if (statisticRepository == null) {
            statisticRepository = new StatisticRepositoryImpl();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setAttribute("adminArea", "admin");
        req.setAttribute("tableKind", "statistics");
        req.setAttribute("pageTitle", "Thống kê hệ thống");
        req.setAttribute("moduleTitle", "Phân tích & Thống kê kinh doanh");
        req.setAttribute("moduleKicker", "CHỈ SỐ THỰC TẾ HỆ THỐNG");
        req.setAttribute("moduleDescription", "Tổng hợp doanh thu, sản phẩm, thương hiệu và trạng thái đơn hàng thực tế từ database.");
        req.setAttribute("primaryAction", "");

        // General Counts
        req.setAttribute("totalRevenue", statisticRepository.getTotalRevenue());
        req.setAttribute("totalOrdersCount", statisticRepository.getTotalOrdersCount());
        req.setAttribute("totalProductsCount", statisticRepository.getTotalProductsCount());
        req.setAttribute("totalCustomersCount", statisticRepository.getTotalCustomersCount());
        req.setAttribute("totalBrandsCount", statisticRepository.getTotalBrandsCount());
        req.setAttribute("totalCategoriesCount", statisticRepository.getTotalCategoriesCount());
        req.setAttribute("totalVouchersCount", statisticRepository.getTotalVouchersCount());
        req.setAttribute("lowStockCount", statisticRepository.getLowStockCount());

        // Grouped Analytics
        req.setAttribute("orderStatusCounts", statisticRepository.getOrderStatusCounts());
        req.setAttribute("brandCounts", statisticRepository.getProductsByBrandCounts());
        req.setAttribute("categoryCounts", statisticRepository.getProductsByCategoryCounts());
        req.setAttribute("dailySalesTrend", statisticRepository.getDailySalesTrend());

        req.setAttribute("contentPage", "/views/admin/statistic.jsp");
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }
}
