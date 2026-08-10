package com.watchstore.controller.admin;

import com.watchstore.model.Brand;
import com.watchstore.model.Category;
import com.watchstore.model.Product;
import com.watchstore.repository.BrandRepository;
import com.watchstore.repository.CategoryRepository;
import com.watchstore.repository.ProductRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet(urlPatterns = {"/manage/admin/products", "/manage/admin/products/*"})
public class ProductController extends HttpServlet {

    private ProductRepository productRepository;
    private BrandRepository brandRepository;
    private CategoryRepository categoryRepository;

    @Override
    public void init() {
        productRepository = (ProductRepository) getServletContext().getAttribute("productRepository");
        brandRepository = (BrandRepository) getServletContext().getAttribute("brandRepository");
        categoryRepository = (CategoryRepository) getServletContext().getAttribute("categoryRepository");
        if (productRepository == null) productRepository = new com.watchstore.repository.ProductRepositoryImpl();
        if (brandRepository == null) brandRepository = new com.watchstore.repository.BrandRepositoryImpl();
        if (categoryRepository == null) categoryRepository = new com.watchstore.repository.CategoryRepositoryImpl();
    }

    private void setCommonAttributes(HttpServletRequest req) {
        req.setAttribute("adminArea", "admin");
        req.setAttribute("tableKind", "products");
        req.setAttribute("pageTitle", "Quản lý sản phẩm");
        req.setAttribute("moduleTitle", "Sản phẩm");
        req.setAttribute("moduleKicker", "DANH MỤC ĐỒNG HỒ");
        req.setAttribute("moduleDescription", "Quản lý sản phẩm đồng hồ, thông số kỹ thuật, giá bán và trạng thái.");
        req.setAttribute("primaryAction", "Thêm sản phẩm");
    }

    private void forwardToList(HttpServletRequest req, HttpServletResponse resp, List<Product> products)
            throws ServletException, IOException {
        setCommonAttributes(req);
        req.setAttribute("products", products);
        req.setAttribute("contentPage", "/views/shared/management-module.jsp");
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }

    private void forwardToForm(HttpServletRequest req, HttpServletResponse resp, String pageTitle)
            throws ServletException, IOException {
        setCommonAttributes(req);
        req.setAttribute("pageTitle", pageTitle);
        req.setAttribute("allBrands", brandRepository.findAll());
        req.setAttribute("allCategories", categoryRepository.findAll());
        req.setAttribute("contentPage", "/views/admin/product-form.jsp");
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getPathInfo();
        if (action == null || action.equals("/")) {
            String keyword = req.getParameter("keyword");
            if (keyword != null && !keyword.isBlank()) {
                forwardToList(req, resp, productRepository.search(keyword));
            } else {
                forwardToList(req, resp, productRepository.findAll());
            }
            return;
        }

        switch (action) {
            case "/add": {
                forwardToForm(req, resp, "Thêm sản phẩm");
                break;
            }
            case "/edit": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        productRepository.findById(id).ifPresent(p -> req.setAttribute("product", p));
                    } catch (NumberFormatException ignored) {}
                }
                forwardToForm(req, resp, "Sửa sản phẩm");
                break;
            }
            case "/delete": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        productRepository.delete(id);
                    } catch (Exception ignored) {}
                }
                resp.sendRedirect(req.getContextPath() + "/manage/admin/products");
                break;
            }
            case "/search": {
                String keyword = req.getParameter("keyword");
                if (keyword != null && !keyword.isBlank()) {
                    forwardToList(req, resp, productRepository.search(keyword));
                } else {
                    forwardToList(req, resp, productRepository.findAll());
                }
                break;
            }
            default:
                resp.sendRedirect(req.getContextPath() + "/manage/admin/products");
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
            resp.sendRedirect(req.getContextPath() + "/manage/admin/products");
        }
    }

    private void handleSave(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String code = trim(req.getParameter("productCode"));
        String name = trim(req.getParameter("productName"));
        String slug = trim(req.getParameter("slug"));
        String brandIdStr = trim(req.getParameter("brandId"));
        String categoryIdStr = trim(req.getParameter("categoryId"));
        String priceStr = trim(req.getParameter("price"));
        String sku = trim(req.getParameter("sku"));
        String movementType = trim(req.getParameter("movementType"));
        String gender = trim(req.getParameter("gender"));
        String status = trim(req.getParameter("status"));
        String isFeaturedStr = trim(req.getParameter("isFeatured"));
        String description = trim(req.getParameter("description"));

        int brandId = parse(brandIdStr, 1);
        int categoryId = parse(categoryIdStr, 1);
        BigDecimal price = parseBigDecimal(priceStr);

        String error = validateProduct(code, name, slug, price);
        if (error == null && productRepository.existsByCode(code, null)) {
            error = "Mã sản phẩm \"" + code + "\" đã tồn tại.";
        }
        if (error == null && !slug.isEmpty() && productRepository.existsBySlug(slug, null)) {
            error = "Slug \"" + slug + "\" đã tồn tại.";
        }

        if (error != null) {
            Product draft = buildProduct(0, code, name, slug, brandId, categoryId, price, sku, movementType, gender, status, "true".equals(isFeaturedStr), description);
            showFormWithError(req, resp, draft, error, "Thêm sản phẩm");
            return;
        }

        Product product = buildProduct(0, code, name, slug, brandId, categoryId, price, sku, movementType, gender, status, "true".equals(isFeaturedStr), description);
        productRepository.insert(product);
        resp.sendRedirect(req.getContextPath() + "/manage/admin/products");
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idStr = trim(req.getParameter("productId"));
        String code = trim(req.getParameter("productCode"));
        String name = trim(req.getParameter("productName"));
        String slug = trim(req.getParameter("slug"));
        String brandIdStr = trim(req.getParameter("brandId"));
        String categoryIdStr = trim(req.getParameter("categoryId"));
        String priceStr = trim(req.getParameter("price"));
        String sku = trim(req.getParameter("sku"));
        String movementType = trim(req.getParameter("movementType"));
        String gender = trim(req.getParameter("gender"));
        String status = trim(req.getParameter("status"));
        String isFeaturedStr = trim(req.getParameter("isFeatured"));
        String description = trim(req.getParameter("description"));

        int id = parse(idStr, 0);
        int brandId = parse(brandIdStr, 1);
        int categoryId = parse(categoryIdStr, 1);
        BigDecimal price = parseBigDecimal(priceStr);

        String error = validateProduct(code, name, slug, price);
        if (error == null && productRepository.existsByCode(code, id)) {
            error = "Mã sản phẩm \"" + code + "\" đã tồn tại ở sản phẩm khác.";
        }
        if (error == null && !slug.isEmpty() && productRepository.existsBySlug(slug, id)) {
            error = "Slug \"" + slug + "\" đã tồn tại ở sản phẩm khác.";
        }

        if (error != null) {
            Product draft = buildProduct(id, code, name, slug, brandId, categoryId, price, sku, movementType, gender, status, "true".equals(isFeaturedStr), description);
            showFormWithError(req, resp, draft, error, "Sửa sản phẩm");
            return;
        }

        Product product = buildProduct(id, code, name, slug, brandId, categoryId, price, sku, movementType, gender, status, "true".equals(isFeaturedStr), description);
        productRepository.update(product);
        resp.sendRedirect(req.getContextPath() + "/manage/admin/products");
    }

    private void showFormWithError(HttpServletRequest req, HttpServletResponse resp, Product draft, String error, String pageTitle)
            throws ServletException, IOException {
        req.setAttribute("errorMessage", error);
        req.setAttribute("product", draft);
        forwardToForm(req, resp, pageTitle);
    }

    private String trim(String s) { return s == null ? "" : s.trim(); }
    private int parse(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }
    private BigDecimal parseBigDecimal(String s) { try { return new BigDecimal(s); } catch (Exception e) { return BigDecimal.ZERO; } }

    private String validateProduct(String code, String name, String slug, BigDecimal price) {
        if (code.isEmpty()) return "Mã sản phẩm không được để trống.";
        if (name.isEmpty()) return "Tên sản phẩm không được để trống.";
        if (slug.isEmpty()) return "Slug không được để trống.";
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) return "Giá bán phải lớn hơn hoặc bằng 0.";
        return null;
    }

    private Product buildProduct(int id, String code, String name, String slug, int brandId, int categoryId,
                                 BigDecimal price, String sku, String movementType, String gender, String status,
                                 boolean isFeatured, String description) {
        Product p = new Product();
        p.setProductId(id);
        p.setProductCode(code);
        p.setProductName(name);
        p.setSlug(slug);
        p.setBrandId(brandId);
        p.setCategoryId(categoryId);
        p.setPrice(price);
        p.setSku(sku.isEmpty() ? code + "-STD" : sku);
        p.setMovementType(movementType.isEmpty() ? "AUTOMATIC" : movementType);
        p.setGender(gender.isEmpty() ? "MEN" : gender);
        p.setStatus(status.isEmpty() ? "ACTIVE" : status);
        p.setIsFeatured(isFeatured);
        p.setDescription(description.isEmpty() ? null : description);
        return p;
    }
}
