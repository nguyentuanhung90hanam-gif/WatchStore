package com.watchstore.controller.admin;

import com.watchstore.model.Category;
import com.watchstore.repository.CategoryRepository;
import com.watchstore.repository.CategoryRepositoryImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/manage/admin/categories", "/manage/admin/categories/*"})
public class CategoryController extends HttpServlet {

    private CategoryRepository categoryRepository;

    @Override
    public void init() {
        categoryRepository = (CategoryRepository) getServletContext().getAttribute("categoryRepository");
        if (categoryRepository == null) categoryRepository = new CategoryRepositoryImpl();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void setCommonAttributes(HttpServletRequest req) {
        req.setAttribute("adminArea", "admin");
        req.setAttribute("tableKind", "categories");
        req.setAttribute("pageTitle", "Quản lý danh mục");
        req.setAttribute("moduleTitle", "Danh mục sản phẩm");
        req.setAttribute("moduleKicker", "PHÂN LOẠI SẢN PHẨM");
        req.setAttribute("moduleDescription", "Quản lý danh mục đồng hồ và thứ tự hiển thị.");
        req.setAttribute("primaryAction", "Thêm danh mục");
    }

    private void forwardToList(HttpServletRequest req, HttpServletResponse resp,
                               List<Category> categories) throws ServletException, IOException {
        setCommonAttributes(req);
        req.setAttribute("categories", categories);
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
                forwardToList(req, resp, categoryRepository.search(keyword));
            } else {
                forwardToList(req, resp, categoryRepository.findAll());
            }
            return;
        }

        switch (action) {
            case "/add": {
                setCommonAttributes(req);
                req.setAttribute("pageTitle", "Thêm danh mục");
                req.setAttribute("contentPage", "/views/admin/category-form.jsp");
                req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
                break;
            }
            case "/edit": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        Category category = categoryRepository.findById(id);
                        req.setAttribute("category", category);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                setCommonAttributes(req);
                req.setAttribute("pageTitle", "Sửa danh mục");
                req.setAttribute("contentPage", "/views/admin/category-form.jsp");
                req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
                break;
            }
            case "/delete": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        if (categoryRepository.isCategoryInUse(id)) {
                            req.getSession().setAttribute("errorMessage", "Không thể xóa danh mục này vì có sản phẩm thuộc danh mục.");
                        } else {
                            categoryRepository.delete(id);
                            req.getSession().setAttribute("successMessage", "Xóa danh mục thành công.");
                        }
                    } catch (Exception e) {
                        req.getSession().setAttribute("errorMessage", "Không thể xóa danh mục.");
                    }
                }
                resp.sendRedirect(req.getContextPath() + "/manage/admin/categories");
                break;
            }
            case "/search": {
                String keyword = req.getParameter("keyword");
                if (keyword != null && !keyword.isBlank()) {
                    forwardToList(req, resp, categoryRepository.search(keyword));
                } else {
                    forwardToList(req, resp, categoryRepository.findAll());
                }
                break;
            }
            default:
                forwardToList(req, resp, categoryRepository.findAll());
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
            resp.sendRedirect(req.getContextPath() + "/manage/admin/categories");
        }
    }

    // ─── Save (INSERT) ────────────────────────────────────────────────────────

    private void handleSave(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String categoryCode = trim(req.getParameter("categoryCode"));
        String categoryName = trim(req.getParameter("categoryName"));
        String slug         = trim(req.getParameter("slug"));
        String description  = trim(req.getParameter("description"));
        String imageUrl     = trim(req.getParameter("imageUrl"));
        String status       = trim(req.getParameter("status"));

        // Validation
        String error = validateCategory(categoryCode, categoryName, slug, description, imageUrl, status, null);
        if (error == null) {
            // Duplicate check
            if (categoryRepository.existsByCode(categoryCode, null)) {
                error = "Mã danh mục \"" + categoryCode + "\" đã tồn tại trong hệ thống.";
            } else if (categoryRepository.existsBySlug(slug, null)) {
                error = "Slug \"" + slug + "\" đã tồn tại. Vui lòng dùng slug khác.";
            }
        }

        if (error != null) {
            setCommonAttributes(req);
            req.setAttribute("pageTitle", "Thêm danh mục");
            req.setAttribute("errorMessage", error);
            // Preserve form data
            Category draft = buildCategory(null, categoryCode, categoryName, slug, description, imageUrl, status);
            req.setAttribute("category", draft);
            req.setAttribute("contentPage", "/views/admin/category-form.jsp");
            req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
            return;
        }

        Category c = buildCategory(null, categoryCode, categoryName, slug, description, imageUrl, status);
        categoryRepository.save(c);
        resp.sendRedirect(req.getContextPath() + "/manage/admin/categories");
    }

    // ─── Update (UPDATE) ──────────────────────────────────────────────────────

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idStr        = trim(req.getParameter("categoryId"));
        String categoryCode = trim(req.getParameter("categoryCode"));
        String categoryName = trim(req.getParameter("categoryName"));
        String slug         = trim(req.getParameter("slug"));
        String description  = trim(req.getParameter("description"));
        String imageUrl     = trim(req.getParameter("imageUrl"));
        String status       = trim(req.getParameter("status"));

        Integer id = null;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException ignored) {}

        // Validation
        String error = validateCategory(categoryCode, categoryName, slug, description, imageUrl, status, id);
        if (error == null) {
            if (categoryRepository.existsByCode(categoryCode, id)) {
                error = "Mã danh mục \"" + categoryCode + "\" đã được sử dụng bởi danh mục khác.";
            } else if (categoryRepository.existsBySlug(slug, id)) {
                error = "Slug \"" + slug + "\" đã tồn tại. Vui lòng dùng slug khác.";
            }
        }

        if (error != null) {
            setCommonAttributes(req);
            req.setAttribute("pageTitle", "Sửa danh mục");
            req.setAttribute("errorMessage", error);
            Category draft = buildCategory(id, categoryCode, categoryName, slug, description, imageUrl, status);
            req.setAttribute("category", draft);
            req.setAttribute("contentPage", "/views/admin/category-form.jsp");
            req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
            return;
        }

        Category c = buildCategory(id, categoryCode, categoryName, slug, description, imageUrl, status);
        categoryRepository.update(c);
        resp.sendRedirect(req.getContextPath() + "/manage/admin/categories");
    }

    // ─── Utility ─────────────────────────────────────────────────────────────

    private String trim(String s) {
        return (s == null) ? "" : s.trim();
    }

    private String validateCategory(String code, String name, String slug, String description, String imageUrl,
                                    String status, Integer excludeId) {
        if (code.isBlank())   return "Mã danh mục không được để trống.";
        if (code.length() > 40) return "Mã danh mục không được vượt quá 40 ký tự.";
        if (name.isBlank())   return "Tên danh mục không được để trống.";
        if (name.length() > 120) return "Tên danh mục không được vượt quá 120 ký tự.";
        if (slug.isBlank())   return "Slug không được để trống.";
        if (slug.length() > 150) return "Slug không được vượt quá 150 ký tự.";
        if (!slug.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$")) {
            return "Slug không hợp lệ (chỉ gồm chữ cái viết thường, chữ số và dấu gạch ngang, VD: dong-ho-nam).";
        }
        if (!description.isEmpty() && description.length() > 1000) return "Mô tả danh mục không được vượt quá 1000 ký tự.";
        if (!imageUrl.isEmpty() && imageUrl.length() > 500) return "URL Ảnh danh mục không được vượt quá 500 ký tự.";
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            return "Trạng thái không hợp lệ (phải là ACTIVE hoặc INACTIVE).";
        }
        return null;
    }

    private Category buildCategory(Integer id, String code, String name,
                                   String slug, String description, String imageUrl,
                                   String status) {
        Category c = new Category();
        c.setCategoryId(id);
        c.setCategoryCode(code);
        c.setCategoryName(name);
        c.setSlug(slug);
        c.setDescription(description.isEmpty() ? null : description);
        c.setImageUrl(imageUrl.isEmpty() ? null : imageUrl);
        c.setDisplayOrder(0);
        c.setStatus(status.isEmpty() ? "ACTIVE" : status);

        return c;
    }
}