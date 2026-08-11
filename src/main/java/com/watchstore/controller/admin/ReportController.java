package com.watchstore.controller.admin;

import com.watchstore.repository.StatisticRepository;
import com.watchstore.repository.StatisticRepositoryImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet(urlPatterns = {"/manage/admin/reports", "/manage/admin/reports/*"})
public class ReportController extends HttpServlet {

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

        String path = req.getPathInfo();

        // Data loading
        req.setAttribute("totalRevenue", statisticRepository.getTotalRevenue());
        req.setAttribute("totalOrdersCount", statisticRepository.getTotalOrdersCount());
        req.setAttribute("totalProductsCount", statisticRepository.getTotalProductsCount());
        req.setAttribute("lowStockCount", statisticRepository.getLowStockCount());

        req.setAttribute("topSellingProducts", statisticRepository.getTopSellingProducts());
        req.setAttribute("topCustomers", statisticRepository.getTopCustomers());
        req.setAttribute("lowStockItems", statisticRepository.getLowStockItems());

        if ("/pdf".equals(path) || "/export-pdf".equals(path)) {
            String exportDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            req.setAttribute("exportDate", exportDate);
            req.getRequestDispatcher("/views/admin/report-pdf.jsp").forward(req, resp);
            return;
        }

        req.setAttribute("adminArea", "admin");
        req.setAttribute("tableKind", "reports");
        req.setAttribute("pageTitle", "Báo cáo quản trị");
        req.setAttribute("moduleTitle", "Báo cáo tổng hợp điều hành");
        req.setAttribute("moduleKicker", "BÁO CÁO DỮ LIỆU ĐIỀU HÀNH");
        req.setAttribute("moduleDescription", "Báo cáo tổng quan sản phẩm bán chạy, khách hàng tiêu biểu và hàng tồn kho sắp hết từ database.");
        req.setAttribute("primaryAction", "");

        req.setAttribute("contentPage", "/views/admin/report.jsp");
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }
}
