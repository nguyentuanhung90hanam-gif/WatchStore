package com.watchstore.controller.customer;

import com.watchstore.model.Product;
import com.watchstore.model.User;
import com.watchstore.repository.CartRepository;
import com.watchstore.repository.ProductRepository;
import com.watchstore.util.SessionCart;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/cart/*")
public class CartController extends HttpServlet {

    private ProductRepository products;
    private CartRepository cartRepository;
    private com.watchstore.repository.AddressRepository addresses;

    @Override
    public void init() {
        products = (ProductRepository) getServletContext().getAttribute("productRepository");
        cartRepository = (CartRepository) getServletContext().getAttribute("cartRepository");
        addresses = (com.watchstore.repository.AddressRepository) getServletContext().getAttribute("addressRepository");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo() == null ? "/view" : req.getPathInfo();
        User user = (User) req.getSession().getAttribute("user");

        try {
            if (user != null) {
                prepareDbCart(req, user.getUserId());
            } else {
                prepareSessionCart(req);
            }

            if ("/checkout".equals(path)) {
                if (user == null) {
                    resp.sendRedirect(req.getContextPath() + "/auth/login?required=1");
                    return;
                }
                req.setAttribute("addresses", addresses.findAll(user.getUserId()));
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

        String path = req.getPathInfo() == null ? "/add" : req.getPathInfo();
        User user = (User) req.getSession().getAttribute("user");

        int productId = parse(req.getParameter("id"), 0);
        int variantId = parse(req.getParameter("variantId"), 0);
        int quantity = parse(req.getParameter("quantity"), 1);

        try {
            if (user == null) {
                // Session Cart
                Map<Integer, Integer> cart = SessionCart.get(req.getSession());
                int targetId = variantId > 0 ? variantId : productId;
                if (targetId <= 0) targetId = 1;
                
                if ("/remove".equals(path)) {
                    cart.remove(targetId);
                } else if ("/update".equals(path)) {
                    cart.put(targetId, Math.max(1, quantity));
                } else {
                    cart.merge(targetId, Math.max(1, quantity), Integer::sum);
                }
            } else {
                // DB Cart
                if ("/add".equals(path)) {
                    if (productId <= 0 || quantity < 1 || quantity > 99) 
                        throw new IllegalArgumentException("Số lượng sản phẩm không hợp lệ.");
                    cartRepository.addProduct(user.getUserId(), productId, quantity);
                } else if ("/update".equals(path)) {
                    if (variantId <= 0 && productId <= 0) 
                        throw new IllegalArgumentException("Sản phẩm trong giỏ không hợp lệ.");
                    cartRepository.update(user.getUserId(), variantId > 0 ? variantId : productId, Math.max(0, quantity));
                } else if ("/remove".equals(path)) {
                    if (variantId <= 0 && productId <= 0) 
                        throw new IllegalArgumentException("Sản phẩm trong giỏ không hợp lệ.");
                    cartRepository.remove(user.getUserId(), variantId > 0 ? variantId : productId);
                }
            }

            req.getSession().setAttribute(
                    "flash",
                    "/remove".equals(path)
                            ? "Đã xóa sản phẩm"
                            : "Đã cập nhật giỏ hàng"
            );
        } catch (Exception e) {
            req.getSession().setAttribute("flash", "Lỗi: " + root(e));
        }

        resp.sendRedirect(req.getContextPath() + "/cart/view");
    }

    private void prepareDbCart(HttpServletRequest req, int userId) throws SQLException {
        List<Map<String, Object>> items = cartRepository.items(userId);
        BigDecimal subtotal = cartRepository.subtotal(userId);
        BigDecimal shipping = subtotal.signum() > 0 && subtotal.compareTo(new BigDecimal("1000000")) < 0 
                ? new BigDecimal("30000") : BigDecimal.ZERO;
        
        req.setAttribute("cartItems", items);
        req.setAttribute("subtotal", subtotal);
        req.setAttribute("discount", BigDecimal.ZERO);
        req.setAttribute("shipping", shipping);
        req.setAttribute("cartCount", cartRepository.count(userId));
    }

    private void prepareSessionCart(HttpServletRequest req) {
        Map<Integer, Integer> cart = SessionCart.get(req.getSession());
        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Product product = products.findById(entry.getKey()).orElse(null);
            if (product == null) continue;

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(entry.getValue()));
            subtotal = subtotal.add(lineTotal);

            Map<String, Object> item = new HashMap<>();
            // Match KH item keys so the same JSP works for both Session and DB cart!
            item.put("product", product); // Fallback for old JSP
            item.put("productId", product.getProductId());
            item.put("variantId", product.getProductId()); // Use productId as variantId fallback
            item.put("quantity", entry.getValue());
            item.put("name", product.getProductName());
            item.put("price", product.getPrice());
            item.put("oldPrice", product.getCompareAtPrice());
            item.put("brand", product.getBrandName());
            item.put("image", product.getImageUrl());
            item.put("lineTotal", lineTotal);

            items.add(item);
        }

        req.setAttribute("cartItems", items);
        req.setAttribute("subtotal", subtotal);
        req.setAttribute("discount", BigDecimal.ZERO);
        req.setAttribute("shipping", subtotal.signum() == 0 || subtotal.compareTo(new BigDecimal("1000000")) >= 0 ? BigDecimal.ZERO : new BigDecimal("30000"));
        req.setAttribute("cartCount", SessionCart.count(req.getSession()));
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