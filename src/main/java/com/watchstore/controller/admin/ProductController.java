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
        
        com.watchstore.model.User currentUser = (com.watchstore.model.User) req.getSession().getAttribute("user");
        if (currentUser != null && currentUser.getRole() == com.watchstore.enums.Role.SALES) {
            if ("/add".equals(action) || "/edit".equals(action) || "/delete".equals(action)) {
                req.getSession().setAttribute("errorMessage", "Bạn không có quyền thực hiện hành động này.");
                resp.sendRedirect(req.getContextPath() + "/manage/admin/products");
                return;
            }
        }

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
            case "/detail": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        productRepository.findById(id).ifPresent(p -> req.setAttribute("product", p));
                    } catch (NumberFormatException ignored) {}
                }
                setCommonAttributes(req);
                req.setAttribute("pageTitle", "Chi tiết sản phẩm");
                req.setAttribute("contentPage", "/views/admin/product-detail.jsp");
                req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
                break;
            }
            case "/delete": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        if (productRepository.isProductInUse(id)) {
                            req.getSession().setAttribute("errorMessage", "Không thể xóa sản phẩm này vì đã có dữ liệu liên quan (đơn hàng, giỏ hàng, đánh giá, kho hàng...).");
                        } else {
                            boolean deleted = productRepository.delete(id);
                            if (deleted) {
                                req.getSession().setAttribute("successMessage", "Xóa sản phẩm thành công.");
                            } else {
                                req.getSession().setAttribute("errorMessage", "Không thể xóa sản phẩm.");
                            }
                        }
                    } catch (Exception e) {
                        req.getSession().setAttribute("errorMessage", "Không thể xóa sản phẩm.");
                    }
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

        com.watchstore.model.User currentUser = (com.watchstore.model.User) req.getSession().getAttribute("user");
        if (currentUser != null && currentUser.getRole() == com.watchstore.enums.Role.SALES) {
            req.getSession().setAttribute("errorMessage", "Bạn không có quyền thực hiện hành động này.");
            resp.sendRedirect(req.getContextPath() + "/manage/admin/products");
            return;
        }

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

        int brandId = parse(brandIdStr, 0);
        int categoryId = parse(categoryIdStr, 0);
        BigDecimal price = parseBigDecimal(priceStr);

        String error = validateProduct(code, name, slug, sku, brandId, categoryId, price, movementType, gender, status);
        if (error == null && productRepository.existsByCode(code, null)) {
            error = "Mã sản phẩm \"" + code + "\" đã tồn tại.";
        }
        if (error == null && !slug.isEmpty() && productRepository.existsBySlug(slug, null)) {
            error = "Slug \"" + slug + "\" đã tồn tại.";
        }
        if (error == null) {
            String effectiveSku = !sku.isEmpty() ? sku : code + "-STD";
            if (productRepository.existsBySku(effectiveSku, null)) {
                error = "SKU đã tồn tại. Vui lòng nhập SKU khác.";
            }
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
        int brandId = parse(brandIdStr, 0);
        int categoryId = parse(categoryIdStr, 0);
        BigDecimal price = parseBigDecimal(priceStr);

        String error = validateProduct(code, name, slug, sku, brandId, categoryId, price, movementType, gender, status);
        if (error == null && productRepository.existsByCode(code, id)) {
            error = "Mã sản phẩm \"" + code + "\" đã tồn tại ở sản phẩm khác.";
        }
        if (error == null && !slug.isEmpty() && productRepository.existsBySlug(slug, id)) {
            error = "Slug \"" + slug + "\" đã tồn tại ở sản phẩm khác.";
        }
        if (error == null) {
            String effectiveSku = !sku.isEmpty() ? sku : code + "-STD";
            if (productRepository.existsBySku(effectiveSku, id)) {
                error = "SKU đã tồn tại. Vui lòng nhập SKU khác.";
            }
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

    private String validateProduct(String code, String name, String slug, String sku, int brandId, int categoryId,
                                   BigDecimal price, String movementType, String gender, String status) {
        if (code.isBlank()) return "Mã sản phẩm không được để trống.";
        if (code.length() > 50) return "Mã sản phẩm không được vượt quá 50 ký tự.";
        if (name.isBlank()) return "Tên sản phẩm không được để trống.";
        if (name.length() > 250) return "Tên sản phẩm không được vượt quá 250 ký tự.";
        if (slug.isBlank()) return "Slug không được để trống.";
        if (slug.length() > 300) return "Slug không được vượt quá 300 ký tự.";
        if (!slug.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$")) {
            return "Slug không hợp lệ (chỉ gồm chữ cái viết thường, chữ số và dấu gạch ngang).";
        }
        if (!sku.isEmpty() && sku.length() > 80) return "SKU không được vượt quá 80 ký tự.";
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) return "Giá bán phải lớn hơn 0.";
        if (brandId <= 0 || brandRepository.findById(brandId) == null) return "Thương hiệu đã chọn không tồn tại trong hệ thống.";
        if (categoryId <= 0 || categoryRepository.findById(categoryId) == null) return "Danh mục đã chọn không tồn tại trong hệ thống.";
        if (!movementType.isEmpty() && !"AUTOMATIC".equals(movementType) && !"QUARTZ".equals(movementType) && !"SOLAR".equals(movementType) && !"SMART".equals(movementType) && !"MECHANICAL".equals(movementType)) {
            return "Loại máy (Movement) không hợp lệ (chấp nhận AUTOMATIC, QUARTZ, SOLAR, SMART, MECHANICAL).";
        }
        if (!gender.isEmpty() && !"MEN".equals(gender) && !"WOMEN".equals(gender) && !"UNISEX".equals(gender) && !"COUPLE".equals(gender)) {
            return "Giới tính không hợp lệ (chấp nhận MEN, WOMEN, UNISEX, COUPLE).";
        }
        if (!status.isEmpty() && !"DRAFT".equals(status) && !"ACTIVE".equals(status) && !"INACTIVE".equals(status) && !"DISCONTINUED".equals(status)) {
            return "Trạng thái sản phẩm không hợp lệ (chấp nhận DRAFT, ACTIVE, INACTIVE, DISCONTINUED).";
        }
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
