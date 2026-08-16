package com.watchstore.controller.guest;

import com.watchstore.model.Product;
import com.watchstore.model.User;
import com.watchstore.repository.OrderRepository;
import com.watchstore.repository.ProductRepository;
import com.watchstore.repository.UserAccountRepository;
import com.watchstore.util.SessionCart;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@WebServlet("/page/*")
public class PageController extends HttpServlet {

    private ProductRepository products;
    private UserAccountRepository accountRepo;
    private OrderRepository orderRepository;
    private com.watchstore.repository.AddressRepository addressRepository;
    private com.watchstore.repository.WishlistRepository wishlistRepository;

    @Override
    public void init() {
        products = (ProductRepository) getServletContext().getAttribute("productRepository");
        accountRepo = new UserAccountRepository();

        orderRepository =
                (OrderRepository) getServletContext().getAttribute("orderRepository");

        if (orderRepository == null) {
            orderRepository = new OrderRepository();
        }
        
        addressRepository = (com.watchstore.repository.AddressRepository) getServletContext().getAttribute("addressRepository");
        wishlistRepository = (com.watchstore.repository.WishlistRepository) getServletContext().getAttribute("wishlistRepository");
    }

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException, IOException {

        String path = req.getPathInfo() == null
                ? "/home"
                : req.getPathInfo();

        req.setAttribute(
                "cartCount",
                SessionCart.count(req.getSession())
        );

        if (products != null) {
            req.setAttribute(
                    "featuredProducts",
                    products.findFeatured()
            );

            req.setAttribute(
                    "products",
                    products.search(req.getParameter("q"))
            );
        }

        if (orderRepository != null) {
            req.setAttribute(
                    "orders",
                    orderRepository.findAll()
            );
        }

        User current = (User) req.getSession().getAttribute("user");
        if (current != null) {
            if (addressRepository != null) {
                try {
                    req.setAttribute("addresses", addressRepository.findAll(current.getId()));
                } catch (Exception e) {
                    req.setAttribute("addresses", java.util.Collections.emptyList());
                }
            }
            if (wishlistRepository != null) {
                try {
                    req.setAttribute("wishlistProducts", wishlistRepository.findAll(current.getId()));
                } catch (Exception e) {
                    req.setAttribute("wishlistProducts", java.util.Collections.emptyList());
                }
            }
        }

        Map<String, String[]> pages = Map.ofEntries(
                Map.entry(
                        "/home",
                        new String[]{"guest/home", "Trang chủ"}
                ),
                Map.entry(
                        "/products",
                        new String[]{"guest/product-list", "Sản phẩm"}
                ),
                Map.entry(
                        "/news",
                        new String[]{"guest/news", "Tin tức"}
                ),
                Map.entry(
                        "/vouchers",
                        new String[]{"guest/voucher", "Kho voucher"}
                ),
                Map.entry(
                        "/profile",
                        new String[]{"member/profile", "Thông tin cá nhân"}
                ),
                Map.entry(
                        "/change-password",
                        new String[]{"member/change-password", "Đổi mật khẩu"}
                ),
                Map.entry(
                        "/forgot-password",
                        new String[]{"guest/forgot-password", "Quên mật khẩu"}
                ),
                Map.entry(
                        "/address",
                        new String[]{"member/address", "Địa chỉ nhận hàng"}
                ),
                Map.entry(
                        "/wishlist",
                        new String[]{"customer/wishlist", "Sản phẩm yêu thích"}
                ),
                Map.entry(
                        "/reviews",
                        new String[]{"customer/review", "Đánh giá của tôi"}
                ),
                Map.entry(
                        "/notifications",
                        new String[]{"customer/notification", "Thông báo"}
                )
        );

        if ("/product".equals(path)) {

            int id = parseInt(
                    req.getParameter("id"),
                    1
            );

            Product product = null;

            if (products != null) {
                product = products.findById(id).orElse(null);

                if (product == null) {
                    var featuredProducts = products.findFeatured();

                    if (featuredProducts != null
                            && !featuredProducts.isEmpty()) {
                        product = featuredProducts.get(0);
                    }
                }
            }

            req.setAttribute(
                    "product",
                    product
            );

            ViewRouter.customer(
                    req,
                    resp,
                    "guest/product-detail",
                    "Chi tiết sản phẩm"
            );

            return;
        }

        if ("/products".equals(path)) {
            com.watchstore.repository.ProductSearchCriteria criteria = criteria(req);
            com.watchstore.repository.ProductPage productPage = products.search(criteria);
            req.setAttribute("productPage", productPage);
            req.setAttribute("products", productPage.getItems());
            req.setAttribute("criteria", criteria);
            req.setAttribute("brands", products.findBrands());
            req.setAttribute("categories", products.findCategories());
            req.setAttribute("queryWithoutPage", criteria.toQueryStringWithoutPage());
            if ("1".equals(req.getAttribute("swappedPriceRange"))) {
                req.setAttribute("filterMessage", "Khoảng giá không hợp lệ, hệ thống đã tự động đảo lại giá trị.");
            }
            ViewRouter.customer(req, resp, "guest/product-list", "Sản phẩm");
            return;
        }

        String[] page = pages.getOrDefault(
                path,
                pages.get("/home")
        );

        if (isProtected(path)
                && req.getSession().getAttribute("user") == null) {

            resp.sendRedirect(
                    req.getContextPath()
                            + "/auth/login?required=1"
            );

            return;
        }

        ViewRouter.customer(
                req,
                resp,
                page[0],
                page[1]
        );
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        req.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo() == null
                ? "/home"
                : req.getPathInfo();

        User user =
                (User) req.getSession().getAttribute("user");

        if (user == null) {
            resp.sendRedirect(
                    req.getContextPath()
                            + "/auth/login?required=1"
            );
            return;
        }

        try {

            if ("/wishlist".equals(path)) {
                
                String productIdStr = req.getParameter("productId");
                if (productIdStr != null && !productIdStr.isBlank() && wishlistRepository != null) {
                    wishlistRepository.toggle(user.getId(), Integer.parseInt(productIdStr));
                    req.getSession().setAttribute("flash", "Đã cập nhật danh sách yêu thích.");
                }

            } else if ("/profile".equals(path)) {

                handleProfile(
                        req,
                        user
                );

            } else if ("/change-password".equals(path)) {

                handleChangePassword(
                        req,
                        user
                );

            } else if ("/address".equals(path)) {

                handleAddress(
                        req,
                        user
                );
            }

        } catch (Exception e) {

            req.getSession().setAttribute(
                    "errorMsg",
                    e.getMessage() == null
                            ? "Không thể cập nhật tài khoản."
                            : e.getMessage()
            );
        }

        resp.sendRedirect(
                req.getContextPath()
                        + "/page"
                        + path
        );
    }

    private void handleProfile(
            HttpServletRequest req,
            User user
    ) throws Exception {

        String fullName =
                req.getParameter("fullName");

        String phone =
                req.getParameter("phone");

        String gender =
                req.getParameter("gender");

        String dateOfBirth =
                req.getParameter("dateOfBirth");

        String address =
                req.getParameter("address");

        LocalDate dob =
                parseDate(dateOfBirth);

        accountRepo.updateProfile(
                user.getId(),
                fullName,
                phone,
                gender,
                dob
        );

        User updatedUser =
                accountRepo.findById(
                        user.getId()
                );

        if (updatedUser != null) {
            if (address != null && !address.isBlank()) {
                updatedUser.setAddress(address.trim());
            }
            req.getSession().setAttribute(
                    "user",
                    updatedUser
            );
        }

        req.getSession().setAttribute(
                "flash",
                "Cập nhật thông tin cá nhân thành công!"
        );
    }

    private com.watchstore.repository.ProductSearchCriteria criteria(HttpServletRequest req) {
        com.watchstore.repository.ProductSearchCriteria c = new com.watchstore.repository.ProductSearchCriteria();
        c.setKeyword(req.getParameter("q"));
        c.setBrand(req.getParameter("brand"));
        c.setSort(req.getParameter("sort") == null ? "newest" : req.getParameter("sort"));
        c.setInStockOnly("1".equals(req.getParameter("inStock")));
        try { c.setPage(Integer.parseInt(req.getParameter("page"))); } catch (Exception e) { c.setPage(1); }
        String p = req.getParameter("price");
        if (p != null) {
            String[] parts = p.split("-");
            try {
                if (parts.length > 0 && !parts[0].isBlank()) c.setMinPrice(new java.math.BigDecimal(parts[0]));
                if (parts.length > 1 && !parts[1].isBlank()) c.setMaxPrice(new java.math.BigDecimal(parts[1]));
            } catch (Exception ignored) {}
        }
        if (c.getMinPrice() != null && c.getMaxPrice() != null && c.getMinPrice().compareTo(c.getMaxPrice()) > 0) {
            java.math.BigDecimal t = c.getMinPrice(); c.setMinPrice(c.getMaxPrice()); c.setMaxPrice(t);
            req.setAttribute("swappedPriceRange", "1");
        }
        return c;
    }

    private void handleChangePassword(
            HttpServletRequest req,
            User user
    ) throws Exception {

        String currentPassword =
                req.getParameter("currentPassword");

        if (currentPassword == null) {
            currentPassword =
                    req.getParameter("oldPassword");
        }

        String newPassword =
                req.getParameter("newPassword");

        String confirmPassword =
                req.getParameter("confirmPassword");

        accountRepo.changePassword(
                user.getId(),
                currentPassword,
                newPassword,
                confirmPassword
        );

        req.getSession().setAttribute(
                "flash",
                "Đổi mật khẩu thành công."
        );
    }

    private void handleAddress(
            HttpServletRequest req,
            User user
    ) {
        try {
            String action = req.getParameter("action");
            int id = parseInt(req.getParameter("id"), 0);
            
            if ("delete".equals(action)) {
                if (id <= 0) throw new IllegalArgumentException("Địa chỉ không hợp lệ.");
                addressRepository.delete(user.getId(), id);
            } else {
                String name = req.getParameter("name");
                String phone = req.getParameter("phone");
                String province = req.getParameter("province");
                String district = req.getParameter("district");
                String ward = req.getParameter("ward");
                String line = req.getParameter("line");
                
                if (name == null || name.trim().length() < 2 || name.trim().length() > 100) 
                    throw new IllegalArgumentException("Tên người nhận không hợp lệ.");
                if (phone == null || !phone.matches("\\d{9,11}")) 
                    throw new IllegalArgumentException("Số điện thoại chỉ được nhập 9-11 chữ số.");
                if (province == null || province.isBlank() || district == null || district.isBlank() || ward == null || ward.isBlank() || line == null || line.isBlank()) 
                    throw new IllegalArgumentException("Vui lòng nhập đầy đủ địa chỉ.");
                if (line.trim().length() > 300) 
                    throw new IllegalArgumentException("Địa chỉ chi tiết quá dài.");
                
                addressRepository.save(
                        user.getId(), 
                        id == 0 ? null : id, 
                        name.trim(), 
                        phone.trim(), 
                        province.trim(), 
                        district.trim(), 
                        ward.trim(), 
                        line.trim(), 
                        req.getParameter("type"), 
                        "1".equals(req.getParameter("default")) || "default".equals(action)
                );
            }
            req.getSession().setAttribute("flash", "Đã cập nhật địa chỉ.");
        } catch (Exception e) {
            Throwable t = e; 
            while (t.getCause() != null) t = t.getCause(); 
            String msg = t.getMessage() == null ? "Thao tác không thành công." : t.getMessage();
            throw new IllegalArgumentException(msg);
        }
    }

    private boolean isProtected(
            String path
    ) {

        return path.matches(
                "/(profile|change-password|address|wishlist|reviews|notifications)"
        );
    }

    private LocalDate parseDate(
            String value
    ) {

        if (value == null
                || value.isBlank()) {
            return null;
        }

        try {

            return LocalDate.parse(
                    value
            );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Ngày sinh không hợp lệ."
            );
        }
    }

    private int parseInt(
            String value,
            int fallback
    ) {

        try {

            return Integer.parseInt(
                    value
            );

        } catch (Exception ignored) {

            return fallback;
        }
    }
}