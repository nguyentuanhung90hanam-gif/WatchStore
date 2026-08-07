package com.watchstore.controller.admin;

import com.watchstore.repository.MockDataStore;
import com.watchstore.repository.ProductRepository;
import com.watchstore.util.ViewRouter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;
import com.watchstore.repository.BrandRepository;
import com.watchstore.model.Brand;
import com.watchstore.repository.UserRepository;
import com.watchstore.repository.RoleRepository;
import com.watchstore.model.Role;
import com.watchstore.repository.RoleRepositoryImpl;
import com.watchstore.model.User;
import com.watchstore.repository.UserRepositoryImpl;

@WebServlet("/manage/admin/*")
public class AdminController extends HttpServlet {
    private ProductRepository products;
    private BrandRepository brands;
    private UserRepository users;
    private RoleRepository roles;
    private static final Map<String, String[]> PAGES = Map.ofEntries(
        Map.entry("/dashboard", new String[]{"dashboard", "Bảng điều khiển"}),
        Map.entry("/accounts", new String[]{"account", "Quản lý tài khoản"}),
        Map.entry("/roles", new String[]{"role", "Vai trò"}),
        Map.entry("/permissions", new String[]{"permission", "Phân quyền"}),
        Map.entry("/categories", new String[]{"category", "Danh mục"}),
        Map.entry("/brands", new String[]{"brand", "Thương hiệu"}),
        Map.entry("/products", new String[]{"product", "Sản phẩm"}),
        Map.entry("/vouchers", new String[]{"voucher", "Voucher"}),
        Map.entry("/banners", new String[]{"banner", "Banner"}),
        Map.entry("/posts", new String[]{"post", "Bài viết"}),
        Map.entry("/notifications", new String[]{"notification", "Thông báo"}),
        Map.entry("/statistics", new String[]{"statistic", "Thống kê"}),
        Map.entry("/reports", new String[]{"report", "Báo cáo"})
    );

    @Override
    public void init() {

        products =
                (ProductRepository)
                        getServletContext()
                                .getAttribute("productRepository");


        brands =
                (BrandRepository)
                        getServletContext()
                                .getAttribute("brandRepository");


        users =
                (UserRepository)
                        getServletContext()
                                .getAttribute("userRepository");
        roles =
                (RoleRepository)
                        getServletContext()
                                .getAttribute("roleRepository");
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {


        String path = req.getPathInfo() == null
                ? "/dashboard"
                : req.getPathInfo();
        // ROLE ACTION

        if ("/roles/delete".equals(path)) {

            int id = Integer.parseInt(req.getParameter("id"));

            roles.delete(id);

            resp.sendRedirect(
                    req.getContextPath() + "/manage/admin/roles"
            );

            return;
        }


        if ("/roles/add".equals(path)) {

            ViewRouter.admin(
                    req,
                    resp,
                    "admin/role-form",
                    "Thêm vai trò",
                    "admin"
            );

            return;
        }


        if ("/roles/edit".equals(path)) {

            int id = Integer.parseInt(req.getParameter("id"));

            req.setAttribute(
                    "role",
                    roles.findById(id)
            );


            ViewRouter.admin(
                    req,
                    resp,
                    "admin/role-form",
                    "Sửa vai trò",
                    "admin"
            );

            return;
        }
        // ACCOUNT ACTION

        if ("/accounts/delete".equals(path)) {

            int id = Integer.parseInt(req.getParameter("id"));

            users.delete(id);

            resp.sendRedirect(
                    req.getContextPath() + "/manage/admin/accounts"
            );

            return;
        }

        if ("/accounts/add".equals(path)) {

            ViewRouter.admin(
                    req,
                    resp,
                    "admin/account-form",
                    "Thêm tài khoản",
                    "admin"
            );

            return;
        }

        if ("/accounts/edit".equals(path)) {

            int id = Integer.parseInt(req.getParameter("id"));

            req.setAttribute(
                    "user",
                    users.findById(id)
            );

            ViewRouter.admin(
                    req,
                    resp,
                    "admin/account-form",
                    "Sửa tài khoản",
                    "admin"
            );

            return;
        }


        String[] page = PAGES.getOrDefault(
                path,
                PAGES.get("/dashboard")
        );


        // BRAND
        if ("/brands".equals(path)) {

            String keyword = req.getParameter("keyword");


            if (keyword != null && !keyword.isBlank()) {

                req.setAttribute(
                        "brands",
                        brands.search(keyword)
                );

            } else {

                req.setAttribute(
                        "brands",
                        brands.findAll()
                );

            }

        }
        // ACCOUNT

        if ("/accounts".equals(path)) {

            String keyword = req.getParameter("keyword");


            if(keyword != null && !keyword.isBlank()) {

                req.setAttribute(
                        "users",
                        users.search(keyword)
                );

            } else {

                req.setAttribute(
                        "users",
                        users.findAll()
                );

            }

        }
        // ROLE

        if ("/roles".equals(path)) {

            String keyword = req.getParameter("keyword");

            if (keyword != null && !keyword.isBlank()) {

                req.setAttribute(
                        "roles",
                        roles.search(keyword)
                );

            } else {

                req.setAttribute(
                        "roles",
                        roles.findAll()
                );

            }

        }


        // PRODUCT

        if ("/products".equals(path)) {

            String keyword = req.getParameter("keyword");


            if (keyword != null && !keyword.isBlank()) {

                req.setAttribute(
                        "products",
                        products.search(keyword)
                );

            } else {

                req.setAttribute(
                        "products",
                        products.findAll()
                );

            }

        }



        req.setAttribute(
                "orders",
                MockDataStore.orders()
        );


        // dữ liệu chung cho management-module.jsp

        req.setAttribute(
                "moduleTitle",
                page[1]
        );


        req.setAttribute(
                "moduleKicker",
                "WATCHSTORE ADMIN"
        );


        req.setAttribute(
                "moduleDescription",
                "Quản lý dữ liệu trong hệ thống"
        );


        req.setAttribute(
                "primaryAction",
                "Thêm mới"
        );



        // xác định bảng hiển thị

        switch (page[0]) {


            case "product":

                req.setAttribute(
                        "tableKind",
                        "products"
                );

                break;


            case "permission":

                req.setAttribute(
                        "tableKind",
                        "permissions"
                );

                break;


            case "brand":

                req.setAttribute(
                        "tableKind",
                        "brands"
                );

                break;


            case "account":

                req.setAttribute(
                        "tableKind",
                        "accounts"
                );

                break;


            case "role":

                req.setAttribute(
                        "tableKind",
                        "roles"
                );

                break;


            default:

                req.setAttribute(
                        "tableKind",
                        ""
                );

        }



        ViewRouter.admin(
                req,
                resp,
                "admin/" + page[0],
                page[1],
                "admin"
        );

    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {


        String path = req.getPathInfo();
        if ("/accounts/save".equals(path)) {

            String id = req.getParameter("id");

            User user;

            if (id != null && !id.isBlank()) {

                user = new User(
                        Integer.parseInt(id),
                        "",
                        "",
                        "",
                        com.watchstore.enums.Role.CUSTOMER
                );

            } else {

                user = new User(
                        ((UserRepositoryImpl) users).generateId(),
                        "",
                        "",
                        "",
                        com.watchstore.enums.Role.CUSTOMER
                );

            }

            user.setFullName(req.getParameter("fullName"));
            user.setEmail(req.getParameter("email"));
            user.setPhone(req.getParameter("phone"));

            user.setRole(
                    com.watchstore.enums.Role.valueOf(
                            req.getParameter("role")
                    )
            );

            if (id != null && !id.isBlank()) {

                users.update(user);

            } else {

                users.save(user);

            }

            resp.sendRedirect(
                    req.getContextPath() + "/manage/admin/accounts"
            );

            return;
        }

        if ("/roles/save".equals(path)) {


            String id = req.getParameter("id");


            Role role = new Role();


            // Nếu sửa
            if(id != null && !id.isBlank()) {

                role.setId(Integer.parseInt(id));

            }
            // Nếu thêm mới
            else {

                role.setId(
                        ((RoleRepositoryImpl) roles).generateId()
                );

            }


            role.setCode(
                    req.getParameter("code")
            );


            role.setName(
                    req.getParameter("name")
            );


            role.setUserCount(
                    Integer.parseInt(
                            req.getParameter("userCount")
                    )
            );


            role.setStatus(
                    Boolean.parseBoolean(
                            req.getParameter("status")
                    )
            );


            // cập nhật
            if(id != null && !id.isBlank()) {

                roles.update(role);

            }
            // thêm mới
            else {

                roles.save(role);

            }


            resp.sendRedirect(
                    req.getContextPath()
                            + "/manage/admin/roles"
            );

            return;

        }

    }
}
