package com.watchstore.controller.member;

import com.watchstore.config.DBContext;
import com.watchstore.enums.Role;
import com.watchstore.model.User;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@WebServlet("/auth/*")
public class AuthController extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException, IOException {

        String path = req.getPathInfo() == null
                ? "/login"
                : req.getPathInfo();

        if ("/logout".equals(path)) {
            req.getSession().invalidate();
            resp.sendRedirect(
                    req.getContextPath() + "/page/home"
            );
            return;
        }

        if ("/register".equals(path)) {
            ViewRouter.customer(
                    req,
                    resp,
                    "guest/register",
                    "─É─âng k├╜ t├ái khoß║ún"
            );
        } else {
            ViewRouter.customer(
                    req,
                    resp,
                    "guest/login",
                    "─É─âng nhß║¡p"
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo() == null
                ? "/login"
                : req.getPathInfo();

        if ("/register".equals(path)) {
            handleRegister(req, resp);
            return;
        }

        handleLogin(req, resp);
    }

    private void handleLogin(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        String email = value(
                req.getParameter("email"),
                ""
        ).toLowerCase();

        if (email.isEmpty()) {
            req.getSession().setAttribute(
                    "errorMsg",
                    "Vui l├▓ng nhß║¡p email."
            );
            resp.sendRedirect(
                    req.getContextPath() + "/auth/login"
            );
            return;
        }

        try {
            String password = value(req.getParameter("password"), "");
            User user = findUserByEmail(email);

            if (user == null) {
                req.getSession().setAttribute(
                        "errorMsg",
                        "T├ái khoß║ún kh├┤ng tß╗ôn tß║íi hoß║╖c ─æ├ú bß╗ï kh├│a."
                );
                resp.sendRedirect(
                        req.getContextPath() + "/auth/login"
                );
                return;
            }

            if (!isPasswordValid(user.getId(), password)) {
                req.getSession().setAttribute("errorMsg", "Email hoß║╖c mß║¡t khß║⌐u kh├┤ng ─æ├║ng.");
                resp.sendRedirect(req.getContextPath() + "/auth/login");
                return;
            }

            req.getSession().setAttribute(
                    "user",
                    user
            );

            req.getSession().setAttribute(
                    "flash",
                    "─É─âng nhß║¡p th├ánh c├┤ng"
            );

            if (user.getRole() == Role.ADMIN) {
                resp.sendRedirect(
                        req.getContextPath()
                                + "/manage/admin/dashboard"
                );
            } else if (user.getRole() == Role.SALES) {
                resp.sendRedirect(
                        req.getContextPath()
                                + "/manage/sales/dashboard"
                );
            } else if (user.getRole() == Role.WAREHOUSE) {
                resp.sendRedirect(
                        req.getContextPath()
                                + "/manage/warehouse/dashboard"
                );
            } else {
                resp.sendRedirect(
                        req.getContextPath()
                                + "/page/home"
                );
            }

        } catch (Exception e) {
            e.printStackTrace();

            req.getSession().setAttribute(
                    "errorMsg",
                    "Kh├┤ng thß╗â ─æ─âng nhß║¡p: " +
                            getErrorMessage(e)
            );

            resp.sendRedirect(
                    req.getContextPath() + "/auth/login"
            );
        }
    }

    private User findUserByEmail(
            String email
    ) throws SQLException {

        String sql =
                "SELECT TOP 1 " +
                        "u.UserID, " +
                        "u.FullName, " +
                        "u.Email, " +
                        "u.Phone, " +
                        "r.RoleCode " +
                        "FROM dbo.Users u " +
                        "LEFT JOIN dbo.UserRoles ur " +
                        "ON ur.UserID = u.UserID " +
                        "LEFT JOIN dbo.Roles r " +
                        "ON r.RoleID = ur.RoleID " +
                        "WHERE LOWER(u.Email) = ? " +
                        "AND u.Status = 'ACTIVE' " +
                        "ORDER BY CASE r.RoleCode " +
                        "WHEN 'ADMIN' THEN 1 " +
                        "WHEN 'SALES' THEN 2 " +
                        "WHEN 'WAREHOUSE' THEN 3 " +
                        "WHEN 'CUSTOMER' THEN 4 " +
                        "ELSE 5 END";

        try (
                Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return new User(
                        rs.getInt("UserID"),
                        rs.getString("FullName"),
                        rs.getString("Email"),
                        rs.getString("Phone"),
                        parseRole(rs.getString("RoleCode"))
                );
            }
        }
    }

    private boolean isPasswordValid(int userId, String password) throws Exception {
        String sql="SELECT PasswordHash FROM dbo.Users WHERE UserID=? AND Status='ACTIVE'";
        try(Connection conn=DBContext.getConnection(); PreparedStatement ps=conn.prepareStatement(sql)){
            ps.setInt(1,userId);
            try(ResultSet rs=ps.executeQuery()){
                if(!rs.next()) return false;
                return rs.getString("PasswordHash").equals(sha256(password));
            }
        }
    }

    private String sha256(String value) throws Exception {
        MessageDigest digest=MessageDigest.getInstance("SHA-256");
        byte[] bytes=digest.digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb=new StringBuilder(bytes.length*2);
        for(byte b:bytes) sb.append(String.format("%02X",b));
        return sb.toString();
    }

    private Role parseRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return Role.CUSTOMER;
        }

        try {
            return Role.valueOf(
                    roleCode.trim().toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            return Role.CUSTOMER;
        }
    }

    private void handleRegister(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        String name = value(
                req.getParameter("fullName"),
                "Kh├ích h├áng WatchStore"
        );

        String email = value(
                req.getParameter("email"),
                "customer@watchstore.vn"
        ).toLowerCase();

        req.getSession().setAttribute(
                "user",
                new User(
                        101,
                        name,
                        email,
                        "",
                        Role.CUSTOMER
                )
        );

        req.getSession().setAttribute(
                "flash",
                "─É─âng k├╜ th├ánh c├┤ng. Ch├áo mß╗½ng bß║ín ─æß║┐n WatchStore!"
        );

        resp.sendRedirect(
                req.getContextPath() + "/page/home"
        );
    }

    private String value(
            String value,
            String fallback
    ) {
        return value == null || value.isBlank()
                ? fallback
                : value.trim();
    }

    private String getErrorMessage(Exception e) {
        return e.getMessage() == null ||
                e.getMessage().isBlank()
                ? "Lß╗ùi kh├┤ng x├íc ─æß╗ïnh."
                : e.getMessage();
    }
}
