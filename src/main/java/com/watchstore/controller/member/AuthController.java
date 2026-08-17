package com.watchstore.controller.member;

import com.watchstore.config.DBContext;
import com.watchstore.enums.Role;
import com.watchstore.model.PendingRegistration;
import com.watchstore.model.User;
import com.watchstore.service.OtpService;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@WebServlet("/auth/*")
public class AuthController extends HttpServlet {
    private OtpService otpService;

    @Override
    public void init() {
        otpService = (OtpService) getServletContext().getAttribute("otpService");
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException, IOException {

        String path = req.getPathInfo() == null ? "/login" : req.getPathInfo();

        if ("/logout".equals(path)) {
            req.getSession().invalidate();
            resp.sendRedirect(req.getContextPath() + "/page/home");
            return;
        }

        if ("/register".equals(path)) {
            ViewRouter.customer(req, resp, "guest/register", "Đăng ký tài khoản");
            return;
        }

        if ("/forgot-password".equals(path)) {
            ViewRouter.customer(req, resp, "guest/forgot-password", "Quên mật khẩu");
            return;
        }

        if ("/verify-otp".equals(path)) {
            String purpose = req.getParameter("purpose");
            req.setAttribute("otpPurpose", purpose == null ? "REGISTER" : purpose.toUpperCase());
            ViewRouter.customer(req, resp, "guest/verify-otp", "Xác thực mã OTP");
            return;
        }

        if ("/reset-password".equals(path)) {
            Boolean verified = (Boolean) req.getSession().getAttribute("otpVerifiedForReset");
            if (verified == null || !verified) {
                resp.sendRedirect(req.getContextPath() + "/auth/forgot-password");
                return;
            }
            ViewRouter.customer(req, resp, "guest/reset-password", "Đặt lại mật khẩu");
            return;
        }

        ViewRouter.customer(req, resp, "guest/login", "Đăng nhập");
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo() == null ? "/login" : req.getPathInfo();

        try {
            if ("/register".equals(path)) {
                handleRegister(req, resp);
                return;
            }
            if ("/forgot-password".equals(path)) {
                handleForgotPassword(req, resp);
                return;
            }
            if ("/verify-otp".equals(path)) {
                handleVerifyOtp(req, resp);
                return;
            }
            if ("/resend-otp".equals(path)) {
                handleResendOtp(req, resp);
                return;
            }
            if ("/reset-password".equals(path)) {
                handleResetPassword(req, resp);
                return;
            }
            if ("/login".equals(path)) {
                handleLogin(req, resp);
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/auth/login");
        } catch (Exception e) {
            req.setAttribute("error", message(e));
            if ("/register".equals(path)) {
                ViewRouter.customer(req, resp, "guest/register", "Đăng ký tài khoản");
            } else if ("/forgot-password".equals(path)) {
                ViewRouter.customer(req, resp, "guest/forgot-password", "Quên mật khẩu");
            } else if ("/verify-otp".equals(path)) {
                String purpose = req.getParameter("purpose");
                req.setAttribute("otpPurpose", purpose == null ? "REGISTER" : purpose.toUpperCase());
                ViewRouter.customer(req, resp, "guest/verify-otp", "Xác thực mã OTP");
            } else if ("/reset-password".equals(path)) {
                ViewRouter.customer(req, resp, "guest/reset-password", "Đặt lại mật khẩu");
            } else {
                ViewRouter.customer(req, resp, "guest/login", "Đăng nhập");
            }
        }
    }

    private void handleLogin(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws Exception {

        String email = value(req.getParameter("email"), "").toLowerCase();
        if (email.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập email.");
        }

        String password = value(req.getParameter("password"), "");
        User user = findUserByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("Tài khoản không tồn tại hoặc đã bị khóa.");
        }

        if (!isPasswordValid(user.getUserId(), password)) { 
            throw new IllegalArgumentException("Email hoặc mật khẩu không đúng.");
        }

        req.getSession().setAttribute("user", user);
        req.getSession().setAttribute("flash", "Đăng nhập thành công");

        com.watchstore.repository.CartRepository cartRepo = (com.watchstore.repository.CartRepository) getServletContext().getAttribute("cartRepository");
        if (cartRepo != null) {
            java.util.Map<Integer, Integer> sessionCart = com.watchstore.util.SessionCart.get(req.getSession());
            if (sessionCart != null && !sessionCart.isEmpty()) {
                for (java.util.Map.Entry<Integer, Integer> entry : sessionCart.entrySet()) {
                    try {
                        cartRepo.addProduct(user.getUserId(), entry.getKey(), entry.getValue());
                    } catch (Exception ignored) {}
                }
                sessionCart.clear();
            }
        }

        if (user.getRole() == Role.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/manage/admin/dashboard");
        } else if (user.getRole() == Role.EMPLOYEE) {
            com.watchstore.repository.PermissionRepository permRepo = (com.watchstore.repository.PermissionRepository) getServletContext().getAttribute("permissionRepository");
            if (permRepo == null) permRepo = new com.watchstore.repository.PermissionRepositoryImpl();
            java.util.Set<String> perms = permRepo.getUserPermissionCodes(user.getUserId());
            req.getSession().setAttribute("userPermissions", perms);

            String landingUrl = resolveEmployeeLandingUrl(req, perms);
            if (landingUrl != null) {
                resp.sendRedirect(landingUrl);
            } else {
                req.getSession().setAttribute("flash", "Tài khoản nhân viên chưa được cấp quyền truy cập chức năng nào.");
                resp.sendRedirect(req.getContextPath() + "/page/home");
            }
        } else {
            resp.sendRedirect(req.getContextPath() + "/page/home");
        }
    }

    private String resolveEmployeeLandingUrl(HttpServletRequest req, java.util.Set<String> perms) {
        if (perms == null || perms.isEmpty()) {
            return null;
        }
        if (perms.contains("SALES_ORDER") || perms.contains("ORDER_VIEW")) {
            return req.getContextPath() + "/manage/sales/orders";
        }
        if (perms.contains("PRODUCT_VIEW")) {
            return req.getContextPath() + "/manage/admin/products";
        }
        if (perms.contains("SALES_CUSTOMER") || perms.contains("CUSTOMER_VIEW")) {
            return req.getContextPath() + "/manage/sales/customers";
        }
        if (perms.contains("SALES_WARRANTY")) {
            return req.getContextPath() + "/manage/sales/warranty";
        }
        if (perms.contains("VOUCHER_VIEW")) {
            return req.getContextPath() + "/manage/admin/vouchers";
        }
        if (perms.contains("SALES_REPORT") || perms.contains("REPORT_VIEW")) {
            return req.getContextPath() + "/manage/sales/report";
        }
        if (perms.contains("SALES_DASHBOARD")) {
            return req.getContextPath() + "/manage/sales/dashboard";
        }
        if (perms.stream().anyMatch(p -> p.startsWith("SALES_") || p.startsWith("ORDER_") || p.startsWith("CUSTOMER_") || p.startsWith("WARRANTY_"))) {
            return req.getContextPath() + "/manage/sales/orders";
        }
        return null;
    }

    private User findUserByEmail(String email) throws SQLException {
        String sql =
                "SELECT TOP 1 " +
                        "u.UserID, " +
                        "u.FullName, " +
                        "u.Email, " +
                        "u.Phone, " +
                        "u.Gender, " +
                        "u.DateOfBirth, " +
                        "u.AvatarUrl, " +
                        "u.Status, " +
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
                        "WHEN 'EMPLOYEE' THEN 2 " +
                        "WHEN 'CUSTOMER' THEN 3 " +
                        "ELSE 4 END";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                
                User user = new User();
                user.setUserId(rs.getInt("UserID"));
                user.setFullName(rs.getString("FullName"));
                user.setEmail(rs.getString("Email"));
                user.setPhone(rs.getString("Phone"));
                user.setGender(rs.getString("Gender"));
                Date dob = rs.getDate("DateOfBirth");
                if (dob != null) {
                    user.setDateOfBirth(dob.toLocalDate());
                }
                user.setAvatarUrl(rs.getString("AvatarUrl"));
                user.setStatus(rs.getString("Status"));
                user.setRole(parseRole(rs.getString("RoleCode")));
                return user;
            }
        }
    }

    private boolean isPasswordValid(int userId, String password) throws Exception {
        String sql = "SELECT PasswordHash FROM dbo.Users WHERE UserID=? AND Status='ACTIVE'";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                return rs.getString("PasswordHash").equals(sha256(password));
            }
        }
    }

    private String sha256(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }

    private Role parseRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return Role.CUSTOMER;
        }

        try {
            return Role.valueOf(roleCode.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Role.CUSTOMER;
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException, IllegalArgumentException {
        String name = trim(req.getParameter("fullName"));
        String email = trim(req.getParameter("email")).toLowerCase();
        String phone = trim(req.getParameter("phone"));
        String pass = req.getParameter("password");
        String confirm = req.getParameter("confirmPassword");

        if (name.length() < 2 || name.length() > 100) {
            throw new IllegalArgumentException("Họ tên phải từ 2 đến 100 ký tự.");
        }
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Email không đúng định dạng.");
        }
        if (!phone.isBlank() && !phone.matches("\\d{9,11}")) {
            throw new IllegalArgumentException("Số điện thoại chỉ được nhập 9-11 chữ số.");
        }
        if (pass == null || pass.length() < 6 || pass.length() > 100) {
            throw new IllegalArgumentException("Mật khẩu phải từ 6 đến 100 ký tự.");
        }
        if (!pass.equals(confirm)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        }

        try {
            User existing = findUserByEmail(email);
            if (existing != null) {
                throw new IllegalArgumentException("Email này đã được đăng ký tài khoản. Vui lòng đăng nhập.");
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Lỗi hệ thống khi kiểm tra email.");
        }

        PendingRegistration pendingReg = new PendingRegistration(name, email, phone, pass);
        req.getSession().setAttribute("pendingReg", pendingReg);

        OtpService.OtpResult result = otpService.generateAndSendOtp(email, "REGISTER");
        if (!result.isSuccess()) {
            throw new IllegalArgumentException(result.getMessage());
        }

        req.getSession().setAttribute("flash", result.getMessage());
        resp.sendRedirect(req.getContextPath() + "/auth/verify-otp?purpose=REGISTER");
    }

    private void handleForgotPassword(HttpServletRequest req, HttpServletResponse resp) throws IOException, IllegalArgumentException {
        String email = trim(req.getParameter("email")).toLowerCase();
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Email không đúng định dạng.");
        }

        try {
            User user = findUserByEmail(email);
            if (user == null) {
                throw new IllegalArgumentException("Email không tồn tại trong hệ thống WatchStore.");
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Lỗi hệ thống khi kiểm tra email.");
        }

        req.getSession().setAttribute("forgotEmail", email);

        OtpService.OtpResult result = otpService.generateAndSendOtp(email, "FORGOT_PASSWORD");
        if (!result.isSuccess()) {
            throw new IllegalArgumentException(result.getMessage());
        }

        req.getSession().setAttribute("flash", result.getMessage());
        resp.sendRedirect(req.getContextPath() + "/auth/verify-otp?purpose=FORGOT_PASSWORD");
    }

    private void handleVerifyOtp(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String purpose = trim(req.getParameter("purpose")).toUpperCase();
        if (!"REGISTER".equals(purpose) && !"FORGOT_PASSWORD".equals(purpose)) {
            purpose = "REGISTER";
        }
        String otpCode = trim(req.getParameter("otpCode"));

        String email = null;
        if ("REGISTER".equals(purpose)) {
            PendingRegistration reg = (PendingRegistration) req.getSession().getAttribute("pendingReg");
            if (reg != null) email = reg.getEmail();
        } else {
            email = (String) req.getSession().getAttribute("forgotEmail");
        }

        if (email == null || email.isBlank()) {
            req.setAttribute("error", "Phiên làm việc đã hết hạn. Vui lòng thực hiện lại từ đầu.");
            req.setAttribute("otpPurpose", purpose);
            ViewRouter.customer(req, resp, "guest/verify-otp", "Xác thực mã OTP");
            return;
        }

        OtpService.OtpResult result = otpService.verifyOtp(email, purpose, otpCode);
        if (!result.isSuccess()) {
            req.setAttribute("error", result.getMessage());
            req.setAttribute("otpPurpose", purpose);
            ViewRouter.customer(req, resp, "guest/verify-otp", "Xác thực mã OTP");
            return;
        }

        if ("REGISTER".equals(purpose)) {
            PendingRegistration reg = (PendingRegistration) req.getSession().getAttribute("pendingReg");
            if (reg == null) {
                req.setAttribute("error", "Không tìm thấy thông tin đăng ký. Vui lòng đăng ký lại.");
                ViewRouter.customer(req, resp, "guest/register", "Đăng ký tài khoản");
                return;
            }
            try {
                int newUserId = registerUser(reg.getFullName(), reg.getEmail(), reg.getPhone(), reg.getPassword());
                req.getSession().removeAttribute("pendingReg");
                
                User u = findUserByEmail(reg.getEmail());
                
                req.getSession().setAttribute("user", u);
                req.getSession().setAttribute("flash", "Đăng ký tài khoản thành công!");
                resp.sendRedirect(req.getContextPath() + "/page/home");
            } catch (Exception ex) {
                req.setAttribute("error", "Lỗi tạo tài khoản: " + message(ex));
                ViewRouter.customer(req, resp, "guest/register", "Đăng ký tài khoản");
            }
        } else {
            req.getSession().setAttribute("otpVerifiedForReset", Boolean.TRUE);
            resp.sendRedirect(req.getContextPath() + "/auth/reset-password");
        }
    }

    private int registerUser(String name, String email, String phone, String password) throws Exception {
        String sqlUser = "INSERT INTO Users (Email, PasswordHash, FullName, Phone, Status) VALUES (?, ?, ?, ?, 'ACTIVE')";
        String sqlRole = "INSERT INTO UserRoles (UserID, RoleID) VALUES (?, (SELECT RoleID FROM Roles WHERE RoleCode='CUSTOMER'))";
        
        try (Connection con = DBContext.getConnection()) {
            con.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement ps = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, email);
                    ps.setString(2, sha256(password));
                    ps.setString(3, name);
                    if(phone == null || phone.isBlank()) {
                        ps.setNull(4, Types.VARCHAR);
                    } else {
                        ps.setString(4, phone);
                    }
                    ps.executeUpdate();
                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys.next()) {
                        userId = keys.getInt(1);
                    } else {
                        throw new SQLException("Không lấy được UserID sau INSERT.");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(sqlRole)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
                con.commit();
                return userId;
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private void handleResendOtp(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String purpose = trim(req.getParameter("purpose")).toUpperCase();
        if (!"REGISTER".equals(purpose) && !"FORGOT_PASSWORD".equals(purpose)) {
            purpose = "REGISTER";
        }

        String email = null;
        if ("REGISTER".equals(purpose)) {
            PendingRegistration reg = (PendingRegistration) req.getSession().getAttribute("pendingReg");
            if (reg != null) email = reg.getEmail();
        } else {
            email = (String) req.getSession().getAttribute("forgotEmail");
        }

        if (email == null || email.isBlank()) {
            req.getSession().setAttribute("flash", "Phiên làm việc đã hết hạn. Vui lòng thao tác lại.");
            resp.sendRedirect(req.getContextPath() + "/auth/" + ("REGISTER".equals(purpose) ? "register" : "forgot-password"));
            return;
        }

        OtpService.OtpResult result = otpService.generateAndSendOtp(email, purpose);
        req.getSession().setAttribute("flash", result.getMessage());
        resp.sendRedirect(req.getContextPath() + "/auth/verify-otp?purpose=" + purpose);
    }

    private void handleResetPassword(HttpServletRequest req, HttpServletResponse resp) throws IOException, IllegalArgumentException {
        Boolean verified = (Boolean) req.getSession().getAttribute("otpVerifiedForReset");
        String email = (String) req.getSession().getAttribute("forgotEmail");
        if (verified == null || !verified || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Bạn chưa xác thực OTP hoặc phiên làm việc đã hết hạn.");
        }

        String pass = req.getParameter("password");
        String confirm = req.getParameter("confirmPassword");

        if (pass == null || pass.length() < 6 || pass.length() > 100) {
            throw new IllegalArgumentException("Mật khẩu mới phải từ 6 đến 100 ký tự.");
        }
        if (!pass.equals(confirm)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        }

        try {
            User user = findUserByEmail(email);
            if (user == null) {
                throw new IllegalArgumentException("Tài khoản không tồn tại.");
            }
            updateUserPassword(user.getUserId(), pass);
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi khi cập nhật mật khẩu.");
        }

        req.getSession().removeAttribute("forgotEmail");
        req.getSession().removeAttribute("otpVerifiedForReset");
        req.getSession().setAttribute("flash", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.");
        resp.sendRedirect(req.getContextPath() + "/auth/login");
    }
    
    private void updateUserPassword(int userId, String newPassword) throws Exception {
        String sql = "UPDATE dbo.Users SET PasswordHash = ?, UpdatedAt = SYSDATETIME() WHERE UserID = ?";
        try (Connection con = DBContext.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sha256(newPassword));
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    private String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String trim(String v) { return v == null ? "" : v.trim(); }

    private String message(Exception e) {
        Throwable t = e;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() == null ? "Thao tác không thành công." : t.getMessage();
    }
}
