package com.watchstore.controller.admin;

import com.watchstore.model.Brand;
import com.watchstore.model.User;
import com.watchstore.repository.BrandRepository;
import com.watchstore.repository.MockDataStore;
import com.watchstore.repository.OrderRepository;
import com.watchstore.repository.ProductRepository;
import com.watchstore.repository.UserRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/manage/admin/*")
public class AdminController extends HttpServlet {
    private ProductRepository products;
    private BrandRepository brands;
    private OrderRepository orderRepository;
    private UserRepository userRepository;

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
        orderRepository = (OrderRepository) getServletContext().getAttribute("orderRepository");
        userRepository = (UserRepository) getServletContext().getAttribute("userRepository");
    }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo() == null ? "/dashboard" : req.getPathInfo();
        String[] page = PAGES.getOrDefault(path, PAGES.get("/dashboard"));
        String keyword = req.getParameter("keyword");

        // Set products & search
        if (products != null) {
            req.setAttribute("products", (keyword != null && !keyword.isBlank()) ? products.search(keyword) : products.findAll());
        } else {
            req.setAttribute("products", MockDataStore.products());
        }

        // Set brands
        if (brands != null) {
            req.setAttribute("brands", (keyword != null && !keyword.isBlank()) ? brands.search(keyword) : brands.findAll());
        }

        // Set orders
        if (orderRepository != null) {
            req.setAttribute("orders", orderRepository.findAll());
        } else {
            req.setAttribute("orders", MockDataStore.orders());
        }

        // Set users
        List<User> userList = userRepository != null ? userRepository.findAll() : MockDataStore.users();
        if (userList.isEmpty()) userList = MockDataStore.users();
        req.setAttribute("users", userList);
        req.setAttribute("accounts", userList);

        req.setAttribute("moduleTitle", page[1]);
        ViewRouter.admin(req, resp, "admin/" + page[0], page[1], "admin");
    }
}
