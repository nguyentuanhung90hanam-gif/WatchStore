package com.watchstore.controller.sales;

import com.watchstore.config.DBContext;
import com.watchstore.model.Customer;
import com.watchstore.model.Order;
import com.watchstore.enums.OrderStatus;
import com.watchstore.model.User; // Đã bổ sung import User
import com.watchstore.repository.CustomerRepository;
import com.watchstore.repository.OrderRepository;
import com.watchstore.repository.WarrantyRepository;
import com.watchstore.util.ViewRouter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/manage/sales/*")
public class SalesController extends HttpServlet {

    private CustomerRepository customerRepository;
    private OrderRepository orderRepository;
    private WarrantyRepository warrantyRepository;

    private static final Map<String, String[]> PAGES = Map.ofEntries(
            Map.entry("/dashboard", new String[]{"dashboard", "Tổng quan bán hàng"}),
            Map.entry("/orders", new String[]{"order-list", "Quản lý đơn hàng"}),
            Map.entry("/order-detail", new String[]{"order-detail", "Chi tiết đơn hàng"}),
            Map.entry("/customers", new String[]{"customer-list", "Danh sách khách hàng"}),
            Map.entry("/customer-detail", new String[]{"customer-detail", "Chi tiết khách hàng"}),
            Map.entry("/customer-add", new String[]{"customer-add", "Thêm khách hàng"}),
            Map.entry("/reviews", new String[]{"review", "Kiểm duyệt đánh giá"}),
            Map.entry("/comments", new String[]{"comment", "Bình luận"}),
            Map.entry("/delivery", new String[]{"delivery", "Vận chuyển"}),
            Map.entry("/returns", new String[]{"return", "Yêu cầu đổi trả"}),
            Map.entry("/warranty", new String[]{"warranty", "Quản lý bảo hành"}),
            Map.entry("/report", new String[]{"report", "Báo cáo bán hàng"})
    );

    @Override
    public void init() {
        customerRepository = (CustomerRepository) getServletContext().getAttribute("customerRepository");
        orderRepository = (OrderRepository) getServletContext().getAttribute("orderRepository");
        warrantyRepository = (WarrantyRepository) getServletContext().getAttribute("warrantyRepository");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();

        if (path == null || path.isBlank() || "/dashboard".equals(path)) {
            showDashboard(req, resp);
            return;
        }

        if ("/customers".equals(path)) {
            showCustomers(req, resp);
            return;
        }

        if ("/customer-add".equals(path)) {
            req.setAttribute("moduleTitle", "Thêm khách hàng");
            ViewRouter.admin(req, resp, "sales/customer-add", "Thêm khách hàng", "sales");
            return;
        }

        if ("/customer-detail".equals(path)) {
            showCustomerDetail(req, resp);
            return;
        }

        if ("/orders".equals(path)) {
            showOrders(req, resp);
            return;
        }

        if ("/order-add".equals(path)) {
            req.setAttribute("moduleTitle", "Thêm đơn hàng");
            ViewRouter.admin(req, resp, "sales/order-add", "Thêm đơn hàng", "sales");
            return;
        }

        if ("/order-edit".equals(path)) {
            showOrderEdit(req, resp);
            return;
        }

        if ("/order-detail".equals(path)) {
            showOrderDetail(req, resp);
            return;
        }

        if ("/warranty".equals(path)) {
            showWarranty(req, resp);
            return;
        }

        if ("/warranty-add".equals(path) && "GET".equals(req.getMethod())) {
            req.setAttribute("moduleTitle", "Thêm phiếu bảo hành");
            req.setAttribute("orders", orderRepository != null ? orderRepository.findAll() : java.util.Collections.emptyList());
            ViewRouter.admin(req, resp, "sales/warranty-add", "Thêm phiếu bảo hành", "sales");
            return;
        }

        if ("/report".equals(path)) {
            showReport(req, resp);
            return;
        }

        if ("/delivery".equals(path)) {
            showDelivery(req, resp);
            return;
        }

        if ("/returns".equals(path)) {
            showReturns(req, resp);
            return;
        }

        if ("/reviews".equals(path)) {
            showReviews(req, resp);
            return;
        }

        if ("/comments".equals(path)) {
            showComments(req, resp);
            return;
        }

        String[] page = PAGES.getOrDefault(path, PAGES.get("/dashboard"));
        req.setAttribute("orders", orderRepository != null ? orderRepository.findAll() : java.util.Collections.emptyList());
        req.setAttribute("moduleTitle", page[1]);
        ViewRouter.admin(req, resp, "sales/" + page[0], page[1], "sales");
    }

    private void showDashboard(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Order> orders = orderRepository != null ? orderRepository.findAll() : java.util.Collections.emptyList();

        double totalRevenue = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .mapToDouble(o -> o.getTotal() != null ? o.getTotal().doubleValue() : 0.0)
                .sum();

        long completedCount = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .count();

        long pendingConfirmCount = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .count();

        long processingCount = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CONFIRMED)
                .count();

        long shippingCount = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.SHIPPING)
                .count();

        List<Map<String, Object>> warranties = warrantyRepository != null ? warrantyRepository.findAll() : new ArrayList<>();
        long pendingWarrantyCount = warranties.stream()
                .filter(w -> {
                    String st = String.valueOf(w.get("status"));
                    return "Đang bảo hành".equalsIgnoreCase(st) || "ACTIVE".equalsIgnoreCase(st) ||
                            "Đang sửa chữa".equalsIgnoreCase(st) || "REPAIR".equalsIgnoreCase(st) ||
                            "Đã sửa xong".equalsIgnoreCase(st);
                })
                .count();

        req.setAttribute("orders", orders);
        req.setAttribute("revenue", totalRevenue);
        req.setAttribute("completedOrders", completedCount);
        req.setAttribute("pendingConfirmOrders", pendingConfirmCount);
        req.setAttribute("processingOrders", processingCount);
        req.setAttribute("shippingOrders", shippingCount);
        req.setAttribute("warrantyCount", pendingWarrantyCount);
        req.setAttribute("moduleTitle", "Tổng quan bán hàng");

        ViewRouter.admin(req, resp, "sales/dashboard", "Tổng quan bán hàng", "sales");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();

        if ("/customer-add".equals(path)) {
            createCustomer(req, resp);
            return;
        }

        if ("/customer-detail".equals(path)) {
            updateCustomer(req, resp);
            return;
        }

        if ("/order-add".equals(path)) {
            createOrder(req, resp);
            return;
        }

        if ("/order-edit".equals(path)) {
            updateOrderDetails(req, resp);
            return;
        }

        if ("/order-update-shipping".equals(path)) {
            updateOrderShipping(req, resp);
            return;
        }

        if ("/order-confirm".equals(path)) {
            confirmOrder(req, resp);
            return;
        }

        if ("/order-cancel".equals(path)) {
            cancelOrder(req, resp);
            return;
        }

        if ("/order-detail".equals(path)) {
            updateOrderStatus(req, resp);
            return;
        }

        if ("/warranty-add".equals(path)) {
            createWarranty(req, resp);
            return;
        }

        if ("/warranty".equals(path)) {
            updateWarrantyStatus(req, resp);
            return;
        }

        if ("/returns".equals(path)) {
            updateReturnStatus(req, resp);
            return;
        }

        if ("/reviews".equals(path)) {
            updateReviewStatus(req, resp);
            return;
        }

        if ("/comments".equals(path)) {
            updateCommentStatus(req, resp);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/manage/sales/dashboard");
    }

    private void showCustomers(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String keyword = req.getParameter("keyword");
        List<Customer> customers;

        if (customerRepository != null) {
            if (keyword == null || keyword.trim().isEmpty()) {
                customers = customerRepository.findAll();
            } else {
                customers = customerRepository.search(keyword.trim());
            }
        } else {
            customers = new ArrayList<>();
        }

        req.setAttribute("customers", customers);
        req.setAttribute("keyword", keyword);
        req.setAttribute("moduleTitle", "Danh sách khách hàng");

        ViewRouter.admin(req, resp, "sales/customer-list", "Danh sách khách hàng", "sales");
    }

    private void showCustomerDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idParam = req.getParameter("id");

        if (idParam == null || idParam.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/manage/sales/customers");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            Customer customer = customerRepository != null ? customerRepository.findById(id) : null;

            if (customer == null) {
                resp.sendRedirect(req.getContextPath() + "/manage/sales/customers");
                return;
            }

            List<Order> customerOrders = orderRepository != null ? orderRepository.findByCustomerId(id) : new ArrayList<>();

            long totalOrdersCount = customerOrders.size();
            BigDecimal totalAmountSpent = customerOrders.stream()
                    .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                    .map(Order::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            req.setAttribute("customer", customer);
            req.setAttribute("customerOrders", customerOrders);
            req.setAttribute("totalOrdersCount", totalOrdersCount);
            req.setAttribute("totalAmountSpent", totalAmountSpent);
            req.setAttribute("moduleTitle", "Chi tiết khách hàng");

            ViewRouter.admin(req, resp, "sales/customer-detail", "Chi tiết khách hàng", "sales");

        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/manage/sales/customers");
        }
    }

    private void showOrderDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idParam = req.getParameter("id");

        if (idParam == null || idParam.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            Order order = orderRepository != null ? orderRepository.findById(id) : null;

            if (order == null) {
                resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
                return;
            }

            req.setAttribute("order", order);
            req.setAttribute("orderItems", orderRepository != null ? orderRepository.getOrderItems(id) : new ArrayList<>());
            req.setAttribute("moduleTitle", "Chi tiết đơn hàng");

            ViewRouter.admin(req, resp, "sales/order-detail", "Chi tiết đơn hàng", "sales");

        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
        }
    }

    private void createCustomer(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");

        Customer customer = new Customer();
        customer.setFullName(fullName);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setAddress(address);

        boolean success = customerRepository != null && customerRepository.insert(customer);
        if (success) {
            req.getSession().setAttribute("flash", "Thêm khách hàng thành công!");
            resp.sendRedirect(req.getContextPath() + "/manage/sales/customers");
        } else {
            req.getSession().setAttribute("flash", "Lỗi: Không thể thêm khách hàng (Có thể Email đã tồn tại).");
            resp.sendRedirect(req.getContextPath() + "/manage/sales/customer-add");
        }
    }

    private void updateCustomer(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/manage/sales/customers");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            String fullName = req.getParameter("fullName");
            String email = req.getParameter("email");
            String phone = req.getParameter("phone");
            String address = req.getParameter("address");

            Customer customer = customerRepository != null ? customerRepository.findById(id) : null;

            if (customer == null) {
                resp.sendRedirect(req.getContextPath() + "/manage/sales/customers");
                return;
            }

            customer.setFullName(fullName);
            customer.setEmail(email);
            customer.setPhone(phone);
            customer.setAddress(address);

            if (customerRepository != null) customerRepository.update(customer);
            req.getSession().setAttribute("flash", "Cập nhật thông tin khách hàng thành công!");

            resp.sendRedirect(req.getContextPath() + "/manage/sales/customer-detail?id=" + id);

        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/manage/sales/customers");
        }
    }

    private void updateOrderStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String idParam = req.getParameter("id");
        String status = req.getParameter("status");
        if (idParam != null && status != null) {
            try {
                int id = Integer.parseInt(idParam);
                if (orderRepository != null) orderRepository.updateStatus(id, status);
                req.getSession().setAttribute("flash", "Cập nhật trạng thái đơn hàng thành công!");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/order-detail?id=" + id);
                return;
            } catch (NumberFormatException ignored) {}
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
    }

    private void showWarranty(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String status  = req.getParameter("status");
        req.setAttribute("warranties", warrantyRepository != null ? warrantyRepository.search(keyword, status) : Collections.emptyList());
        req.setAttribute("moduleTitle", "Quản lý bảo hành");
        ViewRouter.admin(req, resp, "sales/warranty", "Quản lý bảo hành", "sales");
    }

    private void createWarranty(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (warrantyRepository == null) {
            req.getSession().setAttribute("flash", "Lỗi: Chức năng bảo hành chưa khởi tạo.");
            resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
            return;
        }
        try {
            int orderId      = Integer.parseInt(req.getParameter("orderId"));
            String productName = req.getParameter("productName");
            String serial    = req.getParameter("serial");
            int months       = Integer.parseInt(req.getParameter("months"));
            String note      = req.getParameter("note");

            if (orderRepository == null) {
                req.getSession().setAttribute("flash", "Lỗi hệ thống: Không thể kết nối cơ sở dữ liệu đơn hàng.");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
                return;
            }
            Order order = orderRepository.findById(orderId);
            if (order == null) {
                req.getSession().setAttribute("flash", "Lỗi: Đơn hàng #" + orderId + " không tồn tại!");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
                return;
            }

            List<Map<String, Object>> items = orderRepository.getOrderItems(orderId);
            boolean exists = items.stream().anyMatch(item -> {
                String pName = String.valueOf(item.get("ProductName"));
                String vName = String.valueOf(item.get("VariantName"));
                return productName.equalsIgnoreCase(pName) || productName.equalsIgnoreCase(vName) ||
                        pName.toLowerCase().contains(productName.toLowerCase()) ||
                        vName.toLowerCase().contains(productName.toLowerCase());
            });

            if (!exists) {
                req.getSession().setAttribute("flash", "Lỗi: Sản phẩm '" + productName + "' không có trong đơn hàng #" + orderId + "!");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
                return;
            }

            warrantyRepository.insert(orderId, productName, serial, months, note);
            req.getSession().setAttribute("flash", "Đã tạo phiếu bảo hành thành công!");
        } catch (NumberFormatException e) {
            req.getSession().setAttribute("flash", "Lỗi: Định dạng mã đơn hàng hoặc thời hạn không hợp lệ.");
        } catch (Exception e) {
            req.getSession().setAttribute("flash", "Lỗi khi tạo phiếu bảo hành: " + e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
    }

    private void updateWarrantyStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String idParam = req.getParameter("id");
        String status  = req.getParameter("status");
        String action  = req.getParameter("action");

        if (warrantyRepository != null && idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                if ("receive".equalsIgnoreCase(action)) {
                    String receiveDateStr = req.getParameter("receiveDate");
                    String receiveNote = req.getParameter("receiveNote");
                    java.sql.Date receiveDate = java.sql.Date.valueOf(receiveDateStr);
                    warrantyRepository.updateReceive(id, receiveDate, receiveNote);
                    req.getSession().setAttribute("flash", "Đã tiếp nhận sản phẩm bảo hành thành công!");
                }
                else if ("repair".equalsIgnoreCase(action)) {
                    String repairContent = req.getParameter("repairContent");
                    String componentReplaced = req.getParameter("componentReplaced");
                    String repairNote = req.getParameter("repairNote");
                    String completeDateStr = req.getParameter("completeDate");
                    java.sql.Date completeDate = java.sql.Date.valueOf(completeDateStr);
                    warrantyRepository.updateRepair(id, repairContent, componentReplaced, repairNote, completeDate);
                    req.getSession().setAttribute("flash", "Đã ghi nhận kết quả sửa chữa thành công!");
                }
                else if ("return".equalsIgnoreCase(action)) {
                    String returnDateStr = req.getParameter("returnDate");
                    java.sql.Date returnDate = java.sql.Date.valueOf(returnDateStr);
                    warrantyRepository.updateReturn(id, returnDate);
                    req.getSession().setAttribute("flash", "Đã xác nhận trả máy cho khách thành công!");
                }
                else if (status != null) {
                    warrantyRepository.updateStatus(id, status);
                    req.getSession().setAttribute("flash", "Đã cập nhật trạng thái bảo hành!");
                }
            } catch (Exception e) {
                req.getSession().setAttribute("flash", "Lỗi thao tác bảo hành: " + e.getMessage());
            }
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
    }

    private void showOrders(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String status  = req.getParameter("status");
        String fromDate = req.getParameter("fromDate");
        String toDate   = req.getParameter("toDate");

        List<Order> orders = orderRepository != null ? orderRepository.search(keyword, status, fromDate, toDate) : java.util.Collections.emptyList();
        List<Order> allOrders = orderRepository != null ? orderRepository.findAll() : java.util.Collections.emptyList();

        long totalCount = allOrders.size();
        long pendingCount = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long shippingCount = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPING).count();
        long completedCount = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        long cancelledCount = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();

        req.setAttribute("orders", orders);
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
        req.setAttribute("fromDate", fromDate);
        req.setAttribute("toDate", toDate);
        req.setAttribute("totalCount", totalCount);
        req.setAttribute("pendingCount", pendingCount);
        req.setAttribute("shippingCount", shippingCount);
        req.setAttribute("completedCount", completedCount);
        req.setAttribute("cancelledCount", cancelledCount);
        req.setAttribute("moduleTitle", "Quản lý đơn hàng");

        ViewRouter.admin(req, resp, "sales/order-list", "Quản lý đơn hàng", "sales");
    }

    private void showReport(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String status   = req.getParameter("status");
        String fromDate = req.getParameter("fromDate");
        String toDate   = req.getParameter("toDate");

        List<Order> allOrders = orderRepository != null ? orderRepository.findAll() : java.util.Collections.emptyList();
        List<Order> filtered  = new ArrayList<>(allOrders);

        if (status != null && !status.isBlank()) {
            String st = status.trim().toUpperCase();
            filtered = filtered.stream().filter(o -> {
                String s = o.getStatus() != null ? o.getStatus().name() : "";
                if ("COMPLETED".equals(st) || "HOÀN THÀNH".equals(st)) return "COMPLETED".equals(s) || "HOÀN THÀNH".equals(s);
                if ("SHIPPING".equals(st)  || "ĐANG GIAO".equals(st))  return "SHIPPING".equals(s) || "DELIVERED".equals(s) || "ĐANG GIAO".equals(s);
                if ("PENDING".equals(st)   || "ĐANG XỬ LÝ".equals(st)) return "PENDING".equals(s) || "CONFIRMED".equals(s) || "PACKING".equals(s) || "ĐANG XỬ LÝ".equals(s);
                if ("CANCELLED".equals(st) || "ĐÃ HỦY".equals(st))    return "CANCELLED".equals(s) || "ĐÃ HỦY".equals(s);
                return s.equals(st);
            }).toList();
        }

        if (fromDate != null && !fromDate.isBlank()) {
            try {
                LocalDate fd = LocalDate.parse(fromDate);
                filtered = filtered.stream().filter(o -> {
                    if (o.getCreatedAt() == null) return false;
                    LocalDate od = o.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return !od.isBefore(fd);
                }).toList();
            } catch (Exception ignored) {}
        }
        if (toDate != null && !toDate.isBlank()) {
            try {
                LocalDate td = LocalDate.parse(toDate);
                filtered = filtered.stream().filter(o -> {
                    if (o.getCreatedAt() == null) return false;
                    LocalDate od = o.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return !od.isAfter(td);
                }).toList();
            } catch (Exception ignored) {}
        }

        long totalOrders = filtered.size();
        long pendingOrders = filtered.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.CONFIRMED)
                .count();
        long shippingOrders = filtered.stream()
                .filter(o -> o.getStatus() == OrderStatus.SHIPPING)
                .count();
        long completedOrders = filtered.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .count();
        long cancelledOrders = filtered.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
                .count();
        double totalRevenue = filtered.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .mapToDouble(o -> o.getTotal() != null ? o.getTotal().doubleValue() : 0.0)
                .sum();

        req.setAttribute("reportOrders", filtered);
        req.setAttribute("totalOrders", totalOrders);
        req.setAttribute("pendingOrders", pendingOrders);
        req.setAttribute("shippingOrders", shippingOrders);
        req.setAttribute("completedOrders", completedOrders);
        req.setAttribute("cancelledOrders", cancelledOrders);
        req.setAttribute("totalRevenue", String.format("%,.0f", totalRevenue));
        req.setAttribute("status", status);
        req.setAttribute("fromDate", fromDate);
        req.setAttribute("toDate", toDate);
        req.setAttribute("moduleTitle", "Báo cáo bán hàng");

        ViewRouter.admin(req, resp, "sales/report", "Báo cáo bán hàng", "sales");
    }

    private List<Map<String, Object>> getSampleReviews() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(new HashMap<>(Map.ofEntries(
                Map.entry("id", 1),
                Map.entry("customerName", "Lê Thành Công"),
                Map.entry("email", "cong.le@example.com"),
                Map.entry("phone", "0988000007"),
                Map.entry("productName", "Rolex Datejust 41"),
                Map.entry("rating", 5),
                Map.entry("content", "Đồng hồ chạy cực chuẩn, mẫu đẹp hơn mong đợi!"),
                Map.entry("status", "APPROVED"),
                Map.entry("createdAt", "2026-08-05"),
                Map.entry("orderCode", "WS8504"),
                Map.entry("reply", "Cảm ơn anh Lê Thành Công đã tin tưởng lựa chọn Rolex tại WatchStore. Rất mong được phục vụ anh trong các đơn hàng tới!")
        )));
        list.add(new HashMap<>(Map.ofEntries(
                Map.entry("id", 2),
                Map.entry("customerName", "Trần Minh Đức"),
                Map.entry("email", "duc.tran@example.com"),
                Map.entry("phone", "0988000006"),
                Map.entry("productName", "Casio G-Shock GA-2100"),
                Map.entry("rating", 4),
                Map.entry("content", "Giao hàng nhanh, đóng gói chắc chắn."),
                Map.entry("status", "PENDING"),
                Map.entry("createdAt", "2026-08-04"),
                Map.entry("orderCode", "WS8503"),
                Map.entry("reply", "")
        )));
        list.add(new HashMap<>(Map.ofEntries(
                Map.entry("id", 3),
                Map.entry("customerName", "Nguyễn Văn An"),
                Map.entry("email", "an.nguyen@example.com"),
                Map.entry("phone", "0988000005"),
                Map.entry("productName", "Seiko 5 Sports Automatic"),
                Map.entry("rating", 5),
                Map.entry("content", "Máy cơ bền bỉ, tích cót lâu."),
                Map.entry("status", "APPROVED"),
                Map.entry("createdAt", "2026-08-02"),
                Map.entry("orderCode", "WS8502"),
                Map.entry("reply", "")
        )));
        list.add(new HashMap<>(Map.ofEntries(
                Map.entry("id", 4),
                Map.entry("customerName", "Phạm Quốc Bảo"),
                Map.entry("email", "bao.pham@example.com"),
                Map.entry("phone", "0988000004"),
                Map.entry("productName", "Citizen Eco-Drive"),
                Map.entry("rating", 1),
                Map.entry("content", "Hàng bị trầy xước nhẹ ở mặt kính."),
                Map.entry("status", "REJECTED"),
                Map.entry("createdAt", "2026-08-01"),
                Map.entry("orderCode", "WS8501"),
                Map.entry("reply", "")
        )));
        return list;
    }

    private List<Map<String, Object>> getSampleComments() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("id", 1, "customerName", "Lê Thành Công", "productName", "Rolex Datejust 41", "content", "Mẫu này còn hàng màu đơmi vàng không shop?", "commentDate", "2026-08-05 14:20", "status", "APPROVED"));
        list.add(Map.of("id", 2, "customerName", "Trần Minh Đức", "productName", "Citizen Eco-Drive", "content", "Shop có hỗ trợ trả góp qua thẻ tín dụng không?", "commentDate", "2026-08-04 10:15", "status", "APPROVED"));
        list.add(Map.of("id", 3, "customerName", "Nguyễn Văn An", "productName", "Orient Bambino Gen 2", "content", "Bảo hành tại showroom Hà Nội hay chuyển phát về shop?", "commentDate", "2026-08-03 09:45", "status", "PENDING"));
        list.add(Map.of("id", 4, "customerName", "Hoàng Kim Ngân", "productName", "Casio G-Shock", "content", "Spam quảng cáo vô nghĩa.", "commentDate", "2026-08-02 11:10", "status", "HIDDEN"));
        return list;
    }

    private void showReviews(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String status  = req.getParameter("status");
        String ratingParam = req.getParameter("rating");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> reviews = (List<Map<String, Object>>) req.getSession().getAttribute("sampleReviews");
        if (reviews == null) {
            reviews = getSampleReviews();
            req.getSession().setAttribute("sampleReviews", reviews);
        }

        List<Map<String, Object>> filtered = new ArrayList<>(reviews);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim().toLowerCase();
            filtered = filtered.stream().filter(r ->
                    (r.get("customerName") != null && r.get("customerName").toString().toLowerCase().contains(k)) ||
                            (r.get("productName")  != null && r.get("productName").toString().toLowerCase().contains(k))  ||
                            (r.get("content")      != null && r.get("content").toString().toLowerCase().contains(k))
            ).toList();
        }

        if (status != null && !status.trim().isEmpty()) {
            String st = status.trim();
            filtered = filtered.stream().filter(r ->
                    st.equalsIgnoreCase(r.get("status").toString()) ||
                            (st.equals("Chờ duyệt") && "PENDING".equalsIgnoreCase(r.get("status").toString())) ||
                            (st.equals("Đã duyệt")  && "APPROVED".equalsIgnoreCase(r.get("status").toString())) ||
                            (st.equals("Đã từ chối") && "REJECTED".equalsIgnoreCase(r.get("status").toString()))
            ).toList();
        }

        if (ratingParam != null && !ratingParam.trim().isEmpty()) {
            try {
                int rVal = Integer.parseInt(ratingParam.trim());
                filtered = filtered.stream().filter(r ->
                        Integer.valueOf(rVal).equals(r.get("rating"))
                ).toList();
            } catch (NumberFormatException ignored) {}
        }

        req.setAttribute("reviews", filtered);
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
        req.setAttribute("rating", ratingParam);
        req.setAttribute("moduleTitle", "Kiểm duyệt đánh giá");
        ViewRouter.admin(req, resp, "sales/review", "Kiểm duyệt đánh giá", "sales");
    }

    private void showComments(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String status  = req.getParameter("status");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> comments = (List<Map<String, Object>>) req.getSession().getAttribute("sampleComments");
        if (comments == null) {
            comments = getSampleComments();
            req.getSession().setAttribute("sampleComments", comments);
        }
        List<Map<String, Object>> filtered = new ArrayList<>(comments);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim().toLowerCase();
            filtered = filtered.stream().filter(c ->
                    (c.get("customerName") != null && c.get("customerName").toString().toLowerCase().contains(k)) ||
                            (c.get("productName")  != null && c.get("productName").toString().toLowerCase().contains(k))  ||
                            (c.get("content")      != null && c.get("content").toString().toLowerCase().contains(k))
            ).toList();
        }

        if (status != null && !status.trim().isEmpty()) {
            String st = status.trim();
            filtered = filtered.stream().filter(c ->
                    st.equalsIgnoreCase(c.get("status").toString()) ||
                            (st.equals("Chờ duyệt") && "PENDING".equalsIgnoreCase(c.get("status").toString())) ||
                            (st.equals("Hiển thị")  && "APPROVED".equalsIgnoreCase(c.get("status").toString())) ||
                            (st.equals("Ẩn")        && "HIDDEN".equalsIgnoreCase(c.get("status").toString()))
            ).toList();
        }

        req.setAttribute("comments", filtered);
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
        req.setAttribute("moduleTitle", "Bình luận");
        ViewRouter.admin(req, resp, "sales/comment", "Bình luận", "sales");
    }

    private void updateReviewStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String status  = req.getParameter("status");
        String action  = req.getParameter("action");
        String replyContent = req.getParameter("replyContent");

        if (status == null && action != null) {
            if ("approve".equalsIgnoreCase(action)) status = "APPROVED";
            else if ("reject".equalsIgnoreCase(action)) status = "REJECTED";
        }

        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> reviews = (List<Map<String, Object>>) req.getSession().getAttribute("sampleReviews");
                if (reviews == null) {
                    reviews = getSampleReviews();
                }
                for (int i = 0; i < reviews.size(); i++) {
                    Map<String, Object> r = reviews.get(i);
                    if (Integer.valueOf(id).equals(r.get("id"))) {
                        Map<String, Object> updated = new HashMap<>(r);
                        if (status != null) {
                            updated.put("status", status);
                        }
                        if (replyContent != null) {
                            updated.put("reply", replyContent.trim());
                            updated.put("status", "APPROVED");
                        }
                        reviews.set(i, updated);
                        break;
                    }
                }
                req.getSession().setAttribute("sampleReviews", reviews);
                if (replyContent != null) {
                    req.getSession().setAttribute("flash", "Đã gửi phản hồi đánh giá thành công!");
                } else {
                    req.getSession().setAttribute("flash", "Đã cập nhật trạng thái đánh giá thành công!");
                }
            } catch (Exception ignored) {}
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/reviews");
    }

    private void updateCommentStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String status  = req.getParameter("status");

        if (idParam != null && status != null) {
            try {
                int id = Integer.parseInt(idParam);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> comments = (List<Map<String, Object>>) req.getSession().getAttribute("sampleComments");
                if (comments == null) {
                    comments = getSampleComments();
                }
                for (int i = 0; i < comments.size(); i++) {
                    Map<String, Object> c = comments.get(i);
                    if (Integer.valueOf(id).equals(c.get("id"))) {
                        Map<String, Object> updated = new HashMap<>(c);
                        updated.put("status", status);
                        comments.set(i, updated);
                        break;
                    }
                }
                req.getSession().setAttribute("sampleComments", comments);
                req.getSession().setAttribute("flash", "Đã xác nhận cập nhật trạng thái bình luận thành công!");
            } catch (Exception ignored) {}
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/comments");
    }

    private void showOrderEdit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
            return;
        }
        try {
            int id = Integer.parseInt(idParam);
            Order order = orderRepository != null ? orderRepository.findById(id) : null;
            if (order == null) {
                resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
                return;
            }
            if (order.getStatus() == OrderStatus.COMPLETED) {
                req.getSession().setAttribute("flash", "Lỗi: Không thể chỉnh sửa đơn hàng đã hoàn thành!");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/order-detail?id=" + id);
                return;
            }
            req.setAttribute("order", order);
            req.setAttribute("moduleTitle", "Sửa đơn hàng");
            ViewRouter.admin(req, resp, "sales/order-edit", "Sửa đơn hàng", "sales");
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
        }
    }

    private void createOrder(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String customerName = req.getParameter("customerName");
        String phone = req.getParameter("phone");
        String shippingAddress = req.getParameter("shippingAddress");
        String totalPriceStr = req.getParameter("totalPrice");
        String status = req.getParameter("status");
        String paymentStatus = req.getParameter("paymentStatus");

        try {
            BigDecimal totalPrice = new BigDecimal(totalPriceStr);
            String code = "WS" + (8500 + (orderRepository != null ? orderRepository.findAll().size() : 0) + 1);

            Order order = new Order();
            order.setCode(code);
            order.setCustomerName(customerName);
            order.setPhone(phone);
            order.setShippingAddress(shippingAddress);
            order.setTotalPrice(totalPrice);
            order.setStatus(status);
            order.setPaymentStatus(paymentStatus != null ? paymentStatus : "UNPAID");
            order.setUserId(4);
            order.setCreatedAt(new java.util.Date());

            if (orderRepository != null) {
                orderRepository.add(order);
                req.getSession().setAttribute("flash", "Thêm đơn hàng thành công! Mã đơn: " + code);
            } else {
                req.getSession().setAttribute("flash", "Lỗi: Không tìm thấy orderRepository.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("flash", "Lỗi khi thêm đơn hàng: " + e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
    }

    private void updateOrderDetails(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String idParam = req.getParameter("id");
        String customerName = req.getParameter("customerName");
        String phone = req.getParameter("phone");
        String shippingAddress = req.getParameter("shippingAddress");
        String totalPriceStr = req.getParameter("totalPrice");
        String status = req.getParameter("status");
        String paymentStatus = req.getParameter("paymentStatus");

        if (idParam == null || idParam.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            BigDecimal totalPrice = new BigDecimal(totalPriceStr);

            Order order = orderRepository != null ? orderRepository.findById(id) : null;
            if (order != null) {
                if (order.getStatus() == OrderStatus.COMPLETED) {
                    req.getSession().setAttribute("flash", "Lỗi: Không thể chỉnh sửa đơn hàng đã hoàn thành!");
                    resp.sendRedirect(req.getContextPath() + "/manage/sales/order-detail?id=" + id);
                    return;
                }
                order.setCustomerName(customerName);
                order.setPhone(phone);
                order.setShippingAddress(shippingAddress);
                order.setTotalPrice(totalPrice);
                order.setStatus(status);
                if (paymentStatus != null) {
                    order.setPaymentStatus(paymentStatus);
                }

                orderRepository.update(order);
                req.getSession().setAttribute("flash", "Cập nhật đơn hàng thành công!");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/order-detail?id=" + id);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("flash", "Lỗi khi cập nhật đơn hàng: " + e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
    }

    private void deleteOrder(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                if (orderRepository != null && orderRepository.delete(id)) {
                    req.getSession().setAttribute("flash", "Đã xóa đơn hàng thành công!");
                } else {
                    req.getSession().setAttribute("flash", "Lỗi khi xóa đơn hàng.");
                }
            } catch (NumberFormatException ignored) {}
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
    }

    private void confirmOrder(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                if (orderRepository != null && orderRepository.updateStatus(id, "CONFIRMED")) {
                    req.getSession().setAttribute("flash", "Đã xác nhận đơn hàng thành công!");
                } else {
                    req.getSession().setAttribute("flash", "Lỗi khi xác nhận đơn hàng.");
                }
            } catch (NumberFormatException ignored) {}
        }
        String referer = req.getHeader("referer");
        if (referer != null && !referer.isEmpty()) {
            resp.sendRedirect(referer);
        } else {
            resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
        }
    }

    private void cancelOrder(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                if (orderRepository != null && orderRepository.cancelOrderAndRestoreStock(id)) {
                    req.getSession().setAttribute("flash", "Đã hủy đơn hàng và hoàn lại tồn kho thành công!");
                } else {
                    req.getSession().setAttribute("flash", "Lỗi khi hủy đơn hàng.");
                }
            } catch (NumberFormatException ignored) {}
        }

        String referer = req.getHeader("referer");
        if (referer != null && !referer.isEmpty()) {
            resp.sendRedirect(referer);
        } else {
            resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
        }
    }

    private void updateOrderShipping(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String idParam = req.getParameter("id");
        String customerName = req.getParameter("customerName");
        String phone = req.getParameter("phone");
        String shippingAddress = req.getParameter("shippingAddress");
        String status = req.getParameter("status");
        String redirect = req.getParameter("redirect");

        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                Order order = orderRepository != null ? orderRepository.findById(id) : null;
                if (order != null) {
                    if (order.getStatus() == OrderStatus.COMPLETED) {
                        req.getSession().setAttribute("flash", "Lỗi: Không thể sửa thông tin giao hàng của đơn đã hoàn thành!");
                    } else {
                        if (customerName != null) order.setCustomerName(customerName);
                        if (phone != null) order.setPhone(phone);
                        if (shippingAddress != null) order.setShippingAddress(shippingAddress);

                        if (status != null && !status.trim().isEmpty()) {
                            order.setStatus(status.trim());
                        }

                        if (orderRepository != null && orderRepository.update(order)) {
                            if (status != null && !status.trim().isEmpty()) {
                                orderRepository.updateStatus(id, status.trim());
                            }
                            req.getSession().setAttribute("flash", "Cập nhật thông tin giao nhận thành công!");
                        } else {
                            req.getSession().setAttribute("flash", "Lỗi khi cập nhật thông tin giao hàng.");
                        }
                    }
                    if ("delivery".equals(redirect)) {
                        resp.sendRedirect(req.getContextPath() + "/manage/sales/delivery");
                        return;
                    }
                    resp.sendRedirect(req.getContextPath() + "/manage/sales/order-detail?id=" + id);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
    }

    private void showDelivery(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String status = req.getParameter("status");

        List<Order> orders = orderRepository != null ? orderRepository.findAll() : java.util.Collections.emptyList();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim().toLowerCase();
            orders = orders.stream().filter(o ->
                    (String.valueOf(o.getId()).contains(k)) ||
                            (o.getCode() != null && o.getCode().toLowerCase().contains(k)) ||
                            (o.getCustomerName() != null && o.getCustomerName().toLowerCase().contains(k)) ||
                            (o.getPhone() != null && o.getPhone().toLowerCase().contains(k)) ||
                            (o.getShippingAddress() != null && o.getShippingAddress().toLowerCase().contains(k))
            ).toList();
        }

        if (status != null && !status.trim().isEmpty()) {
            String st = status.trim();
            orders = orders.stream().filter(o ->
                    st.equalsIgnoreCase(o.getStatusCode()) ||
                            (st.equals("Chờ giao") && o.getStatus() == OrderStatus.CONFIRMED) ||
                            (st.equals("Đang giao") && o.getStatus() == OrderStatus.SHIPPING) ||
                            (st.equals("Giao thành công") && o.getStatus() == OrderStatus.COMPLETED) ||
                            (st.equals("Giao thất bại") && o.getStatus() == OrderStatus.CANCELLED)
            ).toList();
        }

        req.setAttribute("orders", orders);
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
        req.setAttribute("moduleTitle", "Vận chuyển");
        ViewRouter.admin(req, resp, "sales/delivery", "Vận chuyển", "sales");
    }

    private void showReturns(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String status  = req.getParameter("status");
        com.watchstore.repository.ReturnRepository returnRepo = (com.watchstore.repository.ReturnRepository) getServletContext().getAttribute("returnRepository");
        if (returnRepo == null) returnRepo = new com.watchstore.repository.ReturnRepository();
        req.setAttribute("returns", returnRepo.search(keyword, status));
        req.setAttribute("moduleTitle", "Yêu cầu đổi trả");
        ViewRouter.admin(req, resp, "sales/return", "Yêu cầu đổi trả", "sales");
    }

    private void updateReturnStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String idParam = req.getParameter("id");
        String action  = req.getParameter("action");
        String status  = req.getParameter("status");
        String note    = req.getParameter("employeeNote");
        com.watchstore.repository.ReturnRepository returnRepo = (com.watchstore.repository.ReturnRepository) getServletContext().getAttribute("returnRepository");
        if (returnRepo == null) returnRepo = new com.watchstore.repository.ReturnRepository();

        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                if ("approve".equalsIgnoreCase(action)) {
                    returnRepo.updateStatus(id, "APPROVED", note != null ? note : "Đã duyệt yêu cầu đổi trả");
                    req.getSession().setAttribute("flash", "Đã phê duyệt yêu cầu đổi trả!");
                } else if ("reject".equalsIgnoreCase(action)) {
                    returnRepo.updateStatus(id, "REJECTED", note != null ? note : "Từ chối yêu cầu đổi trả");
                    req.getSession().setAttribute("flash", "Đã từ chối yêu cầu đổi trả!");
                } else if ("receive".equalsIgnoreCase(action)) {
                    returnRepo.updateStatus(id, "ITEM_RECEIVED", note != null ? note : "Kho đã nhận hàng hoàn");
                    req.getSession().setAttribute("flash", "Đã xác nhận kho nhận hàng hoàn!");
                } else if ("refund".equalsIgnoreCase(action)) {
                    returnRepo.updateRefundStatus(id, "REFUNDED");
                    returnRepo.updateStatus(id, "COMPLETED", "Đã hoàn tiền và kết thúc quy trình đổi trả");
                    req.getSession().setAttribute("flash", "Đã ghi nhận hoàn tiền thành công!");
                } else if (status != null && !status.isBlank()) {
                    returnRepo.updateStatus(id, status, note);
                    req.getSession().setAttribute("flash", "Đã cập nhật trạng thái yêu cầu đổi trả!");
                }
            } catch (NumberFormatException ignored) {}
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/returns");
    }
}
