package com.watchstore.controller.admin;

import com.watchstore.model.Notification;
import com.watchstore.model.User;
import com.watchstore.repository.NotificationRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/manage/admin/notifications", "/manage/admin/notifications/*"})
public class NotificationController extends HttpServlet {

    private NotificationRepository notificationRepository;

    @Override
    public void init() {
        notificationRepository = (NotificationRepository) getServletContext().getAttribute("notificationRepository");
        if (notificationRepository == null) {
            notificationRepository = new com.watchstore.repository.NotificationRepositoryImpl();
        }
    }

    private void setCommonAttributes(HttpServletRequest req) {
        req.setAttribute("adminArea", "admin");
        req.setAttribute("tableKind", "notifications");
        req.setAttribute("pageTitle", "Quản lý thông báo");
        req.setAttribute("moduleTitle", "Thông báo hệ thống");
        req.setAttribute("moduleKicker", "THÔNG BÁO & CẢNH BÁO");
        req.setAttribute("moduleDescription", "Quản lý và gửi thông báo hệ thống đến người dùng.");
        req.setAttribute("primaryAction", "Tạo thông báo");
    }

    private void forwardToList(HttpServletRequest req, HttpServletResponse resp, List<Notification> notifications)
            throws ServletException, IOException {
        setCommonAttributes(req);
        req.setAttribute("notifications", notifications);
        req.setAttribute("contentPage", "/views/shared/management-module.jsp");
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }

    private void forwardToForm(HttpServletRequest req, HttpServletResponse resp, String pageTitle)
            throws ServletException, IOException {
        setCommonAttributes(req);
        req.setAttribute("pageTitle", pageTitle);
        req.setAttribute("contentPage", "/views/admin/notification-form.jsp");
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getPathInfo();
        if (action == null || action.equals("/")) {
            String keyword = req.getParameter("keyword");
            if (keyword != null && !keyword.isBlank()) {
                forwardToList(req, resp, notificationRepository.search(keyword));
            } else {
                forwardToList(req, resp, notificationRepository.findAll());
            }
            return;
        }

        switch (action) {
            case "/add": {
                forwardToForm(req, resp, "Tạo thông báo");
                break;
            }
            case "/edit": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        long id = Long.parseLong(idStr);
                        Notification n = notificationRepository.findById(id);
                        if (n != null) req.setAttribute("notification", n);
                    } catch (NumberFormatException ignored) {}
                }
                forwardToForm(req, resp, "Sửa thông báo");
                break;
            }
            case "/delete": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        long id = Long.parseLong(idStr);
                        notificationRepository.delete(id);
                    } catch (Exception ignored) {}
                }
                resp.sendRedirect(req.getContextPath() + "/manage/admin/notifications");
                break;
            }
            case "/search": {
                String keyword = req.getParameter("keyword");
                if (keyword != null && !keyword.isBlank()) {
                    forwardToList(req, resp, notificationRepository.search(keyword));
                } else {
                    forwardToList(req, resp, notificationRepository.findAll());
                }
                break;
            }
            default:
                resp.sendRedirect(req.getContextPath() + "/manage/admin/notifications");
        }
    }

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
            resp.sendRedirect(req.getContextPath() + "/manage/admin/notifications");
        }
    }

    private void handleSave(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String title = trim(req.getParameter("title"));
        String message = trim(req.getParameter("message"));
        String notificationType = trim(req.getParameter("notificationType"));
        String targetUrl = trim(req.getParameter("targetUrl"));

        String error = validate(title, message, notificationType, targetUrl);

        User user = (User) req.getSession().getAttribute("user");
        Integer createdBy = (user != null && user.getUserId() > 0) ? user.getUserId() : null;

        if (error != null) {
            Notification draft = build(0, notificationType, title, message, targetUrl, createdBy);
            req.setAttribute("errorMessage", error);
            req.setAttribute("notification", draft);
            forwardToForm(req, resp, "Tạo thông báo");
            return;
        }

        Notification notification = build(0, notificationType, title, message, targetUrl, createdBy);
        notificationRepository.insert(notification);
        resp.sendRedirect(req.getContextPath() + "/manage/admin/notifications");
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idStr = trim(req.getParameter("notificationId"));
        String title = trim(req.getParameter("title"));
        String message = trim(req.getParameter("message"));
        String notificationType = trim(req.getParameter("notificationType"));
        String targetUrl = trim(req.getParameter("targetUrl"));

        long id = parseLong(idStr, 0);
        String error = validate(title, message, notificationType, targetUrl);

        User user = (User) req.getSession().getAttribute("user");
        Integer createdBy = (user != null && user.getUserId() > 0) ? user.getUserId() : null;

        if (error != null) {
            Notification draft = build(id, notificationType, title, message, targetUrl, createdBy);
            req.setAttribute("errorMessage", error);
            req.setAttribute("notification", draft);
            forwardToForm(req, resp, "Sửa thông báo");
            return;
        }

        Notification notification = build(id, notificationType, title, message, targetUrl, createdBy);
        notificationRepository.update(notification);
        resp.sendRedirect(req.getContextPath() + "/manage/admin/notifications");
    }

    private String trim(String s) { return s == null ? "" : s.trim(); }
    private long parseLong(String s, long def) { try { return Long.parseLong(s); } catch (Exception e) { return def; } }

    private String validate(String title, String message, String notificationType, String targetUrl) {
        if (title.isBlank()) return "Tiêu đề thông báo không được để trống.";
        if (title.length() > 250) return "Tiêu đề không được vượt quá 250 ký tự.";
        if (message.isBlank()) return "Nội dung thông báo không được để trống.";
        if (message.length() > 1500) return "Nội dung thông báo không được vượt quá 1500 ký tự.";
        if (!notificationType.isEmpty() && notificationType.length() > 40) return "Loại thông báo không được vượt quá 40 ký tự.";
        if (!targetUrl.isEmpty() && targetUrl.length() > 500) return "Link liên kết (target URL) không được vượt quá 500 ký tự.";
        return null;
    }

    private Notification build(long id, String type, String title, String message, String target, Integer createdBy) {
        Notification n = new Notification();
        n.setNotificationId(id);
        n.setNotificationType(type.isEmpty() ? "SYSTEM" : type);
        n.setTitle(title);
        n.setMessage(message);
        n.setTargetUrl(target.isEmpty() ? null : target);
        n.setCreatedBy(createdBy);
        return n;
    }
}
