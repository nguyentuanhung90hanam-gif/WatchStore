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

        String error = validate(bannerName, title, imageUrl);
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

        String error = validate(bannerName, title, imageUrl);
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

    private String validate(String name, String title, String img) {
        if (name.isEmpty()) return "Tên banner không được để trống.";
        if (title.isEmpty()) return "Tiêu đề không được để trống.";
        if (img.isEmpty()) return "Đường dẫn hình ảnh không được để trống.";
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
