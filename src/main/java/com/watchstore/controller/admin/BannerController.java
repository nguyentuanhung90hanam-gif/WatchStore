package com.watchstore.controller.admin;

import com.watchstore.model.Banner;
import com.watchstore.repository.BannerRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/manage/admin/banners", "/manage/admin/banners/*"})
public class BannerController extends HttpServlet {

    private BannerRepository bannerRepository;

    @Override
    public void init() {
        bannerRepository = (BannerRepository) getServletContext().getAttribute("bannerRepository");
        if (bannerRepository == null) {
            bannerRepository = new com.watchstore.repository.BannerRepositoryImpl();
        }
    }

    private void setCommonAttributes(HttpServletRequest req) {
        req.setAttribute("adminArea", "admin");
        req.setAttribute("tableKind", "banners");
        req.setAttribute("pageTitle", "Quản lý banner");
        req.setAttribute("moduleTitle", "Banner quảng cáo");
        req.setAttribute("moduleKicker", "BANNER & QUẢNG CÁO");
        req.setAttribute("moduleDescription", "Quản lý các banner hiển thị trên website, vị trí và trạng thái.");
        req.setAttribute("primaryAction", "Thêm banner");
    }

    private void forwardToList(HttpServletRequest req, HttpServletResponse resp, List<Banner> banners)
            throws ServletException, IOException {
        setCommonAttributes(req);
        req.setAttribute("banners", banners);
        req.setAttribute("contentPage", "/views/shared/management-module.jsp");
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }

    private void forwardToForm(HttpServletRequest req, HttpServletResponse resp, String pageTitle)
            throws ServletException, IOException {
        setCommonAttributes(req);
        req.setAttribute("pageTitle", pageTitle);
        req.setAttribute("contentPage", "/views/admin/banner-form.jsp");
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getPathInfo();
        if (action == null || action.equals("/")) {
            String keyword = req.getParameter("keyword");
            if (keyword != null && !keyword.isBlank()) {
                forwardToList(req, resp, bannerRepository.search(keyword));
            } else {
                forwardToList(req, resp, bannerRepository.findAll());
            }
            return;
        }

        switch (action) {
            case "/add": {
                forwardToForm(req, resp, "Thêm banner");
                break;
            }
            case "/edit": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        Banner banner = bannerRepository.findById(id);
                        if (banner != null) req.setAttribute("banner", banner);
                    } catch (NumberFormatException ignored) {}
                }
                forwardToForm(req, resp, "Sửa banner");
                break;
            }
            case "/delete": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        bannerRepository.delete(id);
                    } catch (Exception ignored) {}
                }
                resp.sendRedirect(req.getContextPath() + "/manage/admin/banners");
                break;
            }
            case "/search": {
                String keyword = req.getParameter("keyword");
                if (keyword != null && !keyword.isBlank()) {
                    forwardToList(req, resp, bannerRepository.search(keyword));
                } else {
                    forwardToList(req, resp, bannerRepository.findAll());
                }
                break;
            }
            default:
                resp.sendRedirect(req.getContextPath() + "/manage/admin/banners");
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
            resp.sendRedirect(req.getContextPath() + "/manage/admin/banners");
        }
    }

    private void handleSave(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String bannerName = trim(req.getParameter("bannerName"));
        String title = trim(req.getParameter("title"));
        String subtitle = trim(req.getParameter("subtitle"));
        String imageUrl = trim(req.getParameter("imageUrl"));
        String targetUrl = trim(req.getParameter("targetUrl"));
        String positionCode = trim(req.getParameter("positionCode"));
        String orderStr = trim(req.getParameter("displayOrder"));
        String status = trim(req.getParameter("status"));

        String error = validate(bannerName, title, subtitle, imageUrl, targetUrl, positionCode, orderStr, status);
        if (error != null) {
            Banner draft = build(0, bannerName, title, subtitle, imageUrl, targetUrl, positionCode, parse(orderStr, 0), status);
            req.setAttribute("errorMessage", error);
            req.setAttribute("banner", draft);
            forwardToForm(req, resp, "Thêm banner");
            return;
        }

        Banner banner = build(0, bannerName, title, subtitle, imageUrl, targetUrl, positionCode, parse(orderStr, 0), status);
        bannerRepository.insert(banner);
        resp.sendRedirect(req.getContextPath() + "/manage/admin/banners");
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idStr = trim(req.getParameter("bannerId"));
        String bannerName = trim(req.getParameter("bannerName"));
        String title = trim(req.getParameter("title"));
        String subtitle = trim(req.getParameter("subtitle"));
        String imageUrl = trim(req.getParameter("imageUrl"));
        String targetUrl = trim(req.getParameter("targetUrl"));
        String positionCode = trim(req.getParameter("positionCode"));
        String orderStr = trim(req.getParameter("displayOrder"));
        String status = trim(req.getParameter("status"));

        int id = parse(idStr, 0);

        String error = validate(bannerName, title, subtitle, imageUrl, targetUrl, positionCode, orderStr, status);
        if (error != null) {
            Banner draft = build(id, bannerName, title, subtitle, imageUrl, targetUrl, positionCode, parse(orderStr, 0), status);
            req.setAttribute("errorMessage", error);
            req.setAttribute("banner", draft);
            forwardToForm(req, resp, "Sửa banner");
            return;
        }

        Banner banner = build(id, bannerName, title, subtitle, imageUrl, targetUrl, positionCode, parse(orderStr, 0), status);
        bannerRepository.update(banner);
        resp.sendRedirect(req.getContextPath() + "/manage/admin/banners");
    }

    private String trim(String s) { return s == null ? "" : s.trim(); }
    private int parse(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }

    private String validate(String name, String title, String subtitle, String img, String targetUrl, String positionCode, String orderStr, String status) {
        if (name.isBlank()) return "Tên banner không được để trống.";
        if (name.length() > 150) return "Tên banner không được vượt quá 150 ký tự.";
        if (title.isBlank()) return "Tiêu đề không được để trống.";
        if (title.length() > 250) return "Tiêu đề không được vượt quá 250 ký tự.";
        if (!subtitle.isEmpty() && subtitle.length() > 500) return "Tiêu đề phụ không được vượt quá 500 ký tự.";
        if (img.isBlank()) return "Đường dẫn hình ảnh không được để trống.";
        if (img.length() > 500) return "Đường dẫn hình ảnh không được vượt quá 500 ký tự.";
        if (!targetUrl.isEmpty() && targetUrl.length() > 500) return "Link liên kết (target URL) không được vượt quá 500 ký tự.";
        if (!positionCode.isEmpty() && positionCode.length() > 40) return "Mã vị trí không được vượt quá 40 ký tự.";
        if (!orderStr.isEmpty()) {
            try {
                int order = Integer.parseInt(orderStr);
                if (order < 0) {
                    return "Thứ tự hiển thị không được là số âm.";
                }
            } catch (NumberFormatException e) {
                return "Thứ tự hiển thị phải là số nguyên.";
            }
        }
        if (!status.isEmpty() && !"DRAFT".equals(status) && !"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            return "Trạng thái không hợp lệ (chấp nhận DRAFT, ACTIVE, INACTIVE).";
        }
        return null;
    }

    private Banner build(int id, String name, String title, String subtitle, String img, String target, String pos, int order, String status) {
        Banner b = new Banner();
        b.setBannerId(id);
        b.setBannerName(name);
        b.setTitle(title);
        b.setSubtitle(subtitle.isEmpty() ? null : subtitle);
        b.setImageUrl(img);
        b.setTargetUrl(target.isEmpty() ? null : target);
        b.setPositionCode(pos.isEmpty() ? "HOME_HERO" : pos);
        b.setDisplayOrder(order);
        b.setStatus(status.isEmpty() ? "ACTIVE" : status);
        return b;
    }
}
