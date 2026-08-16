package com.watchstore.controller.customer;

import com.watchstore.enums.OrderStatus;
import com.watchstore.model.Order;
import com.watchstore.model.User;
import com.watchstore.repository.AddressRepository;
import com.watchstore.repository.CartRepository;
import com.watchstore.repository.OrderRepository;
import com.watchstore.repository.ReturnRepository;
import com.watchstore.repository.WarrantyRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/orders/*")
public class OrderController extends HttpServlet {
    private OrderRepository orders;
    private CartRepository cart;
    private AddressRepository addresses;
    private ReturnRepository returnRepo;
    private WarrantyRepository warrantyRepo;

    @Override
    public void init() {
        orders = (OrderRepository) getServletContext().getAttribute("orderRepository");
        cart = (CartRepository) getServletContext().getAttribute("cartRepository");
        addresses = (AddressRepository) getServletContext().getAttribute("addressRepository");
        returnRepo = (ReturnRepository) getServletContext().getAttribute("returnRepository");
        if (returnRepo == null) returnRepo = new ReturnRepository();
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
                Order o = orders.findByCode(req.getParameter("code"));
                if (o == null || o.getUserId() != u.getId()) {
                    resp.sendError(404);
                    return;
                }
                req.setAttribute("order", o);
                req.setAttribute("orderItems", orders.getOrderItems(o.getId()));
                ViewRouter.customer(req, resp, "customer/order-detail", "Chi tiết đơn hàng");
                return;
            }
            req.setAttribute("orders", orders.findByCustomerId(u.getId()));
            ViewRouter.customer(req, resp, "customer/order-list", "Đơn hàng của tôi");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User u = (User) req.getSession().getAttribute("user");
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        String path = req.getPathInfo() == null ? "/place" : req.getPathInfo();
        try {
            if ("/place".equals(path)) {
                int addressId = parsePositive(req.getParameter("addressId"), "Địa chỉ giao hàng không hợp lệ.");
                String payment = req.getParameter("payment");
                if (!"COD".equals(payment) && !"BANK_TRANSFER".equals(payment))
                    throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");
                String voucher = req.getParameter("voucherCode");
                if (voucher != null && voucher.trim().length() > 50)
                    throw new IllegalArgumentException("Mã voucher quá dài.");
                String note = req.getParameter("note");
                if (note != null && note.length() > 500)
                    throw new IllegalArgumentException("Ghi chú không được vượt quá 500 ký tự.");
                long id = orders.createFromCart(u.getId(), addressId, voucher, payment, note);
                Order o = orders.findById((int) id);
                req.getSession().setAttribute("flash", "Đặt hàng thành công. Mã đơn: " + o.getCode());
                resp.sendRedirect(req.getContextPath() + "/orders/detail?code=" + o.getCode());
                return;
            }
            if ("/cancel".equals(path)) {
                long orderId = parseLongPositive(req.getParameter("orderId"), "Đơn hàng không hợp lệ.");
                String reason = req.getParameter("reason");
                if (reason == null || reason.trim().length() < 3 || reason.trim().length() > 500)
                    throw new IllegalArgumentException("Lý do hủy phải từ 3 đến 500 ký tự.");
                orders.cancel(orderId, u.getId(), reason.trim());
                req.getSession().setAttribute("flash", "Đã hủy đơn hàng.");
                resp.sendRedirect(req.getContextPath() + "/orders/list");
                return;
            }
            if ("/return".equals(path)) {
                int orderId = parsePositive(req.getParameter("orderId"), "Đơn hàng không hợp lệ.");
                Order o = orders.findById(orderId);
                if (o == null || o.getUserId() != u.getId()) throw new IllegalArgumentException("Đơn hàng không hợp lệ.");
                if (o.getStatus() != OrderStatus.COMPLETED) throw new IllegalArgumentException("Chỉ được yêu cầu đổi trả với đơn hàng đã hoàn thành.");
                String productName = req.getParameter("productName");
                if (productName == null || productName.isBlank()) throw new IllegalArgumentException("Vui lòng chọn sản phẩm cần đổi trả.");
                String reason = req.getParameter("reason");
                if (reason == null || reason.trim().length() < 3) throw new IllegalArgumentException("Vui lòng nhập lý do đổi trả.");

                returnRepo.createReturn(orderId, null, u.getId(), productName.trim(), reason.trim());
                req.getSession().setAttribute("flash", "Đã gửi yêu cầu đổi trả thành công. Nhân viên sẽ liên hệ để xử lý.");
                resp.sendRedirect(req.getContextPath() + "/orders/detail?code=" + o.getCode());
                return;
            }
            if ("/warranty".equals(path)) {
                int orderId = parsePositive(req.getParameter("orderId"), "Đơn hàng không hợp lệ.");
                Order o = orders.findById(orderId);
                if (o == null || o.getUserId() != u.getId()) throw new IllegalArgumentException("Đơn hàng không hợp lệ.");
                if (o.getStatus() != OrderStatus.COMPLETED) throw new IllegalArgumentException("Chỉ được yêu cầu bảo hành với đơn hàng đã hoàn thành.");
                String productName = req.getParameter("productName");
                if (productName == null || productName.isBlank()) throw new IllegalArgumentException("Vui lòng chọn sản phẩm cần bảo hành.");
                String note = req.getParameter("note");
                if (note == null || note.trim().length() < 3) throw new IllegalArgumentException("Vui lòng mô tả vấn đề sản phẩm gặp phải.");

                warrantyRepo.insert(orderId, productName.trim(), "", 12, note.trim());
                req.getSession().setAttribute("flash", "Đã gửi yêu cầu bảo hành thành công. Nhân viên sẽ kiểm tra và phản hồi.");
                resp.sendRedirect(req.getContextPath() + "/orders/detail?code=" + o.getCode());
                return;
            }
        } catch (Exception e) {
            req.getSession().setAttribute("flash", root(e));
            resp.sendRedirect(req.getContextPath() + "/orders/list");
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
        return t.getMessage() == null ? "Thao tác thất bại." : t.getMessage();
    }
}
