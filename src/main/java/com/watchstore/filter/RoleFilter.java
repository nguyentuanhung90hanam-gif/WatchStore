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

        // 1. ADMIN = SUPER ADMIN -> FULL BYPASS (Bỏ qua toàn bộ kiểm tra permission)
        if (userRole == Role.ADMIN) {
            chain.doFilter(request, response);
            return;
        }

        // 2. CUSTOMER -> CẤM TRUY CẬP VÀO /manage/*
        if (userRole != Role.EMPLOYEE) {
            req.getSession().setAttribute("flash", "Bạn không có quyền truy cập khu vực quản lý.");
            resp.sendRedirect(req.getContextPath() + "/page/home");
            return;
        }

        // 3. EMPLOYEE -> KIỂM TRA PERMISSION GRANULAR THEO URL
        // Cấm tuyệt đối truy cập các cấu hình hệ thống dành riêng cho ADMIN
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
                resp.sendRedirect(req.getContextPath() + "/page/home");
            }
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean checkEmployeePermissionForUri(HttpServletRequest req, String uri, Set<String> perms) {
        String method = req.getMethod();

        // SẢN PHẨM, DANH MỤC, THƯƠNG HIỆU
        if (uri.contains("/manage/admin/products") || uri.contains("/manage/admin/categories") || uri.contains("/manage/admin/brands")) {
            if (uri.contains("/add") || uri.contains("/create")) {
                return perms.contains("PRODUCT_CREATE");
            }
            if (uri.contains("/edit") || uri.contains("/update") || uri.contains("/status") || uri.contains("/toggle") || uri.contains("/delete")) {
                return perms.contains("PRODUCT_EDIT");
            }
            return perms.contains("PRODUCT_VIEW");
        }

        // ĐƠN HÀNG (SALES / ORDERS)
        if (uri.contains("/manage/sales/orders") || uri.contains("/manage/sales/order-")) {
            if (uri.contains("/order-add") || uri.contains("/create")) {
                return perms.contains("ORDER_CREATE") || perms.contains("SALES_ORDER");
            }
            if (uri.contains("/order-edit") || uri.contains("/edit")) {
                return perms.contains("ORDER_EDIT") || perms.contains("SALES_ORDER");
            }
            if (uri.contains("/confirm") || uri.contains("/approve") || uri.contains("/cancel") || uri.contains("/status")) {
                return perms.contains("ORDER_APPROVE") || perms.contains("SALES_ORDER");
            }
            if (uri.contains("/export") || uri.contains("/invoice") || uri.contains("/print")) {
                return perms.contains("ORDER_EXPORT") || perms.contains("SALES_ORDER");
            }
            return perms.contains("ORDER_VIEW") || perms.contains("SALES_ORDER") || perms.contains("SALES_DASHBOARD");
        }

        // KHÁCH HÀNG
        if (uri.contains("/manage/sales/customers") || uri.contains("/manage/sales/customer-")) {
            if (uri.contains("/add") || uri.contains("/create")) {
                return perms.contains("CUSTOMER_CREATE") || perms.contains("SALES_CUSTOMER");
            }
            if (uri.contains("/edit") || uri.contains("/update") || uri.contains("/status")) {
                return perms.contains("CUSTOMER_EDIT") || perms.contains("SALES_CUSTOMER");
            }
            return perms.contains("CUSTOMER_VIEW") || perms.contains("SALES_CUSTOMER");
        }

        // BẢO HÀNH (WARRANTY)
        if (uri.contains("/manage/sales/warranty") || uri.contains("/manage/sales/warranty-add")) {
            return perms.contains("SALES_WARRANTY") || perms.contains("WARRANTY_VIEW") || perms.contains("WARRANTY_CREATE") || perms.contains("ORDER_VIEW") || perms.contains("SALES_ORDER");
        }

        // ĐÁNH GIÁ & BÌNH LUẬN
        if (uri.contains("/manage/sales/reviews") || uri.contains("/manage/sales/comments")) {
            return perms.contains("PRODUCT_VIEW") || perms.contains("SALES_DASHBOARD") || perms.contains("ORDER_VIEW") || perms.contains("SALES_ORDER");
        }

        // MARKETING & NỘI DUNG (VOUCHER, BANNER, BÀI VIẾT)
        if (uri.contains("/manage/admin/vouchers") || uri.contains("/manage/admin/banners") || uri.contains("/manage/admin/posts")) {
            if (uri.contains("/add") || uri.contains("/create")) {
                return perms.contains("VOUCHER_CREATE");
            }
            if (uri.contains("/edit") || uri.contains("/update") || uri.contains("/status") || uri.contains("/delete")) {
                return perms.contains("VOUCHER_EDIT");
            }
            return perms.contains("VOUCHER_VIEW");
        }

        // BÁO CÁO (REPORTS & STATISTICS)
        if (uri.contains("/manage/sales/report") || uri.contains("/manage/admin/reports") || uri.contains("/manage/admin/statistics")) {
            if (uri.contains("/export") || uri.contains("/download")) {
                return perms.contains("REPORT_EXPORT") || perms.contains("ORDER_EXPORT");
            }
            return perms.contains("REPORT_VIEW") || perms.contains("SALES_REPORT") || perms.contains("REPORT_EXPORT");
        }

        // DASHBOARD CHUNG
        if (uri.contains("/manage/sales/dashboard")) {
            return perms.contains("SALES_DASHBOARD") || perms.contains("ORDER_VIEW") || perms.contains("SALES_ORDER");
        }

        // Bất kỳ route nào khác trong sales:
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
        if (perms.contains("SALES_WARRANTY")) {
            return req.getContextPath() + "/manage/sales/warranty";
        }
        if (perms.contains("VOUCHER_VIEW")) {
            return req.getContextPath() + "/manage/admin/vouchers";
        }
        if (perms.contains("REPORT_VIEW") || perms.contains("SALES_REPORT") || perms.contains("REPORT_EXPORT")) {
            return req.getContextPath() + "/manage/sales/report";
        }
        if (perms.stream().anyMatch(p -> p.startsWith("SALES_") || p.startsWith("ORDER_") || p.startsWith("CUSTOMER_") || p.startsWith("WARRANTY_"))) {
            return req.getContextPath() + "/manage/sales/orders";
        }
        return null;
    }
}