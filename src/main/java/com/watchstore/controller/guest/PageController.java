package com.watchstore.controller.guest;

import com.watchstore.model.User;
import com.watchstore.repository.MockDataStore;
import com.watchstore.repository.ProductRepository;
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

    @Override public void init() { products = (ProductRepository) getServletContext().getAttribute("productRepository"); }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo() == null ? "/home" : req.getPathInfo();
        req.setAttribute("cartCount", SessionCart.count(req.getSession()));
        req.setAttribute("featuredProducts", products.findFeatured());
        req.setAttribute("products", products.search(req.getParameter("q")));
        req.setAttribute("orders", MockDataStore.orders());

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
            req.setAttribute("product", products.findById(id).orElseGet(() -> products.findFeatured().get(0)));
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
        if ("/profile".equals(req.getPathInfo())) {
            User user = (User) req.getSession().getAttribute("user");
            if (user != null) {
                user.setFullName(req.getParameter("fullName"));
                user.setPhone(req.getParameter("phone"));
                req.getSession().setAttribute("flash", "Cập nhật thông tin thành công");
            }
        }
        resp.sendRedirect(req.getContextPath() + "/page" + (req.getPathInfo() == null ? "/home" : req.getPathInfo()));
    }

    private boolean isProtected(String path) { return path.matches("/(profile|change-password|address|wishlist|reviews|notifications)"); }
    private int parseInt(String value, int fallback) { try { return Integer.parseInt(value); } catch (Exception ignored) { return fallback; } }
}
