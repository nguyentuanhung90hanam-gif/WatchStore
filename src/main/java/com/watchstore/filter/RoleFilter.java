package com.watchstore.filter;

import com.watchstore.enums.Role;
import com.watchstore.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/manage/*")
public class RoleFilter implements Filter {

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
            resp.sendRedirect(
                    req.getContextPath() + "/auth/login?required=1"
            );
            return;
        }

        if (user.getRole() == Role.ADMIN) {
            chain.doFilter(request, response);
            return;
        }

        String uri = req.getRequestURI();
        boolean allowed = false;

        if (uri.contains("/manage/dashboard") || uri.endsWith("/manage") || uri.endsWith("/manage/")) allowed = user.hasPermission("DASHBOARD_VIEW");

        else if (uri.contains("/manage/sales/orders")) allowed = user.hasAnyPermission("ORDERS_VIEW", "ORDERS_MANAGE");
        else if (uri.contains("/manage/sales/customers")) allowed = user.hasAnyPermission("CUSTOMERS_VIEW", "CUSTOMERS_MANAGE");
        else if (uri.contains("/manage/sales/delivery")) allowed = user.hasPermission("DELIVERY_VIEW");
        else if (uri.contains("/manage/sales/returns")) allowed = user.hasAnyPermission("RETURNS_VIEW", "RETURNS_MANAGE");
        else if (uri.contains("/manage/sales/warranty")) allowed = user.hasPermission("WARRANTY_VIEW");

        else if (uri.contains("/manage/warehouse/inventory")) allowed = user.hasAnyPermission("INVENTORY_VIEW", "INVENTORY_MANAGE");
        else if (uri.contains("/manage/warehouse/receipts")) allowed = user.hasAnyPermission("STOCK_RECEIPT_VIEW", "STOCK_RECEIPT_MANAGE");
        else if (uri.contains("/manage/warehouse/exports")) allowed = user.hasAnyPermission("STOCK_EXPORT_VIEW", "STOCK_EXPORT_MANAGE");
        else if (uri.contains("/manage/warehouse/stocktake")) allowed = user.hasAnyPermission("STOCKTAKE_VIEW", "STOCKTAKE_MANAGE");
        else if (uri.contains("/manage/warehouse/variants")) allowed = user.hasAnyPermission("VARIANTS_VIEW", "VARIANTS_MANAGE");
        else if (uri.contains("/manage/warehouse/suppliers")) allowed = user.hasPermission("SUPPLIERS_VIEW");
        else if (uri.contains("/manage/warehouse/reports")) allowed = user.hasPermission("REPORTS_VIEW");

        else if (uri.contains("/manage/admin/products")) allowed = user.hasAnyPermission("PRODUCTS_VIEW", "PRODUCTS_MANAGE");
        else if (uri.contains("/manage/admin/categories")) allowed = user.hasAnyPermission("CATEGORIES_VIEW", "CATEGORIES_MANAGE");
        else if (uri.contains("/manage/admin/brands")) allowed = user.hasAnyPermission("BRANDS_VIEW", "BRANDS_MANAGE");
        else if (uri.contains("/manage/admin/vouchers")) allowed = user.hasAnyPermission("VOUCHERS_VIEW", "VOUCHERS_MANAGE");
        else if (uri.contains("/manage/admin/banners")) allowed = user.hasAnyPermission("BANNERS_VIEW", "BANNERS_MANAGE");
        else if (uri.contains("/manage/admin/posts")) allowed = user.hasAnyPermission("POSTS_VIEW", "POSTS_MANAGE");
        else if (uri.contains("/manage/admin/notifications")) allowed = user.hasAnyPermission("NOTIFICATIONS_VIEW", "NOTIFICATIONS_MANAGE");
        else if (uri.contains("/manage/admin/statistics")) allowed = user.hasPermission("STATISTICS_VIEW");
        else if (uri.contains("/manage/admin/accounts")) allowed = user.hasAnyPermission("ACCOUNTS_VIEW", "ACCOUNTS_MANAGE");
        else if (uri.contains("/manage/admin/roles")) allowed = user.hasAnyPermission("ROLES_VIEW", "ROLES_MANAGE");
        else if (uri.contains("/manage/admin/permissions")) allowed = user.hasAnyPermission("PERMISSIONS_VIEW", "PERMISSIONS_MANAGE");

        if (!allowed) {
            req.getSession().setAttribute(
                    "flash",
                    "Bạn không có quyền truy cập khu vực này"
            );

            resp.sendRedirect(
                    req.getContextPath() + "/page/home"
            );
            return;
        }

        chain.doFilter(request, response);
    }
}