package com.watchstore.controller.customer;

import com.watchstore.enums.OrderStatus;
import com.watchstore.model.Order;
import com.watchstore.model.User;
import com.watchstore.repository.OrderRepository;
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
    private OrderRepository orderRepository;

    @Override public void init() { 
        orderRepository = (OrderRepository) getServletContext().getAttribute("orderRepository"); 
    }

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo() == null ? "/list" : req.getPathInfo();
        req.setAttribute("orders", orderRepository.findAll());
        if ("/detail".equals(path)) {
            req.setAttribute("order", orderRepository.findByCode(req.getParameter("code")));
            ViewRouter.customer(req, resp, "customer/order-detail", "Chi tiết đơn hàng");
        } else ViewRouter.customer(req, resp, "customer/order-list", "Đơn hàng của tôi");
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = "WS" + (8500 + orderRepository.findAll().size());
        Order order = new Order(code, "Khách hàng WatchStore", LocalDateTime.now(), new BigDecimal("6790000"), OrderStatus.PENDING);
        
        User user = (User) req.getSession().getAttribute("user");
        if (user != null) {
            order.setUserId(user.getId());
            order.setCustomerName(user.getFullName());
            order.setPhone(user.getPhone());
            order.setShippingAddress(user.getAddress());
        }
        
        orderRepository.add(order);
        SessionCart.get(req.getSession()).clear();
        req.getSession().setAttribute("flash", "Đặt hàng thành công. Mã đơn: " + code);
        resp.sendRedirect(req.getContextPath() + "/orders/detail?code=" + code);
    }
}
