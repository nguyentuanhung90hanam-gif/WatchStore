package com.watchstore.controller.admin;

import com.watchstore.model.Permission;
import com.watchstore.model.User;
import com.watchstore.repository.PermissionRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@WebServlet(urlPatterns = {"/manage/admin/permissions", "/manage/admin/permissions/*"})
public class PermissionController extends HttpServlet {

    private PermissionRepository permissionRepository;

    @Override
    public void init() {
        permissionRepository = (PermissionRepository) getServletContext().getAttribute("permissionRepository");
        if (permissionRepository == null) permissionRepository = new com.watchstore.repository.PermissionRepositoryImpl();
    }

    private void setCommonAttributes(HttpServletRequest req) {
        req.setAttribute("adminArea", "admin");
        req.setAttribute("pageTitle", "Phân quyền Nhân viên bán hàng");
        req.setAttribute("moduleTitle", "Phân quyền Nhân viên bán hàng");
        req.setAttribute("moduleKicker", "EMPLOYEE PERMISSION MATRIX");
        req.setAttribute("moduleDescription", "Cấu hình quyền cho tài khoản Nhân viên bán hàng. Chỉ các tài khoản EMPLOYEE được hiển thị.");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setCommonAttributes(req);

        List<User> employees = permissionRepository.findAllEmployees();
        req.setAttribute("employees", employees);

        int selectedUserId = 0;
        String userIdParam = req.getParameter("userId");
        if (userIdParam != null && !userIdParam.isBlank()) {
            try {
                selectedUserId = Integer.parseInt(userIdParam);
            } catch (NumberFormatException ignored) {}
        }

        User selectedEmployee = null;
        if (selectedUserId > 0) {
            for (User u : employees) {
                if (u.getUserId() == selectedUserId) {
                    selectedEmployee = u;
                    break;
                }
            }
            if (selectedEmployee == null) {
                com.watchstore.repository.UserRepository userRepo = (com.watchstore.repository.UserRepository) getServletContext().getAttribute("userRepository");
                if (userRepo == null) userRepo = new com.watchstore.repository.UserRepositoryImpl();
                User found = userRepo.findById(selectedUserId);
                if (found != null && found.getRole() == com.watchstore.enums.Role.EMPLOYEE) {
                    selectedEmployee = found;
                }
            }
        }

        // Tự động chọn nhân viên Sales (ưu tiên sales@watchstore.vn) nếu chưa chỉ định
        if (selectedEmployee == null && employees != null && !employees.isEmpty()) {
            for (User u : employees) {
                if ("sales@watchstore.vn".equalsIgnoreCase(u.getEmail())) {
                    selectedEmployee = u;
                    selectedUserId = u.getUserId();
                    break;
                }
            }
            if (selectedEmployee == null) {
                selectedEmployee = employees.get(0);
                selectedUserId = selectedEmployee.getUserId();
            }
        }

        List<Permission> permissions = permissionRepository.findAll();
        Set<Integer> activePermissionIds = (selectedEmployee != null)
                ? permissionRepository.getUserPermissionIds(selectedUserId)
                : java.util.Collections.emptySet();
        Set<String> activePermissionCodes = (selectedEmployee != null)
                ? permissionRepository.getUserPermissionCodes(selectedUserId)
                : java.util.Collections.emptySet();

        java.util.Map<String, Permission> permByCode = new java.util.HashMap<>();
        for (Permission p : permissions) {
            permByCode.put(p.getPermissionCode(), p);
        }

        req.setAttribute("selectedUserId", selectedUserId);
        req.setAttribute("selectedEmployee", selectedEmployee);
        req.setAttribute("permissions", permissions);
        req.setAttribute("permByCode", permByCode);
        req.setAttribute("activePermissionIds", activePermissionIds);
        req.setAttribute("activePermissionCodes", activePermissionCodes);

        req.setAttribute("contentPage", "/views/admin/permission-matrix.jsp");
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String userIdParam = req.getParameter("userId");
        if (userIdParam == null || userIdParam.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/manage/admin/permissions");
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdParam);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/manage/admin/permissions");
            return;
        }

        // Xác thực người dùng mục tiêu phải có Role EMPLOYEE
        com.watchstore.repository.UserRepository userRepo = (com.watchstore.repository.UserRepository) getServletContext().getAttribute("userRepository");
        if (userRepo == null) userRepo = new com.watchstore.repository.UserRepositoryImpl();
        User targetUser = userRepo.findById(userId);
        if (targetUser == null || targetUser.getRole() != com.watchstore.enums.Role.EMPLOYEE) {
            req.getSession().setAttribute("flash", "Chỉ được phân quyền cho tài khoản Nhân viên (EMPLOYEE).");
            resp.sendRedirect(req.getContextPath() + "/manage/admin/permissions");
            return;
        }

        // Đọc toàn bộ permissions để kiểm tra và loại bỏ quyền Hệ Thống (ADMIN ONLY)
        List<Permission> allPerms = permissionRepository.findAll();
        java.util.Map<Integer, Permission> permMap = new java.util.HashMap<>();
        for (Permission p : allPerms) {
            permMap.put(p.getPermissionId(), p);
        }

        String[] permParamArr = req.getParameterValues("permissionIds");
        List<Integer> sanitizedPermissionIds = new ArrayList<>();
        if (permParamArr != null) {
            for (String pidStr : permParamArr) {
                try {
                    int pid = Integer.parseInt(pidStr);
                    Permission p = permMap.get(pid);
                    if (p != null) {
                        String code = p.getPermissionCode().toUpperCase();
                        String module = p.getModuleCode() != null ? p.getModuleCode().toUpperCase() : "";
                        // KHÓA CỨNG: Tuyệt đối không lưu quyền Hệ thống (Account, Role, Permission) cho Employee
                        if (!code.startsWith("ACCOUNT") && !code.startsWith("ROLE")
                                && !code.startsWith("PERMISSION") && !code.startsWith("SYSTEM")
                                && !module.equals("SYSTEM") && !module.equals("ROLE") && !module.equals("ACCOUNT")) {
                            sanitizedPermissionIds.add(pid);
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        permissionRepository.updateUserPermissions(userId, sanitizedPermissionIds);
        req.getSession().setAttribute("flash", "Đã lưu phân quyền cho nhân viên " + targetUser.getFullName() + " (" + targetUser.getEmail() + ") thành công!");
        resp.sendRedirect(req.getContextPath() + "/manage/admin/permissions?userId=" + userId);
    }
}
