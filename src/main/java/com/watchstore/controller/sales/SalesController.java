package com.watchstore.controller.sales;

import com.watchstore.model.Customer;
import com.watchstore.model.Order;
import com.watchstore.repository.CustomerRepository;
import com.watchstore.repository.MockDataStore;
import com.watchstore.repository.OrderRepository;
import com.watchstore.repository.WarrantyRepository;
import com.watchstore.util.ViewRouter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.ArrayList;
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
            List<Order> orders = orderRepository != null ? orderRepository.findAll() : MockDataStore.orders();
            req.setAttribute("orders", orders);
            req.setAttribute("moduleTitle", "Vận chuyển");
            ViewRouter.admin(req, resp, "sales/delivery", "Vận chuyển", "sales");
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
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()))
                .mapToDouble(o -> o.getTotal() != null ? o.getTotal().doubleValue() : 0.0)
                .sum();

        long completedCount = orders.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()))
                .count();

        long processingCount = orders.stream()
                .filter(o -> "PENDING".equalsIgnoreCase(o.getStatus()) || "CONFIRMED".equalsIgnoreCase(o.getStatus()) || "PACKING".equalsIgnoreCase(o.getStatus()))
                .count();

        long shippingCount = orders.stream()
                .filter(o -> "SHIPPING".equalsIgnoreCase(o.getStatus()) || "DELIVERED".equalsIgnoreCase(o.getStatus()))
                .count();

        req.setAttribute("orders", orders);
        req.setAttribute("revenue", totalRevenue);
        req.setAttribute("completedOrders", completedCount);
        req.setAttribute("processingOrders", processingCount);
        req.setAttribute("shippingOrders", shippingCount);
        req.setAttribute("warrantyCount", warrantyRepository != null ? warrantyRepository.countAll() : 0);
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

            req.setAttribute("customer", customer);
            req.setAttribute("customerOrders", customerOrders);
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
        req.setAttribute("warranties", warrantyRepository != null ? warrantyRepository.search(keyword, status) : java.util.Collections.emptyList());
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
            warrantyRepository.insert(orderId, productName, serial, months, note);
            req.getSession().setAttribute("flash", "Đã tạo phiếu bảo hành thành công!");
        } catch (Exception e) {
            req.getSession().setAttribute("flash", "Lỗi khi tạo phiếu bảo hành: " + e.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
    }

    private void updateWarrantyStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String idParam = req.getParameter("id");
        String status  = req.getParameter("status");
        if (warrantyRepository != null && idParam != null && status != null) {
            try {
                warrantyRepository.updateStatus(Integer.parseInt(idParam), status);
                req.getSession().setAttribute("flash", "Đã cập nhật trạng thái bảo hành!");
            } catch (Exception ignored) {}
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/warranty");
    }

    private void showOrders(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String status  = req.getParameter("status");

        List<Order> orders = orderRepository != null ? orderRepository.search(keyword, status) : MockDataStore.orders();

        req.setAttribute("orders", orders);
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
        req.setAttribute("moduleTitle", "Quản lý đơn hàng");

        ViewRouter.admin(req, resp, "sales/order-list", "Quản lý đơn hàng", "sales");
    }

    private void showReport(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String status   = req.getParameter("status");
        String fromDate = req.getParameter("fromDate");
        String toDate   = req.getParameter("toDate");

        List<Order> allOrders = orderRepository != null ? orderRepository.findAll() : MockDataStore.orders();
        List<Order> filtered  = new ArrayList<>(allOrders);

        if (status != null && !status.isBlank()) {
            String st = status.trim().toUpperCase();
            filtered = filtered.stream().filter(o -> {
                String s = o.getStatus() != null ? o.getStatus().toUpperCase() : "";
                if ("COMPLETED".equals(st) || "HOÀN THÀNH".equals(st)) return "COMPLETED".equals(s) || "HOÀN THÀNH".equals(s);
                if ("SHIPPING".equals(st)  || "ĐANG GIAO".equals(st))  return "SHIPPING".equals(s) || "DELIVERED".equals(s) || "ĐANG GIAO".equals(s);
                if ("PENDING".equals(st)   || "ĐANG XỬ LÝ".equals(st)) return "PENDING".equals(s) || "CONFIRMED".equals(s) || "PACKING".equals(s) || "ĐANG XỬ LÝ".equals(s);
                if ("CANCELLED".equals(st) || "ĐÃ HỦY".equals(st))    return "CANCELLED".equals(s) || "ĐÃ HỦY".equals(s);
                return s.equals(st);
            }).toList();
        }

        double totalRevenue = filtered.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()) || "HOÀN THÀNH".equalsIgnoreCase(o.getStatus()))
                .mapToDouble(o -> o.getTotal() != null ? o.getTotal().doubleValue() : 0.0)
                .sum();

        long completedCount = filtered.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()) || "HOÀN THÀNH".equalsIgnoreCase(o.getStatus()))
                .count();

        long shippingCount = filtered.stream()
                .filter(o -> "SHIPPING".equalsIgnoreCase(o.getStatus()) || "DELIVERED".equalsIgnoreCase(o.getStatus()) || "ĐANG GIAO".equalsIgnoreCase(o.getStatus()))
                .count();

        req.setAttribute("reportOrders", filtered);
        req.setAttribute("totalOrders", filtered.size());
        req.setAttribute("completedOrders", completedCount);
        req.setAttribute("shippingOrders", shippingCount);
        req.setAttribute("totalRevenue", String.format("%,.0f", totalRevenue));
        req.setAttribute("status", status);
        req.setAttribute("fromDate", fromDate);
        req.setAttribute("toDate", toDate);
        req.setAttribute("moduleTitle", "Báo cáo bán hàng");

        ViewRouter.admin(req, resp, "sales/report", "Báo cáo bán hàng", "sales");
    }

    private List<Map<String, Object>> getSampleReviews() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("id", 1, "customerName", "Lê Thành Công", "productName", "Rolex Datejust 41", "rating", 5, "content", "Đồng hồ chạy cực chuẩn, mẫu đẹp hơn mong đợi!", "status", "APPROVED", "createdAt", "2026-08-05"));
        list.add(Map.of("id", 2, "customerName", "Trần Minh Đức", "productName", "Casio G-Shock GA-2100", "rating", 4, "content", "Giao hàng nhanh, đóng gói chắc chắn.", "status", "PENDING", "createdAt", "2026-08-04"));
        list.add(Map.of("id", 3, "customerName", "Nguyễn Văn An", "productName", "Seiko 5 Sports Automatic", "rating", 5, "content", "Máy cơ bền bỉ, tích cót lâu.", "status", "APPROVED", "createdAt", "2026-08-02"));
        list.add(Map.of("id", 4, "customerName", "Phạm Quốc Bảo", "productName", "Citizen Eco-Drive", "rating", 1, "content", "Hàng bị trầy xước nhẹ ở mặt kính.", "status", "REJECTED", "createdAt", "2026-08-01"));
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

        req.setAttribute("reviews", filtered);
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
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

    private List<Map<String, Object>> getSampleReturns() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("id", 1, "orderId", 4, "customerName", "Lê Thành Công", "reason", "Kích thước dây đeo không vừa", "requestDate", "2026-08-05", "status", "Chờ xử lý"));
        list.add(Map.of("id", 2, "orderId", 2, "customerName", "Nguyễn Văn An", "reason", "Đổi sang màu mặt số xanh navy", "requestDate", "2026-08-03", "status", "Đã duyệt"));
        return list;
    }

    private void showReturns(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String status  = req.getParameter("status");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> returns = (List<Map<String, Object>>) req.getSession().getAttribute("sampleReturns");
        if (returns == null) {
            returns = getSampleReturns();
            req.getSession().setAttribute("sampleReturns", returns);
        }

        List<Map<String, Object>> filtered = new ArrayList<>(returns);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim().toLowerCase();
            filtered = filtered.stream().filter(r ->
                (r.get("customerName") != null && r.get("customerName").toString().toLowerCase().contains(k)) ||
                (r.get("reason")       != null && r.get("reason").toString().toLowerCase().contains(k))       ||
                (r.get("orderId")      != null && ("#" + r.get("orderId")).toLowerCase().contains(k))
            ).toList();
        }

        if (status != null && !status.trim().isEmpty()) {
            String st = status.trim();
            filtered = filtered.stream().filter(r ->
                st.equalsIgnoreCase(r.get("status").toString())
            ).toList();
        }

        req.setAttribute("returns", filtered);
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
        req.setAttribute("moduleTitle", "Yêu cầu đổi trả");

        ViewRouter.admin(req, resp, "sales/return", "Yêu cầu đổi trả", "sales");
    }

    private void updateReturnStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String status  = req.getParameter("status");

        if (idParam != null && status != null) {
            try {
                int id = Integer.parseInt(idParam);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> returns = (List<Map<String, Object>>) req.getSession().getAttribute("sampleReturns");
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
            } catch (Exception ignored) {}
        }
        resp.sendRedirect(req.getContextPath() + "/manage/sales/returns");
    }

    private void updateReviewStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String status  = req.getParameter("status");
        String action  = req.getParameter("action");

        if (status == null && action != null) {
            if ("approve".equalsIgnoreCase(action)) status = "APPROVED";
            else if ("reject".equalsIgnoreCase(action)) status = "REJECTED";
        }

        if (idParam != null && status != null) {
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
                        updated.put("status", status);
                        reviews.set(i, updated);
                        break;
                    }
                }
                req.getSession().setAttribute("sampleReviews", reviews);
                req.getSession().setAttribute("flash", "Đã xác nhận cập nhật trạng thái đánh giá thành công!");
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
}