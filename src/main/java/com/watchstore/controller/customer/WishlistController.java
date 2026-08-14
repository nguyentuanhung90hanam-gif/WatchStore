package com.watchstore.controller.customer;

import com.watchstore.model.Product;
import com.watchstore.model.User;
import com.watchstore.repository.WishlistRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebServlet(urlPatterns = {"/wishlist/*", "/customer/wishlist/*"})
public class WishlistController extends HttpServlet {
    private WishlistRepository wishlistRepository;

    @Override
    public void init() {
        wishlistRepository = (WishlistRepository) getServletContext().getAttribute("wishlistRepository");
        if (wishlistRepository == null) {
            wishlistRepository = new WishlistRepository();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login?required=1");
            return;
        }

        try {
            List<Product> wishlistProducts = wishlistRepository.findAll(user.getId());
            req.setAttribute("wishlistProducts", wishlistProducts);
        } catch (Exception e) {
            req.setAttribute("wishlistProducts", Collections.emptyList());
            req.getSession().setAttribute("flash", "Không thể tải danh sách yêu thích: " + e.getMessage());
        }

        ViewRouter.customer(req, resp, "customer/wishlist", "Sản phẩm yêu thích");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login?required=1");
            return;
        }

        String productIdStr = req.getParameter("productId");
        try {
            if (productIdStr != null && !productIdStr.isBlank()) {
                int productId = Integer.parseInt(productIdStr.trim());
                boolean isFavorite = wishlistRepository.toggle(user.getId(), productId);
                if (isFavorite) {
                    req.getSession().setAttribute("flash", "Đã thêm sản phẩm vào danh sách yêu thích ❤️");
                } else {
                    req.getSession().setAttribute("flash", "Đã xóa sản phẩm khỏi danh sách yêu thích");
                }
            }
        } catch (Exception e) {
            req.getSession().setAttribute("flash", "Lỗi cập nhật danh sách yêu thích: " + e.getMessage());
        }

        String redirectUri = req.getParameter("redirectUri");
        if (redirectUri != null && !redirectUri.isBlank() && redirectUri.startsWith(req.getContextPath())) {
            resp.sendRedirect(redirectUri);
        } else {
            resp.sendRedirect(req.getContextPath() + "/wishlist");
        }
    }
}
