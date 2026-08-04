package com.watchstore.controller.customer;

import com.watchstore.model.Product;
import com.watchstore.repository.ProductRepository;
import com.watchstore.util.SessionCart;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@WebServlet("/cart/*")
public class CartController extends HttpServlet {
    private ProductRepository products;
    @Override public void init() { products = (ProductRepository) getServletContext().getAttribute("productRepository"); }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo() == null ? "/view" : req.getPathInfo();
        prepare(req);
        if ("/checkout".equals(path)) ViewRouter.customer(req, resp, "customer/checkout", "Thanh toán");
        else ViewRouter.customer(req, resp, "customer/cart", "Giỏ hàng");
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo() == null ? "/add" : req.getPathInfo();
        int id = parse(req.getParameter("id"), 1);
        Map<Integer, Integer> cart = SessionCart.get(req.getSession());
        if ("/remove".equals(path)) cart.remove(id);
        else if ("/update".equals(path)) cart.put(id, Math.max(1, parse(req.getParameter("quantity"), 1)));
        else cart.merge(id, Math.max(1, parse(req.getParameter("quantity"), 1)), Integer::sum);
        req.getSession().setAttribute("flash", "/remove".equals(path) ? "Đã xóa sản phẩm" : "Đã cập nhật giỏ hàng");
        resp.sendRedirect(req.getContextPath() + "/cart/view");
    }

    private void prepare(HttpServletRequest req) {
        Map<Integer, Integer> cart = SessionCart.get(req.getSession());
        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Optional<Product> product = products.findById(entry.getKey());
            if (product.isEmpty()) continue;
            BigDecimal lineTotal = product.get().getPrice().multiply(BigDecimal.valueOf(entry.getValue()));
            subtotal = subtotal.add(lineTotal);
            Map<String, Object> item = new HashMap<>();
            item.put("product", product.get()); item.put("quantity", entry.getValue()); item.put("lineTotal", lineTotal);
            items.add(item);
        }
        req.setAttribute("cartItems", items);
        req.setAttribute("subtotal", subtotal);
        req.setAttribute("discount", subtotal.compareTo(new BigDecimal("10000000")) >= 0 ? new BigDecimal("500000") : BigDecimal.ZERO);
        req.setAttribute("shipping", subtotal.signum() == 0 || subtotal.compareTo(new BigDecimal("1000000")) >= 0 ? BigDecimal.ZERO : new BigDecimal("30000"));
        req.setAttribute("cartCount", SessionCart.count(req.getSession()));
    }
    private int parse(String value, int fallback) { try { return Integer.parseInt(value); } catch (Exception ignored) { return fallback; } }
}
