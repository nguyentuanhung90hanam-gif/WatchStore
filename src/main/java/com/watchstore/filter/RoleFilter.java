package com.watchstore.filter;

import com.watchstore.enums.Role;
import com.watchstore.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/manage/*")
public class RoleFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            ((HttpServletResponse) response).sendRedirect(req.getContextPath() + "/auth/login?required=1");
            return;
        }

        String uri = req.getRequestURI();

        // SỬA TẠI ĐÂY: Chuyển Enum sang String bằng .name() để so sánh với user.getRole()
        boolean allowed = (uri.contains("/manage/admin/") && Role.ADMIN.name().equalsIgnoreCase(user.getRole()))
                || (uri.contains("/manage/sales/") && (Role.SALES.name().equalsIgnoreCase(user.getRole()) || Role.ADMIN.name().equalsIgnoreCase(user.getRole())))
                || (uri.contains("/manage/warehouse/") && (Role.WAREHOUSE.name().equalsIgnoreCase(user.getRole()) || Role.ADMIN.name().equalsIgnoreCase(user.getRole())));

        if (!allowed) {
            req.getSession().setAttribute("flash", "Bạn không có quyền truy cập khu vực này");
            ((HttpServletResponse) response).sendRedirect(req.getContextPath() + "/page/home");
            return;
        }
        chain.doFilter(request, response);
    }
}