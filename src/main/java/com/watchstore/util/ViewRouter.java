package com.watchstore.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class ViewRouter {
    private ViewRouter() {}
    public static void customer(HttpServletRequest req, HttpServletResponse resp, String view, String title) throws ServletException, IOException {
        req.setAttribute("contentPage", "/views/" + view + ".jsp");
        req.setAttribute("pageTitle", title);
        req.getRequestDispatcher("/views/layout/customer-layout.jsp").forward(req, resp);
    }
    public static void admin(HttpServletRequest req, HttpServletResponse resp, String view, String title, String area) throws ServletException, IOException {
        req.setAttribute("contentPage", "/views/" + view + ".jsp");
        req.setAttribute("pageTitle", title);
        req.setAttribute("adminArea", area);
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }
}
