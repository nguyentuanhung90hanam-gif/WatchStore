package com.watchstore.controller.admin;

import com.watchstore.model.Role;
import com.watchstore.repository.RoleRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

/**
 * Controller riêng cho module Roles.
 * Pattern giống BrandController — tách khỏi AdminController.
 */
@WebServlet(urlPatterns = {"/manage/admin/roles", "/manage/admin/roles/*"})
public class RoleController extends HttpServlet {

    private RoleRepository roleRepository;

    @Override
    public void init() {
        roleRepository = (RoleRepository) getServletContext().getAttribute("roleRepository");
        if (roleRepository == null) roleRepository = new com.watchstore.repository.RoleRepositoryImpl();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void setCommonAttributes(HttpServletRequest req) {
        req.setAttribute("adminArea", "admin");
        req.setAttribute("tableKind", "roles");
        req.setAttribute("pageTitle", "Quản lý vai trò");
        req.setAttribute("moduleTitle", "Vai trò");
        req.setAttribute("moduleKicker", "VAI TRÒ HỆ THỐNG");
        req.setAttribute("moduleDescription", "Nhóm quyền cho quản trị, bán hàng, kho và khách hàng.");
        req.setAttribute("primaryAction", "Thêm vai trò");
    }

    private void forwardToList(HttpServletRequest req, HttpServletResponse resp,
                               List<Role> roles) throws ServletException, IOException {
        setCommonAttributes(req);
        req.setAttribute("roles", roles);
        req.setAttribute("contentPage", "/views/shared/management-module.jsp");
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }

    private void forwardToForm(HttpServletRequest req, HttpServletResponse resp,
                               String pageTitle) throws ServletException, IOException {
        setCommonAttributes(req);
        req.setAttribute("pageTitle", pageTitle);
        req.setAttribute("contentPage", "/views/admin/role-form.jsp");
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }

    // ─── GET ──────────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getPathInfo();
        if (action == null || action.equals("/")) {
            String keyword = req.getParameter("keyword");
            if (keyword != null && !keyword.isBlank()) {
                forwardToList(req, resp, roleRepository.search(keyword));
            } else {
                forwardToList(req, resp, roleRepository.findAll());
            }
            return;
        }

        switch (action) {

            case "/add": {
                forwardToForm(req, resp, "Thêm vai trò");
                break;
            }

            case "/edit": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        Role role = roleRepository.findById(id);
                        if (role != null) {
                            req.setAttribute("role", role);
                        }
                    } catch (NumberFormatException ignored) {}
                }
                forwardToForm(req, resp, "Sửa vai trò");
                break;
            }

            case "/delete": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        Role role = roleRepository.findById(id);
                        if (role == null) {
                            req.getSession().setAttribute("errorMessage", "Không thể xóa vai trò.");
                        } else if (roleRepository.isRoleInUse(id) || role.getUserCount() > 0) {
                            req.getSession().setAttribute("errorMessage", "Không thể xóa vai trò vì vai trò này đang được sử dụng bởi tài khoản.");
                        } else if (role.getIsSystem() || isSystemRoleCode(role.getRoleCode())) {
                            req.getSession().setAttribute("errorMessage", "Không thể xóa vai trò vì đây là vai trò hệ thống.");
                        } else {
                            boolean deleted = roleRepository.deleteById(id);
                            if (deleted) {
                                req.getSession().setAttribute("successMessage", "Xóa vai trò thành công.");
                            } else {
                                req.getSession().setAttribute("errorMessage", "Không thể xóa vai trò.");
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("[DEBUG RoleController] Error deleting role id=" + idStr);
                        e.printStackTrace();
                        req.getSession().setAttribute("errorMessage", "Không thể xóa vai trò.");
                    }
                }
                resp.sendRedirect(req.getContextPath() + "/manage/admin/roles");
                break;
            }

            case "/search": {
                String keyword = req.getParameter("keyword");
                if (keyword != null && !keyword.isBlank()) {
                    forwardToList(req, resp, roleRepository.search(keyword));
                } else {
                    forwardToList(req, resp, roleRepository.findAll());
                }
                break;
            }

            default: {
                String keyword = req.getParameter("keyword");
                if (keyword != null && !keyword.isBlank()) {
                    forwardToList(req, resp, roleRepository.search(keyword));
                } else {
                    forwardToList(req, resp, roleRepository.findAll());
                }
            }
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
            resp.sendRedirect(req.getContextPath() + "/manage/admin/roles");
        }
    }

    // ─── Save (INSERT) ────────────────────────────────────────────────────────

    private void handleSave(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String roleCode    = trim(req.getParameter("roleCode"));
        String roleName    = trim(req.getParameter("roleName"));
        String description = trim(req.getParameter("description"));
        String isSystemStr = trim(req.getParameter("isSystem"));

        // Validation
        String error = validateRole(roleCode, roleName, description);
        if (error == null && roleRepository.existsByCode(roleCode, null)) {
            error = "Mã vai trò \"" + roleCode + "\" đã tồn tại trong hệ thống.";
        }

        if (error != null) {
            Role draft = buildRole(0, roleCode, roleName, description, "true".equals(isSystemStr));
            showFormWithError(req, resp, draft, error, "Thêm vai trò");
            return;
        }

        Role role = buildRole(0, roleCode, roleName, description, "true".equals(isSystemStr));
        roleRepository.insert(role);
        resp.sendRedirect(req.getContextPath() + "/manage/admin/roles");
    }

    // ─── Update (UPDATE) ──────────────────────────────────────────────────────

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idStr       = trim(req.getParameter("roleId"));
        String roleCode    = trim(req.getParameter("roleCode"));
        String roleName    = trim(req.getParameter("roleName"));
        String description = trim(req.getParameter("description"));
        String isSystemStr = trim(req.getParameter("isSystem"));

        int id = 0;
        try { id = Integer.parseInt(idStr); } catch (NumberFormatException ignored) {}

        // Validation
        String error = validateRole(roleCode, roleName, description);
        if (error == null && roleRepository.existsByCode(roleCode, id)) {
            error = "Mã vai trò \"" + roleCode + "\" đã được dùng bởi vai trò khác.";
        }

        if (error != null) {
            Role draft = buildRole(id, roleCode, roleName, description, "true".equals(isSystemStr));
            showFormWithError(req, resp, draft, error, "Sửa vai trò");
            return;
        }

        Role role = buildRole(id, roleCode, roleName, description, "true".equals(isSystemStr));
        roleRepository.update(role);
        resp.sendRedirect(req.getContextPath() + "/manage/admin/roles");
    }

    // ─── Utilities ───────────────────────────────────────────────────────────

    private void showFormWithError(HttpServletRequest req, HttpServletResponse resp,
                                   Role draft, String error, String pageTitle)
            throws ServletException, IOException {
        req.setAttribute("errorMessage", error);
        req.setAttribute("role", draft);
        forwardToForm(req, resp, pageTitle);
    }

    private String trim(String s) {
        return (s == null) ? "" : s.trim();
    }

    private String validateRole(String roleCode, String roleName, String description) {
        if (roleCode.isBlank()) return "Mã vai trò không được để trống.";
        if (roleCode.length() > 30) return "Mã vai trò không được vượt quá 30 ký tự.";
        if (!roleCode.matches("^[A-Za-z0-9_]+$")) {
            return "Mã vai trò chỉ được gồm chữ cái, chữ số và dấu gạch dưới (VD: ADMIN, SALES_STAFF).";
        }
        if (roleName.isBlank()) return "Tên vai trò không được để trống.";
        if (roleName.length() > 100) return "Tên vai trò không được vượt quá 100 ký tự.";
        if (!description.isEmpty() && description.length() > 500) return "Mô tả không được vượt quá 500 ký tự.";
        return null;
    }

    private Role buildRole(int id, String code, String name, String desc, boolean isSystem) {
        Role r = new Role();
        r.setRoleId(id);
        r.setRoleCode(code);
        r.setRoleName(name);
        r.setDescription(desc.isEmpty() ? null : desc);
        r.setIsSystem(isSystem);
        return r;
    }

    private boolean isSystemRoleCode(String roleCode) {
        if (roleCode == null) return false;
        String code = roleCode.trim().toUpperCase();
        return "ADMIN".equals(code) || "SALES".equals(code) || "WAREHOUSE".equals(code) || "CUSTOMER".equals(code);
    }
}
