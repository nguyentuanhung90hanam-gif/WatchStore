package com.watchstore.controller.admin;

import com.watchstore.model.Role;
import com.watchstore.model.User;
import com.watchstore.repository.RoleRepository;
import com.watchstore.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller riêng cho module Accounts.
 * Pattern giống BrandController — tách khỏi AdminController.
 */
@WebServlet(urlPatterns = {"/manage/admin/accounts", "/manage/admin/accounts/*"})
public class AccountController extends HttpServlet {

    private UserRepository userRepository;
    private RoleRepository roleRepository;

    @Override
    public void init() {
        userRepository = (UserRepository) getServletContext().getAttribute("userRepository");
        roleRepository = (RoleRepository) getServletContext().getAttribute("roleRepository");
        if (userRepository == null) userRepository = new com.watchstore.repository.UserRepositoryImpl();
        if (roleRepository == null) roleRepository = new com.watchstore.repository.RoleRepositoryImpl();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void setCommonAttributes(HttpServletRequest req) {
        req.setAttribute("adminArea", "admin");
        req.setAttribute("tableKind", "accounts");
        req.setAttribute("pageTitle", "Quản lý tài khoản");
        req.setAttribute("moduleTitle", "Quản lý tài khoản");
        req.setAttribute("moduleKicker", "NGƯỜI DÙNG HỆ THỐNG");
        req.setAttribute("moduleDescription", "Quản lý tài khoản khách hàng, nhân viên và trạng thái truy cập.");
        req.setAttribute("primaryAction", "Thêm tài khoản");
    }

    private void forwardToList(HttpServletRequest req, HttpServletResponse resp,
                               List<User> users) throws ServletException, IOException {
        setCommonAttributes(req);
        req.setAttribute("users", users);
        req.setAttribute("contentPage", "/views/shared/management-module.jsp");
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }

    private void forwardToForm(HttpServletRequest req, HttpServletResponse resp,
                               String pageTitle) throws ServletException, IOException {
        setCommonAttributes(req);
        req.setAttribute("pageTitle", pageTitle);
        // Luôn truyền danh sách roles để hiển thị checkbox
        req.setAttribute("allRoles", roleRepository.findAll());
        req.setAttribute("contentPage", "/views/admin/account-form.jsp");
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }

    // ─── GET ──────────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getPathInfo();
        if (action == null || action.equals("/")) {
            // List all
            forwardToList(req, resp, userRepository.findAll());
            return;
        }

        switch (action) {

            case "/add": {
                req.setAttribute("account", null);
                req.setAttribute("user", null);
                req.setAttribute("formMode", "add");
                req.setAttribute("selectedRoleIds", new ArrayList<Integer>());

                System.out.println("[DEBUG AccountController] ACCOUNT ADD MODE");
                System.out.println("formMode = " + req.getAttribute("formMode"));
                System.out.println("account = " + req.getAttribute("account"));
                System.out.println("user = " + req.getAttribute("user"));

                forwardToForm(req, resp, "Thêm tài khoản");
                break;
            }

            case "/edit": {
                String idStr = req.getParameter("id");
                User account = null;
                List<Integer> selectedRoleIds = new ArrayList<>();
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        account = userRepository.findById(id);
                        if (account != null && account.getRoles() != null) {
                            for (Role r : account.getRoles()) {
                                selectedRoleIds.add(r.getRoleId());
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
                req.setAttribute("account", account);
                req.setAttribute("user", account);
                req.setAttribute("formMode", "edit");
                req.setAttribute("selectedRoleIds", selectedRoleIds);

                System.out.println("[DEBUG AccountController] ACCOUNT EDIT MODE");
                System.out.println("formMode = " + req.getAttribute("formMode"));
                System.out.println("account = " + req.getAttribute("account"));

                forwardToForm(req, resp, "Sửa tài khoản");
                break;
            }

            case "/delete": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        if (userRepository.isUserInUse(id)) {
                            req.getSession().setAttribute("errorMessage", "Không thể xóa tài khoản này vì đã có dữ liệu liên quan (đơn hàng, bài viết, đánh giá...).");
                        } else {
                            boolean deleted = userRepository.delete(id);
                            if (deleted) {
                                req.getSession().setAttribute("successMessage", "Xóa tài khoản thành công.");
                            } else {
                                req.getSession().setAttribute("errorMessage", "Không thể xóa tài khoản.");
                            }
                        }
                    } catch (Exception e) {
                        req.getSession().setAttribute("errorMessage", "Không thể xóa tài khoản.");
                    }
                }
                resp.sendRedirect(req.getContextPath() + "/manage/admin/accounts");
                break;
            }

            case "/search": {
                String keyword = req.getParameter("keyword");
                if (keyword != null && !keyword.isBlank()) {
                    forwardToList(req, resp, userRepository.search(keyword));
                } else {
                    forwardToList(req, resp, userRepository.findAll());
                }
                break;
            }

            default:
                forwardToList(req, resp, userRepository.findAll());
        }
    }

    // ─── POST ─────────────────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String action = req.getPathInfo();
        if (action == null) action = "/";

        if ("/save".equals(action)) {
            handleSave(req, resp);
        } else if ("/update".equals(action)) {
            handleUpdate(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/manage/admin/accounts");
        }
    }

    // ─── Save (INSERT) ────────────────────────────────────────────────────────

    private void handleSave(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email       = trim(req.getParameter("email"));
        String password    = trim(req.getParameter("password"));
        String fullName    = trim(req.getParameter("fullName"));
        String phone       = trim(req.getParameter("phone"));
        String gender      = trim(req.getParameter("gender"));
        String dobStr      = trim(req.getParameter("dateOfBirth"));
        String status      = trim(req.getParameter("status"));
        String[] roleIdStrs = req.getParameterValues("roleIds");

        List<Integer> roleIds = parseRoleIds(roleIdStrs);

        // Validation
        String error = validateUser(email, fullName, password, phone, dobStr, status, true);
        if (error == null && userRepository.existsByEmail(email, null)) {
            error = "Email \"" + email + "\" đã tồn tại trong hệ thống.";
        }
        if (error == null && !phone.isEmpty() && userRepository.existsByPhone(phone, null)) {
            error = "Số điện thoại \"" + phone + "\" đã được sử dụng.";
        }

        if (error != null) {
            User draft = buildUser(0, email, null, fullName, phone, gender, dobStr, status);
            showFormWithError(req, resp, draft, roleIds, error, "Thêm tài khoản", "add");
            return;
        }

        // Hash password
        String passwordHash = hashSHA256(password);

        User user = buildUser(0, email, passwordHash, fullName, phone, gender, dobStr, status);
        userRepository.insert(user, roleIds);
        resp.sendRedirect(req.getContextPath() + "/manage/admin/accounts");
    }

    // ─── Update (UPDATE) ──────────────────────────────────────────────────────

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idStr       = trim(req.getParameter("userId"));
        String email       = trim(req.getParameter("email"));
        String password    = trim(req.getParameter("password"));
        String fullName    = trim(req.getParameter("fullName"));
        String phone       = trim(req.getParameter("phone"));
        String gender      = trim(req.getParameter("gender"));
        String dobStr      = trim(req.getParameter("dateOfBirth"));
        String status      = trim(req.getParameter("status"));
        String[] roleIdStrs = req.getParameterValues("roleIds");

        int id = 0;
        try { id = Integer.parseInt(idStr); } catch (NumberFormatException ignored) {}

        List<Integer> roleIds = parseRoleIds(roleIdStrs);

        // Validation — password không bắt buộc khi update
        String error = validateUser(email, fullName, password, phone, dobStr, status, false);
        if (error == null && userRepository.existsByEmail(email, id)) {
            error = "Email \"" + email + "\" đã được dùng bởi tài khoản khác.";
        }
        if (error == null && !phone.isEmpty() && userRepository.existsByPhone(phone, id)) {
            error = "Số điện thoại \"" + phone + "\" đã được dùng bởi tài khoản khác.";
        }

        if (error != null) {
            User draft = buildUser(id, email, null, fullName, phone, gender, dobStr, status);
            showFormWithError(req, resp, draft, roleIds, error, "Sửa tài khoản", "edit");
            return;
        }

        // Xử lý password: nếu để trống → giữ hash cũ
        String passwordHash;
        if (password.isEmpty()) {
            User existing = userRepository.findById(id);
            passwordHash = (existing != null) ? existing.getPasswordHash() : "";
        } else {
            passwordHash = hashSHA256(password);
        }

        User user = buildUser(id, email, passwordHash, fullName, phone, gender, dobStr, status);
        userRepository.update(user, roleIds);
        resp.sendRedirect(req.getContextPath() + "/manage/admin/accounts");
    }

    // ─── Utilities ───────────────────────────────────────────────────────────

    private void showFormWithError(HttpServletRequest req, HttpServletResponse resp,
                                   User draft, List<Integer> roleIds, String error,
                                   String pageTitle, String formMode)
            throws ServletException, IOException {
        req.setAttribute("errorMessage", error);
        req.setAttribute("account", draft);
        req.setAttribute("user", draft);
        req.setAttribute("formMode", formMode);
        req.setAttribute("selectedRoleIds", roleIds);
        forwardToForm(req, resp, pageTitle);
    }

    private String trim(String s) {
        return (s == null) ? "" : s.trim();
    }

    private String validateUser(String email, String fullName, String password, String phone, String dobStr, String status, boolean isNew) {
        if (email.isBlank())    return "Tên đăng nhập / Email không được để trống.";
        if (email.length() > 150) return "Email không được vượt quá 150 ký tự.";
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return "Email không đúng định dạng.";
        }
        if (fullName.isBlank()) return "Họ và tên không được để trống.";
        if (fullName.length() > 150) return "Họ và tên không được vượt quá 150 ký tự.";
        
        if (isNew && password.isBlank()) {
            return "Mật khẩu không được để trống khi tạo mới.";
        }
        if (!password.isEmpty()) {
            if (password.isBlank()) return "Mật khẩu không được chỉ chứa khoảng trắng.";
            if (password.length() < 6) return "Mật khẩu phải chứa ít nhất 6 ký tự.";
            if (password.length() > 255) return "Mật khẩu không được vượt quá 255 ký tự.";
        }

        if (!phone.isBlank()) {
            if (phone.length() > 20) return "Số điện thoại không được vượt quá 20 ký tự.";
            if (!phone.matches("^(0|\\+84)[0-9]{9}$")) {
                return "Số điện thoại phải gồm 10 chữ số (VD: 0912345678 hoặc +84912345678).";
            }
        }

        if (!dobStr.isBlank()) {
            try {
                LocalDate dob = LocalDate.parse(dobStr);
                if (dob.isAfter(LocalDate.now())) {
                    return "Ngày sinh không được lớn hơn ngày hiện tại.";
                }
            } catch (Exception e) {
                return "Ngày sinh không đúng định dạng.";
            }
        }

        if (!status.isEmpty() && !"ACTIVE".equals(status) && !"INACTIVE".equals(status) && !"LOCKED".equals(status)) {
            return "Trạng thái tài khoản không hợp lệ (chỉ chấp nhận ACTIVE, INACTIVE, LOCKED).";
        }
        return null;
    }

    private User buildUser(int id, String email, String passwordHash, String fullName,
                            String phone, String gender, String dobStr, String status) {
        User u = new User();
        u.setUserId(id);
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        u.setFullName(fullName);
        u.setPhone(phone.isEmpty() ? null : phone);
        u.setGender(gender.isEmpty() ? null : gender);
        if (dobStr != null && !dobStr.isEmpty()) {
            try {
                u.setDateOfBirth(LocalDate.parse(dobStr));
            } catch (Exception ignored) {}
        }
        u.setStatus(status.isEmpty() ? "ACTIVE" : status);
        return u;
    }

    private List<Integer> parseRoleIds(String[] roleIdStrs) {
        List<Integer> ids = new ArrayList<>();
        if (roleIdStrs != null) {
            for (String s : roleIdStrs) {
                try { ids.add(Integer.parseInt(s)); } catch (NumberFormatException ignored) {}
            }
        }
        return ids;
    }

    /**
     * Hash SHA-256 — tương thích CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', ?), 2) của SQL Server.
     * Trả về uppercase hex string 64 ký tự.
     */
    private String hashSHA256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
