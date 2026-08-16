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

        // SẢN PHẨM & BIẾN THỂ
        if (uri.contains("/manage/admin/products") || uri.contains("/manage/warehouse/variants") || uri.contains("/manage/warehouse/variant")) {
            if (uri.contains("/add") || uri.contains("/create")) {
                return perms.contains("PRODUCT_CREATE") || perms.contains("INVENTORY_CREATE");
            }
            if (uri.contains("/edit") || uri.contains("/update") || uri.contains("/status") || uri.contains("/toggle") || uri.contains("/delete")) {
                return perms.contains("PRODUCT_EDIT") || perms.contains("INVENTORY_EDIT");
            }
            return perms.contains("PRODUCT_VIEW") || perms.contains("INVENTORY_VIEW") || perms.contains("WAREHOUSE_INVENTORY");
        }

        // ĐƠN HÀNG (SALES / ORDERS)
        if (uri.contains("/manage/sales/orders") || uri.contains("/manage/sales/order-") || uri.contains("/manage/sales/pos")) {
            if (uri.contains("/order-add") || uri.contains("/pos") || uri.contains("/create")) {
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

        // KHO HÀNG (INVENTORY)
        if (uri.contains("/manage/warehouse/inventory") || uri.contains("/manage/warehouse/transactions") || uri.contains("/manage/warehouse/alerts")) {
            return perms.contains("INVENTORY_VIEW") || perms.contains("WAREHOUSE_INVENTORY") || perms.contains("WAREHOUSE_DASHBOARD");
        }

        // NHẬP KHO (RECEIPTS)
        if (uri.contains("/manage/warehouse/receipt")) {
            if (uri.contains("/receipt-create") || uri.contains("/create") || uri.contains("/add")) {
                return perms.contains("INVENTORY_CREATE") || perms.contains("WAREHOUSE_RECEIPT");
            }
            if (uri.contains("/approve") || uri.contains("/complete")) {
                return perms.contains("INVENTORY_APPROVE") || perms.contains("WAREHOUSE_RECEIPT");
            }
            return perms.contains("INVENTORY_VIEW") || perms.contains("WAREHOUSE_RECEIPT") || perms.contains("WAREHOUSE_INVENTORY");
        }

        // XUẤT KHO (EXPORTS)
        if (uri.contains("/manage/warehouse/export")) {
            if (uri.contains("/export-create") || uri.contains("/create") || uri.contains("/add")) {
                return perms.contains("INVENTORY_CREATE") || perms.contains("WAREHOUSE_EXPORT");
            }
            if (uri.contains("/approve") || uri.contains("/complete")) {
                return perms.contains("INVENTORY_APPROVE") || perms.contains("WAREHOUSE_EXPORT");
            }
            return perms.contains("INVENTORY_VIEW") || perms.contains("WAREHOUSE_EXPORT") || perms.contains("WAREHOUSE_INVENTORY");
        }

        // KIỂM KÊ (STOCKTAKE)
        if (uri.contains("/manage/warehouse/stocktake")) {
            if (uri.contains("/stocktake-create") || uri.contains("/create")) {
                return perms.contains("INVENTORY_CREATE") || perms.contains("WAREHOUSE_STOCKTAKE");
            }
            if (uri.contains("/approve") || uri.contains("/balance")) {
                return perms.contains("INVENTORY_APPROVE") || perms.contains("WAREHOUSE_STOCKTAKE");
            }
            return perms.contains("INVENTORY_VIEW") || perms.contains("WAREHOUSE_STOCKTAKE") || perms.contains("WAREHOUSE_INVENTORY");
        }

        // VOUCHER
        if (uri.contains("/manage/admin/vouchers")) {
            if (uri.contains("/add") || uri.contains("/create")) {
                return perms.contains("VOUCHER_CREATE");
            }
            if (uri.contains("/edit") || uri.contains("/update") || uri.contains("/status")) {
                return perms.contains("VOUCHER_EDIT");
            }
            return perms.contains("VOUCHER_VIEW");
        }

        // BÁO CÁO (REPORTS & STATISTICS)
        if (uri.contains("/manage/sales/report") || uri.contains("/manage/warehouse/reports") || uri.contains("/manage/warehouse/report")
                || uri.contains("/manage/admin/reports") || uri.contains("/manage/admin/statistics")) {
            if (uri.contains("/export") || uri.contains("/download")) {
                return perms.contains("REPORT_EXPORT") || perms.contains("INVENTORY_EXPORT") || perms.contains("ORDER_EXPORT");
            }
            return perms.contains("REPORT_VIEW") || perms.contains("SALES_REPORT") || perms.contains("WAREHOUSE_REPORT") || perms.contains("REPORT_EXPORT");
        }

        // DASHBOARD CHUNG
        if (uri.contains("/manage/sales/dashboard")) {
            return perms.contains("SALES_DASHBOARD") || perms.contains("ORDER_VIEW") || perms.contains("SALES_ORDER");
        }
        if (uri.contains("/manage/warehouse/dashboard")) {
            return perms.contains("WAREHOUSE_DASHBOARD") || perms.contains("INVENTORY_VIEW") || perms.contains("WAREHOUSE_INVENTORY");
        }

        // CÁC ROUTE PHỤ TRỢ KHÁC
        if (uri.contains("/manage/sales/delivery") || uri.contains("/manage/sales/returns") || uri.contains("/manage/sales/warranty")) {
            return perms.contains("ORDER_VIEW") || perms.contains("SALES_ORDER") || perms.contains("SALES_DELIVERY") || perms.contains("SALES_RETURN") || perms.contains("SALES_WARRANTY");
        }

        // Bất kỳ route nào khác trong sales hoặc warehouse:
        if (uri.contains("/manage/sales/")) {
            return perms.stream().anyMatch(p -> p.startsWith("SALES_") || p.startsWith("ORDER_") || p.startsWith("CUSTOMER_"));
        }
        if (uri.contains("/manage/warehouse/")) {
            return perms.stream().anyMatch(p -> p.startsWith("WAREHOUSE_") || p.startsWith("INVENTORY_") || p.startsWith("PRODUCT_"));
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
        if (perms.contains("INVENTORY_VIEW") || perms.contains("WAREHOUSE_INVENTORY") || perms.contains("WAREHOUSE_DASHBOARD")) {
            return req.getContextPath() + "/manage/warehouse/inventory";
        }
        if (perms.contains("PRODUCT_VIEW")) {
            return req.getContextPath() + "/manage/admin/products";
        }
        if (perms.contains("CUSTOMER_VIEW") || perms.contains("SALES_CUSTOMER")) {
            return req.getContextPath() + "/manage/sales/customers";
        }
        if (perms.contains("INVENTORY_CREATE") || perms.contains("WAREHOUSE_RECEIPT")) {
            return req.getContextPath() + "/manage/warehouse/receipts";
        }
        if (perms.contains("VOUCHER_VIEW")) {
            return req.getContextPath() + "/manage/admin/vouchers";
        }
        if (perms.contains("REPORT_VIEW") || perms.contains("SALES_REPORT") || perms.contains("WAREHOUSE_REPORT") || perms.contains("REPORT_EXPORT")) {
            return req.getContextPath() + "/manage/sales/report";
        }
        if (perms.stream().anyMatch(p -> p.startsWith("SALES_") || p.startsWith("ORDER_") || p.startsWith("CUSTOMER_"))) {
            return req.getContextPath() + "/manage/sales/orders";
        }
        if (perms.stream().anyMatch(p -> p.startsWith("WAREHOUSE_") || p.startsWith("INVENTORY_") || p.startsWith("PRODUCT_"))) {
            return req.getContextPath() + "/manage/warehouse/inventory";
        }
        return null;
    }
}