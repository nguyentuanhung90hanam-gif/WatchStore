package com.watchstore.controller.guest;

import com.watchstore.model.Product;
import com.watchstore.model.User;
import com.watchstore.repository.OrderRepository;
import com.watchstore.repository.ProductRepository;
import com.watchstore.repository.UserAccountRepository;
import com.watchstore.util.SessionCart;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@WebServlet("/page/*")
public class PageController extends HttpServlet {

    private ProductRepository products;
    private UserAccountRepository accountRepo;
    private OrderRepository orderRepository;

    @Override
    public void init() {
        products = (ProductRepository) getServletContext().getAttribute("productRepository");
        accountRepo = new UserAccountRepository();

        orderRepository =
                (OrderRepository) getServletContext().getAttribute("orderRepository");

        if (orderRepository == null) {
            orderRepository = new OrderRepository();
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException, IOException {

        String path = req.getPathInfo() == null
                ? "/home"
                : req.getPathInfo();

        req.setAttribute(
                "cartCount",
                SessionCart.count(req.getSession())
        );

        if (products != null) {
            req.setAttribute(
                    "featuredProducts",
                    products.findFeatured()
            );

            req.setAttribute(
                    "products",
                    products.search(req.getParameter("q"))
            );
        }

        if (orderRepository != null) {
            req.setAttribute(
                    "orders",
                    orderRepository.findAll()
            );
        }

        Map<String, String[]> pages = Map.ofEntries(
                Map.entry(
                        "/home",
                        new String[]{"guest/home", "Trang chủ"}
                ),
                Map.entry(
                        "/products",
                        new String[]{"guest/product-list", "Sản phẩm"}
                ),
                Map.entry(
                        "/news",
                        new String[]{"guest/news", "Tin tức"}
                ),
                Map.entry(
                        "/vouchers",
                        new String[]{"guest/voucher", "Kho voucher"}
                ),
                Map.entry(
                        "/profile",
                        new String[]{"member/profile", "Thông tin cá nhân"}
                ),
                Map.entry(
                        "/change-password",
                        new String[]{"member/change-password", "Đổi mật khẩu"}
                ),
                Map.entry(
                        "/forgot-password",
                        new String[]{"guest/forgot-password", "Quên mật khẩu"}
                ),
                Map.entry(
                        "/address",
                        new String[]{"member/address", "Địa chỉ nhận hàng"}
                ),
                Map.entry(
                        "/wishlist",
                        new String[]{"customer/wishlist", "Sản phẩm yêu thích"}
                ),
                Map.entry(
                        "/reviews",
                        new String[]{"customer/review", "Đánh giá của tôi"}
                ),
                Map.entry(
                        "/notifications",
                        new String[]{"customer/notification", "Thông báo"}
                )
        );

        if ("/product".equals(path)) {

            int id = parseInt(
                    req.getParameter("id"),
                    1
            );

            Product product = null;

            if (products != null) {
                product = products.findById(id).orElse(null);

                if (product == null) {
                    var featuredProducts = products.findFeatured();

                    if (featuredProducts != null
                            && !featuredProducts.isEmpty()) {
                        product = featuredProducts.get(0);
                    }
                }
            }

            req.setAttribute(
                    "product",
                    product
            );

            ViewRouter.customer(
                    req,
                    resp,
                    "guest/product-detail",
                    "Chi tiết sản phẩm"
            );

            return;
        }

        String[] page = pages.getOrDefault(
                path,
                pages.get("/home")
        );

        if (isProtected(path)
                && req.getSession().getAttribute("user") == null) {

            resp.sendRedirect(
                    req.getContextPath()
                            + "/auth/login?required=1"
            );

            return;
        }

        ViewRouter.customer(
                req,
                resp,
                page[0],
                page[1]
        );
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        req.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo() == null
                ? "/home"
                : req.getPathInfo();

        User user =
                (User) req.getSession().getAttribute("user");

        if (user == null) {
            resp.sendRedirect(
                    req.getContextPath()
                            + "/auth/login?required=1"
            );
            return;
        }

        try {

            if ("/profile".equals(path)) {

                handleProfile(
                        req,
                        user
                );

            } else if ("/change-password".equals(path)) {

                handleChangePassword(
                        req,
                        user
                );

            } else if ("/address".equals(path)) {

                handleAddress(
                        req,
                        user
                );
            }

        } catch (Exception e) {

            req.getSession().setAttribute(
                    "errorMsg",
                    e.getMessage() == null
                            ? "Không thể cập nhật tài khoản."
                            : e.getMessage()
            );
        }

        resp.sendRedirect(
                req.getContextPath()
                        + "/page"
                        + path
        );
    }

    private void handleProfile(
            HttpServletRequest req,
            User user
    ) throws Exception {

        String fullName =
                req.getParameter("fullName");

        String phone =
                req.getParameter("phone");

        String gender =
                req.getParameter("gender");

        String dateOfBirth =
                req.getParameter("dateOfBirth");

        LocalDate dob =
                parseDate(dateOfBirth);

        accountRepo.updateProfile(
                user.getId(),
                fullName,
                phone,
                gender,
                dob
        );

        User updatedUser =
                accountRepo.findById(
                        user.getId()
                );

        if (updatedUser != null) {
            updatedUser.setAddress(
                    user.getAddress()
            );

            req.getSession().setAttribute(
                    "user",
                    updatedUser
            );
        }

        req.getSession().setAttribute(
                "flash",
                "Cập nhật thông tin thành công"
        );
    }

    private void handleChangePassword(
            HttpServletRequest req,
            User user
    ) throws Exception {

        String currentPassword =
                req.getParameter("currentPassword");

        if (currentPassword == null) {
            currentPassword =
                    req.getParameter("oldPassword");
        }

        String newPassword =
                req.getParameter("newPassword");

        String confirmPassword =
                req.getParameter("confirmPassword");

        accountRepo.changePassword(
                user.getId(),
                currentPassword,
                newPassword,
                confirmPassword
        );

        req.getSession().setAttribute(
                "flash",
                "Đổi mật khẩu thành công."
        );
    }

    private void handleAddress(
            HttpServletRequest req,
            User user
    ) {

        String address =
                req.getParameter("address");

        if (address == null
                || address.isBlank()) {

            throw new IllegalArgumentException(
                    "Địa chỉ nhận hàng không được để trống."
            );
        }

        user.setAddress(
                address.trim()
        );

        req.getSession().setAttribute(
                "user",
                user
        );

        req.getSession().setAttribute(
                "flash",
                "Cập nhật địa chỉ nhận hàng thành công!"
        );
    }

    private boolean isProtected(
            String path
    ) {

        return path.matches(
                "/(profile|change-password|address|wishlist|reviews|notifications)"
        );
    }

    private LocalDate parseDate(
            String value
    ) {

        if (value == null
                || value.isBlank()) {
            return null;
        }

        try {

            return LocalDate.parse(
                    value
            );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Ngày sinh không hợp lệ."
            );
        }
    }

    private int parseInt(
            String value,
            int fallback
    ) {

        try {

            return Integer.parseInt(
                    value
            );

        } catch (Exception ignored) {

            return fallback;
        }
    }
}