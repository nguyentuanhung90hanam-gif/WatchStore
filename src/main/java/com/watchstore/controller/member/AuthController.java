package com.watchstore.controller.member;

import com.watchstore.model.PendingRegistration;
import com.watchstore.model.User;
import com.watchstore.repository.UserRepository;
import com.watchstore.service.OtpService;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/auth/*")
public class AuthController extends HttpServlet {
    private UserRepository users;
    private OtpService otpService;

    @Override
    public void init() {
        users = (UserRepository) getServletContext().getAttribute("userRepository");
        otpService = (OtpService) getServletContext().getAttribute("otpService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String email = trim(req.getParameter("email")).toLowerCase();
        String pass = req.getParameter("password");
        if (email.isBlank() || pass == null || pass.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ email và mật khẩu.");
        }
        User u = users.login(email, pass);
        if (u == null) {
            throw new IllegalArgumentException("Email hoặc mật khẩu không chính xác.");
        }
        req.getSession().setAttribute("user", u);
        req.getSession().setAttribute("flash", "Đăng nhập thành công!");
        switch (u.getRole()) {
            case ADMIN -> resp.sendRedirect(req.getContextPath() + "/manage/admin/dashboard");
            case SALES -> resp.sendRedirect(req.getContextPath() + "/manage/sales/dashboard");
            case WAREHOUSE -> resp.sendRedirect(req.getContextPath() + "/manage/warehouse/dashboard");
            default -> resp.sendRedirect(req.getContextPath() + "/page/home");
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

        // Check if email already exists
        User existing = users.findByEmail(email);
        if (existing != null) {
            throw new IllegalArgumentException("Email này đã được đăng ký tài khoản. Vui lòng đăng nhập.");
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

        User user = users.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("Email không tồn tại trong hệ thống WatchStore.");
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
                User u = users.register(reg.getFullName(), reg.getEmail(), reg.getPhone(), reg.getPassword());
                req.getSession().removeAttribute("pendingReg");
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

        User user = users.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("Tài khoản không tồn tại.");
        }

        boolean updated = users.updatePassword(user.getId(), "", pass);
        if (!updated) {
            throw new IllegalArgumentException("Không thể cập nhật mật khẩu. Vui lòng thử lại.");
        }

        req.getSession().removeAttribute("forgotEmail");
        req.getSession().removeAttribute("otpVerifiedForReset");
        req.getSession().setAttribute("flash", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.");
        resp.sendRedirect(req.getContextPath() + "/auth/login");
    }

    private String trim(String v) { return v == null ? "" : v.trim(); }
    private String message(Exception e) {
        Throwable t = e;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() == null ? "Thao tác không thành công." : t.getMessage();
    }
}
