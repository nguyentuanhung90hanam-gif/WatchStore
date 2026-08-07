package com.watchstore.controller.admin;

import com.watchstore.model.Category;
import com.watchstore.repository.CategoryRepository;
import com.watchstore.repository.CategoryRepositoryImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;


@WebServlet("/manage/admin/categories/*")
public class CategoryController extends HttpServlet {


    private CategoryRepository categoryRepository;


    @Override
    public void init() {

        categoryRepository = new CategoryRepositoryImpl();

    }



    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {


        String action = request.getPathInfo();


        if (action == null) {
            action = "/";
        }


        switch (action) {

            case "/add":
                showForm(request, response, null);
                break;


            case "/edit":
                editForm(request, response);
                break;


            case "/delete":
                deleteCategory(request, response);
                break;


            case "/search":
                search(request, response);
                break;


            default:
                list(request, response);
                break;
        }

    }




    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {


        request.setCharacterEncoding("UTF-8");


        String action = request.getPathInfo();


        if ("/save".equals(action)) {

            save(request, response);

        }
        else if ("/update".equals(action)) {

            update(request, response);

        }

    }






    private void list(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {


        List<Category> categories =
                categoryRepository.findAll();


        request.setAttribute(
                "categories",
                categories
        );


        request.getRequestDispatcher(
                "/views/admin/category/category-list.jsp"
        ).forward(request, response);

    }







    private void showForm(
            HttpServletRequest request,
            HttpServletResponse response,
            Category category
    ) throws ServletException, IOException {


        request.setAttribute(
                "category",
                category
        );


        request.getRequestDispatcher(
                "/views/admin/category/category-form.jsp"
        ).forward(request, response);

    }








    private void editForm(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {


        Integer id =
                Integer.parseInt(
                        request.getParameter("id")
                );


        Category category =
                categoryRepository.findById(id);


        showForm(
                request,
                response,
                category
        );

    }








    private void save(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {


        Category c = new Category();


        c.setCategoryCode(
                request.getParameter("categoryCode")
        );


        c.setCategoryName(
                request.getParameter("categoryName")
        );


        c.setSlug(
                request.getParameter("slug")
        );


        c.setDescription(
                request.getParameter("description")
        );


        c.setImage(
                request.getParameter("image")
        );


        c.setSortOrder(
                Integer.parseInt(
                        request.getParameter("sortOrder")
                )
        );


        c.setStatus("ACTIVE");


        categoryRepository.save(c);



        response.sendRedirect(
                request.getContextPath()
                        + "/manage/admin/categories"
        );

    }









    private void update(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {


        Category c = new Category();


        c.setCategoryId(
                Integer.parseInt(
                        request.getParameter("categoryId")
                )
        );


        c.setCategoryCode(
                request.getParameter("categoryCode")
        );


        c.setCategoryName(
                request.getParameter("categoryName")
        );


        c.setSlug(
                request.getParameter("slug")
        );


        c.setDescription(
                request.getParameter("description")
        );


        c.setImage(
                request.getParameter("image")
        );


        c.setSortOrder(
                Integer.parseInt(
                        request.getParameter("sortOrder")
                )
        );


        c.setStatus("ACTIVE");


        categoryRepository.update(c);



        response.sendRedirect(
                request.getContextPath()
                        + "/manage/admin/categories"
        );

    }








    private void deleteCategory(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {


        Integer id =
                Integer.parseInt(
                        request.getParameter("id")
                );


        categoryRepository.delete(id);



        response.sendRedirect(
                request.getContextPath()
                        + "/manage/admin/categories"
        );

    }








    private void search(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {


        String keyword =
                request.getParameter("keyword");



        List<Category> categories =
                categoryRepository.search(keyword);



        request.setAttribute(
                "categories",
                categories
        );



        request.getRequestDispatcher(
                "/views/admin/category/category-list.jsp"
        ).forward(request, response);

    }

}