package com.watchstore.controller.admin;

import com.watchstore.model.Permission;
import com.watchstore.model.Role;
import com.watchstore.model.User;
import com.watchstore.repository.PermissionRepository;
import com.watchstore.repository.RoleRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/manage/admin/permissions", "/manage/admin/permissions/*"})
public class PermissionController extends HttpServlet {

    private PermissionRepository permissionRepository;
    private RoleRepository roleRepository;

    @Override
    public void init() {
        permissionRepository = (PermissionRepository) getServletContext().getAttribute("permissionRepository");
        if (permissionRepository == null) permissionRepository = new com.watchstore.repository.PermissionRepositoryImpl();

        roleRepository = (RoleRepository) getServletContext().getAttribute("roleRepository");
        if (roleRepository == null) roleRepository = new com.watchstore.repository.RoleRepositoryImpl();
    }

    private void setCommonAttributes(HttpServletRequest req) {
        req.setAttribute("adminArea", "admin");
        req.setAttribute("tableKind", "permissions");
        req.setAttribute("pageTitle", "Quản lý phân quyền");
        req.setAttribute("moduleTitle", "Phân quyền & Vai trò");
        req.setAttribute("moduleKicker", "MA TRẬN PHÂN QUYỀN");
        req.setAttribute("moduleDescription", "Danh sách các quyền hạn hệ thống theo từng module chức năng.");
        req.setAttribute("primaryAction", "");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        setCommonAttributes(req);
        
        List<Role> allRoles = roleRepository.findAll();
        // Chỉ cho phép chọn SALES và WAREHOUSE (loại bỏ ADMIN)
        List<Role> editableRoles = allRoles.stream()
                .filter(r -> !"ADMIN".equalsIgnoreCase(r.getRoleCode()))
                .collect(Collectors.toList());
        req.setAttribute("roles", editableRoles);

        String roleIdStr = req.getParameter("roleId");
        if (roleIdStr != null && !roleIdStr.isBlank()) {
            try {
                int selectedRoleId = Integer.parseInt(roleIdStr);
                Role selectedRole = roleRepository.findById(selectedRoleId);
                if (selectedRole != null) {
                    if ("ADMIN".equalsIgnoreCase(selectedRole.getRoleCode())) {
                        req.setAttribute("errorMessage", "Không thể chỉnh sửa quyền của ADMIN.");
                    } else {
                        req.setAttribute("selectedRoleId", selectedRoleId);
                        List<Integer> assignedIds = permissionRepository.getPermissionIdsByRoleId(selectedRoleId);
                        req.setAttribute("assignedPermissionIds", assignedIds);
                    }
                }
            } catch (NumberFormatException ignored) {}
        }

        String keyword = req.getParameter("keyword");
        List<Permission> permissions;
        if (keyword != null && !keyword.isBlank()) {
            permissions = permissionRepository.search(keyword);
        } else {
            permissions = permissionRepository.findAll();
        }

        req.setAttribute("permissions", permissions);
        req.setAttribute("contentPage", "/views/shared/management-module.jsp");
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if ("/toggle".equals(path)) {
            User user = (User) req.getSession().getAttribute("user");
            if (user == null || (!user.hasPermission("PERMISSIONS_MANAGE") && user.getRole() != com.watchstore.enums.Role.ADMIN)) {
                req.getSession().setAttribute("errorMessage", "Bạn không có quyền thực hiện thao tác này.");
                resp.sendRedirect(req.getContextPath() + "/manage/admin/permissions");
                return;
            }

            try {
                int roleId = Integer.parseInt(req.getParameter("roleId"));
                int permissionId = Integer.parseInt(req.getParameter("permissionId"));
                String action = req.getParameter("action");

                Role targetRole = roleRepository.findById(roleId);
                if (targetRole == null || "ADMIN".equalsIgnoreCase(targetRole.getRoleCode())) {
                    req.getSession().setAttribute("errorMessage", "Role không hợp lệ hoặc không được phép sửa.");
                } else {
                    boolean success = false;
                    if ("open".equals(action)) {
                        success = permissionRepository.addRolePermission(roleId, permissionId);
                    } else if ("close".equals(action)) {
                        success = permissionRepository.removeRolePermission(roleId, permissionId);
                    }

                    if (success) {
                        req.getSession().setAttribute("successMessage", "Cập nhật quyền thành công.");
                    } else {
                        req.getSession().setAttribute("errorMessage", "Cập nhật quyền thất bại.");
                    }
                }
                resp.sendRedirect(req.getContextPath() + "/manage/admin/permissions?roleId=" + roleId);
                return;
            } catch (NumberFormatException e) {
                req.getSession().setAttribute("errorMessage", "Dữ liệu không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/manage/admin/permissions");
                return;
            }
        }
        resp.sendRedirect(req.getContextPath() + "/manage/admin/permissions");
    }
}
