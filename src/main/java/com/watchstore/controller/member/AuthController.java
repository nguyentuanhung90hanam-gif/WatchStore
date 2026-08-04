package com.watchstore.controller.member;

import com.watchstore.enums.Role;
import com.watchstore.model.User;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/auth/*")
public class AuthController extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo() == null ? "/login" : req.getPathInfo();
        if ("/logout".equals(path)) {
            req.getSession().invalidate();
            resp.sendRedirect(req.getContextPath() + "/page/home");
            return;
        }
        if ("/register".equals(path)) ViewRouter.customer(req, resp, "guest/register", "Đăng ký tài khoản");
        else ViewRouter.customer(req, resp, "guest/login", "Đăng nhập");
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo() == null ? "/login" : req.getPathInfo();
        if ("/register".equals(path)) {
            String name = value(req.getParameter("fullName"), "Khách hàng WatchStore");
            String email = value(req.getParameter("email"), "customer@watchstore.vn");
            req.getSession().setAttribute("user", new User(101, name, email, "", Role.CUSTOMER));
            req.getSession().setAttribute("flash", "Đăng ký thành công. Chào mừng bạn đến WatchStore!");
            resp.sendRedirect(req.getContextPath() + "/page/home");
            return;
        }
        String email = value(req.getParameter("email"), "customer@watchstore.vn").toLowerCase();
        Role role = email.startsWith("admin") ? Role.ADMIN : email.startsWith("sales") ? Role.SALES : email.startsWith("warehouse") ? Role.WAREHOUSE : Role.CUSTOMER;
        String name = role == Role.ADMIN ? "Thạch Như Thuận" : role == Role.SALES ? "Nhân viên bán hàng" : role == Role.WAREHOUSE ? "Nhân viên kho" : "Khách hàng WatchStore";
        req.getSession().setAttribute("user", new User(1, name, email, "0988 686 868", role));
        req.getSession().setAttribute("flash", "Đăng nhập thành công");
        if (role == Role.ADMIN) resp.sendRedirect(req.getContextPath() + "/manage/admin/dashboard");
        else if (role == Role.SALES) resp.sendRedirect(req.getContextPath() + "/manage/sales/dashboard");
        else if (role == Role.WAREHOUSE) resp.sendRedirect(req.getContextPath() + "/manage/warehouse/dashboard");
        else resp.sendRedirect(req.getContextPath() + "/page/home");
    }
    private String value(String value, String fallback) { return value == null || value.isBlank() ? fallback : value.trim(); }
}
