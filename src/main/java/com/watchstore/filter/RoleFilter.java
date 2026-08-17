package com.watchstore.filter;

import com.watchstore.enums.Role;
import com.watchstore.model.User;
import com.watchstore.repository.PermissionRepository;
import com.watchstore.repository.PermissionRepositoryImpl;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

@WebFilter("/manage/*")
public class RoleFilter implements Filter {

    private PermissionRepository permissionRepository = new PermissionRepositoryImpl();

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        User user = (User) req.getSession().getAttribute("user");

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login?required=1");
            return;
        }

        Role userRole = user.getRole();
        String uri = req.getRequestURI();

        // 1. ADMIN = SUPER ADMIN -> FULL ACCESS (Toàn quyền truy cập mọi route)
        if (userRole == Role.ADMIN) {
            chain.doFilter(request, response);
            return;
        }

        // 2. CUSTOMER / KHÁC -> CẤM TUYỆT ĐỐI VÀO /manage/*
        if (userRole != Role.EMPLOYEE) {
            req.getSession().setAttribute("flash", "Bạn không có quyền truy cập khu vực quản lý.");
            resp.sendRedirect(req.getContextPath() + "/page/home");
            return;
        }

        // 3. EMPLOYEE -> CẤM VĨNH VIỄN CÁC ROUTE HỆ THỐNG DÀNH RIÊNG CHO ADMIN
        if (uri.contains("/manage/admin/accounts")
                || uri.contains("/manage/admin/roles")
                || uri.contains("/manage/admin/permissions")) {
            req.getSession().setAttribute("flash", "Chức năng này chỉ dành riêng cho Quản trị viên.");
            String fallbackUrl = resolveEmployeeFallbackUrl(req, permissionRepository.getUserPermissionCodes(user.getUserId()));
            resp.sendRedirect(fallbackUrl != null ? fallbackUrl : req.getContextPath() + "/page/home");
            return;
        }

        // Đọc danh sách UserPermissions trực tiếp từ database trên mỗi request
        Set<String> perms = permissionRepository.getUserPermissionCodes(user.getUserId());
        req.getSession().setAttribute("userPermissions", perms);

        if (perms == null || perms.isEmpty()) {
            req.getSession().setAttribute("flash", "Tài khoản nhân viên chưa được cấp quyền nào.");
            resp.sendRedirect(req.getContextPath() + "/page/home");
            return;
        }

        boolean hasAccess = checkEmployeePermissionForUri(req, uri, perms);

        if (!hasAccess) {
            req.getSession().setAttribute("flash", "Bạn chưa được cấp quyền truy cập tính năng này.");
            String fallbackUrl = resolveEmployeeFallbackUrl(req, perms);
            if (fallbackUrl != null && !fallbackUrl.equals(uri) && !uri.startsWith(fallbackUrl)) {
                resp.sendRedirect(fallbackUrl);
            } else {
                resp.sendRedirect(req.getContextPath() + "/manage/sales/dashboard");
            }
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean checkEmployeePermissionForUri(HttpServletRequest req, String uri, Set<String> perms) {
        // 1. NHÓM SẢN PHẨM (Sản phẩm, Danh mục, Thương hiệu)
        if (uri.contains("/manage/admin/products") || uri.contains("/manage/admin/categories") || uri.contains("/manage/admin/brands")) {
            return perms.contains("PRODUCT_VIEW") || perms.contains("PRODUCT_CREATE") || perms.contains("PRODUCT_EDIT");
        }

        // 2. NHÓM BÁN HÀNG - ĐƠN HÀNG
        if (uri.contains("/manage/sales/orders") || uri.contains("/manage/sales/order-")) {
            return perms.contains("ORDER_VIEW") || perms.contains("SALES_ORDER")
                    || perms.contains("ORDER_CREATE") || perms.contains("ORDER_EDIT")
                    || perms.contains("ORDER_APPROVE") || perms.contains("ORDER_EXPORT");
        }

        // 2. NHÓM BÁN HÀNG - KHÁCH HÀNG
        if (uri.contains("/manage/sales/customers") || uri.contains("/manage/sales/customer-")) {
            return perms.contains("CUSTOMER_VIEW") || perms.contains("SALES_CUSTOMER")
                    || perms.contains("CUSTOMER_CREATE") || perms.contains("CUSTOMER_EDIT");
        }

        // 3. NHÓM REVIEW & COMMENT
        if (uri.contains("/manage/sales/reviews") || uri.contains("/manage/sales/comments")) {
            return perms.contains("SALES_RETURN") || perms.contains("SALES_DELIVERY")
                    || perms.contains("REVIEW_VIEW") || perms.contains("COMMENT_VIEW");
        }

        // 4. NHÓM BẢO HÀNH
        if (uri.contains("/manage/sales/warranty") || uri.contains("/manage/sales/warranty-add")) {
            return perms.contains("SALES_WARRANTY") || perms.contains("WARRANTY_VIEW");
        }

        // 5. NHÓM VOUCHER
        if (uri.contains("/manage/admin/vouchers")) {
            return perms.contains("VOUCHER_VIEW") || perms.contains("VOUCHER_CREATE") || perms.contains("VOUCHER_EDIT");
        }

        // 6. NHÓM BANNER & BÀI VIẾT
        if (uri.contains("/manage/admin/banners") || uri.contains("/manage/admin/posts")) {
            return perms.contains("INVENTORY_VIEW") || perms.contains("BANNER_VIEW") || perms.contains("POST_VIEW");
        }

        // 7. NHÓM BÁO CÁO (Báo cáo & Thống kê)
        if (uri.contains("/manage/sales/report") || uri.contains("/manage/admin/reports") || uri.contains("/manage/admin/statistics")) {
            return perms.contains("REPORT_VIEW") || perms.contains("SALES_REPORT") || perms.contains("REPORT_EXPORT");
        }

        // DASHBOARD TỔNG QUAN EMPLOYEE
        if (uri.contains("/manage/sales/dashboard")) {
            return true;
        }

        // Bất kỳ route nào khác trong /manage/sales/
        if (uri.contains("/manage/sales/")) {
            return perms.stream().anyMatch(p -> p.startsWith("SALES_") || p.startsWith("ORDER_") || p.startsWith("CUSTOMER_") || p.startsWith("WARRANTY_"));
        }

        return false;
    }

    private String resolveEmployeeFallbackUrl(HttpServletRequest req, Set<String> perms) {
        if (perms == null || perms.isEmpty()) {
            return null;
        }
        if (perms.contains("ORDER_VIEW") || perms.contains("SALES_ORDER") || perms.contains("SALES_DASHBOARD")) {
            return req.getContextPath() + "/manage/sales/orders";
        }
        if (perms.contains("PRODUCT_VIEW")) {
            return req.getContextPath() + "/manage/admin/products";
        }
        if (perms.contains("CUSTOMER_VIEW") || perms.contains("SALES_CUSTOMER")) {
            return req.getContextPath() + "/manage/sales/customers";
        }
        if (perms.contains("SALES_RETURN") || perms.contains("SALES_DELIVERY")) {
            return req.getContextPath() + "/manage/sales/reviews";
        }
        if (perms.contains("SALES_WARRANTY")) {
            return req.getContextPath() + "/manage/sales/warranty";
        }
        if (perms.contains("VOUCHER_VIEW")) {
            return req.getContextPath() + "/manage/admin/vouchers";
        }
        if (perms.contains("INVENTORY_VIEW")) {
            return req.getContextPath() + "/manage/admin/banners";
        }
        if (perms.contains("REPORT_VIEW") || perms.contains("SALES_REPORT") || perms.contains("REPORT_EXPORT")) {
            return req.getContextPath() + "/manage/sales/report";
        }
        return req.getContextPath() + "/manage/sales/dashboard";
    }
}