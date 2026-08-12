package com.watchstore.controller.admin;

import com.watchstore.model.Permission;
import com.watchstore.repository.PermissionRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

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
}
