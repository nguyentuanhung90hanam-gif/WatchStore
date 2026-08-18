package com.watchstore.controller.admin;

import com.watchstore.model.Permission;
import com.watchstore.model.User;
import com.watchstore.repository.PermissionRepository;
import com.watchstore.repository.UserRepository;
import com.watchstore.repository.UserRepositoryImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.*;

@WebServlet(urlPatterns = {"/manage/admin/permissions", "/manage/admin/permissions/*"})
public class PermissionController extends HttpServlet {

    private PermissionRepository permissionRepository;
    private UserRepository userRepository;

    @Override
    public void init() {
        permissionRepository = (PermissionRepository) getServletContext().getAttribute("permissionRepository");
        if (permissionRepository == null) {
            permissionRepository = new com.watchstore.repository.PermissionRepositoryImpl();
        }
        userRepository = (UserRepository) getServletContext().getAttribute("userRepository");
        if (userRepository == null) {
            userRepository = new UserRepositoryImpl();
        }
    }

    private void setCommonAttributes(HttpServletRequest req) {
        req.setAttribute("adminArea", "admin");
        req.setAttribute("pageTitle", "Phân quyền Nhân viên");
        req.setAttribute("moduleTitle", "Phân quyền Nhân viên");
        req.setAttribute("moduleKicker", "EMPLOYEE PERMISSIONS");
        req.setAttribute("moduleDescription", "Cấu hình phân quyền chức năng chi tiết cho từng tài khoản Nhân viên (EMPLOYEE).");
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
            if (employees != null) {
                for (User u : employees) {
                    if (u.getUserId() == selectedUserId) {
                        selectedEmployee = u;
                        break;
                    }
                }
            }
            if (selectedEmployee == null) {
                User found = userRepository.findById(selectedUserId);
                if (found != null && found.getRole() == com.watchstore.enums.Role.EMPLOYEE) {
                    selectedEmployee = found;
                }
            }
        }

        // Nếu chưa chọn hoặc không tìm thấy: tự động chọn nhân viên đầu tiên trong danh sách
        if (selectedEmployee == null && employees != null && !employees.isEmpty()) {
            selectedEmployee = employees.get(0);
            selectedUserId = selectedEmployee.getUserId();
        }

        Set<Integer> activePermissionIds = Collections.emptySet();
        Set<String> activePermissionCodes = Collections.emptySet();

        if (selectedEmployee != null) {
            activePermissionIds = permissionRepository.getUserPermissionIds(selectedEmployee.getUserId());
            activePermissionCodes = permissionRepository.getUserPermissionCodes(selectedEmployee.getUserId());
        }

        // Tải toàn bộ permissions và đưa vào Map theo PermissionCode để JSP tra cứu nhanh
        // LẤY toàn bộ permissions thuộc phạm vi nghiệp vụ cho Employee
        List<Permission> allPerms = permissionRepository.findAll();
        Map<String, Permission> permByCode = new HashMap<>();
        if (allPerms != null) {
            for (Permission p : allPerms) {
                if (p.getPermissionCode() != null) {
                    String code = p.getPermissionCode().toUpperCase();
                    // Cho phép các quyền nghiệp vụ, loại trừ hoàn toàn SYSTEM, KHO, ĐỔI TRẢ, VẬN CHUYỂN
                    if (isAllowedEmployeePermission(code)) {
                        permByCode.put(code, p);
                    }
                }
            }
        }

        req.setAttribute("selectedUserId", selectedUserId);
        req.setAttribute("selectedEmployee", selectedEmployee);
        req.setAttribute("activePermissionIds", activePermissionIds);
        req.setAttribute("activePermissionCodes", activePermissionCodes);
        req.setAttribute("permByCode", permByCode);

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
        User targetUser = userRepository.findById(userId);
        if (targetUser == null || targetUser.getRole() != com.watchstore.enums.Role.EMPLOYEE) {
            req.getSession().setAttribute("flash", "Chỉ được phân quyền cho tài khoản Nhân viên (EMPLOYEE).");
            resp.sendRedirect(req.getContextPath() + "/manage/admin/permissions");
            return;
        }

        // Đọc danh sách PermissionID được chọn từ checkboxes
        String[] permIdStrs = req.getParameterValues("permissionIds");
        Set<Integer> submittedIds = new HashSet<>();
        if (permIdStrs != null) {
            for (String s : permIdStrs) {
                try {
                    int pid = Integer.parseInt(s.trim());
                    if (pid > 0) submittedIds.add(pid);
                } catch (NumberFormatException ignored) {}
            }
        }

        List<Permission> allPerms = permissionRepository.findAll();
        List<Integer> sanitizedPermissionIds = new ArrayList<>();

        for (Integer pid : submittedIds) {
            for (Permission p : allPerms) {
                if (p.getPermissionId() == pid) {
                    String code = p.getPermissionCode().toUpperCase();
                    // KHÓA CỨNG SERVER-SIDE: Tuyệt đối không cho phép lưu quyền Hệ thống (ACCOUNT, ROLE, PERMISSION) hoặc Kho/Đổi trả/Vận chuyển
                    if (isAllowedEmployeePermission(code)) {
                        sanitizedPermissionIds.add(pid);
                    }
                    break;
                }
            }
        }

        // Cập nhật bảng UserPermissions cho đúng userId của nhân viên
        permissionRepository.updateUserPermissions(userId, sanitizedPermissionIds);

        req.getSession().setAttribute("flash", "Đã lưu phân quyền cho nhân viên " + targetUser.getFullName() + " (" + targetUser.getEmail() + ") thành công!");
        resp.sendRedirect(req.getContextPath() + "/manage/admin/permissions?userId=" + userId);
    }

    private boolean isAllowedEmployeePermission(String code) {
        if (code == null) return false;
        String c = code.toUpperCase();
        // KHÓA CHẶT: Cấm tuyệt đối quyền Quản trị Hệ thống và các module đã bỏ
        if (c.startsWith("ACCOUNT") || c.startsWith("ROLE") || c.startsWith("PERMISSION")
                || c.startsWith("SYSTEM") || c.startsWith("INVENTORY") || c.startsWith("WAREHOUSE")
                || c.startsWith("STOCK") || c.startsWith("RETURN") || c.equals("SALES_RETURN")
                || c.startsWith("DELIVERY") || c.equals("SALES_DELIVERY") || c.equals("SALES_DASHBOARD")) {
            return false;
        }

        // CHO PHÉP tất cả các quyền nghiệp vụ thực tế
        return c.startsWith("PRODUCT_") || c.startsWith("VOUCHER_") || c.startsWith("ORDER_")
                || c.startsWith("CUSTOMER_") || c.startsWith("REPORT_") || c.startsWith("BANNER_")
                || c.startsWith("POST_") || c.equals("SALES_ORDER") || c.equals("SALES_CUSTOMER")
                || c.equals("SALES_WARRANTY") || c.equals("SALES_REPORT");
    }
}
