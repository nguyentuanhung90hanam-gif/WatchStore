package com.watchstore.controller.guest;

import com.watchstore.model.Product;
import com.watchstore.model.User;
import com.watchstore.repository.ProductRepository;
import com.watchstore.repository.OrderRepository;
import com.watchstore.repository.UserRepository;
import com.watchstore.util.SessionCart;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

@WebServlet("/page/*")
public class PageController extends HttpServlet {
    private ProductRepository products;
    private OrderRepository orderRepository;
    private UserRepository userRepository;

    @Override public void init() { 
        products = (ProductRepository) getServletContext().getAttribute("productRepository"); 
        orderRepository = (OrderRepository) getServletContext().getAttribute("orderRepository");
        userRepository = (UserRepository) getServletContext().getAttribute("userRepository");
    }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo() == null ? "/home" : req.getPathInfo();
        req.setAttribute("cartCount", SessionCart.count(req.getSession()));
        req.setAttribute("featuredProducts", products.findFeatured());
        req.setAttribute("products", products.search(req.getParameter("q")));
        req.setAttribute("orders", orderRepository.findAll());

        Map<String, String[]> pages = Map.ofEntries(
                Map.entry("/home", new String[]{"guest/home", "Trang chủ"}),
                Map.entry("/products", new String[]{"guest/product-list", "Sản phẩm"}),
                Map.entry("/news", new String[]{"guest/news", "Tin tức"}),
                Map.entry("/vouchers", new String[]{"guest/voucher", "Kho voucher"}),
                Map.entry("/profile", new String[]{"member/profile", "Thông tin cá nhân"}),
                Map.entry("/change-password", new String[]{"member/change-password", "Đổi mật khẩu"}),
                Map.entry("/forgot-password", new String[]{"guest/forgot-password", "Quên mật khẩu"}),
                Map.entry("/address", new String[]{"member/address", "Địa chỉ nhận hàng"}),
                Map.entry("/wishlist", new String[]{"customer/wishlist", "Sản phẩm yêu thích"}),
                Map.entry("/reviews", new String[]{"customer/review", "Đánh giá của tôi"}),
                Map.entry("/notifications", new String[]{"customer/notification", "Thông báo"})
        );

        if (path.equals("/product")) {
            int id = parseInt(req.getParameter("id"), 1);
            Product product = products.findById(id);
            if (product == null && !products.findFeatured().isEmpty()) {
                product = products.findFeatured().get(0);
            }
            req.setAttribute("product", product);
            ViewRouter.customer(req, resp, "guest/product-detail", "Chi tiết sản phẩm");
            return;
        }

        String[] page = pages.getOrDefault(path, pages.get("/home"));
        if (isProtected(path) && req.getSession().getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login?required=1");
            return;
        }
        ViewRouter.customer(req, resp, page[0], page[1]);
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        User user = (User) req.getSession().getAttribute("user");

        if ("/profile".equals(path) && user != null) {
            String fullName = req.getParameter("fullName");
            String phone = req.getParameter("phone");
            String address = req.getParameter("address");

            if (fullName != null && !fullName.isBlank()) user.setFullName(fullName);
            if (phone != null && !phone.isBlank()) user.setPhone(phone);
            if (address != null && !address.isBlank()) user.setAddress(address);

            if (userRepository != null) {
                userRepository.update(user);
            }
            req.getSession().setAttribute("user", user);
            req.getSession().setAttribute("flash", "Cập nhật thông tin cá nhân thành công!");
        } else if ("/change-password".equals(path) && user != null) {
            String oldPass = req.getParameter("oldPassword");
            String newPass = req.getParameter("newPassword");
            String confirmPass = req.getParameter("confirmPassword");

            if (newPass == null || !newPass.equals(confirmPass)) {
                req.getSession().setAttribute("flash", "Lỗi: Mật khẩu xác nhận không trùng khớp!");
            } else if (userRepository != null) {
                boolean success = userRepository.updatePassword(user.getId(), oldPass, newPass);
                if (success) {
                    req.getSession().setAttribute("flash", "Đổi mật khẩu thành công!");
                } else {
                    req.getSession().setAttribute("flash", "Lỗi: Mật khẩu hiện tại không chính xác!");
                }
            } else {
                user.setPassword(newPass);
                req.getSession().setAttribute("flash", "Đổi mật khẩu thành công!");
            }
        } else if ("/address".equals(path) && user != null) {
            String address = req.getParameter("address");
            if (address != null && !address.isBlank()) {
                user.setAddress(address);
                if (userRepository != null) {
                    userRepository.update(user);
                }
                req.getSession().setAttribute("user", user);
                req.getSession().setAttribute("flash", "Cập nhật địa chỉ nhận hàng thành công!");
            }
        }

        resp.sendRedirect(req.getContextPath() + "/page" + (path == null ? "/home" : path));
    }

    private boolean isProtected(String path) { return path.matches("/(profile|change-password|address|wishlist|reviews|notifications)"); }
    private int parseInt(String value, int fallback) { try { return Integer.parseInt(value); } catch (Exception ignored) { return fallback; } }
}