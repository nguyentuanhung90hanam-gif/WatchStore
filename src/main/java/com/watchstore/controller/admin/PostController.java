package com.watchstore.controller.admin;

import com.watchstore.model.Post;
import com.watchstore.model.User;
import com.watchstore.repository.PostRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/manage/admin/posts", "/manage/admin/posts/*"})
public class PostController extends HttpServlet {

    private PostRepository postRepository;

    @Override
    public void init() {
        postRepository = (PostRepository) getServletContext().getAttribute("postRepository");
        if (postRepository == null) {
            postRepository = new com.watchstore.repository.PostRepositoryImpl();
        }
    }

    private void setCommonAttributes(HttpServletRequest req) {
        req.setAttribute("adminArea", "admin");
        req.setAttribute("tableKind", "posts");
        req.setAttribute("pageTitle", "Quản lý bài viết");
        req.setAttribute("moduleTitle", "Bài viết & Tin tức");
        req.setAttribute("moduleKicker", "NỘI DUNG & BLOG");
        req.setAttribute("moduleDescription", "Quản lý bài viết, tin tức khuyến mãi, hướng dẫn sử dụng và chính sách.");
        req.setAttribute("primaryAction", "Thêm bài viết");
    }

    private void forwardToList(HttpServletRequest req, HttpServletResponse resp, List<Post> posts)
            throws ServletException, IOException {
        setCommonAttributes(req);
        req.setAttribute("posts", posts);
        req.setAttribute("contentPage", "/views/shared/management-module.jsp");
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }

    private void forwardToForm(HttpServletRequest req, HttpServletResponse resp, String pageTitle)
            throws ServletException, IOException {
        setCommonAttributes(req);
        req.setAttribute("pageTitle", pageTitle);
        req.setAttribute("contentPage", "/views/admin/post-form.jsp");
        req.getRequestDispatcher("/views/layout/admin-layout.jsp").forward(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getPathInfo();
        if (action == null || action.equals("/")) {
            String keyword = req.getParameter("keyword");
            if (keyword != null && !keyword.isBlank()) {
                forwardToList(req, resp, postRepository.search(keyword));
            } else {
                forwardToList(req, resp, postRepository.findAll());
            }
            return;
        }

        switch (action) {
            case "/add": {
                forwardToForm(req, resp, "Thêm bài viết");
                break;
            }
            case "/edit": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        Post post = postRepository.findById(id);
                        if (post != null) req.setAttribute("post", post);
                    } catch (NumberFormatException ignored) {}
                }
                forwardToForm(req, resp, "Sửa bài viết");
                break;
            }
            case "/delete": {
                String idStr = req.getParameter("id");
                if (idStr != null && !idStr.isBlank()) {
                    try {
                        int id = Integer.parseInt(idStr);
                        postRepository.delete(id);
                    } catch (Exception ignored) {}
                }
                resp.sendRedirect(req.getContextPath() + "/manage/admin/posts");
                break;
            }
            case "/search": {
                String keyword = req.getParameter("keyword");
                if (keyword != null && !keyword.isBlank()) {
                    forwardToList(req, resp, postRepository.search(keyword));
                } else {
                    forwardToList(req, resp, postRepository.findAll());
                }
                break;
            }
            default:
                resp.sendRedirect(req.getContextPath() + "/manage/admin/posts");
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
            resp.sendRedirect(req.getContextPath() + "/manage/admin/posts");
        }
    }

    private void handleSave(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String title = trim(req.getParameter("title"));
        String slug = trim(req.getParameter("slug"));
        String postType = trim(req.getParameter("postType"));
        String summary = trim(req.getParameter("summary"));
        String content = trim(req.getParameter("content"));
        String thumbnailUrl = trim(req.getParameter("thumbnailUrl"));
        String status = trim(req.getParameter("status"));

        String error = validate(title, slug, content);
        if (error == null && postRepository.existsBySlug(slug, null)) {
            error = "Slug \"" + slug + "\" đã tồn tại. Vui lòng chọn slug khác.";
        }

        User user = (User) req.getSession().getAttribute("user");
        int authorId = (user != null && user.getUserId() > 0) ? user.getUserId() : 1;

        if (error != null) {
            Post draft = build(0, postType, title, slug, summary, content, thumbnailUrl, status, authorId);
            req.setAttribute("errorMessage", error);
            req.setAttribute("post", draft);
            forwardToForm(req, resp, "Thêm bài viết");
            return;
        }

        Post post = build(0, postType, title, slug, summary, content, thumbnailUrl, status, authorId);
        postRepository.insert(post);
        resp.sendRedirect(req.getContextPath() + "/manage/admin/posts");
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idStr = trim(req.getParameter("postId"));
        String title = trim(req.getParameter("title"));
        String slug = trim(req.getParameter("slug"));
        String postType = trim(req.getParameter("postType"));
        String summary = trim(req.getParameter("summary"));
        String content = trim(req.getParameter("content"));
        String thumbnailUrl = trim(req.getParameter("thumbnailUrl"));
        String status = trim(req.getParameter("status"));

        int id = parse(idStr, 0);

        String error = validate(title, slug, content);
        if (error == null && postRepository.existsBySlug(slug, id)) {
            error = "Slug \"" + slug + "\" đã tồn tại ở bài viết khác.";
        }

        User user = (User) req.getSession().getAttribute("user");
        int authorId = (user != null && user.getUserId() > 0) ? user.getUserId() : 1;

        if (error != null) {
            Post draft = build(id, postType, title, slug, summary, content, thumbnailUrl, status, authorId);
            req.setAttribute("errorMessage", error);
            req.setAttribute("post", draft);
            forwardToForm(req, resp, "Sửa bài viết");
            return;
        }

        Post post = build(id, postType, title, slug, summary, content, thumbnailUrl, status, authorId);
        postRepository.update(post);
        resp.sendRedirect(req.getContextPath() + "/manage/admin/posts");
    }

    private String trim(String s) { return s == null ? "" : s.trim(); }
    private int parse(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }

    private String validate(String title, String slug, String content) {
        if (title.isEmpty()) return "Tiêu đề bài viết không được để trống.";
        if (slug.isEmpty()) return "Slug không được để trống.";
        if (content.isEmpty()) return "Nội dung bài viết không được để trống.";
        return null;
    }

    private Post build(int id, String type, String title, String slug, String summary, String content, String thumb, String status, int authorId) {
        Post p = new Post();
        p.setPostId(id);
        p.setPostType(type.isEmpty() ? "NEWS" : type);
        p.setTitle(title);
        p.setSlug(slug);
        p.setSummary(summary.isEmpty() ? null : summary);
        p.setContent(content);
        p.setThumbnailUrl(thumb.isEmpty() ? null : thumb);
        p.setStatus(status.isEmpty() ? "DRAFT" : status);
        p.setAuthorId(authorId);
        return p;
    }
}
