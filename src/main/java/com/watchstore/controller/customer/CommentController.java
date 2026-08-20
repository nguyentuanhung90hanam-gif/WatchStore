package com.watchstore.controller.customer;

import com.watchstore.model.User;
import com.watchstore.repository.CommentRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/comments/*")
public class CommentController extends HttpServlet {

    private CommentRepository commentRepository;

    @Override
    public void init() {
        commentRepository = (CommentRepository) getServletContext().getAttribute("commentRepository");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login?required=1");
            return;
        }

        int productId = parseInt(req.getParameter("productId"), 0);
        String content = req.getParameter("content");

        try {
            if (productId <= 0) {
                throw new IllegalArgumentException("Sản phẩm không hợp lệ.");
            }
            if (content == null || content.trim().length() < 2) {
                throw new IllegalArgumentException("Nội dung bình luận phải từ 2 ký tự trở lên.");
            }
            if (content.trim().length() > 1000) {
                throw new IllegalArgumentException("Nội dung bình luận không được vượt quá 1000 ký tự.");
            }

            if (commentRepository != null) {
                commentRepository.insert(productId, user.getUserId(), content.trim());
            }

            req.getSession().setAttribute("flash", "Đã gửi bình luận thành công!");
        } catch (Exception e) {
            req.getSession().setAttribute("flash", "Lỗi: " + root(e));
        }

        resp.sendRedirect(req.getContextPath() + "/page/product?id=" + productId);
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
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
