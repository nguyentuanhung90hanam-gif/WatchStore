package com.watchstore.controller.customer;

import com.watchstore.enums.OrderStatus;
import com.watchstore.model.Order;
import com.watchstore.model.User;
import com.watchstore.repository.AddressRepository;
import com.watchstore.repository.CartRepository;
import com.watchstore.repository.OrderRepository;
import com.watchstore.repository.WarrantyRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/orders/*")
public class OrderController extends HttpServlet {
    private OrderRepository orders;
    private CartRepository cart;
    private AddressRepository addresses;
    private WarrantyRepository warrantyRepo;

    @Override
    public void init() {
        orders = (OrderRepository) getServletContext().getAttribute("orderRepository");
        if (orders == null) orders = new OrderRepository();
        cart = (CartRepository) getServletContext().getAttribute("cartRepository");
        if (cart == null) cart = new CartRepository();
        addresses = (AddressRepository) getServletContext().getAttribute("addressRepository");
        if (addresses == null) addresses = new AddressRepository();
        warrantyRepo = (WarrantyRepository) getServletContext().getAttribute("warrantyRepository");
        if (warrantyRepo == null) warrantyRepo = new WarrantyRepository();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = (User) req.getSession().getAttribute("user");
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login?required=1");
            return;
        }
        String path = req.getPathInfo() == null ? "/list" : req.getPathInfo();
        try {
            if ("/detail".equals(path)) {
                String code = req.getParameter("code");
                if (code == null || code.isBlank()) {
                    resp.sendRedirect(req.getContextPath() + "/orders/list");
                    return;
                }
                Order o = orders.findByCode(code.trim());
                if (o == null || o.getUserId() != u.getUserId()) {
                    resp.sendError(404, "Không tìm thấy đơn hàng hoặc đơn hàng không thuộc tài khoản của bạn.");
                    return;
                }
                req.setAttribute("order", o);
                List<Map<String, Object>> items = orders.getOrderItems(o.getId());
                req.setAttribute("orderItems", items);
                req.setAttribute("warranties", warrantyRepo.findByOrderId(o.getId()));
                ViewRouter.customer(req, resp, "customer/order-detail", "Chi tiết đơn hàng");
                return;
            }
            req.setAttribute("orders", orders.findByCustomerId(u.getUserId()));
            ViewRouter.customer(req, resp, "customer/order-list", "Đơn hàng của tôi");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        User u = (User) req.getSession().getAttribute("user");
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login?required=1");
            return;
        }
        String path = req.getPathInfo() == null ? "/place" : req.getPathInfo();
        try {
            if ("/place".equals(path)) {
                int addressId = parsePositive(req.getParameter("addressId"), "Vui lòng chọn địa chỉ nhận hàng.");
                String payment = req.getParameter("payment");
                if (!"COD".equalsIgnoreCase(payment) && !"BANK_TRANSFER".equalsIgnoreCase(payment)) {
                    payment = "COD";
                }
                String voucher = req.getParameter("voucherCode");
                if (voucher != null && voucher.trim().length() > 50) {
                    throw new IllegalArgumentException("Mã giảm giá không hợp lệ.");
                }
                String note = req.getParameter("note");
                if (note != null && note.length() > 500) {
                    throw new IllegalArgumentException("Ghi chú không được vượt quá 500 ký tự.");
                }

                long orderId = orders.createFromCart(u.getUserId(), addressId, voucher, payment, note);
                Order o = orders.findById((int) orderId);
                req.getSession().setAttribute("flash", "Đặt hàng thành công! Mã đơn hàng: #" + (o != null ? o.getCode() : orderId));
                if (o != null) {
                    resp.sendRedirect(req.getContextPath() + "/orders/detail?code=" + o.getCode());
                } else {
                    resp.sendRedirect(req.getContextPath() + "/orders/list");
                }
                return;
            }
            if ("/cancel".equals(path)) {
                long orderId = parseLongPositive(req.getParameter("orderId"), "Đơn hàng không hợp lệ.");
                String reason = req.getParameter("reason");
                if (reason == null || reason.trim().length() < 3 || reason.trim().length() > 500) {
                    throw new IllegalArgumentException("Lý do hủy phải từ 3 đến 500 ký tự.");
                }
                orders.cancel(orderId, u.getUserId(), reason.trim());
                req.getSession().setAttribute("flash", "Đã hủy đơn hàng thành công.");
                resp.sendRedirect(req.getContextPath() + "/orders/list");
                return;
            }
            if ("/warranty".equals(path)) {
                int orderId = parsePositive(req.getParameter("orderId"), "Đơn hàng không hợp lệ.");
                Order o = orders.findById(orderId);
                if (o == null || o.getUserId() != u.getUserId()) {
                    throw new IllegalArgumentException("Đơn hàng không hợp lệ hoặc không thuộc tài khoản của bạn.");
                }
                if (o.getStatus() != OrderStatus.COMPLETED) {
                    throw new IllegalArgumentException("Chỉ được yêu cầu bảo hành với đơn hàng đã giao thành công (COMPLETED).");
                }
                String productName = req.getParameter("productName");
                if (productName == null || productName.isBlank()) {
                    throw new IllegalArgumentException("Vui lòng chọn sản phẩm cần bảo hành.");
                }
                String note = req.getParameter("note");
                if (note == null || note.trim().length() < 3) {
                    throw new IllegalArgumentException("Vui lòng mô tả chi tiết lỗi sản phẩm gặp phải (từ 3 ký tự trở lên).");
                }

                String imageUrl = req.getParameter("imageUrl");
                if (imageUrl == null || imageUrl.isBlank()) {
                    imageUrl = req.getParameter("productImage");
                }

                // Gọi insertOnlineWarranty với backend validation đầy đủ
                warrantyRepo.insertOnlineWarranty(orderId, productName.trim(), note.trim(), imageUrl != null ? imageUrl.trim() : null);
                req.getSession().setAttribute("flash", "Đã gửi yêu cầu bảo hành thành công! Nhân viên kỹ thuật sẽ tiếp nhận và liên hệ với bạn.");
                resp.sendRedirect(req.getContextPath() + "/orders/detail?code=" + o.getCode());
                return;
            }
        } catch (Exception e) {
            req.getSession().setAttribute("flash", "Lỗi: " + root(e));
            String code = req.getParameter("orderCode");
            if (code != null && !code.isBlank()) {
                resp.sendRedirect(req.getContextPath() + "/orders/detail?code=" + code.trim());
            } else {
                resp.sendRedirect(req.getContextPath() + "/orders/list");
            }
        }
    }

    private int parsePositive(String value, String message) {
        try {
            int n = Integer.parseInt(value);
            if (n > 0) return n;
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException(message);
    }

    private long parseLongPositive(String value, String message) {
        try {
            long n = Long.parseLong(value);
            if (n > 0) return n;
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException(message);
    }

    private String root(Exception e) {
        Throwable t = e;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() == null ? "Thao tác không thành công." : t.getMessage();
    }
}
