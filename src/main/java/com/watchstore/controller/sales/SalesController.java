package com.watchstore.controller.sales;

import com.watchstore.config.DBContext;
import com.watchstore.model.Customer;
import com.watchstore.model.Order;
import com.watchstore.enums.OrderStatus;
import com.watchstore.model.User; // Đã bổ sung import User
import com.watchstore.repository.CustomerRepository;
import com.watchstore.repository.MockDataStore;
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
            Map.entry("/dashboard", new String[] { "dashboard", "Tổng quan bán hàng" }),
            Map.entry("/orders", new String[] { "order-list", "Quản lý đơn hàng" }),
            Map.entry("/order-detail", new String[] { "order-detail", "Chi tiết đơn hàng" }),
            Map.entry("/customers", new String[] { "customer-list", "Danh sách khách hàng" }),
            Map.entry("/customer-detail", new String[] { "customer-detail", "Chi tiết khách hàng" }),
            Map.entry("/customer-add", new String[] { "customer-add", "Thêm khách hàng" }),
            Map.entry("/reviews", new String[] { "review", "Kiểm duyệt đánh giá" }),
            Map.entry("/comments", new String[] { "comment", "Bình luận" }),
            Map.entry("/delivery", new String[] { "delivery", "Vận chuyển" }),
            Map.entry("/returns", new String[] { "return", "Yêu cầu đổi trả" }),
            Map.entry("/warranty", new String[] { "warranty", "Quản lý bảo hành" }),
            Map.entry("/report", new String[] { "report", "Báo cáo bán hàng" }),
            Map.entry("/pos", new String[] { "pos", "Bán hàng tại quầy (POS)" }));

    @Override
    public void init() {
        customerRepository = (CustomerRepository) getServletContext().getAttribute("customerRepository");
        orderRepository = (OrderRepository) getServletContext().getAttribute("orderRepository");
        warrantyRepository = (WarrantyRepository) getServletContext().getAttribute("warrantyRepository");
        if (warrantyRepository != null) {
            warrantyRepository.ensureTable();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();

        if (path != null && path.startsWith("/pos")) {
            handlePOSGet(path, req, resp);
            return;
        }

        if (path == null || path.isBlank() || "/dashboard".equals(path)) {
            resp.sendRedirect(req.getContextPath() + "/manage/dashboard");
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
            req.setAttribute("orders", orderRepository != null ? orderRepository.findAll() : MockDataStore.orders());
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
        req.setAttribute("orders", orderRepository != null ? orderRepository.findAll() : MockDataStore.orders());
        req.setAttribute("moduleTitle", page[1]);
        ViewRouter.admin(req, resp, "sales/" + page[0], page[1], "sales");
    }

    private void showDashboard(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Order> orders = orderRepository != null ? orderRepository.findAll() : MockDataStore.orders();

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

        List<Map<String, Object>> warranties = warrantyRepository != null ? warrantyRepository.findAll()
                : new ArrayList<>();
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

        if (path != null && path.startsWith("/pos")) {
            handlePOSPost(path, req, resp);
            return;
        }

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

            List<Order> customerOrders = orderRepository != null ? orderRepository.findByCustomerId(id)
                    : new ArrayList<>();

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
            req.setAttribute("orderItems",
                    orderRepository != null ? orderRepository.getOrderItems(id) : new ArrayList<>());
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

            if (customerRepository != null)
                customerRepository.update(customer);
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
                if (orderRepository != null)
                    orderRepository.updateStatus(id, status);
                req.getSession().setAttribute("flash", "Cập nhật trạng thái đơn hàng thành công!");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/order-detail?id=" + id);
                return;
            } catch (NumberFormatException ignored) {
            }
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
    }

    private void showWarranty(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String status = req.getParameter("status");
        req.setAttribute("warranties",
                warrantyRepository != null ? warrantyRepository.getWarrantiesFromReturnRequests() : Collections.emptyList());
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
            int orderId = Integer.parseInt(req.getParameter("orderId"));
            String productName = req.getParameter("productName");
            String serial = req.getParameter("serial");
            int months = Integer.parseInt(req.getParameter("months"));
            String note = req.getParameter("note");

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
                req.getSession().setAttribute("flash",
                        "Lỗi: Sản phẩm '" + productName + "' không có trong đơn hàng #" + orderId + "!");
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
        String status = req.getParameter("status");
        String action = req.getParameter("action");

        if (warrantyRepository != null && idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                if ("receive".equalsIgnoreCase(action)) {
                    String receiveDateStr = req.getParameter("receiveDate");
                    String receiveNote = req.getParameter("receiveNote");
                    java.sql.Date receiveDate = java.sql.Date.valueOf(receiveDateStr);
                    warrantyRepository.updateReceive(id, receiveDate, receiveNote);
                    req.getSession().setAttribute("flash", "Đã tiếp nhận sản phẩm bảo hành thành công!");
                } else if ("repair".equalsIgnoreCase(action)) {
                    String repairContent = req.getParameter("repairContent");
                    String componentReplaced = req.getParameter("componentReplaced");
                    String repairNote = req.getParameter("repairNote");
                    String completeDateStr = req.getParameter("completeDate");
                    java.sql.Date completeDate = java.sql.Date.valueOf(completeDateStr);
                    warrantyRepository.updateRepair(id, repairContent, componentReplaced, repairNote, completeDate);
                    req.getSession().setAttribute("flash", "Đã ghi nhận kết quả sửa chữa thành công!");
                } else if ("return".equalsIgnoreCase(action)) {
                    String returnDateStr = req.getParameter("returnDate");
                    java.sql.Date returnDate = java.sql.Date.valueOf(returnDateStr);
                    warrantyRepository.updateReturn(id, returnDate);
                    req.getSession().setAttribute("flash", "Đã xác nhận trả máy cho khách thành công!");
                } else if (status != null) {
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
        String status = req.getParameter("status");
        String fromDate = req.getParameter("fromDate");
        String toDate = req.getParameter("toDate");

        List<Order> orders = orderRepository != null ? orderRepository.search(keyword, status, fromDate, toDate)
                : MockDataStore.orders();
        List<Order> allOrders = orderRepository != null ? orderRepository.findAll() : MockDataStore.orders();

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
        String status = req.getParameter("status");
        String fromDate = req.getParameter("fromDate");
        String toDate = req.getParameter("toDate");

        List<Order> allOrders = orderRepository != null ? orderRepository.findAll() : MockDataStore.orders();
        List<Order> filtered = new ArrayList<>(allOrders);

        if (status != null && !status.isBlank()) {
            String st = status.trim().toUpperCase();
            filtered = filtered.stream().filter(o -> {
                String s = o.getStatus() != null ? o.getStatus().name() : "";
                if ("COMPLETED".equals(st) || "HOÀN THÀNH".equals(st))
                    return "COMPLETED".equals(s) || "HOÀN THÀNH".equals(s);
                if ("SHIPPING".equals(st) || "ĐANG GIAO".equals(st))
                    return "SHIPPING".equals(s) || "DELIVERED".equals(s) || "ĐANG GIAO".equals(s);
                if ("PENDING".equals(st) || "ĐANG XỬ LÝ".equals(st))
                    return "PENDING".equals(s) || "CONFIRMED".equals(s) || "PACKING".equals(s)
                            || "ĐANG XỬ LÝ".equals(s);
                if ("CANCELLED".equals(st) || "ĐÃ HỦY".equals(st))
                    return "CANCELLED".equals(s) || "ĐÃ HỦY".equals(s);
                return s.equals(st);
            }).toList();
        }

        if (fromDate != null && !fromDate.isBlank()) {
            try {
                LocalDate fd = LocalDate.parse(fromDate);
                filtered = filtered.stream().filter(o -> {
                    if (o.getCreatedAt() == null)
                        return false;
                    LocalDate od = o.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return !od.isBefore(fd);
                }).toList();
            } catch (Exception ignored) {
            }
        }
        if (toDate != null && !toDate.isBlank()) {
            try {
                LocalDate td = LocalDate.parse(toDate);
                filtered = filtered.stream().filter(o -> {
                    if (o.getCreatedAt() == null)
                        return false;
                    LocalDate od = o.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return !od.isAfter(td);
                }).toList();
            } catch (Exception ignored) {
            }
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
                Map.entry("reply",
                        "Cảm ơn anh Lê Thành Công đã tin tưởng lựa chọn Rolex tại WatchStore. Rất mong được phục vụ anh trong các đơn hàng tới!"))));
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
                Map.entry("reply", ""))));
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
                Map.entry("reply", ""))));
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
                Map.entry("reply", ""))));
        return list;
    }

    private List<Map<String, Object>> getSampleComments() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("id", 1, "customerName", "Lê Thành Công", "productName", "Rolex Datejust 41", "content",
                "Mẫu này còn hàng màu đơmi vàng không shop?", "commentDate", "2026-08-05 14:20", "status", "APPROVED"));
        list.add(Map.of("id", 2, "customerName", "Trần Minh Đức", "productName", "Citizen Eco-Drive", "content",
                "Shop có hỗ trợ trả góp qua thẻ tín dụng không?", "commentDate", "2026-08-04 10:15", "status",
                "APPROVED"));
        list.add(Map.of("id", 3, "customerName", "Nguyễn Văn An", "productName", "Orient Bambino Gen 2", "content",
                "Bảo hành tại showroom Hà Nội hay chuyển phát về shop?", "commentDate", "2026-08-03 09:45", "status",
                "PENDING"));
        list.add(Map.of("id", 4, "customerName", "Hoàng Kim Ngân", "productName", "Casio G-Shock", "content",
                "Spam quảng cáo vô nghĩa.", "commentDate", "2026-08-02 11:10", "status", "HIDDEN"));
        return list;
    }

    private void showReviews(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String status = req.getParameter("status");
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
            filtered = filtered.stream()
                    .filter(r -> (r.get("customerName") != null
                            && r.get("customerName").toString().toLowerCase().contains(k)) ||
                            (r.get("productName") != null && r.get("productName").toString().toLowerCase().contains(k))
                            ||
                            (r.get("content") != null && r.get("content").toString().toLowerCase().contains(k)))
                    .toList();
        }

        if (status != null && !status.trim().isEmpty()) {
            String st = status.trim();
            filtered = filtered.stream().filter(r -> st.equalsIgnoreCase(r.get("status").toString()) ||
                    (st.equals("Chờ duyệt") && "PENDING".equalsIgnoreCase(r.get("status").toString())) ||
                    (st.equals("Đã duyệt") && "APPROVED".equalsIgnoreCase(r.get("status").toString())) ||
                    (st.equals("Đã từ chối") && "REJECTED".equalsIgnoreCase(r.get("status").toString()))).toList();
        }

        if (ratingParam != null && !ratingParam.trim().isEmpty()) {
            try {
                int rVal = Integer.parseInt(ratingParam.trim());
                filtered = filtered.stream().filter(r -> Integer.valueOf(rVal).equals(r.get("rating"))).toList();
            } catch (NumberFormatException ignored) {
            }
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
        String status = req.getParameter("status");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> comments = (List<Map<String, Object>>) req.getSession()
                .getAttribute("sampleComments");
        if (comments == null) {
            comments = getSampleComments();
            req.getSession().setAttribute("sampleComments", comments);
        }
        List<Map<String, Object>> filtered = new ArrayList<>(comments);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim().toLowerCase();
            filtered = filtered.stream()
                    .filter(c -> (c.get("customerName") != null
                            && c.get("customerName").toString().toLowerCase().contains(k)) ||
                            (c.get("productName") != null && c.get("productName").toString().toLowerCase().contains(k))
                            ||
                            (c.get("content") != null && c.get("content").toString().toLowerCase().contains(k)))
                    .toList();
        }

        if (status != null && !status.trim().isEmpty()) {
            String st = status.trim();
            filtered = filtered.stream().filter(c -> st.equalsIgnoreCase(c.get("status").toString()) ||
                    (st.equals("Chờ duyệt") && "PENDING".equalsIgnoreCase(c.get("status").toString())) ||
                    (st.equals("Hiển thị") && "APPROVED".equalsIgnoreCase(c.get("status").toString())) ||
                    (st.equals("Ẩn") && "HIDDEN".equalsIgnoreCase(c.get("status").toString()))).toList();
        }

        req.setAttribute("comments", filtered);
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
        req.setAttribute("moduleTitle", "Bình luận");
        ViewRouter.admin(req, resp, "sales/comment", "Bình luận", "sales");
    }

    private List<Map<String, Object>> getSampleReturns() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(new HashMap<>(Map.ofEntries(
                Map.entry("id", 1),
                Map.entry("orderId", 4),
                Map.entry("orderCode", "WS8504"),
                Map.entry("customerName", "Lê Thành Công"),
                Map.entry("customerPhone", "0988000007"),
                Map.entry("customerEmail", "cong.le@example.com"),
                Map.entry("reason", "Kích thước dây đeo không vừa"),
                Map.entry("requestDate", "2026-08-05"),
                Map.entry("status", "Chờ xử lý"),
                Map.entry("productName", "Rolex Datejust 41"),
                Map.entry("quantity", 1),
                Map.entry("evidenceImg", "Ảnh chụp dây đeo bị rộng"),
                Map.entry("orderDate", "2026-08-03"),
                Map.entry("totalPrice", "9,940,000"),
                Map.entry("isWithinPeriod", "Còn trong thời hạn (2 ngày từ khi mua, tối đa 7 ngày)"),
                Map.entry("isCorrectCondition", "Đúng điều kiện (Hàng còn nguyên tem mác, hộp đựng)"),
                Map.entry("isStoreError", "Không (Lỗi chọn nhầm size của khách)"))));
        list.add(new HashMap<>(Map.ofEntries(
                Map.entry("id", 2),
                Map.entry("orderId", 2),
                Map.entry("orderCode", "WS8502"),
                Map.entry("customerName", "Nguyễn Văn An"),
                Map.entry("customerPhone", "0988000005"),
                Map.entry("customerEmail", "an.nguyen@example.com"),
                Map.entry("reason", "Đổi sang màu mặt số xanh navy"),
                Map.entry("requestDate", "2026-08-03"),
                Map.entry("status", "Đã duyệt"),
                Map.entry("productName", "Seiko 5 Sports Automatic"),
                Map.entry("quantity", 1),
                Map.entry("evidenceImg", "Ảnh chụp mặt số nguyên bản"),
                Map.entry("orderDate", "2026-07-28"),
                Map.entry("totalPrice", "4,250,000"),
                Map.entry("isWithinPeriod", "Còn trong thời hạn (6 ngày từ khi mua, tối đa 7 ngày)"),
                Map.entry("isCorrectCondition", "Đúng điều kiện (Chưa qua sử dụng, còn seal)"),
                Map.entry("isStoreError", "Không (Khách thay đổi ý định)"))));
        return list;
    }

    private void showReturns(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String status = req.getParameter("status");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> returns = (List<Map<String, Object>>) req.getSession().getAttribute("sampleReturns");
        if (returns == null) {
            returns = getSampleReturns();
            req.getSession().setAttribute("sampleReturns", returns);
        }

        List<Map<String, Object>> filtered = new ArrayList<>(returns);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim().toLowerCase();
            filtered = filtered.stream()
                    .filter(r -> (r.get("customerName") != null
                            && r.get("customerName").toString().toLowerCase().contains(k)) ||
                            (r.get("reason") != null && r.get("reason").toString().toLowerCase().contains(k)) ||
                            (r.get("orderId") != null && ("#" + r.get("orderId")).toLowerCase().contains(k)))
                    .toList();
        }

        if (status != null && !status.trim().isEmpty()) {
            String st = status.trim();
            filtered = filtered.stream().filter(r -> st.equalsIgnoreCase(r.get("status").toString()) ||
                    (st.equals("PENDING") && "Chờ xử lý".equalsIgnoreCase(r.get("status").toString())) ||
                    (st.equals("APPROVED") && "Đã duyệt".equalsIgnoreCase(r.get("status").toString())) ||
                    (st.equals("RECEIVED") && "Đã nhận hàng".equalsIgnoreCase(r.get("status").toString())) ||
                    (st.equals("REPLACED") && "Đã đổi hàng".equalsIgnoreCase(r.get("status").toString())) ||
                    (st.equals("REFUNDED") && "Đã hoàn tiền".equalsIgnoreCase(r.get("status").toString())) ||
                    (st.equals("REJECTED") && "Từ chối".equalsIgnoreCase(r.get("status").toString()))).toList();
        }

        req.setAttribute("returns", filtered);
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
        req.setAttribute("moduleTitle", "Yêu cầu đổi trả");

        ViewRouter.admin(req, resp, "sales/return", "Yêu cầu đổi trả", "sales");
    }

    private void updateReturnStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String status = req.getParameter("status");

        if (idParam != null && status != null) {
            try {
                int id = Integer.parseInt(idParam);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> returns = (List<Map<String, Object>>) req.getSession()
                        .getAttribute("sampleReturns");
                if (returns == null) {
                    returns = getSampleReturns();
                }
                for (int i = 0; i < returns.size(); i++) {
                    Map<String, Object> r = returns.get(i);
                    if (Integer.valueOf(id).equals(r.get("id"))) {
                        Map<String, Object> updated = new HashMap<>(r);
                        updated.put("status", status);
                        returns.set(i, updated);
                        break;
                    }
                }
                req.getSession().setAttribute("sampleReturns", returns);
                req.getSession().setAttribute("flash", "Đã cập nhật trạng thái yêu cầu đổi trả thành công!");
            } catch (Exception ignored) {
            }
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/returns");
    }

    private void updateReviewStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String status = req.getParameter("status");
        String action = req.getParameter("action");
        String replyContent = req.getParameter("replyContent");

        if (status == null && action != null) {
            if ("approve".equalsIgnoreCase(action))
                status = "APPROVED";
            else if ("reject".equalsIgnoreCase(action))
                status = "REJECTED";
        }

        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> reviews = (List<Map<String, Object>>) req.getSession()
                        .getAttribute("sampleReviews");
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
            } catch (Exception ignored) {
            }
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/reviews");
    }

    private void updateCommentStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String status = req.getParameter("status");

        if (idParam != null && status != null) {
            try {
                int id = Integer.parseInt(idParam);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> comments = (List<Map<String, Object>>) req.getSession()
                        .getAttribute("sampleComments");
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
            } catch (Exception ignored) {
            }
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
            } catch (NumberFormatException ignored) {
            }
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
            } catch (NumberFormatException ignored) {
            }
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
            } catch (NumberFormatException ignored) {
            }
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
                        req.getSession().setAttribute("flash",
                                "Lỗi: Không thể sửa thông tin giao hàng của đơn đã hoàn thành!");
                    } else {
                        if (customerName != null)
                            order.setCustomerName(customerName);
                        if (phone != null)
                            order.setPhone(phone);
                        if (shippingAddress != null)
                            order.setShippingAddress(shippingAddress);

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

        List<Order> orders = orderRepository != null ? orderRepository.findAll() : MockDataStore.orders();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim().toLowerCase();
            orders = orders.stream().filter(o -> (String.valueOf(o.getId()).contains(k)) ||
                    (o.getCode() != null && o.getCode().toLowerCase().contains(k)) ||
                    (o.getCustomerName() != null && o.getCustomerName().toLowerCase().contains(k)) ||
                    (o.getPhone() != null && o.getPhone().toLowerCase().contains(k)) ||
                    (o.getShippingAddress() != null && o.getShippingAddress().toLowerCase().contains(k))).toList();
        }

        if (status != null && !status.trim().isEmpty()) {
            String st = status.trim();
            orders = orders.stream().filter(o -> st.equalsIgnoreCase(o.getStatusCode()) ||
                    (st.equals("Chờ giao") && o.getStatus() == OrderStatus.CONFIRMED) ||
                    (st.equals("Đang giao") && o.getStatus() == OrderStatus.SHIPPING) ||
                    (st.equals("Giao thành công") && o.getStatus() == OrderStatus.COMPLETED) ||
                    (st.equals("Giao thất bại") && o.getStatus() == OrderStatus.CANCELLED)).toList();
        }

        req.setAttribute("orders", orders);
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
        req.setAttribute("moduleTitle", "Vận chuyển");
        ViewRouter.admin(req, resp, "sales/delivery", "Vận chuyển", "sales");
    }

    private void handlePOSGet(String path, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/pos".equals(path)) {
            showPOS(req, resp);
        } else if ("/pos/search-products".equals(path)) {
            searchPOSProducts(req, resp);
        } else if ("/pos/search-customers".equals(path)) {
            searchPOSCustomers(req, resp);
        } else if ("/pos/print".equals(path)) {
            printPOSInvoice(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handlePOSPost(String path, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/pos/add-customer".equals(path)) {
            addPOSCustomer(req, resp);
        } else if ("/pos/check-voucher".equals(path)) {
            checkPOSVoucher(req, resp);
        } else if ("/pos/checkout".equals(path)) {
            checkoutPOS(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showPOS(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Map<String, Object>> categories = new ArrayList<>();
        List<Map<String, Object>> brands = new ArrayList<>();
        List<Map<String, Object>> vouchers = new ArrayList<>();

        String queryCategories = "SELECT CategoryID, CategoryName FROM Categories ORDER BY CategoryName ASC";
        String queryBrands = "SELECT BrandID, BrandName FROM Brands ORDER BY BrandName ASC";
        String queryVouchers = """
                SELECT VoucherID, VoucherCode, VoucherName, DiscountType, DiscountValue, MinimumOrderValue
                FROM Vouchers
                WHERE Status = 'ACTIVE' AND GETDATE() BETWEEN StartAt AND EndAt
                """;

        try (Connection conn = DBContext.getConnection();
                PreparedStatement psCat = conn.prepareStatement(queryCategories);
                ResultSet rsCat = psCat.executeQuery();
                PreparedStatement psBrand = conn.prepareStatement(queryBrands);
                ResultSet rsBrand = psBrand.executeQuery();
                PreparedStatement psVoucher = conn.prepareStatement(queryVouchers);
                ResultSet rsVoucher = psVoucher.executeQuery()) {

            while (rsCat.next()) {
                Map<String, Object> cat = new HashMap<>();
                cat.put("id", rsCat.getInt("CategoryID"));
                cat.put("name", rsCat.getString("CategoryName"));
                categories.add(cat);
            }

            while (rsBrand.next()) {
                Map<String, Object> br = new HashMap<>();
                br.put("id", rsBrand.getInt("BrandID"));
                br.put("name", rsBrand.getString("BrandName"));
                brands.add(br);
            }

            while (rsVoucher.next()) {
                Map<String, Object> vc = new HashMap<>();
                vc.put("id", rsVoucher.getInt("VoucherID"));
                vc.put("code", rsVoucher.getString("VoucherCode"));
                vc.put("name", rsVoucher.getString("VoucherName"));
                vc.put("discountType", rsVoucher.getString("DiscountType"));
                vc.put("discountValue", rsVoucher.getBigDecimal("DiscountValue"));
                vc.put("minOrder", rsVoucher.getBigDecimal("MinimumOrderValue"));
                vouchers.add(vc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<Order> orders = orderRepository != null ? orderRepository.findAll() : new ArrayList<>();
        req.setAttribute("ordersSize", orders.size());

        req.setAttribute("categories", categories);
        req.setAttribute("brands", brands);
        req.setAttribute("vouchersList", vouchers);
        req.setAttribute("moduleTitle", "Bán hàng tại quầy (POS)");

        ViewRouter.admin(req, resp, "sales/pos", "Bán hàng tại quầy (POS)", "sales");
    }

    private void searchPOSProducts(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String keyword = req.getParameter("keyword");
        String catIdParam = req.getParameter("categoryId");
        String brandIdParam = req.getParameter("brandId");

        StringBuilder query = new StringBuilder(
                """
                        SELECT pv.VariantID, p.ProductName, pv.VariantName, pv.SKU, pv.Barcode, pv.SalePrice, pv.CompareAtPrice, ib.QuantityOnHand, b.BrandName, c.CategoryName
                        FROM ProductVariants pv
                        JOIN Products p ON pv.ProductID = p.ProductID
                        LEFT JOIN Brands b ON p.BrandID = b.BrandID
                        LEFT JOIN Categories c ON p.CategoryID = c.CategoryID
                        LEFT JOIN InventoryBalances ib ON pv.VariantID = ib.VariantID
                        WHERE p.Status = 'ACTIVE' AND pv.Status = 'ACTIVE'
                        """);

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append(
                    " AND (LOWER(p.ProductName) LIKE ? OR LOWER(pv.VariantName) LIKE ? OR LOWER(pv.SKU) LIKE ? OR pv.Barcode = ?)");
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(keyword.trim());
        }
        if (catIdParam != null && !catIdParam.trim().isEmpty()) {
            try {
                int catId = Integer.parseInt(catIdParam.trim());
                query.append(" AND p.CategoryID = ?");
                params.add(catId);
            } catch (NumberFormatException ignored) {
            }
        }
        if (brandIdParam != null && !brandIdParam.trim().isEmpty()) {
            try {
                int brandId = Integer.parseInt(brandIdParam.trim());
                query.append(" AND p.BrandID = ?");
                params.add(brandId);
            } catch (NumberFormatException ignored) {
            }
        }

        query.append(" ORDER BY p.ProductName ASC, pv.VariantName ASC");

        StringBuilder sb = new StringBuilder("[");
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        sb.append(",");
                    first = false;
                    sb.append("{");
                    sb.append("\"variantId\":").append(rs.getInt("VariantID")).append(",");
                    sb.append("\"productName\":\"").append(escapeJson(rs.getString("ProductName"))).append("\",");
                    sb.append("\"variantName\":\"").append(escapeJson(rs.getString("VariantName"))).append("\",");
                    sb.append("\"sku\":\"").append(escapeJson(rs.getString("SKU"))).append("\",");
                    sb.append("\"barcode\":\"")
                            .append(escapeJson(rs.getString("Barcode") != null ? rs.getString("Barcode") : ""))
                            .append("\",");
                    sb.append("\"price\":").append(rs.getBigDecimal("SalePrice")).append(",");
                    sb.append("\"oldPrice\":")
                            .append(rs.getBigDecimal("CompareAtPrice") != null ? rs.getBigDecimal("CompareAtPrice")
                                    : rs.getBigDecimal("SalePrice"))
                            .append(",");
                    sb.append("\"stock\":").append(rs.getInt("QuantityOnHand")).append(",");
                    sb.append("\"brand\":\"")
                            .append(escapeJson(rs.getString("BrandName") != null ? rs.getString("BrandName") : ""))
                            .append("\",");
                    sb.append("\"category\":\"")
                            .append(escapeJson(
                                    rs.getString("CategoryName") != null ? rs.getString("CategoryName") : ""))
                            .append("\"");
                    sb.append("}");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sb.append("]");
        writeJson(resp, sb.toString());
    }

    private void searchPOSCustomers(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String keyword = req.getParameter("keyword");

        StringBuilder query = new StringBuilder(
                """
                        SELECT u.UserID, u.FullName, u.Phone, u.Email,
                               (SELECT TOP 1 AddressLine + ', ' + Ward + ', ' + District + ', ' + Province FROM UserAddresses WHERE UserID = u.UserID ORDER BY IsDefault DESC) AS Address
                        FROM Users u
                        JOIN UserRoles ur ON u.UserID = ur.UserID
                        JOIN Roles r ON ur.RoleID = r.RoleID
                        WHERE r.RoleCode = 'CUSTOMER' AND u.Status = 'ACTIVE'
                        """);

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append(" AND (LOWER(u.FullName) LIKE ? OR u.Phone LIKE ? OR LOWER(u.Email) LIKE ?)");
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add("%" + keyword.trim() + "%");
            params.add(pattern);
        }

        query.append(" ORDER BY u.FullName ASC");

        StringBuilder sb = new StringBuilder("[");
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        sb.append(",");
                    first = false;
                    sb.append("{");
                    sb.append("\"id\":").append(rs.getInt("UserID")).append(",");
                    sb.append("\"fullName\":\"").append(escapeJson(rs.getString("FullName"))).append("\",");
                    sb.append("\"phone\":\"").append(escapeJson(rs.getString("Phone"))).append("\",");
                    sb.append("\"email\":\"").append(escapeJson(rs.getString("Email"))).append("\",");
                    sb.append("\"address\":\"").append(escapeJson(
                            rs.getString("Address") != null ? rs.getString("Address") : "Chưa cập nhật địa chỉ"))
                            .append("\"");
                    sb.append("}");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sb.append("]");
        writeJson(resp, sb.toString());
    }

    private void addPOSCustomer(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String fullName = req.getParameter("fullName");
        String phone = req.getParameter("phone");
        String email = req.getParameter("email");
        String address = req.getParameter("address");

        if (fullName == null || fullName.trim().isEmpty() || phone == null || phone.trim().isEmpty() || email == null
                || email.trim().isEmpty()) {
            writeJson(resp,
                    "{\"status\":\"error\",\"message\":\"Vui lòng điền đầy đủ Họ tên, Số điện thoại và Email.\"}");
            return;
        }

        Customer customer = new Customer();
        customer.setFullName(fullName.trim());
        customer.setPhone(phone.trim());
        customer.setEmail(email.trim());
        customer.setAddress(address != null ? address.trim() : "");

        boolean success = customerRepository != null && customerRepository.insert(customer);
        if (success) {
            Customer created = null;
            List<Customer> list = customerRepository.search(phone.trim());
            if (!list.isEmpty()) {
                created = list.get(0);
            }
            if (created != null) {
                writeJson(resp, String.format(
                        "{\"status\":\"success\",\"id\":%d,\"fullName\":\"%s\",\"phone\":\"%s\",\"address\":\"%s\"}",
                        created.getId(), escapeJson(created.getFullName()), escapeJson(created.getPhone()),
                        escapeJson(created.getAddress())));
            } else {
                writeJson(resp, "{\"status\":\"error\",\"message\":\"Không tìm thấy khách hàng sau khi tạo\"}");
            }
        } else {
            writeJson(resp,
                    "{\"status\":\"error\",\"message\":\"Email hoặc Số điện thoại đã tồn tại trong hệ thống.\"}");
        }
    }

    private void checkPOSVoucher(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String code = req.getParameter("code");
        String subtotalParam = req.getParameter("subtotal");

        if (code == null || code.trim().isEmpty() || subtotalParam == null || subtotalParam.trim().isEmpty()) {
            writeJson(resp, "{\"status\":\"invalid\",\"message\":\"Dữ liệu kiểm tra voucher không hợp lệ\"}");
            return;
        }

        double subtotal = Double.parseDouble(subtotalParam.trim());
        String sql = """
                SELECT VoucherID, VoucherCode, VoucherName, DiscountType, DiscountValue, MaximumDiscount, MinimumOrderValue, UsageLimit, UsedCount
                FROM Vouchers
                WHERE VoucherCode = ? AND Status = 'ACTIVE' AND GETDATE() BETWEEN StartAt AND EndAt
                """;

        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int usageLimit = rs.getInt("UsageLimit");
                    int usedCount = rs.getInt("UsedCount");
                    if (usageLimit > 0 && usedCount >= usageLimit) {
                        writeJson(resp, "{\"status\":\"invalid\",\"message\":\"Voucher này đã hết lượt sử dụng\"}");
                        return;
                    }

                    double minOrder = rs.getBigDecimal("MinimumOrderValue").doubleValue();
                    if (subtotal < minOrder) {
                        writeJson(resp, String.format(
                                "{\"status\":\"invalid\",\"message\":\"Chưa đạt giá trị đơn hàng tối thiểu để áp dụng (Yêu cầu ít nhất %,.0f ₫)\"}",
                                minOrder));
                        return;
                    }

                    String discountType = rs.getString("DiscountType");
                    double discountValue = rs.getBigDecimal("DiscountValue").doubleValue();
                    double maxDiscount = rs.getBigDecimal("MaximumDiscount").doubleValue();

                    double discountAmount = 0.0;
                    if ("PERCENT".equalsIgnoreCase(discountType)) {
                        discountAmount = subtotal * (discountValue / 100.0);
                        if (maxDiscount > 0 && discountAmount > maxDiscount) {
                            discountAmount = maxDiscount;
                        }
                    } else if ("AMOUNT".equalsIgnoreCase(discountType)) {
                        discountAmount = discountValue;
                    }

                    writeJson(resp, String.format(
                            "{\"status\":\"valid\",\"voucherId\":%d,\"code\":\"%s\",\"discountAmount\":%.2f,\"message\":\"Áp dụng voucher thành công!\"}",
                            rs.getInt("VoucherID"), rs.getString("VoucherCode"), discountAmount));
                } else {
                    writeJson(resp,
                            "{\"status\":\"invalid\",\"message\":\"Voucher không tồn tại hoặc đã hết hạn sử dụng\"}");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeJson(resp, "{\"status\":\"invalid\",\"message\":\"Lỗi hệ thống khi kiểm tra voucher: " + e.getMessage()
                    + "\"}");
        }
    }

    private void checkoutPOS(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String customerIdParam = req.getParameter("customerId");
        String voucherIdParam = req.getParameter("voucherId");
        String discountAmountParam = req.getParameter("discountAmount");
        String customerName = req.getParameter("customerName");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");
        String itemsJson = req.getParameter("items");

        if (itemsJson == null || itemsJson.trim().isEmpty()) {
            writeJson(resp, "{\"status\":\"error\",\"message\":\"Không có sản phẩm nào trong hóa đơn\"}");
            return;
        }

        try {
            int customerId = Integer.parseInt(customerIdParam.trim());
            int voucherId = Integer.parseInt(voucherIdParam.trim());
            BigDecimal discountAmount = new BigDecimal(discountAmountParam.trim());

            List<Map<String, Object>> items = parseItemsJson(itemsJson);
            if (items.isEmpty()) {
                writeJson(resp, "{\"status\":\"error\",\"message\":\"Giỏ hàng rỗng hoặc định dạng không hợp lệ\"}");
                return;
            }

            User staff = (User) req.getSession().getAttribute("user");
            int staffId = staff != null ? staff.getId() : 2;

            String code = "WS" + (8500 + (orderRepository != null ? orderRepository.findAll().size() : 0) + 1);

            Order order = new Order();
            order.setCode(code);
            order.setUserId(customerId);
            order.setCustomerName(customerName != null && !customerName.trim().isEmpty() ? customerName.trim()
                    : "Khách mua tại quầy");
            order.setPhone(phone != null ? phone.trim() : "");
            order.setShippingAddress(address != null && !address.trim().isEmpty() ? address.trim() : "Mua tại quầy");

            long orderId = orderRepository.createPOSOrder(order, items, voucherId, discountAmount, staffId);
            writeJson(resp,
                    String.format("{\"status\":\"success\",\"orderId\":%d,\"orderCode\":\"%s\"}", orderId, code));

        } catch (Exception e) {
            e.printStackTrace();
            writeJson(resp,
                    "{\"status\":\"error\",\"message\":\"Lỗi thanh toán: " + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void printPOSInvoice(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                int id = Integer.parseInt(idParam);
                Order order = orderRepository != null ? orderRepository.findById(id) : null;
                if (order != null) {
                    req.setAttribute("order", order);
                    req.setAttribute("orderItems",
                            orderRepository != null ? orderRepository.getOrderItems(id) : new ArrayList<>());
                    req.getRequestDispatcher("/views/sales/pos-print.jsp").forward(req, resp);
                    return;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy hóa đơn");
    }

    private void writeJson(HttpServletResponse resp, String json) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json);
    }

    private String escapeJson(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private List<Map<String, Object>> parseItemsJson(String json) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (json == null || json.trim().isEmpty() || "[]".equals(json.trim())) {
            return list;
        }
        String cleaned = json.trim().replace("[", "").replace("]", "").replace(" ", "");
        if (cleaned.isEmpty())
            return list;

        String[] parts = cleaned.split("\\},\\{");
        for (String part : parts) {
            String cleanPart = part.replace("{", "").replace("}", "").replace("\"", "");
            String[] pairs = cleanPart.split(",");
            int variantId = 0;
            int quantity = 0;
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                if (kv.length == 2) {
                    if ("variantId".equalsIgnoreCase(kv[0])) {
                        try {
                            variantId = Integer.parseInt(kv[1]);
                        } catch (NumberFormatException ignored) {
                        }
                    } else if ("quantity".equalsIgnoreCase(kv[0])) {
                        try {
                            quantity = Integer.parseInt(kv[1]);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
            if (variantId > 0 && quantity > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("variantId", variantId);
                item.put("quantity", quantity);
                list.add(item);
            }
        }
        return list;
    }
}
