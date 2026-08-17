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
        req.setAttribute("pageTitle", "Phân quyền Nhân viên bán hàng");
        req.setAttribute("moduleTitle", "Phân quyền Nhân viên");
        req.setAttribute("moduleKicker", "EMPLOYEE PERMISSIONS");
        req.setAttribute("moduleDescription", "Cấu hình phân công nhóm chức năng cho tài khoản Nhân viên (EMPLOYEE).");
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
                User found = userRepository.findById(selectedUserId);
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

        Set<String> activePermissionCodes = (selectedEmployee != null)
                ? permissionRepository.getUserPermissionCodes(selectedUserId)
                : Collections.emptySet();

        // Xác định các nhóm chức năng đang kích hoạt
        Set<String> activeGroups = new HashSet<>();
        if (activePermissionCodes.contains("PRODUCT_VIEW")
                || activePermissionCodes.contains("PRODUCT_CREATE")
                || activePermissionCodes.contains("PRODUCT_EDIT")) {
            activeGroups.add("PRODUCT");
        }
        if (activePermissionCodes.contains("ORDER_VIEW")
                || activePermissionCodes.contains("SALES_ORDER")
                || activePermissionCodes.contains("CUSTOMER_VIEW")
                || activePermissionCodes.contains("SALES_CUSTOMER")) {
            activeGroups.add("SALES");
        }
        if (activePermissionCodes.contains("SALES_RETURN")
                || activePermissionCodes.contains("SALES_DELIVERY")
                || activePermissionCodes.contains("REVIEW_VIEW")
                || activePermissionCodes.contains("COMMENT_VIEW")) {
            activeGroups.add("REVIEW_COMMENT");
        }
        if (activePermissionCodes.contains("SALES_WARRANTY")
                || activePermissionCodes.contains("WARRANTY_VIEW")) {
            activeGroups.add("WARRANTY");
        }
        if (activePermissionCodes.contains("VOUCHER_VIEW")
                || activePermissionCodes.contains("VOUCHER_CREATE")
                || activePermissionCodes.contains("VOUCHER_EDIT")) {
            activeGroups.add("VOUCHER");
        }
        if (activePermissionCodes.contains("INVENTORY_VIEW")
                || activePermissionCodes.contains("BANNER_VIEW")
                || activePermissionCodes.contains("POST_VIEW")) {
            activeGroups.add("BANNER_POST");
        }
        if (activePermissionCodes.contains("REPORT_VIEW")
                || activePermissionCodes.contains("SALES_REPORT")
                || activePermissionCodes.contains("REPORT_EXPORT")) {
            activeGroups.add("REPORT");
        }

        req.setAttribute("selectedUserId", selectedUserId);
        req.setAttribute("selectedEmployee", selectedEmployee);
        req.setAttribute("activeGroups", activeGroups);
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
        User targetUser = userRepository.findById(userId);
        if (targetUser == null || targetUser.getRole() != com.watchstore.enums.Role.EMPLOYEE) {
            req.getSession().setAttribute("flash", "Chỉ được phân quyền cho tài khoản Nhân viên (EMPLOYEE).");
            resp.sendRedirect(req.getContextPath() + "/manage/admin/permissions");
            return;
        }

        List<Permission> allPerms = permissionRepository.findAll();
        Map<String, Integer> codeToIdMap = new HashMap<>();
        for (Permission p : allPerms) {
            if (p.getPermissionCode() != null) {
                codeToIdMap.put(p.getPermissionCode().toUpperCase(), p.getPermissionId());
            }
        }

        String[] groupParamArr = req.getParameterValues("functionalGroups");
        Set<String> selectedGroups = new HashSet<>();
        if (groupParamArr != null) {
            selectedGroups.addAll(Arrays.asList(groupParamArr));
        }

        Set<Integer> targetPermissionIds = new HashSet<>();

        // 1. SẢN PHẨM: Quản lý sản phẩm, danh mục và thương hiệu
        if (selectedGroups.contains("PRODUCT")) {
            addPermId(targetPermissionIds, codeToIdMap, "PRODUCT_VIEW");
            addPermId(targetPermissionIds, codeToIdMap, "PRODUCT_CREATE");
            addPermId(targetPermissionIds, codeToIdMap, "PRODUCT_EDIT");
        }

        // 2. BÁN HÀNG: Đơn hàng và khách hàng
        if (selectedGroups.contains("SALES")) {
            addPermId(targetPermissionIds, codeToIdMap, "ORDER_VIEW");
            addPermId(targetPermissionIds, codeToIdMap, "ORDER_CREATE");
            addPermId(targetPermissionIds, codeToIdMap, "ORDER_EDIT");
            addPermId(targetPermissionIds, codeToIdMap, "ORDER_APPROVE");
            addPermId(targetPermissionIds, codeToIdMap, "ORDER_EXPORT");
            addPermId(targetPermissionIds, codeToIdMap, "CUSTOMER_VIEW");
            addPermId(targetPermissionIds, codeToIdMap, "CUSTOMER_CREATE");
            addPermId(targetPermissionIds, codeToIdMap, "CUSTOMER_EDIT");
            addPermId(targetPermissionIds, codeToIdMap, "SALES_DASHBOARD");
            addPermId(targetPermissionIds, codeToIdMap, "SALES_ORDER");
            addPermId(targetPermissionIds, codeToIdMap, "SALES_CUSTOMER");
        }

        // 3. REVIEW & COMMENT: Quản lý đánh giá và bình luận
        if (selectedGroups.contains("REVIEW_COMMENT")) {
            addPermId(targetPermissionIds, codeToIdMap, "SALES_RETURN");
            addPermId(targetPermissionIds, codeToIdMap, "SALES_DELIVERY");
        }

        // 4. BẢO HÀNH: Tiếp nhận và xử lý bảo hành
        if (selectedGroups.contains("WARRANTY")) {
            addPermId(targetPermissionIds, codeToIdMap, "SALES_WARRANTY");
        }

        // 5. VOUCHER: Quản lý mã giảm giá
        if (selectedGroups.contains("VOUCHER")) {
            addPermId(targetPermissionIds, codeToIdMap, "VOUCHER_VIEW");
            addPermId(targetPermissionIds, codeToIdMap, "VOUCHER_CREATE");
            addPermId(targetPermissionIds, codeToIdMap, "VOUCHER_EDIT");
        }

        // 6. BANNER & BÀI VIẾT: Quản lý nội dung website
        if (selectedGroups.contains("BANNER_POST")) {
            addPermId(targetPermissionIds, codeToIdMap, "INVENTORY_VIEW");
            addPermId(targetPermissionIds, codeToIdMap, "INVENTORY_CREATE");
            addPermId(targetPermissionIds, codeToIdMap, "INVENTORY_EDIT");
        }

        // 7. BÁO CÁO: Thống kê và báo cáo bán hàng
        if (selectedGroups.contains("REPORT")) {
            addPermId(targetPermissionIds, codeToIdMap, "REPORT_VIEW");
            addPermId(targetPermissionIds, codeToIdMap, "REPORT_EXPORT");
            addPermId(targetPermissionIds, codeToIdMap, "SALES_REPORT");
        }

        // KHÓA CỨNG: Tuyệt đối không lưu quyền Hệ thống (Account, Role, Permission) cho Employee
        List<Integer> sanitizedPermissionIds = new ArrayList<>();
        for (Integer pid : targetPermissionIds) {
            for (Permission p : allPerms) {
                if (p.getPermissionId() == pid) {
                    String code = p.getPermissionCode().toUpperCase();
                    String module = p.getModuleCode() != null ? p.getModuleCode().toUpperCase() : "";
                    if (!code.startsWith("ACCOUNT") && !code.startsWith("ROLE")
                            && !code.startsWith("PERMISSION") && !code.startsWith("SYSTEM")
                            && !module.equals("SYSTEM") && !module.equals("ROLE") && !module.equals("ACCOUNT")) {
                        sanitizedPermissionIds.add(pid);
                    }
                    break;
                }
            }
        }

        permissionRepository.updateUserPermissions(userId, sanitizedPermissionIds);
        req.getSession().setAttribute("flash", "Đã lưu phân quyền cho nhân viên " + targetUser.getFullName() + " (" + targetUser.getEmail() + ") thành công!");
        resp.sendRedirect(req.getContextPath() + "/manage/admin/permissions?userId=" + userId);
    }

    private void addPermId(Set<Integer> targetSet, Map<String, Integer> map, String code) {
        Integer id = map.get(code.toUpperCase());
        if (id != null) {
            targetSet.add(id);
        }
    }
}
