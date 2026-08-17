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
import java.util.List;
import java.util.Map;

@WebServlet("/page/*")
public class PageController extends HttpServlet {

    private ProductRepository products;
    private UserAccountRepository accountRepo;
    private OrderRepository orderRepository;
    private com.watchstore.repository.ReviewRepository reviewRepository;

    @Override
    public void init() {
        products = (ProductRepository) getServletContext().getAttribute("productRepository");
        accountRepo = new UserAccountRepository();

        orderRepository =
                (OrderRepository) getServletContext().getAttribute("orderRepository");

        if (orderRepository == null) {
            orderRepository = new OrderRepository();
        }
        reviewRepository = (com.watchstore.repository.ReviewRepository) getServletContext().getAttribute("reviewRepository");
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
                        "/notifications",
                        new String[]{"customer/notification", "Thông báo"}
                )
        );

        if ("/reviews".equals(path)) {
            User currentUser = (User) req.getSession().getAttribute("user");
            if (currentUser == null) {
                resp.sendRedirect(req.getContextPath() + "/auth/login?required=1");
                return;
            }
            String prodIdParam = req.getParameter("productId");
            if (prodIdParam != null && !prodIdParam.trim().isEmpty()) {
                try {
                    int prodId = Integer.parseInt(prodIdParam.trim());
                    Product product = products != null ? products.findById(prodId).orElse(null) : null;
                    if (product != null && reviewRepository != null) {
                        req.setAttribute("reviewProduct", product);
                        Long orderItemId = reviewRepository.getCompletedOrderItemId(currentUser.getId(), prodId);
                        req.setAttribute("orderItemId", orderItemId);
                        req.setAttribute("hasPurchased", orderItemId != null);
                    }
                } catch (NumberFormatException ignored) {}
            }
            ViewRouter.customer(req, resp, "customer/review", "Đánh giá sản phẩm");
            return;
        }

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

            if (product != null && reviewRepository != null) {
                List<com.watchstore.model.Review> reviewsList = reviewRepository.findApprovedByProductId(id);
                double avgRating = 5.0;
                if (reviewsList != null && !reviewsList.isEmpty()) {
                    double sum = 0;
                    for (com.watchstore.model.Review r : reviewsList) {
                        sum += r.getRating();
                    }
                    avgRating = sum / reviewsList.size();
                }
                boolean hasPurchased = false;
                User currentUser = (User) req.getSession().getAttribute("user");
                if (currentUser != null) {
                    hasPurchased = reviewRepository.hasPurchased(currentUser.getId(), id);
                }
                req.setAttribute("reviewsList", reviewsList);
                req.setAttribute("reviewsCount", reviewsList != null ? reviewsList.size() : 0);
                req.setAttribute("averageRating", String.format("%.1f", avgRating));
                req.setAttribute("hasPurchased", hasPurchased);
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

        if ("/reviews/submit".equals(path)) {
            submitReview(req, resp);
            return;
        }

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

            if ("/profile".equals(path)) {

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
            updatedUser.setAddress(
                    user.getAddress()
            );

            req.getSession().setAttribute(
                    "user",
                    updatedUser
            );
        }

        req.getSession().setAttribute(
                "flash",
                "Cập nhật thông tin thành công"
        );
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

        String address =
                req.getParameter("address");

        if (address == null
                || address.isBlank()) {

            throw new IllegalArgumentException(
                    "Địa chỉ nhận hàng không được để trống."
            );
        }

        user.setAddress(
                address.trim()
        );

        req.getSession().setAttribute(
                "user",
                user
        );

        req.getSession().setAttribute(
                "flash",
                "Cập nhật địa chỉ nhận hàng thành công!"
        );
    }

    private boolean isProtected(
            String path
    ) {

        return path.matches(
                "/(profile|change-password|address|wishlist|notifications)"
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



    private void submitReview(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = (User) req.getSession().getAttribute("user");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login?required=1");
            return;
        }

        String prodIdParam = req.getParameter("productId");
        String ratingParam = req.getParameter("rating");
        String title = req.getParameter("title");
        String content = req.getParameter("content");

        if (prodIdParam == null || ratingParam == null) {
            req.getSession().setAttribute("flash", "Thiếu thông tin đánh giá.");
            resp.sendRedirect(req.getContextPath() + "/page/home");
            return;
        }

        try {
            int prodId = Integer.parseInt(prodIdParam.trim());
            int rating = Integer.parseInt(ratingParam.trim());

            if (reviewRepository != null) {
                Long orderItemId = reviewRepository.getCompletedOrderItemId(currentUser.getId(), prodId);
                if (orderItemId == null) {
                    req.getSession().setAttribute("flash", "Lỗi: Bạn chỉ được đánh giá sản phẩm sau khi đơn hàng hoàn thành!");
                    resp.sendRedirect(req.getContextPath() + "/page/product?id=" + prodId);
                    return;
                }

                com.watchstore.model.Review review = new com.watchstore.model.Review();
                review.setProductId(prodId);
                review.setOrderItemId(orderItemId);
                review.setUserId(currentUser.getId());
                review.setRating(rating);
                review.setReviewTitle(title != null ? title.trim() : "Đánh giá sản phẩm");
                review.setReviewContent(content != null ? content.trim() : "");
                review.setVerifiedPurchase(true);
                review.setStatus("PENDING");

                boolean success = reviewRepository.add(review);
                if (success) {
                    req.getSession().setAttribute("flash", "Cảm ơn bạn! Đánh giá của bạn đã được gửi và đang chờ duyệt.");
                } else {
                    req.getSession().setAttribute("flash", "Lỗi: Không thể lưu đánh giá của bạn.");
                }
            }
            resp.sendRedirect(req.getContextPath() + "/page/product?id=" + prodId);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/page/home");
        }
    }
}