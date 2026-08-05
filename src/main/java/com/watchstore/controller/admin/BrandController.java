package com.watchstore.controller.admin;

import com.watchstore.model.Brand;
import com.watchstore.repository.BrandRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/manage/admin/brands/*")
public class BrandController extends HttpServlet {

    private BrandRepository brandRepository;

    @Override
    public void init() {
        brandRepository = (BrandRepository)
                getServletContext().getAttribute("brandRepository");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getPathInfo();

        if (action == null || "/".equals(action)) {
            showList(req, resp, null);
            return;
        }

        switch (action) {

            case "/add":
                showForm(req, resp, "Thêm thương hiệu");
                break;

            case "/edit":
                Brand brand = brandRepository.findById(readId(req));
                if (brand == null) {
                    req.getSession().setAttribute("flashError", "Không tìm thấy thương hiệu cần chỉnh sửa.");
                    resp.sendRedirect(req.getContextPath() + "/manage/admin/brands");
                    return;
                }
                req.setAttribute("brand", brand);
                showForm(req, resp, "Cập nhật thương hiệu");
                break;

            case "/delete":
                boolean deleted = brandRepository.delete(readId(req));
                req.getSession().setAttribute(
                        deleted ? "flash" : "flashError",
                        deleted ? "Đã xóa thương hiệu." : "Không thể xóa thương hiệu đang được sử dụng."
                );
                resp.sendRedirect(req.getContextPath() + "/manage/admin/brands");
                break;

            case "/search":
                String keyword = req.getParameter("keyword") == null
                        ? ""
                        : req.getParameter("keyword").trim();
                showList(req, resp, keyword);
                break;

            default:
                resp.sendRedirect(req.getContextPath()
                        + "/manage/admin/brands");

        }

    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String id = req.getParameter("id");

        Brand brand = new Brand();

        brand.setBrandCode(req.getParameter("brandCode"));
        brand.setBrandName(req.getParameter("brandName"));
        brand.setSlug(req.getParameter("slug"));
        brand.setOriginCountry(req.getParameter("originCountry"));
        brand.setLogoUrl(req.getParameter("logoUrl"));
        brand.setDescription(req.getParameter("description"));
        brand.setStatus(req.getParameter("status"));

        boolean creating = id == null || id.isBlank();
        boolean saved;
        if (creating) {
            saved = brandRepository.insert(brand);
        } else {
            brand.setBrandID(Integer.parseInt(id));
            saved = brandRepository.update(brand);
        }

        req.getSession().setAttribute(
                saved ? "flash" : "flashError",
                saved
                        ? (creating ? "Đã thêm thương hiệu mới." : "Đã cập nhật thương hiệu.")
                        : "Không thể lưu thương hiệu. Vui lòng kiểm tra dữ liệu và thử lại."
        );
        resp.sendRedirect(req.getContextPath() + "/manage/admin/brands");

    }

    private void showList(HttpServletRequest req, HttpServletResponse resp, String keyword)
            throws ServletException, IOException {
        req.setAttribute("keyword", keyword == null ? "" : keyword);
        req.setAttribute(
                "brands",
                keyword == null || keyword.isBlank()
                        ? brandRepository.findAll()
                        : brandRepository.search(keyword)
        );
        ViewRouter.admin(req, resp, "admin/brand-list", "Quản lý thương hiệu", "admin");
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, String title)
            throws ServletException, IOException {
        ViewRouter.admin(req, resp, "admin/brand-form", title, "admin");
    }

    private int readId(HttpServletRequest req) {
        try {
            return Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Mã thương hiệu không hợp lệ.", ex);
        }

    }

}
