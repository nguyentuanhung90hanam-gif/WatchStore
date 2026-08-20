package com.watchstore.controller.sales;

import com.watchstore.config.DBContext;
import com.watchstore.model.Customer;
import com.watchstore.model.Order;
import com.watchstore.enums.OrderStatus;
import com.watchstore.model.User; // Đã bổ sung import User
import com.watchstore.repository.CustomerRepository;
import com.watchstore.repository.OrderRepository;
import com.watchstore.repository.ProductRepository;
import com.watchstore.repository.WarrantyRepository;
import com.watchstore.util.ViewRouter;
import java.sql.Date;

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
import com.watchstore.model.Review;
import com.watchstore.model.ProductComment;
import com.watchstore.repository.ReviewRepository;
import com.watchstore.repository.CommentRepository;
import java.util.List;
import java.util.Map;

@WebServlet("/manage/sales/*")
public class SalesController extends HttpServlet {

    private CustomerRepository customerRepository;
    private OrderRepository orderRepository;
    private WarrantyRepository warrantyRepository;
    private ReviewRepository reviewRepository;
    private CommentRepository commentRepository;
    private com.watchstore.repository.VariantRepository variantRepository = new com.watchstore.repository.VariantRepository();

    private static final Map<String, String[]> PAGES = Map.ofEntries(
            Map.entry("/dashboard", new String[]{"dashboard", "Tổng quan bán hàng"}),
            Map.entry("/orders", new String[]{"order-list", "Quản lý đơn hàng"}),
            Map.entry("/order-detail", new String[]{"order-detail", "Chi tiết đơn hàng"}),
            Map.entry("/customers", new String[]{"customer-list", "Danh sách khách hàng"}),
            Map.entry("/customer-detail", new String[]{"customer-detail", "Chi tiết khách hàng"}),
            Map.entry("/customer-add", new String[]{"customer-add", "Thêm khách hàng"}),
            Map.entry("/reviews", new String[]{"review", "Kiểm duyệt đánh giá"}),
            Map.entry("/comments", new String[]{"comment", "Bình luận"}),
            Map.entry("/warranty", new String[]{"warranty", "Quản lý bảo hành"}),
            Map.entry("/report", new String[]{"report", "Báo cáo bán hàng"})
    );

    @Override
    public void init() {
        customerRepository = (CustomerRepository) getServletContext().getAttribute("customerRepository");
        orderRepository = (OrderRepository) getServletContext().getAttribute("orderRepository");
        warrantyRepository = (WarrantyRepository) getServletContext().getAttribute("warrantyRepository");
        reviewRepository = (ReviewRepository) getServletContext().getAttribute("reviewRepository");
        commentRepository = (CommentRepository) getServletContext().getAttribute("commentRepository");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();

        if ("/api/order-details".equals(path)) {
            getOrderDetailsJson(req, resp);
            return;
        }

        if ("/api/orders-search".equals(path)) {
            searchOrdersJson(req, resp);
            return;
        }

        if ("/api/products-search".equals(path)) {
            searchProductsJson(req, resp);
            return;
        }

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
            req.setAttribute("customers", customerRepository != null ? customerRepository.findAll() : java.util.Collections.emptyList());
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
            User user = (User) req.getSession().getAttribute("user");
            if (user != null && user.getRole() == com.watchstore.enums.Role.ADMIN) {
                req.getSession().setAttribute("flash", "Lỗi: Quản trị viên không thực hiện lập phiếu bảo hành trong luồng nghiệp vụ thông thường.");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
                return;
            }
            ProductRepository prodRepo = (ProductRepository) getServletContext().getAttribute("productRepository");
            if (prodRepo != null) {
                req.setAttribute("activeProducts", prodRepo.findAll());
            }
            req.setAttribute("moduleTitle", "Thêm phiếu bảo hành");
            ViewRouter.admin(req, resp, "sales/warranty-add", "Thêm phiếu bảo hành", "sales");
            return;
        }

        if ("/report".equals(path)) {
            showReport(req, resp);
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
            User user = (User) req.getSession().getAttribute("user");
            if (user != null && user.getRole() == com.watchstore.enums.Role.ADMIN) {
                req.getSession().setAttribute("flash", "Lỗi: Quản trị viên không thực hiện lập phiếu bảo hành trong luồng nghiệp vụ thông thường.");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
                return;
            }
            createWarranty(req, resp);
            return;
        }

        if ("/warranty".equals(path)) {
            updateWarrantyStatus(req, resp);
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
            req.setAttribute("orderHistory", orderRepository != null ? orderRepository.getHistory(id) : new ArrayList<>());
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
                User user = (User) req.getSession().getAttribute("user");
                if (user != null && user.getRole() == com.watchstore.enums.Role.EMPLOYEE) {
                    if (!"PENDING".equals(status) && !"CONFIRMED".equals(status) && !"PACKING".equals(status) && !"CANCELLED".equals(status)) {
                        req.getSession().setAttribute("flash", "Lỗi: Nhân viên bán hàng không có quyền cập nhật trạng thái vận chuyển hoặc hoàn thành đơn hàng.");
                        resp.sendRedirect(req.getContextPath() + "/manage/sales/order-detail?id=" + id);
                        return;
                    }
                }
                if (orderRepository != null) {
                    orderRepository.updateStatus(id, status);
                    String performerName = user != null ? user.getFullName() : "Hệ thống";
                    orderRepository.logHistory(id, "UPDATE_STATUS", "Cập nhật trạng thái sang: " + status, performerName);
                }
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
            resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty-add");
            return;
        }
        try {
            String warrantyType = req.getParameter("warrantyType");
            String orderIdStr   = req.getParameter("orderId");

            if ("OFFLINE".equalsIgnoreCase(warrantyType) || orderIdStr == null || orderIdStr.trim().isEmpty() || "0".equals(orderIdStr.trim())) {
                String customerName  = req.getParameter("customerName");
                String customerPhone = req.getParameter("customerPhone");
                String customerEmail = req.getParameter("customerEmail");
                String productName   = req.getParameter("productName");
                String serial        = req.getParameter("serial");
                int months           = 12;
                try { months = Integer.parseInt(req.getParameter("months")); } catch (Exception ignored) {}
                String buyDateStr    = req.getParameter("buyDate");
                Date buyDate         = null;
                if (buyDateStr != null && !buyDateStr.isBlank()) {
                    try { buyDate = Date.valueOf(buyDateStr.trim()); } catch (Exception ignored) {}
                }
                String note          = req.getParameter("note");
                String imageUrl      = req.getParameter("imageUrl");

                warrantyRepository.insertOfflineWarranty(customerName, customerPhone, customerEmail, productName, serial, months, buyDate, note, imageUrl);
                req.getSession().setAttribute("flash", "Đã lập phiếu bảo hành trực tiếp cho khách mua tại quầy thành công! Trạng thái: Đang xử lý.");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
                return;
            }

            int orderId = Integer.parseInt(orderIdStr.trim());
            String productName = req.getParameter("productName");
            String note        = req.getParameter("note");
            String imageUrl    = req.getParameter("imageUrl");

            if (orderRepository == null) {
                req.getSession().setAttribute("flash", "Lỗi hệ thống: Không thể kết nối cơ sở dữ liệu đơn hàng.");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty-add");
                return;
            }
            Order order = orderRepository.findById(orderId);
            if (order == null) {
                req.getSession().setAttribute("flash", "Lỗi: Đơn hàng #" + orderId + " không tồn tại!");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty-add");
                return;
            }

            // Check if an active warranty card for this product in this order already exists
            if (warrantyRepository.hasActiveWarranty(orderId, productName)) {
                req.getSession().setAttribute("flash", "Lỗi: Phiếu bảo hành đang hoạt động cho sản phẩm '" + productName + "' của đơn hàng #" + orderId + " đã tồn tại!");
                resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty-add");
                return;
            }

            warrantyRepository.insertOnlineWarranty(orderId, productName, note != null && !note.isBlank() ? note : "Lập phiếu bảo hành tại quầy", imageUrl);
            req.getSession().setAttribute("flash", "Đã tạo phiếu bảo hành thành công!");
            resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
            return;
        } catch (Exception e) {
            Throwable t = e;
            while (t.getCause() != null) t = t.getCause();
            req.getSession().setAttribute("flash", "Lỗi: " + (t.getMessage() == null ? "Thao tác không thành công." : t.getMessage()));
            resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty-add");
        }
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
                else if ("start_processing".equalsIgnoreCase(action)) {
                    warrantyRepository.updateProcessing(id);
                    req.getSession().setAttribute("flash", "Đã chuyển trạng thái bảo hành sang Đang xử lý!");
                }
                else if (status != null) {
                    String note = req.getParameter("note");
                    if (note != null && !note.trim().isEmpty()) {
                        warrantyRepository.updateStatusAndNote(id, status, note);
                    } else {
                        warrantyRepository.updateStatus(id, status);
                    }
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
        String minAmountStr = req.getParameter("minAmount");
        String maxAmountStr = req.getParameter("maxAmount");

        BigDecimal minAmount = null;
        BigDecimal maxAmount = null;
        try {
            if (minAmountStr != null && !minAmountStr.trim().isEmpty()) {
                minAmount = new BigDecimal(minAmountStr.trim());
            }
        } catch (NumberFormatException ignored) {}
        try {
            if (maxAmountStr != null && !maxAmountStr.trim().isEmpty()) {
                maxAmount = new BigDecimal(maxAmountStr.trim());
            }
        } catch (NumberFormatException ignored) {}

        List<Order> orders = orderRepository != null ? orderRepository.search(keyword, status, fromDate, toDate, minAmount, maxAmount) : java.util.Collections.emptyList();
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
        req.setAttribute("minAmount", minAmountStr);
        req.setAttribute("maxAmount", maxAmountStr);
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

    private void showReviews(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String status  = req.getParameter("status");
        String ratingParam = req.getParameter("rating");

        Integer rating = null;
        if (ratingParam != null && !ratingParam.trim().isEmpty()) {
            try {
                rating = Integer.parseInt(ratingParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        List<Review> list = reviewRepository != null ? reviewRepository.search(keyword, status, rating) : Collections.emptyList();
        List<Map<String, Object>> reviews = new ArrayList<>();
        for (Review r : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getReviewId());
            m.put("customerName", r.getUserFullName() != null ? r.getUserFullName() : "Khách hàng #" + r.getUserId());
            m.put("productName", r.getProductName() != null ? r.getProductName() : "Sản phẩm #" + r.getProductId());
            m.put("rating", r.getRating());
            m.put("content", r.getContent());
            m.put("status", r.getStatus());
            m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString().substring(0, 10) : "");
            m.put("orderCode", r.getOrderCode() != null ? r.getOrderCode() : (r.getOrderId() != null ? "WS" + r.getOrderId() : "N/A"));
            m.put("reply", "");
            reviews.add(m);
        }

        req.setAttribute("reviews", reviews);
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

        List<ProductComment> list = commentRepository != null ? commentRepository.search(keyword, status) : Collections.emptyList();
        List<Map<String, Object>> comments = new ArrayList<>();
        for (ProductComment c : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getCommentId());
            m.put("customerName", c.getUserFullName() != null ? c.getUserFullName() : "Khách hàng #" + c.getUserId());
            m.put("productName", c.getProductName() != null ? c.getProductName() : "Sản phẩm #" + c.getProductId());
            m.put("content", c.getContent());
            m.put("status", c.getStatus());
            m.put("commentDate", c.getCreatedAt() != null ? c.getCreatedAt().toString().substring(0, 16) : "");
            comments.add(m);
        }

        req.setAttribute("comments", comments);
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
        req.setAttribute("moduleTitle", "Bình luận");
        ViewRouter.admin(req, resp, "sales/comment", "Bình luận", "sales");
    }

    private void updateReviewStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String status  = req.getParameter("status");
        String action  = req.getParameter("action");

        if (status == null && action != null) {
            if ("approve".equalsIgnoreCase(action)) status = "APPROVED";
            else if ("reject".equalsIgnoreCase(action)) status = "REJECTED";
        }

        if (idParam != null && status != null && reviewRepository != null) {
            try {
                long id = Long.parseLong(idParam);
                reviewRepository.updateStatus(id, status);
                req.getSession().setAttribute("flash", "Đã cập nhật trạng thái đánh giá thành công!");
            } catch (Exception e) {
                req.getSession().setAttribute("flash", "Lỗi: " + e.getMessage());
            }
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/reviews");
    }

    private void updateCommentStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String status  = req.getParameter("status");

        if (idParam != null && status != null && commentRepository != null) {
            try {
                long id = Long.parseLong(idParam);
                commentRepository.updateStatus(id, status);
                req.getSession().setAttribute("flash", "Đã xác nhận cập nhật trạng thái bình luận thành công!");
            } catch (Exception e) {
                req.getSession().setAttribute("flash", "Lỗi: " + e.getMessage());
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
            if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
                req.getSession().setAttribute("flash", "Lỗi: Chỉ có thể sửa thông tin đơn hàng ở trạng thái Chờ xử lý hoặc Đã xác nhận!");
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
        User user = (User) req.getSession().getAttribute("user");
        String customerIdStr = req.getParameter("customerId");
        String customerName = req.getParameter("customerName");
        String phone = req.getParameter("phone");
        String shippingAddress = req.getParameter("shippingAddress");
        String status = req.getParameter("status");
        String paymentStatus = req.getParameter("paymentStatus");
        String cartItemsJson = req.getParameter("cartItemsJson");

        if (customerName == null || customerName.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty() ||
            shippingAddress == null || shippingAddress.trim().isEmpty()) {
            req.getSession().setAttribute("flash", "Lỗi: Vui lòng nhập đầy đủ thông tin khách hàng.");
            resp.sendRedirect(req.getContextPath() + "/manage/sales/order-add");
            return;
        }

        if (cartItemsJson == null || cartItemsJson.trim().isEmpty() || "[]".equals(cartItemsJson.trim())) {
            req.getSession().setAttribute("flash", "Lỗi: Đơn hàng phải có ít nhất một sản phẩm.");
            resp.sendRedirect(req.getContextPath() + "/manage/sales/order-add");
            return;
        }

        // Parse JSON items
        List<Map<String, Object>> parsedItems = new ArrayList<>();
        try {
            String json = cartItemsJson.trim();
            json = json.substring(1, json.length() - 1); // remove outer brackets
            String[] parts = json.split("\\},\\{");
            for (String part : parts) {
                part = part.replace("{", "").replace("}", "").trim();
                if (part.isEmpty()) continue;
                String[] fields = part.split(",");
                int variantId = 0;
                int qty = 0;
                for (String field : fields) {
                    String[] kv = field.split(":");
                    if (kv.length == 2) {
                        String key = kv[0].replace("\"", "").trim();
                        String val = kv[1].replace("\"", "").trim();
                        if ("variantId".equals(key)) {
                            variantId = Integer.parseInt(val);
                        } else if ("quantity".equals(key)) {
                            qty = Integer.parseInt(val);
                        }
                    }
                }
                if (variantId > 0 && qty > 0) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("variantId", variantId);
                    map.put("quantity", qty);
                    parsedItems.add(map);
                }
            }
        } catch (Exception e) {
            req.getSession().setAttribute("flash", "Lỗi định dạng giỏ hàng: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/manage/sales/order-add");
            return;
        }

        if (parsedItems.isEmpty()) {
            req.getSession().setAttribute("flash", "Lỗi: Không phân tích được sản phẩm nào trong giỏ hàng.");
            resp.sendRedirect(req.getContextPath() + "/manage/sales/order-add");
            return;
        }

        Connection conn = null;
        try {
            conn = com.watchstore.config.DBContext.getConnection();
            conn.setAutoCommit(false); // start transaction

            // 1. Resolve customer
            int finalCustomerId = 4; // guest fallback
            if (customerIdStr != null && !customerIdStr.trim().isEmpty() && !"new".equals(customerIdStr.trim())) {
                finalCustomerId = Integer.parseInt(customerIdStr.trim());
            } else {
                // Check if customer phone already exists
                List<com.watchstore.model.Customer> match = customerRepository.search(phone.trim());
                com.watchstore.model.Customer existing = null;
                if (match != null) {
                    for (com.watchstore.model.Customer c : match) {
                        if (phone.trim().equals(c.getPhone())) {
                            existing = c;
                            break;
                        }
                    }
                }
                if (existing != null) {
                    finalCustomerId = existing.getId();
                } else {
                    // Create new customer
                    com.watchstore.model.Customer newCustomer = new com.watchstore.model.Customer();
                    newCustomer.setFullName(customerName);
                    newCustomer.setPhone(phone);
                    newCustomer.setEmail(phone.trim() + "@watchstore.com");
                    newCustomer.setAddress(shippingAddress);
                    customerRepository.insert(newCustomer);

                    // Re-find to get ID
                    List<com.watchstore.model.Customer> matchNew = customerRepository.search(phone.trim());
                    if (matchNew != null) {
                        for (com.watchstore.model.Customer c : matchNew) {
                            if (phone.trim().equals(c.getPhone())) {
                                finalCustomerId = c.getId();
                                break;
                            }
                        }
                    }
                }
            }

            // 2. Resolve items & check stock & calculate total
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<Map<String, Object>> resolvedItems = new ArrayList<>();

            String selectVarSql = """
                SELECT 
                    pv.VariantID, 
                    pv.SKU, 
                    p.ProductName, 
                    pv.VariantName, 
                    pv.SalePrice,
                    (SELECT COALESCE(SUM(ib.QuantityOnHand - ib.QuantityReserved), 0) 
                     FROM dbo.InventoryBalances ib 
                     WHERE ib.VariantID = pv.VariantID) AS AvailableStock,
                    (SELECT TOP 1 pi.ImageUrl 
                     FROM dbo.ProductImages pi 
                     WHERE pi.ProductID = p.ProductID 
                     ORDER BY pi.IsPrimary DESC, pi.DisplayOrder ASC) AS ImageUrl
                FROM dbo.ProductVariants pv
                INNER JOIN dbo.Products p ON pv.ProductID = p.ProductID
                WHERE pv.VariantID = ?
                """;

            for (Map<String, Object> item : parsedItems) {
                int vId = (Integer) item.get("variantId");
                int quantity = (Integer) item.get("quantity");

                try (PreparedStatement ps = conn.prepareStatement(selectVarSql)) {
                    ps.setInt(1, vId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new Exception("Sản phẩm ID #" + vId + " không tồn tại trong hệ thống.");
                        }
                        String pName = rs.getString("ProductName");
                        String vName = rs.getString("VariantName");
                        String sku = rs.getString("SKU");
                        BigDecimal price = rs.getBigDecimal("SalePrice");
                        int stock = rs.getInt("AvailableStock");
                        String imgUrl = rs.getString("ImageUrl");

                        if (quantity > stock) {
                            throw new Exception("Sản phẩm '" + pName + " (" + vName + ")' không đủ hàng tồn kho (Tồn: " + stock + ", yêu cầu: " + quantity + ").");
                        }

                        BigDecimal lineTotal = price.multiply(new BigDecimal(quantity));
                        totalAmount = totalAmount.add(lineTotal);

                        Map<String, Object> resolved = new HashMap<>();
                        resolved.put("variantId", vId);
                        resolved.put("productName", pName);
                        resolved.put("variantName", vName);
                        resolved.put("sku", sku);
                        resolved.put("price", price);
                        resolved.put("quantity", quantity);
                        resolved.put("imageUrl", imgUrl);
                        resolvedItems.add(resolved);
                    }
                }
            }

            // 3. Insert order
            String orderCode = "WS" + (8500 + (orderRepository != null ? orderRepository.findAll().size() : 0) + 1);
            String insertOrderSql = """
                INSERT INTO Orders (OrderCode, CustomerID, RecipientName, RecipientPhone, ShippingAddress, SubtotalAmount, DiscountAmount, ShippingFee, TotalAmount, OrderStatus, PaymentStatus)
                VALUES (?, ?, ?, ?, ?, ?, 0, 0, ?, ?, ?)
                """;

            long orderId = 0;
            try (PreparedStatement ps = conn.prepareStatement(insertOrderSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, orderCode);
                ps.setInt(2, finalCustomerId);
                ps.setString(3, customerName);
                ps.setString(4, phone);
                ps.setString(5, shippingAddress);
                ps.setBigDecimal(6, totalAmount);
                ps.setBigDecimal(7, totalAmount);
                ps.setString(8, status);
                ps.setString(9, paymentStatus);
                ps.executeUpdate();

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        orderId = generatedKeys.getLong(1);
                    } else {
                        throw new SQLException("Không thể tạo đơn hàng (Không lấy được ID).");
                    }
                }
            }

            // 4. Insert order items & update stock
            String insertItemSql = """
                INSERT INTO OrderItems (OrderID, VariantID, ProductName, VariantName, SKU, ImageUrl, UnitPrice, Quantity, DiscountAmount)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)
                """;

            String updateStockSql = """
                UPDATE dbo.InventoryBalances 
                SET QuantityOnHand = QuantityOnHand - ? 
                WHERE VariantID = ? AND WarehouseID = 1
                """;

            for (Map<String, Object> resolved : resolvedItems) {
                int vId = (Integer) resolved.get("variantId");
                String pName = (String) resolved.get("productName");
                String vName = (String) resolved.get("variantName");
                String sku = (String) resolved.get("sku");
                BigDecimal price = (BigDecimal) resolved.get("price");
                int qty = (Integer) resolved.get("quantity");
                String imgUrl = (String) resolved.get("imageUrl");

                // Insert item
                try (PreparedStatement ps = conn.prepareStatement(insertItemSql)) {
                    ps.setLong(1, orderId);
                    ps.setInt(2, vId);
                    ps.setString(3, pName);
                    ps.setString(4, vName);
                    ps.setString(5, sku);
                    ps.setString(6, imgUrl);
                    ps.setBigDecimal(7, price);
                    ps.setInt(8, qty);
                    ps.executeUpdate();
                }

                // Update stock
                try (PreparedStatement ps = conn.prepareStatement(updateStockSql)) {
                    ps.setInt(1, qty);
                    ps.setInt(2, vId);
                    ps.executeUpdate();
                }
            }

            conn.commit(); // commit transaction
            String performerName = user != null ? user.getFullName() : "Hệ thống";
            if (orderRepository != null) {
                orderRepository.logHistory(orderId, "CREATE", "Khởi tạo đơn hàng từ nhân viên/quản trị.", performerName);
            }
            req.getSession().setAttribute("flash", "Tạo đơn hàng thành công! Mã đơn: " + orderCode);

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            e.printStackTrace();
            req.getSession().setAttribute("flash", "Lỗi khi tạo đơn hàng: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/manage/sales/order-add");
            return;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
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
                if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
                    req.getSession().setAttribute("flash", "Lỗi: Chỉ có thể sửa thông tin đơn hàng ở trạng thái Chờ xử lý hoặc Đã xác nhận!");
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

                if (orderRepository != null && orderRepository.update(order)) {
                    User user = (User) req.getSession().getAttribute("user");
                    String performerName = user != null ? user.getFullName() : "Hệ thống";
                    orderRepository.logHistory(id, "EDIT", "Chỉnh sửa chi tiết thông tin đơn hàng.", performerName);
                    req.getSession().setAttribute("flash", "Cập nhật đơn hàng thành công!");
                } else {
                    req.getSession().setAttribute("flash", "Lỗi khi cập nhật thông tin đơn hàng.");
                }
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
                User user = (User) req.getSession().getAttribute("user");
                if (user != null && user.getRole() == com.watchstore.enums.Role.ADMIN) {
                    req.getSession().setAttribute("flash", "Lỗi: Quản trị viên không thực hiện xác nhận đơn hàng trong nghiệp vụ thông thường.");
                    resp.sendRedirect(req.getContextPath() + "/manage/sales/order-detail?id=" + id);
                    return;
                }
                if (orderRepository != null && orderRepository.updateStatus(id, "CONFIRMED")) {
                    String performerName = user != null ? user.getFullName() : "Hệ thống";
                    orderRepository.logHistory(id, "CONFIRM", "Xác nhận đơn hàng thành công.", performerName);
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
                Order order = orderRepository != null ? orderRepository.findById(id) : null;
                if (order != null) {
                    if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
                        req.getSession().setAttribute("flash", "Lỗi: Không thể hủy đơn hàng đã hoàn thành hoặc đã hủy.");
                        resp.sendRedirect(req.getContextPath() + "/manage/sales/order-detail?id=" + id);
                        return;
                    }
                    User user = (User) req.getSession().getAttribute("user");
                    if (user != null && user.getRole() == com.watchstore.enums.Role.EMPLOYEE) {
                        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.PACKING) {
                            req.getSession().setAttribute("flash", "Lỗi: Nhân viên bán hàng chỉ có thể hủy đơn ở trạng thái Chờ xử lý, Đã xác nhận hoặc Đang đóng gói.");
                            resp.sendRedirect(req.getContextPath() + "/manage/sales/order-detail?id=" + id);
                            return;
                        }
                    }
                    if (orderRepository != null && orderRepository.cancelOrderAndRestoreStock(id)) {
                        String performerName = user != null ? user.getFullName() : "Hệ thống";
                        orderRepository.logHistory(id, "CANCEL", "Hủy đơn hàng và hoàn trả lại số lượng tồn kho.", performerName);
                        req.getSession().setAttribute("flash", "Đã hủy đơn hàng thành công!");
                    } else {
                        req.getSession().setAttribute("flash", "Lỗi khi hủy đơn hàng.");
                    }
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
                            User user = (User) req.getSession().getAttribute("user");
                            String performerName = user != null ? user.getFullName() : "Hệ thống";
                            orderRepository.logHistory(id, "EDIT_SHIPPING", "Cập nhật thông tin giao nhận: Người nhận: " + customerName + ", SĐT: " + phone + ", Địa chỉ: " + shippingAddress, performerName);
                            req.getSession().setAttribute("flash", "Cập nhật thông tin giao nhận thành công!");
                        } else {
                            req.getSession().setAttribute("flash", "Lỗi khi cập nhật thông tin giao hàng.");
                        }
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

    private void getOrderDetailsJson(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String idParam = req.getParameter("orderId");
        if (idParam == null || idParam.isBlank()) {
            resp.getWriter().write("{\"error\": \"Missing orderId\"}");
            return;
        }
        try {
            int orderId = Integer.parseInt(idParam);
            Order order = orderRepository.findById(orderId);
            if (order == null) {
                resp.getWriter().write("{\"error\": \"Order not found\"}");
                return;
            }
            List<Map<String, Object>> items = orderRepository.getOrderItems(orderId);
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"id\":").append(order.getId()).append(",");
            sb.append("\"code\":\"").append(escapeJson(order.getCode())).append("\",");
            sb.append("\"customerName\":\"").append(escapeJson(order.getCustomerName())).append("\",");
            sb.append("\"phone\":\"").append(escapeJson(order.getPhone())).append("\",");
            sb.append("\"items\":[");
            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> item = items.get(i);
                String pName = String.valueOf(item.get("productName"));
                String vName = String.valueOf(item.get("variantName"));
                
                String targetName = (vName != null && !vName.trim().isEmpty()) ? vName : pName;
                boolean hasWarranty = warrantyRepository.hasActiveWarranty(orderId, targetName);

                sb.append("{");
                sb.append("\"productName\":\"").append(escapeJson(pName)).append("\",");
                sb.append("\"variantName\":\"").append(escapeJson(vName)).append("\",");
                sb.append("\"sku\":\"").append(escapeJson(String.valueOf(item.get("sku")))).append("\",");
                sb.append("\"hasWarranty\":").append(hasWarranty);
                sb.append("}");
                if (i < items.size() - 1) sb.append(",");
            }
            sb.append("]");
            sb.append("}");
            resp.getWriter().write(sb.toString());
        } catch (Exception e) {
            resp.getWriter().write("{\"error\": \"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void searchOrdersJson(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String query = req.getParameter("query");
        if (query == null) query = "";
        try {
            List<Order> orders = orderRepository.search(query, null);
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < orders.size(); i++) {
                Order o = orders.get(i);
                sb.append("{");
                sb.append("\"id\":").append(o.getId()).append(",");
                sb.append("\"code\":\"").append(escapeJson(o.getCode())).append("\",");
                sb.append("\"customerName\":\"").append(escapeJson(o.getCustomerName())).append("\",");
                sb.append("\"phone\":\"").append(escapeJson(o.getPhone())).append("\"");
                sb.append("}");
                if (i < orders.size() - 1) sb.append(",");
            }
            sb.append("]");
            resp.getWriter().write(sb.toString());
        } catch (Exception e) {
            resp.getWriter().write("{\"error\": \"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void searchProductsJson(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String query = req.getParameter("query");
        List<Map<String, Object>> list = variantRepository.searchForPOS(query);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = list.get(i);
            sb.append("{");
            sb.append("\"variantId\":").append(map.get("variantId")).append(",");
            sb.append("\"productId\":").append(map.get("productId")).append(",");
            sb.append("\"productName\":\"").append(escapeJson(String.valueOf(map.get("productName")))).append("\",");
            sb.append("\"variantName\":\"").append(escapeJson(String.valueOf(map.get("variantName")))).append("\",");
            sb.append("\"sku\":\"").append(escapeJson(String.valueOf(map.get("sku")))).append("\",");
            sb.append("\"salePrice\":").append(map.get("salePrice")).append(",");
            sb.append("\"stock\":").append(map.get("stock")).append(",");
            String imageUrl = map.get("imageUrl") != null ? String.valueOf(map.get("imageUrl")) : "";
            sb.append("\"imageUrl\":\"").append(escapeJson(imageUrl)).append("\"");
            sb.append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        resp.getWriter().write(sb.toString());
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
