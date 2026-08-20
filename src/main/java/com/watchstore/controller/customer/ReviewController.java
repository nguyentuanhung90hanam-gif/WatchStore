package com.watchstore.controller.customer;

import com.watchstore.model.Review;
import com.watchstore.model.User;
import com.watchstore.repository.ReviewRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/reviews/*")
public class ReviewController extends HttpServlet {

    private ReviewRepository reviewRepository;

    @Override
    public void init() {
        reviewRepository = (ReviewRepository) getServletContext().getAttribute("reviewRepository");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login?required=1");
            return;
        }

        req.setAttribute("myReviews", reviewRepository != null ? reviewRepository.findByUserId(user.getUserId()) : java.util.Collections.emptyList());
        req.setAttribute("pendingReviewItems", reviewRepository != null ? reviewRepository.findPendingReviewItems(user.getUserId()) : java.util.Collections.emptyList());
        ViewRouter.customer(req, resp, "customer/review", "Đánh giá của tôi");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login?required=1");
            return;
        }

        String path = req.getPathInfo() == null ? "/add" : req.getPathInfo();
        String returnUrl = req.getParameter("returnUrl");

        try {
            if ("/add".equals(path)) {
                int productId = parseInt(req.getParameter("productId"), 0);
                long orderId = parseLong(req.getParameter("orderId"), 0L);
                int rating = parseInt(req.getParameter("rating"), 5);
                String title = req.getParameter("title");
                String content = req.getParameter("content");

                if (productId <= 0 || orderId <= 0) {
                    throw new IllegalArgumentException("Thông tin sản phẩm hoặc đơn hàng đánh giá không hợp lệ.");
                }
                if (rating < 1 || rating > 5) {
                    throw new IllegalArgumentException("Số sao đánh giá phải từ 1 đến 5.");
                }
                if (content == null || content.trim().length() < 3) {
                    throw new IllegalArgumentException("Vui lòng nhập nội dung đánh giá từ 3 ký tự trở lên.");
                }

                Review review = new Review();
                review.setProductId(productId);
                review.setOrderId(orderId);
                review.setUserId(user.getUserId());
                review.setRating(rating);
                review.setTitle(title);
                review.setContent(content);
                review.setStatus("APPROVED");

                if (reviewRepository != null) {
                    reviewRepository.createReview(review);
                }

                req.getSession().setAttribute("flash", "Gửi đánh giá thành công! Cảm ơn bạn đã đóng góp ý kiến.");
            }
        } catch (Exception e) {
            req.getSession().setAttribute("flash", "Lỗi: " + root(e));
        }

        if (returnUrl != null && !returnUrl.isBlank() && !returnUrl.contains("\n") && !returnUrl.contains("\r")) {
            resp.sendRedirect(returnUrl);
        } else {
            resp.sendRedirect(req.getContextPath() + "/page/reviews");
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String root(Exception e) {
        Throwable t = e;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() == null ? "Thao tác không thành công." : t.getMessage();
    }
}
