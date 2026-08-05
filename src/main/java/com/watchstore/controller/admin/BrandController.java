package com.watchstore.controller.admin;

import com.watchstore.model.Brand;
import com.watchstore.repository.BrandRepository;
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

            req.setAttribute("brands", brandRepository.findAll());

            req.setAttribute("adminArea", "admin");
            req.setAttribute("pageTitle", "Quản lý thương hiệu");
            req.setAttribute("contentPage", "/views/admin/brand-list.jsp");

            req.getRequestDispatcher("/views/layout/admin-layout.jsp")
                    .forward(req, resp);

            return;
        }

        switch (action) {

            case "/add":

                req.setAttribute("adminArea", "admin");
                req.setAttribute("pageTitle", "Thêm thương hiệu");
                req.setAttribute("contentPage", "/views/admin/brand-form.jsp");

                req.getRequestDispatcher("/views/layout/admin-layout.jsp")
                        .forward(req, resp);

                break;


            case "/edit":

                int id = Integer.parseInt(req.getParameter("id"));

                req.setAttribute("brand",
                        brandRepository.findById(id));

                req.setAttribute("adminArea", "admin");
                req.setAttribute("pageTitle", "Sửa thương hiệu");
                req.setAttribute("contentPage", "/views/admin/brand-form.jsp");

                req.getRequestDispatcher("/views/layout/admin-layout.jsp")
                        .forward(req, resp);

                break;


            case "/delete":

                brandRepository.delete(
                        Integer.parseInt(req.getParameter("id")));

                resp.sendRedirect(req.getContextPath()
                        + "/manage/admin/brands");

                break;


            case "/search":

                String keyword = req.getParameter("keyword");

                req.setAttribute("brands",
                        brandRepository.search(keyword));

                req.setAttribute("adminArea", "admin");
                req.setAttribute("pageTitle", "Quản lý thương hiệu");
                req.setAttribute("contentPage", "/views/admin/brand-list.jsp");

                req.getRequestDispatcher("/views/layout/admin-layout.jsp")
                        .forward(req, resp);

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


        if (id == null || id.isBlank()) {

            brandRepository.insert(brand);

        } else {

            brand.setBrandID(Integer.parseInt(id));

            brandRepository.update(brand);

        }


        resp.sendRedirect(req.getContextPath()
                + "/manage/admin/brands");

    }

}