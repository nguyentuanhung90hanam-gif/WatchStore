package com.watchstore.controller.sales;

import com.watchstore.config.DBContext;
import com.watchstore.model.Customer;
import com.watchstore.model.Order;
import com.watchstore.enums.OrderStatus;
import com.watchstore.model.User;
import com.watchstore.repository.CustomerRepository;
import com.watchstore.repository.MockDataStore;
import com.watchstore.repository.OrderRepository;
import com.watchstore.repository.WarrantyRepository;
import com.watchstore.util.ViewRouter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;

import java.io.File;
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
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
        maxFileSize = 1024 * 1024 * 10,       // 10MB
        maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class SalesController extends HttpServlet {

    private CustomerRepository customerRepository;
    private OrderRepository orderRepository;
    private WarrantyRepository warrantyRepository;
    private com.watchstore.repository.ReviewRepository reviewRepository;

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
        reviewRepository = (com.watchstore.repository.ReviewRepository) getServletContext().getAttribute("reviewRepository");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();

        com.watchstore.model.User currentUser = (com.watchstore.model.User) req.getSession().getAttribute("user");
        boolean isAdmin = (currentUser != null && currentUser.getRole() == com.watchstore.enums.Role.ADMIN);

        if (currentUser != null && currentUser.getRole() == com.watchstore.enums.Role.SALES) {
            if ("/delivery".equals(path) || "/returns".equals(path) || "/report".equals(path)) {
                req.getSession().setAttribute("errorMessage", "Bạn không có quyền truy cập chức năng này.");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
                return;
            }
        }

        if (path != null && (path.equals("/customer-add") || path.equals("/order-add") || path.equals("/order-edit"))) {
            String requiredPerm = path.startsWith("/customer") ? "CUSTOMERS_MANAGE" : "ORDERS_MANAGE";
            if (currentUser == null || (!currentUser.hasPermission(requiredPerm) && !isAdmin)) {
                req.getSession().setAttribute("errorMessage", "Bạn không có quyền sử dụng chức năng này.");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/dashboard");
                return;
            }
        }

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
            showOrderAdd(req, resp);
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

        if ("/warranty/get-items".equals(path)) {
            getWarrantyOrderItems(req, resp);
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

        com.watchstore.model.User currentUser = (com.watchstore.model.User) req.getSession().getAttribute("user");
        boolean isAdmin = (currentUser != null && currentUser.getRole() == com.watchstore.enums.Role.ADMIN);

        if (currentUser != null && currentUser.getRole() == com.watchstore.enums.Role.SALES) {
            if ("/delivery".equals(path) || "/returns".equals(path) || "/report".equals(path)) {
                req.getSession().setAttribute("errorMessage", "Bạn không có quyền thực hiện chức năng này.");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
                return;
            }
        }

        if (path != null) {
            String requiredPermission = null;
            if (path.startsWith("/customer-")) {
                requiredPermission = "CUSTOMERS_MANAGE";
            } else if (path.startsWith("/order-")) {
                requiredPermission = "ORDERS_MANAGE";
            } else if (path.equals("/returns")) {
                requiredPermission = "RETURNS_MANAGE";
            }

            if (requiredPermission != null) {
                if (currentUser == null || (!currentUser.hasPermission(requiredPermission) && !isAdmin)) {
                    req.getSession().setAttribute("errorMessage", "Bạn không có quyền sử dụng chức năng này.");
                    resp.sendRedirect(req.getContextPath() + "/manage/sales/dashboard");
                    return;
                }
            }
        }

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

        if ("/warranty/approve".equals(path)) {
            approveWarrantyRequest(req, resp);
            return;
        }

        if ("/warranty/reject".equals(path)) {
            rejectWarrantyRequest(req, resp);
            return;
        }

        if ("/warranty/create".equals(path)) {
            createWarrantyRequest(req, resp);
            return;
        }

        if ("/warranty/receive".equals(path)) {
            receiveWarrantyRequest(req, resp);
            return;
        }

        if ("/warranty/complete".equals(path)) {
            completeWarrantyRequest(req, resp);
            return;
        }

        if ("/warranty/cancel".equals(path)) {
            cancelWarrantyRequest(req, resp);
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
        String minPrice = req.getParameter("minPrice");
        String maxPrice = req.getParameter("maxPrice");

        List<Order> orders = orderRepository != null ? orderRepository.search(keyword, status, fromDate, toDate, minPrice, maxPrice)
                : MockDataStore.orders();
        if (orders != null && orderRepository != null) {
            for (Order o : orders) {
                o.setItems(orderRepository.getOrderItems((int) o.getId()));
            }
        }
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
        req.setAttribute("minPrice", minPrice);
        req.setAttribute("maxPrice", maxPrice);
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

        StringBuilder sql = new StringBuilder("""
                SELECT r.ReviewID AS id,
                       r.Rating AS rating,
                       r.ReviewContent AS content,
                       r.Status AS status,
                       r.CreatedAt AS createdAt,
                       u.FullName AS customerName,
                       u.Email AS email,
                       u.Phone AS phone,
                       p.ProductName AS productName,
                       o.OrderCode AS orderCode,
                       (SELECT TOP 1 ReplyContent FROM ReviewReplies WHERE ReviewID = r.ReviewID ORDER BY CreatedAt DESC) AS reply
                FROM Reviews r
                JOIN Users u ON r.UserID = u.UserID
                JOIN Products p ON r.ProductID = p.ProductID
                LEFT JOIN OrderItems oi ON r.OrderItemID = oi.OrderItemID
                LEFT JOIN Orders o ON oi.OrderID = o.OrderID
                WHERE 1 = 1
                """);
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(u.FullName) LIKE ? OR LOWER(p.ProductName) LIKE ? OR LOWER(r.ReviewContent) LIKE ?)");
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND r.Status = ?");
            params.add(status.trim());
        }

        if (ratingParam != null && !ratingParam.trim().isEmpty()) {
            try {
                int rVal = Integer.parseInt(ratingParam.trim());
                sql.append(" AND r.Rating = ?");
                params.add(rVal);
            } catch (NumberFormatException ignored) {
            }
        }

        sql.append(" ORDER BY r.CreatedAt DESC");

        List<Map<String, Object>> reviewsList = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> r = new HashMap<>();
                    r.put("id", rs.getLong("id"));
                    r.put("rating", rs.getInt("rating"));
                    r.put("content", rs.getString("content"));
                    r.put("status", rs.getString("status"));
                    
                    java.sql.Timestamp ts = rs.getTimestamp("createdAt");
                    r.put("createdAt", ts != null ? ts.toString().substring(0, 16) : "");
                    
                    r.put("customerName", rs.getString("customerName"));
                    r.put("email", rs.getString("email"));
                    r.put("phone", rs.getString("phone"));
                    r.put("productName", rs.getString("productName"));
                    r.put("orderCode", rs.getString("orderCode"));
                    r.put("reply", rs.getString("reply"));
                    reviewsList.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        req.setAttribute("reviews", reviewsList);
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

        StringBuilder sql = new StringBuilder("""
                SELECT pc.PostCommentID AS id,
                       COALESCE(u.FullName, pc.GuestName) AS customerName,
                       p.Title AS productName,
                       pc.CommentContent AS content,
                       pc.CreatedAt AS commentDate,
                       pc.Status AS status
                FROM PostComments pc
                JOIN Posts p ON pc.PostID = p.PostID
                LEFT JOIN Users u ON pc.UserID = u.UserID
                WHERE 1 = 1
                """);
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(COALESCE(u.FullName, pc.GuestName)) LIKE ? OR LOWER(p.Title) LIKE ? OR LOWER(pc.CommentContent) LIKE ?)");
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND pc.Status = ?");
            params.add(status.trim());
        }

        sql.append(" ORDER BY pc.CreatedAt DESC");

        List<Map<String, Object>> commentsList = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> c = new HashMap<>();
                    c.put("id", rs.getLong("id"));
                    c.put("customerName", rs.getString("customerName"));
                    c.put("productName", rs.getString("productName"));
                    c.put("content", rs.getString("content"));
                    
                    java.sql.Timestamp ts = rs.getTimestamp("commentDate");
                    c.put("commentDate", ts != null ? ts.toString().substring(0, 16) : "");
                    
                    c.put("status", rs.getString("status"));
                    commentsList.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        req.setAttribute("comments", commentsList);
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
                long reviewId = Long.parseLong(idParam);
                User staff = (User) req.getSession().getAttribute("user");
                if (staff == null) {
                    req.getSession().setAttribute("flash", "Lỗi: Bạn chưa đăng nhập.");
                    resp.sendRedirect(req.getContextPath() + "/manage/sales/reviews");
                    return;
                }

                boolean isAdmin = (staff.getRole() == com.watchstore.enums.Role.ADMIN);
                boolean isSales = (staff.getRole() == com.watchstore.enums.Role.SALES);

                if (status != null && !isAdmin) {
                    req.getSession().setAttribute("flash", "Lỗi: Chỉ Admin mới có quyền phê duyệt/ẩn bình luận.");
                    resp.sendRedirect(req.getContextPath() + "/manage/sales/reviews");
                    return;
                }

                if (replyContent != null && !replyContent.trim().isEmpty() && !isSales) {
                    req.getSession().setAttribute("flash", "Lỗi: Chỉ Nhân viên bán hàng mới có quyền trả lời bình luận.");
                    resp.sendRedirect(req.getContextPath() + "/manage/sales/reviews");
                    return;
                }

                int staffId = staff.getId();

                try (Connection conn = DBContext.getConnection()) {
                    conn.setAutoCommit(false);
                    try {
                        if (status != null) {
                            String sqlUpdate = "UPDATE Reviews SET Status = ?, UpdatedAt = GETDATE() WHERE ReviewID = ?";
                            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                                ps.setString(1, status.trim());
                                ps.setLong(2, reviewId);
                                ps.executeUpdate();
                            }
                        }

                        if (replyContent != null && !replyContent.trim().isEmpty()) {
                            // Insert reply
                            String sqlInsertReply = "INSERT INTO ReviewReplies (ReviewID, StaffID, ReplyContent, CreatedAt) VALUES (?, ?, ?, GETDATE())";
                            try (PreparedStatement ps = conn.prepareStatement(sqlInsertReply)) {
                                ps.setLong(1, reviewId);
                                ps.setInt(2, staffId);
                                ps.setString(3, replyContent.trim());
                                ps.executeUpdate();
                            }
                            
                            // Auto approve when replied
                            String sqlApprove = "UPDATE Reviews SET Status = 'APPROVED', UpdatedAt = GETDATE() WHERE ReviewID = ?";
                            try (PreparedStatement ps = conn.prepareStatement(sqlApprove)) {
                                ps.setLong(1, reviewId);
                                ps.executeUpdate();
                            }
                        }
                        
                        conn.commit();
                        if (replyContent != null) {
                            req.getSession().setAttribute("flash", "Đã gửi phản hồi đánh giá thành công!");
                        } else {
                            req.getSession().setAttribute("flash", "Đã cập nhật trạng thái đánh giá thành công!");
                        }
                    } catch (SQLException e) {
                        conn.rollback();
                        throw e;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                req.getSession().setAttribute("flash", "Lỗi khi xử lý đánh giá: " + e.getMessage());
            }
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/reviews");
    }

    private void updateCommentStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String status = req.getParameter("status");

        if (idParam != null && status != null) {
            try {
                long commentId = Long.parseLong(idParam);
                String sql = "UPDATE PostComments SET Status = ? WHERE PostCommentID = ?";
                try (Connection conn = DBContext.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, status.trim());
                    ps.setLong(2, commentId);
                    ps.executeUpdate();
                    req.getSession().setAttribute("flash", "Đã cập nhật trạng thái bình luận thành công!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                req.getSession().setAttribute("flash", "Lỗi khi cập nhật trạng thái bình luận: " + e.getMessage());
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
        String note = req.getParameter("note");
        String itemsJson = req.getParameter("items");

        try {
            BigDecimal totalPrice = new BigDecimal(totalPriceStr);
            String code = "WS" + (8500 + (orderRepository != null ? orderRepository.findAll().size() : 0) + 1);

            Order order = new Order();
            order.setCode(code);
            order.setCustomerName(customerName);
            order.setPhone(phone);
            order.setShippingAddress(shippingAddress);
            order.setCustomerNote(note);
            order.setTotalPrice(totalPrice);
            order.setStatus(status);
            order.setPaymentStatus(paymentStatus != null ? paymentStatus : "UNPAID");
            order.setCreatedAt(new java.util.Date());

            int customerId = 4;
            if (customerRepository != null && phone != null && !phone.trim().isEmpty()) {
                com.watchstore.model.Customer cust = customerRepository.findByPhone(phone.trim());
                if (cust == null) {
                    cust = new com.watchstore.model.Customer();
                    cust.setFullName(customerName);
                    cust.setPhone(phone.trim());
                    cust.setEmail(phone.trim() + "@customer.watchstore.vn");
                    cust.setAddress(shippingAddress != null ? shippingAddress : "Hà Nội");
                    boolean inserted = customerRepository.insert(cust);
                    if (inserted) {
                        cust = customerRepository.findByPhone(phone.trim());
                    }
                }
                if (cust != null) {
                    customerId = cust.getId();
                }
            }
            order.setUserId(customerId);

            List<Map<String, Object>> items = parseItemsJson(itemsJson);
            if (items == null || items.isEmpty()) {
                req.getSession().setAttribute("flash", "Lỗi: Đơn hàng phải có ít nhất 1 sản phẩm.");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/orders");
                return;
            }

            BigDecimal computedTotal = BigDecimal.ZERO;
            for (Map<String, Object> item : items) {
                int qty = ((Number) item.get("quantity")).intValue();
                double unitPrice = ((Number) item.get("unitPrice")).doubleValue();
                computedTotal = computedTotal.add(BigDecimal.valueOf(unitPrice).multiply(BigDecimal.valueOf(qty)));
            }
            order.setTotalPrice(computedTotal);

            User staff = (User) req.getSession().getAttribute("user");
            int staffId = staff != null ? staff.getId() : 2;

            if (orderRepository != null) {
                orderRepository.createSalesOrder(order, items, staffId);
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
                        SELECT pv.VariantID, p.ProductName, pv.VariantName, pv.SKU, pv.Barcode, pv.SalePrice, pv.CompareAtPrice, ib.QuantityOnHand, b.BrandName, c.CategoryName, p.Image
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
                    sb.append("\"image\":\"")
                            .append(escapeJson(rs.getString("Image") != null ? rs.getString("Image") : ""))
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
            double unitPrice = 0.0;
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
                    } else if ("unitPrice".equalsIgnoreCase(kv[0])) {
                        try {
                            unitPrice = Double.parseDouble(kv[1]);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
            if (variantId > 0 && quantity > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("variantId", variantId);
                item.put("quantity", quantity);
                item.put("unitPrice", unitPrice);
                list.add(item);
            }
        }
        return list;
    }

    private void getWarrantyOrderItems(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String input = req.getParameter("orderIdOrPhone");
        if (input == null || input.trim().isEmpty()) {
            writeJson(resp, "{\"success\":false,\"message\":\"Vui lòng nhập mã đơn hàng hoặc số điện thoại.\"}");
            return;
        }

        String search = input.trim();
        long orderId = 0;
        String orderCode = "";
        String customerName = "";
        String phone = "";
        String buyDate = "";

        // 1. Try to find by OrderCode or OrderID
        String sqlOrder = "SELECT TOP 1 OrderID, OrderCode, RecipientName, RecipientPhone, CreatedAt FROM Orders WHERE OrderCode = ? OR CAST(OrderID AS VARCHAR) = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlOrder)) {
            ps.setString(1, search);
            ps.setString(2, search);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    orderId = rs.getLong("OrderID");
                    orderCode = rs.getString("OrderCode");
                    customerName = rs.getString("RecipientName");
                    phone = rs.getString("RecipientPhone");
                    java.sql.Timestamp ts = rs.getTimestamp("CreatedAt");
                    buyDate = ts != null ? ts.toString().substring(0, 16) : "";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 2. If not found, try to find by phone
        if (orderId == 0) {
            String sqlPhone = "SELECT TOP 1 OrderID, OrderCode, RecipientName, RecipientPhone, CreatedAt FROM Orders WHERE RecipientPhone = ? ORDER BY CreatedAt DESC";
            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlPhone)) {
                ps.setString(1, search);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        orderId = rs.getLong("OrderID");
                        orderCode = rs.getString("OrderCode");
                        customerName = rs.getString("RecipientName");
                        phone = rs.getString("RecipientPhone");
                        java.sql.Timestamp ts = rs.getTimestamp("CreatedAt");
                        buyDate = ts != null ? ts.toString().substring(0, 16) : "";
                    } else {
                        // Phone not found
                        writeJson(resp, "{\"success\":false,\"message\":\"Không tìm thấy đơn hàng của khách hàng với số điện thoại này.\"}");
                        return;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // If still not found
        if (orderId == 0) {
            writeJson(resp, "{\"success\":false,\"message\":\"Không tìm thấy đơn hàng với mã này.\"}");
            return;
        }

        // 3. Find order items
        List<Map<String, Object>> items = new ArrayList<>();
        String sqlItems = "SELECT VariantID, ProductName, VariantName FROM OrderItems WHERE OrderID = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlItems)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("variantId", rs.getInt("VariantID"));
                    item.put("name", rs.getString("ProductName") + " (" + rs.getString("VariantName") + ")");
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (items.isEmpty()) {
            writeJson(resp, "{\"success\":false,\"message\":\"Đơn hàng này không có sản phẩm nào.\"}");
            return;
        }

        // Build response JSON
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"success\":true,");
        sb.append("\"orderId\":").append(orderId).append(",");
        sb.append("\"orderCode\":\"").append(escapeJson(orderCode)).append("\",");
        sb.append("\"customerName\":\"").append(escapeJson(customerName)).append("\",");
        sb.append("\"phone\":\"").append(escapeJson(phone)).append("\",");
        sb.append("\"buyDate\":\"").append(escapeJson(buyDate)).append("\",");
        sb.append("\"items\":[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            Map<String, Object> item = items.get(i);
            sb.append("{");
            sb.append("\"variantId\":").append(item.get("variantId")).append(",");
            sb.append("\"name\":\"").append(escapeJson((String) item.get("name"))).append("\"");
            sb.append("}");
        }
        sb.append("]");
        sb.append("}");

        writeJson(resp, sb.toString());
    }

    private void approveWarrantyRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                long id = Long.parseLong(idParam);
                String sql = "UPDATE dbo.ReturnRequests SET Status = 'APPROVED', ProcessedBy = ?, ProcessedAt = SYSDATETIME() WHERE ReturnRequestID = ?";
                User staff = (User) req.getSession().getAttribute("user");
                int staffId = staff != null ? staff.getId() : 2;

                try (Connection conn = DBContext.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, staffId);
                    ps.setLong(2, id);
                    ps.executeUpdate();
                    req.getSession().setAttribute("flash", "Đã duyệt đồng ý yêu cầu bảo hành!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                req.getSession().setAttribute("flash", "Lỗi duyệt: " + e.getMessage());
            }
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
    }

    private void rejectWarrantyRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String reason = req.getParameter("rejectReason");
        if (idParam != null && reason != null && !reason.trim().isEmpty()) {
            try {
                long id = Long.parseLong(idParam);
                String sql = "UPDATE dbo.ReturnRequests SET Status = 'REJECTED', Reason = Reason + N' (Từ chối: ' + ? + ')', ProcessedBy = ?, ProcessedAt = SYSDATETIME() WHERE ReturnRequestID = ?";
                User staff = (User) req.getSession().getAttribute("user");
                int staffId = staff != null ? staff.getId() : 2;

                try (Connection conn = DBContext.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, reason.trim());
                    ps.setInt(2, staffId);
                    ps.setLong(3, id);
                    ps.executeUpdate();
                    req.getSession().setAttribute("flash", "Đã từ chối yêu cầu bảo hành!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                req.getSession().setAttribute("flash", "Lỗi từ chối: " + e.getMessage());
            }
        } else {
            req.getSession().setAttribute("flash", "Lỗi: Lý do từ chối không được để trống!");
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
    }

    private void createWarrantyRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String orderIdOrPhone = req.getParameter("orderIdOrPhone");
        String variantIdStr = req.getParameter("variantId");
        String reason = req.getParameter("reason");

        if (orderIdOrPhone == null || orderIdOrPhone.trim().isEmpty() ||
                variantIdStr == null || variantIdStr.trim().isEmpty() ||
                reason == null || reason.trim().isEmpty()) {
            req.getSession().setAttribute("flash", "Lỗi: Vui lòng nhập đầy đủ các trường thông tin!");
            resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
            return;
        }

        String imagePath = saveUploadedFile(req);

        try {
            int variantId = Integer.parseInt(variantIdStr);

            String sqlFind = """
                SELECT DISTINCT o.OrderID, o.CustomerID, o.OrderCode, oi.ProductName, oi.VariantName
                FROM OrderItems oi
                JOIN Orders o ON oi.OrderID = o.OrderID
                WHERE (CAST(o.OrderID AS VARCHAR) = ? OR o.OrderCode = ? OR o.RecipientPhone = ?) AND oi.VariantID = ?
                """;

            long orderId = 0;
            int customerId = 0;
            String orderCode = "";
            String productName = "";
            String variantName = "";

            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlFind)) {
                ps.setString(1, orderIdOrPhone.trim());
                ps.setString(2, orderIdOrPhone.trim());
                ps.setString(3, orderIdOrPhone.trim());
                ps.setInt(4, variantId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        orderId = rs.getLong("OrderID");
                        customerId = rs.getInt("CustomerID");
                        orderCode = rs.getString("OrderCode");
                        productName = rs.getString("ProductName");
                        variantName = rs.getString("VariantName");
                    }
                }
            }

            if (orderId == 0) {
                req.getSession().setAttribute("flash", "Lỗi: Không tìm thấy sản phẩm trong đơn hàng tương ứng!");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
                return;
            }

            if (warrantyRepository != null && warrantyRepository.hasActiveWarranty(orderId, variantId)) {
                req.getSession().setAttribute("flash", "Sản phẩm này đã có phiếu bảo hành đang được xử lý. Không thể tạo thêm phiếu bảo hành mới.");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
                return;
            }

            String returnCode = "WR" + (System.currentTimeMillis() % 1000000);

            String sqlInsert = """
                INSERT INTO dbo.ReturnRequests (ReturnCode, OrderID, CustomerID, RequestType, Reason, EvidenceNote, Status, RefundAmount, CreatedAt)
                VALUES (?, ?, ?, 'WARRANTY', ?, ?, 'PENDING', 0, GETDATE())
                """;

            try (Connection conn = DBContext.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlInsert, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, returnCode);
                ps.setLong(2, orderId);
                ps.setInt(3, customerId);
                ps.setString(4, "Phiếu bảo hành tại quầy cho sản phẩm: " + productName + " (" + variantName + "). Lỗi: " + reason.trim());
                ps.setString(5, imagePath);
                ps.executeUpdate();

                long returnRequestId = 0;
                try (ResultSet rsKeys = ps.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        returnRequestId = rsKeys.getLong(1);
                    }
                }

                if (returnRequestId > 0) {
                    long orderItemId = 0;
                    String sqlOrderItem = "SELECT OrderItemID FROM dbo.OrderItems WHERE OrderID = ? AND VariantID = ?";
                    try (PreparedStatement psItem = conn.prepareStatement(sqlOrderItem)) {
                        psItem.setLong(1, orderId);
                        psItem.setInt(2, variantId);
                        try (ResultSet rsItem = psItem.executeQuery()) {
                            if (rsItem.next()) {
                                orderItemId = rsItem.getLong("OrderItemID");
                            }
                        }
                    }

                    if (orderItemId > 0) {
                        String sqlInsertItem = """
                            INSERT INTO dbo.ReturnItems (ReturnRequestID, OrderItemID, Quantity, ItemCondition, Resolution)
                            VALUES (?, ?, 1, 'NORMAL', 'REPAIR')
                            """;
                        try (PreparedStatement psInsertItem = conn.prepareStatement(sqlInsertItem)) {
                            psInsertItem.setLong(1, returnRequestId);
                            psInsertItem.setLong(2, orderItemId);
                            psInsertItem.executeUpdate();
                        }
                    }
                }

                req.getSession().setAttribute("flash", "Tạo phiếu bảo hành thành công! Mã yêu cầu: " + returnCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("flash", "Lỗi khi tạo phiếu bảo hành: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
    }

    private String saveUploadedFile(HttpServletRequest req) throws ServletException, IOException {
        try {
            Part filePart = req.getPart("evidenceImage");
            if (filePart == null || filePart.getSize() == 0) {
                return null;
            }
            String fileName = System.currentTimeMillis() + "_" + getFileName(filePart);
            String uploadPath = req.getServletContext().getRealPath("") + File.separator + "uploads";
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            filePart.write(uploadPath + File.separator + fileName);
            return "uploads/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String content : contentDisposition.split(";")) {
            if (content.trim().startsWith("filename")) {
                String name = content.substring(content.indexOf("=") + 2, content.length() - 1);
                int lastSlash = name.lastIndexOf(File.separator);
                if (lastSlash >= 0) {
                    name = name.substring(lastSlash + 1);
                }
                return name;
            }
        }
        return "evidence.jpg";
    }

    private void showOrderAdd(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Map<String, Object>> categories = new ArrayList<>();
        List<Map<String, Object>> brands = new ArrayList<>();
        List<Map<String, Object>> productList = new ArrayList<>();

        String queryCategories = "SELECT CategoryID, CategoryName FROM Categories ORDER BY CategoryName ASC";
        String queryBrands = "SELECT BrandID, BrandName FROM Brands ORDER BY BrandName ASC";
        String queryProducts = """
            SELECT pv.VariantID, p.ProductName, pv.VariantName, pv.SKU, pv.SalePrice, ISNULL(ib.QuantityOnHand, 0) AS Stock
            FROM ProductVariants pv
            JOIN Products p ON pv.ProductID = p.ProductID
            LEFT JOIN InventoryBalances ib ON pv.VariantID = ib.VariantID
            WHERE p.Status = 'ACTIVE' AND pv.Status = 'ACTIVE'
            ORDER BY p.ProductName ASC, pv.VariantName ASC
            """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement psCat = conn.prepareStatement(queryCategories);
             ResultSet rsCat = psCat.executeQuery();
             PreparedStatement psBrand = conn.prepareStatement(queryBrands);
             ResultSet rsBrand = psBrand.executeQuery();
             PreparedStatement psProd = conn.prepareStatement(queryProducts);
             ResultSet rsProd = psProd.executeQuery()) {

            while (rsCat.next()) {
                Map<String, Object> cat = new HashMap<>();
                cat.put("categoryId", rsCat.getInt("CategoryID"));
                cat.put("categoryName", rsCat.getString("CategoryName"));
                categories.add(cat);
            }

            while (rsBrand.next()) {
                Map<String, Object> br = new HashMap<>();
                br.put("brandID", rsBrand.getInt("BrandID"));
                br.put("brandName", rsBrand.getString("BrandName"));
                brands.add(br);
            }

            while (rsProd.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("variantId", rsProd.getInt("VariantID"));
                p.put("productName", rsProd.getString("ProductName"));
                p.put("variantName", rsProd.getString("VariantName") != null ? rsProd.getString("VariantName") : "");
                double price = rsProd.getDouble("SalePrice");
                p.put("price", price);
                p.put("stock", rsProd.getInt("Stock"));
                p.put("sku", rsProd.getString("SKU"));
                p.put("formattedPrice", String.format("%,.0f ₫", price));
                productList.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        req.setAttribute("allCategories", categories);
        req.setAttribute("allBrands", brands);
        req.setAttribute("productList", productList);
        req.setAttribute("moduleTitle", "Thêm đơn hàng");
        ViewRouter.admin(req, resp, "sales/order-add", "Thêm đơn hàng", "sales");
    }

    private void receiveWarrantyRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                long id = Long.parseLong(idParam);
                String sql = "UPDATE dbo.ReturnRequests SET Status = 'RECEIVED', ProcessedBy = ?, ProcessedAt = SYSDATETIME() WHERE ReturnRequestID = ?";
                User staff = (User) req.getSession().getAttribute("user");
                int staffId = staff != null ? staff.getId() : 2;

                try (Connection conn = DBContext.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, staffId);
                    ps.setLong(2, id);
                    ps.executeUpdate();
                    req.getSession().setAttribute("flash", "Đã tiếp nhận sản phẩm và bắt đầu bảo hành!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                req.getSession().setAttribute("flash", "Lỗi tiếp nhận: " + e.getMessage());
            }
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
    }

    private void completeWarrantyRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                long id = Long.parseLong(idParam);
                String sql = "UPDATE dbo.ReturnRequests SET Status = 'COMPLETED', ProcessedBy = ?, ProcessedAt = SYSDATETIME() WHERE ReturnRequestID = ?";
                User staff = (User) req.getSession().getAttribute("user");
                int staffId = staff != null ? staff.getId() : 2;

                try (Connection conn = DBContext.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, staffId);
                    ps.setLong(2, id);
                    ps.executeUpdate();
                    req.getSession().setAttribute("flash", "Đã xác nhận bảo hành thành công và hoàn thành phiếu!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                req.getSession().setAttribute("flash", "Lỗi hoàn tất: " + e.getMessage());
            }
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
    }

    private void cancelWarrantyRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam != null) {
            try {
                long id = Long.parseLong(idParam);
                String sql = "UPDATE dbo.ReturnRequests SET Status = 'CANCELLED', ProcessedBy = ?, ProcessedAt = SYSDATETIME() WHERE ReturnRequestID = ?";
                User staff = (User) req.getSession().getAttribute("user");
                int staffId = staff != null ? staff.getId() : 2;

                try (Connection conn = DBContext.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, staffId);
                    ps.setLong(2, id);
                    ps.executeUpdate();
                    req.getSession().setAttribute("flash", "Đã hủy phiếu bảo hành!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                req.getSession().setAttribute("flash", "Lỗi hủy phiếu: " + e.getMessage());
            }
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
    }
}