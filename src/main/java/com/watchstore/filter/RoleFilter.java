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

        Role userRole = user.getRole();

        String uri = req.getRequestURI();

        boolean allowed = false;

        if (uri.contains("/manage/admin/")) {
            allowed = userRole == Role.ADMIN;

        } else if (uri.contains("/manage/sales/")) {
            allowed = userRole == Role.SALES
                    || userRole == Role.ADMIN;

        } else if (uri.contains("/manage/warehouse/")) {
            allowed = userRole == Role.WAREHOUSE
                    || userRole == Role.ADMIN;
        }

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