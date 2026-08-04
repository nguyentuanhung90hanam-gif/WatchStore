package com.watchstore.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = {"/manage/*", "/orders/*"})
public class AuthFilter implements Filter {
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (req.getSession().getAttribute("user") == null) {
            ((HttpServletResponse) response).sendRedirect(req.getContextPath() + "/auth/login?required=1");
            return;
        }
        chain.doFilter(request, response);
    }
}
