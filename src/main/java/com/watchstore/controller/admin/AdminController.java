package com.watchstore.controller.admin;

import com.watchstore.model.Order;
import com.watchstore.model.User;
import com.watchstore.repository.BrandRepository;
import com.watchstore.repository.CategoryRepository;
import com.watchstore.repository.OrderRepository;
import com.watchstore.repository.ProductRepository;
import com.watchstore.repository.StatisticRepository;
import com.watchstore.repository.StatisticRepositoryImpl;
import com.watchstore.repository.UserRepository;
import com.watchstore.repository.VoucherRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@WebServlet("/manage/admin/*")
public class AdminController extends HttpServlet {

    private ProductRepository products;
    private BrandRepository brands;
    private VoucherRepository vouchers;
    private UserRepository userRepository;
    private CategoryRepository categories;
    private OrderRepository orderRepository;
    private StatisticRepository statisticRepository;

    private static final Map<String, String[]> PAGES = Map.ofEntries(
            Map.entry(
                    "/dashboard",
                    new String[]{"dashboard", "Bảng điều khiển"}
            ),
            Map.entry(
                    "/accounts",
                    new String[]{"account", "Quản lý tài khoản"}
            ),
            Map.entry(
                    "/roles",
                    new String[]{"role", "Vai trò"}
            ),
            Map.entry(
                    "/permissions",
                    new String[]{"permission", "Phân quyền"}
            ),
            Map.entry(
                    "/categories",
                    new String[]{"category", "Danh mục"}
            ),
            Map.entry(
                    "/brands",
                    new String[]{"brand", "Thương hiệu"}
            ),
            Map.entry(
                    "/products",
                    new String[]{"product", "Sản phẩm"}
            ),
            Map.entry(
                    "/vouchers",
                    new String[]{"voucher", "Voucher"}
            ),
            Map.entry(
                    "/banners",
                    new String[]{"banner", "Banner"}
            ),
            Map.entry(
                    "/posts",
                    new String[]{"post", "Bài viết"}
            ),
            Map.entry(
                    "/notifications",
                    new String[]{"notification", "Thông báo"}
            ),
            Map.entry(
                    "/statistics",
                    new String[]{"statistic", "Thống kê"}
            ),
            Map.entry(
                    "/reports",
                    new String[]{"report", "Báo cáo"}
            )
    );

    @Override
    public void init() {

        products =
                (ProductRepository)
                        getServletContext()
                                .getAttribute("productRepository");

        brands =
                (BrandRepository)
                        getServletContext()
                                .getAttribute("brandRepository");

        vouchers =
                (VoucherRepository)
                        getServletContext()
                                .getAttribute("voucherRepository");
        if (vouchers == null) {
            vouchers = new com.watchstore.repository.VoucherRepositoryImpl();
            getServletContext().setAttribute("voucherRepository", vouchers);
        }

        categories =
                (CategoryRepository)
                        getServletContext()
                                .getAttribute("categoryRepository");

        userRepository =
                (UserRepository)
                        getServletContext()
                                .getAttribute("userRepository");

        orderRepository =
                (OrderRepository)
                        getServletContext()
                                .getAttribute("orderRepository");

        statisticRepository =
                (StatisticRepository)
                        getServletContext()
                                .getAttribute("statisticRepository");

        if (statisticRepository == null) {
            statisticRepository =
                    new StatisticRepositoryImpl();
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException, IOException {

        String path =
                req.getPathInfo() == null
                        ? "/dashboard"
                        : req.getPathInfo();

        String[] page =
                PAGES.getOrDefault(
                        path,
                        PAGES.get("/dashboard")
                );

        String keyword =
                req.getParameter("keyword");

        loadProducts(
                req,
                keyword
        );

        loadBrands(
                req,
                keyword
        );

        loadOrders(
                req
        );

        loadUsers(
                req,
                keyword
        );

        loadVouchers(
                req,
                keyword
        );

        loadCategories(
                req,
                keyword
        );

        if ("/dashboard".equals(path)) {
            loadDashboardStatistics(req);
        }

        req.setAttribute(
                "moduleTitle",
                page[1]
        );

        req.setAttribute(
                "moduleKicker",
                "WATCHSTORE ADMIN"
        );

        req.setAttribute(
                "moduleDescription",
                getModuleDescription(path)
        );

        req.setAttribute(
                "primaryAction",
                getPrimaryAction(path)
        );

        ViewRouter.admin(
                req,
                resp,
                "admin/" + page[0],
                page[1],
                "admin"
        );
    }

    private void loadProducts(
            HttpServletRequest req,
            String keyword
    ) {

        if (products != null) {

            if (keyword != null
                    && !keyword.isBlank()) {

                req.setAttribute(
                        "products",
                        products.search(keyword)
                );

            } else {

                req.setAttribute(
                        "products",
                        products.findAll()
                );
            }

        } else {

            req.setAttribute(
                    "products",
                    java.util.Collections.emptyList()
            );
        }
    }

    private void loadBrands(
            HttpServletRequest req,
            String keyword
    ) {

        if (brands != null) {

            if (keyword != null
                    && !keyword.isBlank()) {

                req.setAttribute(
                        "brands",
                        brands.search(keyword)
                );

            } else {

                req.setAttribute(
                        "brands",
                        brands.findAll()
                );
            }
        }
    }

    private void loadOrders(
            HttpServletRequest req
    ) {

        if (orderRepository != null) {

            req.setAttribute(
                    "orders",
                    orderRepository.findAll()
            );

        } else {

            req.setAttribute(
                    "orders",
                    java.util.Collections.emptyList()
            );
        }
    }

    private void loadUsers(
            HttpServletRequest req,
            String keyword
    ) {

        if (userRepository == null) {

            List<User> users =
                    java.util.Collections.emptyList();

            req.setAttribute(
                    "users",
                    users
            );

            req.setAttribute(
                    "accounts",
                    users
            );

            return;
        }

        List<User> users;

        if (keyword != null
                && !keyword.isBlank()) {

            users =
                    userRepository.search(keyword);

        } else {

            users =
                    userRepository.findAll();
        }

        if (users == null) {
            users =
                    java.util.Collections.emptyList();
        }

        req.setAttribute(
                "users",
                users
        );

        req.setAttribute(
                "accounts",
                users
        );
    }

    private void loadVouchers(
            HttpServletRequest req,
            String keyword
    ) {

        if (vouchers == null) {
            return;
        }

        if (keyword != null
                && !keyword.isBlank()) {

            req.setAttribute(
                    "vouchers",
                    vouchers.search(keyword)
            );

        } else {

            req.setAttribute(
                    "vouchers",
                    vouchers.findAll()
            );
        }
    }

    private void loadCategories(
            HttpServletRequest req,
            String keyword
    ) {

        if (categories == null) {
            return;
        }

        if (keyword != null
                && !keyword.isBlank()) {

            req.setAttribute(
                    "categories",
                    categories.search(keyword)
            );

        } else {

            req.setAttribute(
                    "categories",
                    categories.findAll()
            );
        }

        req.setAttribute(
                "allCategories",
                categories.findAll()
        );
    }

    private void loadDashboardStatistics(
            HttpServletRequest req
    ) {

        int totalOrders =
                statisticRepository.getTotalOrdersCount();

        int pendingOrders =
                statisticRepository.getPendingOrdersCount();

        BigDecimal totalRevenue =
                statisticRepository.getTotalRevenue();

        int totalProducts =
                statisticRepository.getTotalProductsCount();

        int activeProducts =
                statisticRepository.getActiveProductsCount();

        int totalUsers =
                statisticRepository.getTotalCustomersCount();

        int activeUsers =
                statisticRepository.getActiveCustomersCount();

        int lowStockCount =
                statisticRepository.getLowStockCount();

        int expiringVouchersCount =
                statisticRepository.getExpiringVouchersCount();

        List<Map<String, Object>> last7DaysSales =
                statisticRepository.getLast7DaysSales();

        List<Order> recentOrders =
                statisticRepository.getRecentOrders(8);

        req.setAttribute(
                "totalOrders",
                totalOrders
        );

        req.setAttribute(
                "pendingOrders",
                pendingOrders
        );

        req.setAttribute(
                "totalRevenue",
                totalRevenue
        );

        req.setAttribute(
                "totalProducts",
                totalProducts
        );

        req.setAttribute(
                "activeProducts",
                activeProducts
        );

        req.setAttribute(
                "totalUsers",
                totalUsers
        );

        req.setAttribute(
                "activeUsers",
                activeUsers
        );

        req.setAttribute(
                "lowStockCount",
                lowStockCount
        );

        req.setAttribute(
                "expiringVouchersCount",
                expiringVouchersCount
        );

        req.setAttribute(
                "last7DaysSales",
                last7DaysSales
        );

        req.setAttribute(
                "orders",
                recentOrders
        );

        int totalBrands =
                brands != null
                        ? brands.findAll().size()
                        : statisticRepository.getTotalBrandsCount();

        int totalVouchers =
                vouchers != null
                        ? vouchers.findAll().size()
                        : statisticRepository.getTotalVouchersCount();

        int totalCategories =
                categories != null
                        ? categories.findAll().size()
                        : statisticRepository.getTotalCategoriesCount();

        req.setAttribute(
                "totalBrands",
                totalBrands
        );

        req.setAttribute(
                "totalVouchers",
                totalVouchers
        );

        req.setAttribute(
                "totalCategories",
                totalCategories
        );
    }

    private String getModuleDescription(
            String path
    ) {

        switch (path) {

            case "/accounts":
                return "Quản lý tài khoản người dùng";

            case "/roles":
                return "Quản lý vai trò và quyền hạn";

            case "/permissions":
                return "Quản lý ma trận phân quyền";

            case "/categories":
                return "Quản lý danh mục sản phẩm";

            case "/brands":
                return "Quản lý thương hiệu";

            case "/products":
                return "Quản lý sản phẩm";

            case "/vouchers":
                return "Quản lý voucher";

            case "/banners":
                return "Quản lý banner";

            case "/posts":
                return "Quản lý bài viết";

            case "/notifications":
                return "Quản lý thông báo";

            case "/statistics":
                return "Theo dõi số liệu hoạt động";

            case "/reports":
                return "Báo cáo hoạt động kinh doanh";

            case "/dashboard":
            default:
                return "Quản lý dữ liệu trong hệ thống";
        }
    }

    private String getPrimaryAction(
            String path
    ) {

        switch (path) {

            case "/accounts":
                return "Thêm tài khoản";

            case "/roles":
                return "Thêm vai trò";

            case "/categories":
                return "Thêm danh mục";

            case "/brands":
                return "Thêm thương hiệu";

            case "/products":
                return "Thêm sản phẩm";

            case "/vouchers":
                return "Tạo voucher";

            case "/banners":
                return "Thêm banner";

            case "/posts":
                return "Thêm bài viết";

            case "/notifications":
                return "Tạo thông báo";

            default:
                return "";
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException, IOException {

        resp.sendRedirect(
                req.getContextPath()
                        + "/manage/admin/dashboard"
        );
    }
}