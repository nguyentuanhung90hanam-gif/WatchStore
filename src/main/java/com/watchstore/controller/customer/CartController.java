package com.watchstore.controller.customer;

import com.watchstore.model.User;
import com.watchstore.repository.AddressRepository;
import com.watchstore.repository.CartRepository;
import com.watchstore.repository.ProductRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet("/cart/*")
public class CartController extends HttpServlet {

    private ProductRepository products;
    private CartRepository cartRepository;
    private AddressRepository addresses;

    @Override
    public void init() {
        products = (ProductRepository) getServletContext().getAttribute("productRepository");
        cartRepository = (CartRepository) getServletContext().getAttribute("cartRepository");
        addresses = (AddressRepository) getServletContext().getAttribute("addressRepository");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login?required=1");
            return;
        }

        String path = req.getPathInfo() == null ? "/view" : req.getPathInfo();

        try {
            prepareDbCart(req, user.getUserId());

            if ("/checkout".equals(path)) {
                if (addresses != null) {
                    req.setAttribute("addresses", addresses.findAll(user.getUserId()));
                }
                ViewRouter.customer(req, resp, "customer/checkout", "Thanh toán");
            } else {
                ViewRouter.customer(req, resp, "customer/cart", "Giỏ hàng");
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login?required=1");
            return;
        }

        String path = req.getPathInfo() == null ? "/add" : req.getPathInfo();
        String action = req.getParameter("action");

        int productId = parse(req.getParameter("id"), 0);
        if (productId <= 0) productId = parse(req.getParameter("productId"), 0);
        int variantId = parse(req.getParameter("variantId"), 0);
        int quantity = parse(req.getParameter("quantity"), 1);

        try {
            if ("/add".equals(path) || "buy-now".equalsIgnoreCase(action) || "/buy-now".equals(path)) {
                if (quantity < 1 || quantity > 99) {
                    throw new IllegalArgumentException("Số lượng sản phẩm không hợp lệ.");
                }
                if (variantId > 0) {
                    cartRepository.add(user.getUserId(), variantId, quantity);
                } else if (productId > 0) {
                    cartRepository.addProduct(user.getUserId(), productId, quantity);
                } else {
                    throw new IllegalArgumentException("Vui lòng chọn sản phẩm hợp lệ.");
                }

                if ("buy-now".equalsIgnoreCase(action) || "/buy-now".equals(path)) {
                    resp.sendRedirect(req.getContextPath() + "/cart/checkout");
                    return;
                }
                req.getSession().setAttribute("flash", "Đã thêm sản phẩm vào giỏ hàng");
            } else if ("/update".equals(path)) {
                if (variantId <= 0 && productId <= 0) {
                    throw new IllegalArgumentException("Sản phẩm trong giỏ không hợp lệ.");
                }
                int targetVariantId = variantId > 0 ? variantId : productId;
                cartRepository.update(user.getUserId(), targetVariantId, Math.max(0, quantity));
                req.getSession().setAttribute("flash", "Đã cập nhật số lượng sản phẩm");
            } else if ("/remove".equals(path)) {
                if (variantId <= 0 && productId <= 0) {
                    throw new IllegalArgumentException("Sản phẩm trong giỏ không hợp lệ.");
                }
                int targetVariantId = variantId > 0 ? variantId : productId;
                cartRepository.remove(user.getUserId(), targetVariantId);
                req.getSession().setAttribute("flash", "Đã xóa sản phẩm khỏi giỏ hàng");
            }
        } catch (Exception e) {
            req.getSession().setAttribute("flash", "Lỗi: " + root(e));
        }

        resp.sendRedirect(req.getContextPath() + "/cart/view");
    }

    private void prepareDbCart(HttpServletRequest req, int userId) throws SQLException {
        List<Map<String, Object>> items = cartRepository.items(userId);
        BigDecimal subtotal = cartRepository.subtotal(userId);
        BigDecimal shipping = (subtotal.signum() > 0 && subtotal.compareTo(new BigDecimal("1000000")) < 0)
                ? new BigDecimal("30000") : BigDecimal.ZERO;

        req.setAttribute("cartItems", items);
        req.setAttribute("subtotal", subtotal);
        req.setAttribute("discount", BigDecimal.ZERO);
        req.setAttribute("shipping", shipping);
        req.setAttribute("cartCount", cartRepository.count(userId));
    }

    private int parse(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String root(Exception e) {
        Throwable t = e;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() == null ? "Thao tác thất bại." : t.getMessage();
    }
}