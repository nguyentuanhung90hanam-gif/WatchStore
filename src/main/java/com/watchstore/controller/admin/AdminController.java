package com.watchstore.controller.admin;

import com.watchstore.repository.MockDataStore;
import com.watchstore.repository.ProductRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;
import com.watchstore.repository.BrandRepository;
import com.watchstore.model.Brand;

@WebServlet("/manage/admin/*")
public class AdminController extends HttpServlet {
    private ProductRepository products;
    private BrandRepository brands;
    private static final Map<String, String[]> PAGES = Map.ofEntries(
        Map.entry("/dashboard", new String[]{"dashboard", "Bảng điều khiển"}),
        Map.entry("/accounts", new String[]{"account", "Quản lý tài khoản"}),
        Map.entry("/roles", new String[]{"role", "Vai trò"}),
        Map.entry("/permissions", new String[]{"permission", "Phân quyền"}),
        Map.entry("/categories", new String[]{"category", "Danh mục"}),
        Map.entry("/brands", new String[]{"brand", "Thương hiệu"}),
        Map.entry("/products", new String[]{"product", "Sản phẩm"}),
        Map.entry("/vouchers", new String[]{"voucher", "Voucher"}),
        Map.entry("/banners", new String[]{"banner", "Banner"}),
        Map.entry("/posts", new String[]{"post", "Bài viết"}),
        Map.entry("/notifications", new String[]{"notification", "Thông báo"}),
        Map.entry("/statistics", new String[]{"statistic", "Thống kê"}),
        Map.entry("/reports", new String[]{"report", "Báo cáo"})
    );
    @Override
    public void init() {
        products = (ProductRepository) getServletContext().getAttribute("productRepository");
        brands = (BrandRepository) getServletContext().getAttribute("brandRepository");
     }
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo() == null ? "/dashboard" : req.getPathInfo();
        String[] page = PAGES.getOrDefault(path, PAGES.get("/dashboard"));
        if ("/brands".equals(path)) {

            String keyword = req.getParameter("keyword");

            if (keyword != null && !keyword.isBlank()) {
                req.setAttribute("brands", brands.search(keyword));
            } else {
                req.setAttribute("brands", brands.findAll());
            }

        }
        String keyword = req.getParameter("keyword");

        if (keyword != null && !keyword.isBlank()) {
            req.setAttribute("products", products.search(keyword));
        } else {
            req.setAttribute("products", products.findAll());
        }
        req.setAttribute("orders", MockDataStore.orders());
        req.setAttribute("moduleTitle", page[1]);
        ViewRouter.admin(req, resp, "admin/" + page[0], page[1], "admin");
    }
}
