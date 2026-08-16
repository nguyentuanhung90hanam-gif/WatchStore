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
        req.setAttribute("pageTitle", "Ma trận Phân quyền Nhân viên");
        req.setAttribute("moduleTitle", "Phân quyền & Vai trò");
        req.setAttribute("moduleKicker", "EMPLOYEE PERMISSION MATRIX");
        req.setAttribute("moduleDescription", "Bật/Tắt quyền hạn chi tiết từng phân hệ Bán hàng & Kho cho mỗi Nhân viên.");
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

        if (selectedUserId == 0 && !employees.isEmpty()) {
            selectedUserId = employees.get(0).getUserId();
        }

        User selectedEmployee = null;
        for (User u : employees) {
            if (u.getUserId() == selectedUserId) {
                selectedEmployee = u;
                break;
            }
        }

        List<Permission> permissions = permissionRepository.findAll();
        Set<Integer> activePermissionIds = permissionRepository.getUserPermissionIds(selectedUserId);
        Set<String> activePermissionCodes = permissionRepository.getUserPermissionCodes(selectedUserId);

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

        int userId = Integer.parseInt(userIdParam);
        String[] permParamArr = req.getParameterValues("permissionIds");

        List<Integer> permissionIds = new ArrayList<>();
        if (permParamArr != null) {
            for (String pidStr : permParamArr) {
                try {
                    permissionIds.add(Integer.parseInt(pidStr));
                } catch (NumberFormatException ignored) {}
            }
        }

        permissionRepository.updateUserPermissions(userId, permissionIds);
        req.getSession().setAttribute("flash", "Đã lưu ma trận phân quyền chi tiết cho nhân viên thành công!");
        resp.sendRedirect(req.getContextPath() + "/manage/admin/permissions?userId=" + userId);
    }
}
