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

        // 3. EMPLOYEE -> CẤM VĨNH VIỄN CÁC ROUTE QUẢN TRỊ HỆ THỐNG (/manage/admin/accounts, roles, permissions)
        if (uri.contains("/manage/admin/accounts")
                || uri.contains("/manage/admin/roles")
                || uri.contains("/manage/admin/permissions")) {
            req.getSession().setAttribute("flash", "Chức năng quản trị hệ thống chỉ dành riêng cho Quản trị viên.");
            String fallbackUrl = resolveEmployeeFallbackUrl(req, permissionRepository.getUserPermissionCodes(user.getUserId()));
            resp.sendRedirect(fallbackUrl != null ? fallbackUrl : req.getContextPath() + "/manage/sales/dashboard");
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
            if (uri.contains("/add") || uri.contains("/create")) {
                return perms.contains("PRODUCT_CREATE");
            }
            if (uri.contains("/edit") || uri.contains("/delete") || uri.contains("/update") || uri.contains("/status")) {
                return perms.contains("PRODUCT_EDIT");
            }
            return perms.contains("PRODUCT_VIEW") || perms.contains("PRODUCT_CREATE") || perms.contains("PRODUCT_EDIT");
        }

        // 2. NHÓM VOUCHER
        if (uri.contains("/manage/admin/vouchers")) {
            if (uri.contains("/add") || uri.contains("/create")) {
                return perms.contains("VOUCHER_CREATE");
            }
            if (uri.contains("/edit") || uri.contains("/delete") || uri.contains("/update") || uri.contains("/status")) {
                return perms.contains("VOUCHER_EDIT");
            }
            return perms.contains("VOUCHER_VIEW") || perms.contains("VOUCHER_CREATE") || perms.contains("VOUCHER_EDIT");
        }

        // 3. NHÓM BANNER & BÀI VIẾT
        if (uri.contains("/manage/admin/banners") || uri.contains("/manage/admin/posts")) {
            return perms.contains("BANNER_VIEW") || perms.contains("POST_VIEW") || perms.contains("PRODUCT_VIEW");
        }

        // 4. NHÓM BÁO CÁO & THỐNG KÊ
        if (uri.contains("/manage/sales/report") || uri.contains("/manage/admin/reports") || uri.contains("/manage/admin/statistics")) {
            if (uri.contains("/pdf") || uri.contains("/export")) {
                return perms.contains("REPORT_EXPORT");
            }
            return perms.contains("REPORT_VIEW") || perms.contains("SALES_REPORT") || perms.contains("REPORT_EXPORT");
        }

        // 5. NHÓM ĐƠN HÀNG
        if (uri.contains("/manage/sales/orders") || uri.contains("/manage/sales/order-") || uri.contains("/manage/sales/api/order")) {
            if (uri.contains("/export") || uri.contains("/invoice") || uri.contains("/print")) {
                return perms.contains("ORDER_EXPORT");
            }
            if (uri.contains("/approve") || uri.contains("/confirm") || uri.contains("/status")) {
                return perms.contains("ORDER_APPROVE") || perms.contains("ORDER_EDIT");
            }
            if (uri.contains("/add") || uri.contains("order-add")) {
                return perms.contains("ORDER_CREATE");
            }
            if (uri.contains("/edit") || uri.contains("order-edit") || uri.contains("/update")) {
                return perms.contains("ORDER_EDIT");
            }
            return perms.contains("ORDER_VIEW") || perms.contains("SALES_ORDER")
                    || perms.contains("ORDER_CREATE") || perms.contains("ORDER_EDIT")
                    || perms.contains("ORDER_APPROVE") || perms.contains("ORDER_EXPORT");
        }

        // 6. NHÓM KHÁCH HÀNG
        if (uri.contains("/manage/sales/customers") || uri.contains("/manage/sales/customer-")) {
            if (uri.contains("/add") || uri.contains("customer-add")) {
                return perms.contains("CUSTOMER_CREATE");
            }
            if (uri.contains("/edit") || uri.contains("customer-edit") || uri.contains("/update")) {
                return perms.contains("CUSTOMER_EDIT");
            }
            return perms.contains("CUSTOMER_VIEW") || perms.contains("SALES_CUSTOMER")
                    || perms.contains("CUSTOMER_CREATE") || perms.contains("CUSTOMER_EDIT");
        }

        // 7. NHÓM REVIEW & COMMENT
        if (uri.contains("/manage/sales/reviews") || uri.contains("/manage/sales/comments")) {
            if (uri.contains("/status") || uri.contains("/delete") || uri.contains("/approve")) {
                return perms.contains("CUSTOMER_EDIT");
            }
            return perms.contains("CUSTOMER_VIEW") || perms.contains("SALES_CUSTOMER")
                    || perms.contains("CUSTOMER_EDIT");
        }

        // 8. NHÓM BẢO HÀNH
        if (uri.contains("/manage/sales/warranty") || uri.contains("/manage/sales/warranty-")) {
            return perms.contains("SALES_WARRANTY");
        }

        // 9. DASHBOARD TỔNG QUAN EMPLOYEE (Landing page mặc định cho Employee đã đăng nhập)
        if (uri.contains("/manage/sales/dashboard") || uri.equals(req.getContextPath() + "/manage/sales") || uri.equals(req.getContextPath() + "/manage/sales/")) {
            return true;
        }

        // 10. Bất kỳ route nào khác trong /manage/sales/
        if (uri.contains("/manage/sales/")) {
            return perms.stream().anyMatch(p -> p.startsWith("SALES_") || p.startsWith("ORDER_") || p.startsWith("CUSTOMER_"));
        }

        return false;
    }

    private String resolveEmployeeFallbackUrl(HttpServletRequest req, Set<String> perms) {
        if (perms == null || perms.isEmpty()) {
            return null;
        }
        if (perms.contains("PRODUCT_VIEW")) {
            return req.getContextPath() + "/manage/admin/products";
        }
        if (perms.contains("ORDER_VIEW") || perms.contains("SALES_ORDER") || perms.contains("ORDER_CREATE")) {
            return req.getContextPath() + "/manage/sales/orders";
        }
        if (perms.contains("CUSTOMER_VIEW") || perms.contains("SALES_CUSTOMER") || perms.contains("CUSTOMER_CREATE")) {
            return req.getContextPath() + "/manage/sales/customers";
        }
        if (perms.contains("VOUCHER_VIEW")) {
            return req.getContextPath() + "/manage/admin/vouchers";
        }
        if (perms.contains("SALES_WARRANTY")) {
            return req.getContextPath() + "/manage/sales/warranty";
        }
        if (perms.contains("REPORT_VIEW") || perms.contains("SALES_REPORT") || perms.contains("REPORT_EXPORT")) {
            return req.getContextPath() + "/manage/admin/statistics";
        }
        return req.getContextPath() + "/manage/sales/dashboard";
    }
}