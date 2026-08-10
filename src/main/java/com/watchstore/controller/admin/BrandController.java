package com.watchstore.controller.admin;

import com.watchstore.model.Brand;
import com.watchstore.repository.BrandRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/manage/admin/brands", "/manage/admin/brands/*"})
public class BrandController extends HttpServlet {

    private BrandRepository brandRepository;

    @Override
    public void init() {
        brandRepository = (BrandRepository) getServletContext().getAttribute("brandRepository");
        if (brandRepository == null) brandRepository = new com.watchstore.repository.BrandRepositoryImpl();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void setCommonAttributes(HttpServletRequest req) {
        req.setAttribute("adminArea", "admin");
        req.setAttribute("tableKind", "brands");
        req.setAttribute("pageTitle", "Quản lý thương hiệu");
        req.setAttribute("moduleTitle", "Thương hiệu");
        req.setAttribute("moduleKicker", "THƯƠNG HIỆU ĐỒNG HỒ");
        req.setAttribute("moduleDescription", "Quản lý thương hiệu đồng hồ nổi tiếng thế giới trong hệ thống.");
        req.setAttribute("primaryAction", "Thêm thương hiệu");
    }

    private void forwardToList(HttpServletRequest req, HttpServletResponse resp,
                               List<Brand> brands) throws ServletException, IOException {
        setCommonAttributes(req);
        req.setAttribute("brands", brands);
        req.setAttribute("contentPage", "/views/shared/management-module.jsp");
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
                forwardToList(req, resp, brandRepository.search(keyword));
            } else {
                forwardToList(req, resp, brandRepository.findAll());
            }
            return;
        }

        switch (action) {

            case "/add": {
                setCommonAttributes(req);
                req.setAttribute("pageTitle", "Thêm thương hiệu");
                req.setAttribute("contentPage", "/views/admin/brand-form.jsp");
                req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
                break;
            }

            case "/edit": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        Brand brand = brandRepository.findById(id);
                        req.setAttribute("brand", brand);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                setCommonAttributes(req);
                req.setAttribute("pageTitle", "Sửa thương hiệu");
                req.setAttribute("contentPage", "/views/admin/brand-form.jsp");
                req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
                break;
            }

            case "/delete": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        brandRepository.delete(Integer.parseInt(idStr));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                resp.sendRedirect(req.getContextPath() + "/manage/admin/brands");
                break;
            }

            case "/search": {
                String keyword = req.getParameter("keyword");
                if (keyword != null && !keyword.isBlank()) {
                    forwardToList(req, resp, brandRepository.search(keyword));
                } else {
                    forwardToList(req, resp, brandRepository.findAll());
                }
                break;
            }

            default:
                forwardToList(req, resp, brandRepository.findAll());
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
            resp.sendRedirect(req.getContextPath() + "/manage/admin/brands");
        }
    }

    // ─── Save (INSERT) ────────────────────────────────────────────────────────

    private void handleSave(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String brandCode    = trim(req.getParameter("brandCode"));
        String brandName    = trim(req.getParameter("brandName"));
        String slug         = trim(req.getParameter("slug"));
        String originCountry = trim(req.getParameter("originCountry"));
        String logoUrl      = trim(req.getParameter("logoUrl"));
        String description  = trim(req.getParameter("description"));
        String status       = trim(req.getParameter("status"));

        // Validation
        String error = validateBrand(brandCode, brandName, slug, status);
        if (error == null && brandRepository.existsByCode(brandCode, null)) {
            error = "Mã thương hiệu \"" + brandCode + "\" đã tồn tại trong hệ thống.";
        }
        if (error == null && !slug.isEmpty() && brandRepository.existsBySlug(slug, null)) {
            error = "Slug \"" + slug + "\" đã tồn tại. Vui lòng dùng slug khác.";
        }

        if (error != null) {
            Brand draft = buildBrand(0, brandCode, brandName, slug, originCountry, logoUrl, description, status);
            showFormWithError(req, resp, draft, error, "Thêm thương hiệu");
            return;
        }

        Brand brand = buildBrand(0, brandCode, brandName, slug, originCountry, logoUrl, description, status);
        brandRepository.insert(brand);
        resp.sendRedirect(req.getContextPath() + "/manage/admin/brands");
    }

    // ─── Update (UPDATE) ──────────────────────────────────────────────────────

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idStr        = trim(req.getParameter("brandId"));
        String brandCode    = trim(req.getParameter("brandCode"));
        String brandName    = trim(req.getParameter("brandName"));
        String slug         = trim(req.getParameter("slug"));
        String originCountry = trim(req.getParameter("originCountry"));
        String logoUrl      = trim(req.getParameter("logoUrl"));
        String description  = trim(req.getParameter("description"));
        String status       = trim(req.getParameter("status"));

        int id = 0;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException ignored) {}

        // Validation
        String error = validateBrand(brandCode, brandName, slug, status);
        if (error == null && brandRepository.existsByCode(brandCode, id)) {
            error = "Mã thương hiệu \"" + brandCode + "\" đã được dùng bởi thương hiệu khác.";
        }
        if (error == null && !slug.isEmpty() && brandRepository.existsBySlug(slug, id)) {
            error = "Slug \"" + slug + "\" đã tồn tại. Vui lòng dùng slug khác.";
        }

        if (error != null) {
            Brand draft = buildBrand(id, brandCode, brandName, slug, originCountry, logoUrl, description, status);
            showFormWithError(req, resp, draft, error, "Sửa thương hiệu");
            return;
        }

        Brand brand = buildBrand(id, brandCode, brandName, slug, originCountry, logoUrl, description, status);
        brandRepository.update(brand);
        resp.sendRedirect(req.getContextPath() + "/manage/admin/brands");
    }

    // ─── Utilities ───────────────────────────────────────────────────────────

    private void showFormWithError(HttpServletRequest req, HttpServletResponse resp,
                                   Brand draft, String error, String pageTitle)
            throws ServletException, IOException {
        setCommonAttributes(req);
        req.setAttribute("pageTitle", pageTitle);
        req.setAttribute("errorMessage", error);
        req.setAttribute("brand", draft);
        req.setAttribute("contentPage", "/views/admin/brand-form.jsp");
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }

    private String trim(String s) {
        return (s == null) ? "" : s.trim();
    }

    private String validateBrand(String code, String name, String slug, String status) {
        if (code.isEmpty())  return "Mã thương hiệu không được để trống.";
        if (name.isEmpty())  return "Tên thương hiệu không được để trống.";
        if (slug.isEmpty())  return "Slug không được để trống.";
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            return "Trạng thái không hợp lệ (phải là ACTIVE hoặc INACTIVE).";
        }
        return null;
    }

    private Brand buildBrand(int id, String code, String name, String slug,
                              String country, String logo, String desc, String status) {
        Brand b = new Brand();
        b.setBrandID(id);
        b.setBrandCode(code);
        b.setBrandName(name);
        b.setSlug(slug);
        b.setOriginCountry(country.isEmpty() ? null : country);
        b.setLogoUrl(logo.isEmpty() ? null : logo);
        b.setDescription(desc.isEmpty() ? null : desc);
        b.setStatus(status.isEmpty() ? "ACTIVE" : status);
        return b;
    }
}