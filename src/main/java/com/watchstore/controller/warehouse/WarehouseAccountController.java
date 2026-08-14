package com.watchstore.controller.warehouse;

import com.watchstore.model.User;
import com.watchstore.repository.UserAccountRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {
        "/manage/warehouse/account",
        "/manage/warehouse/account/password"
})
public class WarehouseAccountController extends HttpServlet {

    private UserAccountRepository accountRepo;

    @Override
    public void init() {
        accountRepo = new UserAccountRepository();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            User sessionUser = (User) req.getSession().getAttribute("user");
            if (sessionUser == null) {
                resp.sendRedirect(req.getContextPath() + "/auth/login?required=1");
                return;
            }

            User user = accountRepo.findById(sessionUser.getId());
            req.getSession().setAttribute("user", user);
            req.setAttribute("accountUser", user);

            ViewRouter.admin(
                    req,
                    resp,
                    "warehouse/account",
                    "Tài khoản cá nhân",
                    "warehouse"
            );
        } catch (Exception e) {
            req.getSession().setAttribute("errorMsg", getErrorMessage(e));
            resp.sendRedirect(req.getContextPath() + "/manage/warehouse/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getServletPath();
        User sessionUser = (User) req.getSession().getAttribute("user");

        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login?required=1");
            return;
        }

        try {
            if (path.endsWith("/password")) {
                accountRepo.changePassword(
                        sessionUser.getId(),
                        req.getParameter("currentPassword"),
                        req.getParameter("newPassword"),
                        req.getParameter("confirmPassword")
                );
                req.getSession().setAttribute("flash", "Đổi mật khẩu thành công.");
            } else {
                accountRepo.updateProfile(
                        sessionUser.getId(),
                        req.getParameter("fullName"),
                        req.getParameter("phone"),
                        req.getParameter("gender"),
                        parseDate(req.getParameter("dateOfBirth"))
                );
                User updatedUser = accountRepo.findById(sessionUser.getId());
                req.getSession().setAttribute("user", updatedUser);
                req.getSession().setAttribute("flash", "Cập nhật thông tin tài khoản thành công.");
            }
        } catch (Exception e) {
            req.getSession().setAttribute("errorMsg", getErrorMessage(e));
        }

        resp.sendRedirect(req.getContextPath() + "/manage/warehouse/account");
    }

    private java.time.LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return java.time.LocalDate.parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Ngày sinh không hợp lệ.");
        }
    }

    private String getErrorMessage(Exception e) {
        if (e.getMessage() == null || e.getMessage().isBlank()) {
            return "Không thể thực hiện thao tác với tài khoản.";
        }
        return e.getMessage();
    }
}
