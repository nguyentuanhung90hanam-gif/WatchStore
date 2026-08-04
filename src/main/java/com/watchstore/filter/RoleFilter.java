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
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            ((HttpServletResponse) response).sendRedirect(req.getContextPath() + "/auth/login?required=1");
            return;
        }
        String uri = req.getRequestURI();
        boolean allowed = (uri.contains("/manage/admin/") && user.getRole() == Role.ADMIN)
            || (uri.contains("/manage/sales/") && (user.getRole() == Role.SALES || user.getRole() == Role.ADMIN))
            || (uri.contains("/manage/warehouse/") && (user.getRole() == Role.WAREHOUSE || user.getRole() == Role.ADMIN));
        if (!allowed) {
            req.getSession().setAttribute("flash", "Bạn không có quyền truy cập khu vực này");
            ((HttpServletResponse) response).sendRedirect(req.getContextPath() + "/page/home");
            return;
        }
        chain.doFilter(request, response);
    }
}
