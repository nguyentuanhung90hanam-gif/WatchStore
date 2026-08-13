package com.watchstore.controller.guest;

import com.watchstore.model.Product;
import com.watchstore.model.User;
import com.watchstore.repository.OrderRepository;
import com.watchstore.repository.ProductPage;
import com.watchstore.repository.ProductRepository;
import com.watchstore.repository.ProductSearchCriteria;
import com.watchstore.repository.UserRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@WebServlet("/page/*")
public class PageController extends HttpServlet {
    private ProductRepository products;
    private OrderRepository orderRepository;
    private UserRepository userRepository;
    private com.watchstore.repository.CartRepository cartRepository;
    private com.watchstore.repository.WishlistRepository wishlistRepository;
    private com.watchstore.repository.AddressRepository addressRepository;

    @Override
    public void init() {
        products = (ProductRepository) getServletContext().getAttribute("productRepository");
        orderRepository = (OrderRepository) getServletContext().getAttribute("orderRepository");
        userRepository = (UserRepository) getServletContext().getAttribute("userRepository");
        cartRepository = (com.watchstore.repository.CartRepository) getServletContext().getAttribute("cartRepository");
        wishlistRepository = (com.watchstore.repository.WishlistRepository) getServletContext().getAttribute("wishlistRepository");
        addressRepository = (com.watchstore.repository.AddressRepository) getServletContext().getAttribute("addressRepository");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo() == null ? "/home" : req.getPathInfo();
        User current = (User) req.getSession().getAttribute("user");
        try {
            req.setAttribute("cartCount", current == null ? 0 : cartRepository.count(current.getId()));
        } catch (Exception e) {
            req.setAttribute("cartCount", 0);
        }
        req.setAttribute("featuredProducts", products.findFeatured());
        req.setAttribute("orders", current == null ? Collections.emptyList() : orderRepository.findByCustomerId(current.getId()));
        if (current != null) {
            try {
                req.setAttribute("wishlistProducts", wishlistRepository.findAll(current.getId()));
                req.setAttribute("addresses", addressRepository.findAll(current.getId()));
            } catch (Exception e) {
                req.setAttribute("wishlistProducts", Collections.emptyList());
                req.setAttribute("addresses", Collections.emptyList());
            }
        }

        if (path.equals("/product")) {
            int id = parseInt(req.getParameter("id"), 1);
            Optional<Product> product = products.findById(id);
            req.setAttribute("product", product.orElseGet(() -> products.findFeatured().isEmpty() ? null : products.findFeatured().get(0)));
            ViewRouter.customer(req, resp, "guest/product-detail", "Chi tiet san pham");
            return;
        }

        if (path.equals("/products")) {
            ProductSearchCriteria criteria = criteria(req);
            ProductPage productPage = products.search(criteria);
            req.setAttribute("productPage", productPage);
            req.setAttribute("products", productPage.getItems());
            req.setAttribute("criteria", criteria);
            req.setAttribute("brands", products.findBrands());
            req.setAttribute("categories", products.findCategories());
            req.setAttribute("queryWithoutPage", criteria.toQueryStringWithoutPage());
            if ("1".equals(req.getAttribute("swappedPriceRange"))) {
                req.setAttribute("filterMessage", "Khoang gia khong hop le, he thong da tu dong dao lai gia tri.");
            }
            ViewRouter.customer(req, resp, "guest/product-list", "San pham");
            return;
        }

        Map<String, String[]> pages = Map.ofEntries(
                Map.entry("/home", new String[]{"guest/home", "Trang chu"}),
                Map.entry("/news", new String[]{"guest/news", "Tin tuc"}),
                Map.entry("/vouchers", new String[]{"guest/voucher", "Kho voucher"}),
                Map.entry("/profile", new String[]{"member/profile", "Thong tin ca nhan"}),
                Map.entry("/change-password", new String[]{"member/change-password", "Doi mat khau"}),
                Map.entry("/forgot-password", new String[]{"guest/forgot-password", "Quen mat khau"}),
                Map.entry("/address", new String[]{"member/address", "Dia chi nhan hang"}),
                Map.entry("/wishlist", new String[]{"customer/wishlist", "San pham yeu thich"}),
                Map.entry("/reviews", new String[]{"customer/review", "Danh gia cua toi"}),
                Map.entry("/notifications", new String[]{"customer/notification", "Thong bao"})
        );

        String[] page = pages.getOrDefault(path, pages.get("/home"));
        if (isProtected(path) && req.getSession().getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login?required=1");
            return;
        }
        ViewRouter.customer(req, resp, page[0], page[1]);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        User user = (User) req.getSession().getAttribute("user");

        if ("/wishlist".equals(path) && user != null) {
            try {
                wishlistRepository.toggle(user.getId(), Integer.parseInt(req.getParameter("productId")));
                req.getSession().setAttribute("flash", "Da cap nhat danh sach yeu thich.");
            } catch (Exception e) {
                req.getSession().setAttribute("flash", e.getMessage());
            }
        } else if ("/profile".equals(path) && user != null) {
            String fullName = req.getParameter("fullName");
            String phone = req.getParameter("phone");
            String address = req.getParameter("address");
            try {
                if (fullName == null || fullName.trim().length() < 2 || fullName.trim().length() > 100) {
                    throw new IllegalArgumentException("Ho ten phai tu 2 den 100 ky tu.");
                }
                if (phone != null && !phone.isBlank() && !phone.matches("\\d{9,11}")) {
                    throw new IllegalArgumentException("So dien thoai chi duoc nhap 9-11 chu so.");
                }
                if (address != null && address.length() > 500) {
                    throw new IllegalArgumentException("Dia chi khong duoc vuot qua 500 ky tu.");
                }
                user.setFullName(fullName.trim());
                if (phone != null) user.setPhone(phone.trim());
                if (address != null) user.setAddress(address.trim());
                if (userRepository != null) userRepository.update(user);
                req.getSession().setAttribute("user", user);
                req.getSession().setAttribute("flash", "Cap nhat thong tin ca nhan thanh cong.");
            } catch (Exception ex) {
                req.getSession().setAttribute("flash", "Loi: " + root(ex));
            }
        } else if ("/change-password".equals(path) && user != null) {
            changePassword(req, user);
        } else if ("/address".equals(path) && user != null) {
            saveAddress(req, user);
        }

        resp.sendRedirect(req.getContextPath() + "/page" + (path == null ? "/home" : path));
    }

    private void changePassword(HttpServletRequest req, User user) {
        String oldPass = req.getParameter("oldPassword");
        String newPass = req.getParameter("newPassword");
        String confirmPass = req.getParameter("confirmPassword");
        try {
            if (newPass == null || newPass.length() < 6 || newPass.length() > 100) {
                throw new IllegalArgumentException("Mat khau moi phai tu 6 den 100 ky tu.");
            }
            if (!newPass.equals(confirmPass)) {
                throw new IllegalArgumentException("Mat khau xac nhan khong trung khop.");
            }
            boolean success = userRepository != null && userRepository.updatePassword(user.getId(), oldPass, newPass);
            req.getSession().setAttribute("flash", success ? "Doi mat khau thanh cong." : "Loi: Mat khau hien tai khong chinh xac.");
        } catch (Exception e) {
            req.getSession().setAttribute("flash", "Loi: " + root(e));
        }
    }

    private void saveAddress(HttpServletRequest req, User user) {
        try {
            String action = req.getParameter("action");
            int id = parseInt(req.getParameter("id"), 0);
            if ("delete".equals(action)) {
                if (id <= 0) throw new IllegalArgumentException("Dia chi khong hop le.");
                addressRepository.delete(user.getId(), id);
            } else {
                String name = req.getParameter("name");
                String phone = req.getParameter("phone");
                String province = req.getParameter("province");
                String district = req.getParameter("district");
                String ward = req.getParameter("ward");
                String line = req.getParameter("line");
                if (name == null || name.trim().length() < 2 || name.trim().length() > 100) throw new IllegalArgumentException("Ten nguoi nhan khong hop le.");
                if (phone == null || !phone.matches("\\d{9,11}")) throw new IllegalArgumentException("So dien thoai chi duoc nhap 9-11 chu so.");
                if (blank(province) || blank(district) || blank(ward) || blank(line)) throw new IllegalArgumentException("Vui long nhap day du dia chi.");
                if (line.trim().length() > 300) throw new IllegalArgumentException("Dia chi chi tiet qua dai.");
                addressRepository.save(user.getId(), id == 0 ? null : id, name.trim(), phone.trim(), province.trim(), district.trim(), ward.trim(), line.trim(), req.getParameter("type"), "1".equals(req.getParameter("default")) || "default".equals(action));
            }
            req.getSession().setAttribute("flash", "Da cap nhat dia chi.");
        } catch (Exception e) {
            req.getSession().setAttribute("flash", "Loi: " + root(e));
        }
    }

    private ProductSearchCriteria criteria(HttpServletRequest req) {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setKeyword(req.getParameter("q"));
        criteria.setBrand(req.getParameter("brand"));
        criteria.setCategory(req.getParameter("category"));
        criteria.setMinPrice(parseMoney(req.getParameter("minPrice")));
        criteria.setMaxPrice(parseMoney(req.getParameter("maxPrice")));
        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null && criteria.getMinPrice().compareTo(criteria.getMaxPrice()) > 0) {
            BigDecimal min = criteria.getMinPrice();
            criteria.setMinPrice(criteria.getMaxPrice());
            criteria.setMaxPrice(min);
            req.setAttribute("swappedPriceRange", "1");
        }
        criteria.setInStockOnly("1".equals(req.getParameter("inStock")));
        criteria.setSort(req.getParameter("sort"));
        criteria.setPage(parseInt(req.getParameter("page"), 1));
        criteria.setSize(parseInt(req.getParameter("size"), 8));
        return criteria;
    }

    private BigDecimal parseMoney(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            BigDecimal money = new BigDecimal(value.trim());
            return money.signum() < 0 ? null : money;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean blank(String value) { return value == null || value.trim().isEmpty(); }
    private boolean isProtected(String path) { return path.matches("/(profile|change-password|address|wishlist|reviews|notifications)"); }
    private int parseInt(String value, int fallback) { try { return Integer.parseInt(value); } catch (Exception ignored) { return fallback; } }
    private String root(Exception e) { Throwable t = e; while (t.getCause() != null) t = t.getCause(); return t.getMessage() == null ? "Thao tac khong thanh cong." : t.getMessage(); }
}
