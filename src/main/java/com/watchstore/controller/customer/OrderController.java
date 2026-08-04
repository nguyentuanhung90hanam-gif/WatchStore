package com.watchstore.controller.customer;

import com.watchstore.enums.OrderStatus;
import com.watchstore.model.Order;
import com.watchstore.repository.MockDataStore;
import com.watchstore.util.SessionCart;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@WebServlet("/orders/*")
public class OrderController extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo() == null ? "/list" : req.getPathInfo();
        req.setAttribute("orders", MockDataStore.orders());
        if ("/detail".equals(path)) {
            req.setAttribute("order", MockDataStore.findOrder(req.getParameter("code")));
            ViewRouter.customer(req, resp, "customer/order-detail", "Chi tiết đơn hàng");
        } else ViewRouter.customer(req, resp, "customer/order-list", "Đơn hàng của tôi");
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = "WS" + (8500 + MockDataStore.orders().size());
        MockDataStore.addOrder(new Order(code, "Khách hàng WatchStore", LocalDateTime.now(), new BigDecimal("6790000"), OrderStatus.PENDING));
        SessionCart.get(req.getSession()).clear();
        req.getSession().setAttribute("flash", "Đặt hàng thành công. Mã đơn: " + code);
        resp.sendRedirect(req.getContextPath() + "/orders/detail?code=" + code);
    }
}
