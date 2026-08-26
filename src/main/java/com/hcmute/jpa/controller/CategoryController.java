package com.hcmute.jpa.controller;

import com.hcmute.jpa.entity.Category;
import com.hcmute.jpa.service.CategoryServiceImpl;
import com.hcmute.jpa.service.ICategoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/categories")
public class CategoryController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ICategoryService categoryService;

    @Override
    public void init() {
        categoryService = new CategoryServiceImpl();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        if (action == null) {
            action = "list";
        }

        try {

            switch (action) {

                case "add":
                    showAddForm(request, response);
                    break;

                case "edit":
                    showEditForm(request, response);
                    break;

                case "delete":
                    deleteCategory(request, response);
                    break;

                default:
                    listCategories(request, response);
                    break;
            }

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        if (action == null) {
            response.sendRedirect(
                    request.getContextPath() + "/categories"
            );
            return;
        }

        try {

            switch (action) {

                case "insert":
                    insertCategory(request, response);
                    break;

                case "update":
                    updateCategory(request, response);
                    break;

                default:
                    response.sendRedirect(
                            request.getContextPath() + "/categories"
                    );
                    break;
            }

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void listCategories(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        List<Category> categories =
                categoryService.findAll();

        request.setAttribute(
                "categories",
                categories
        );

        request.getRequestDispatcher(
                "/views/category-list.jsp"
        ).forward(request, response);
    }

    private void showAddForm(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.getRequestDispatcher(
                "/views/category-add.jsp"
        ).forward(request, response);
    }

    private void showEditForm(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        int id = Integer.parseInt(
                request.getParameter("id")
        );

        Category category =
                categoryService.findById(id);

        if (category == null) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/categories"
            );
            return;
        }

        request.setAttribute(
                "category",
                category
        );

        request.getRequestDispatcher(
                "/views/category-edit.jsp"
        ).forward(request, response);
    }

    private void insertCategory(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, ServletException {

        String categoryname =
                request.getParameter("categoryname");

        String images =
                request.getParameter("images");

        int status =
                Integer.parseInt(
                        request.getParameter("status")
                );

        if (categoryname == null
                || categoryname.trim().isEmpty()) {

            request.setAttribute(
                    "error",
                    "Category name cannot be empty."
            );

            request.getRequestDispatcher(
                    "/views/category-add.jsp"
            ).forward(request, response);

            return;
        }

        Category category = new Category();

        category.setCategoryname(
                categoryname.trim()
        );

        category.setImages(
                images == null
                        ? ""
                        : images.trim()
        );

        category.setStatus(status);

        categoryService.insert(category);

        response.sendRedirect(
                request.getContextPath()
                        + "/categories?message=add_success"
        );
    }

    private void updateCategory(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, ServletException {

        int id = Integer.parseInt(
                request.getParameter("categoryid")
        );

        String categoryname =
                request.getParameter("categoryname");

        String images =
                request.getParameter("images");

        int status =
                Integer.parseInt(
                        request.getParameter("status")
                );

        if (categoryname == null
                || categoryname.trim().isEmpty()) {

            Category category =
                    categoryService.findById(id);

            request.setAttribute(
                    "category",
                    category
            );

            request.setAttribute(
                    "error",
                    "Category name cannot be empty."
            );

            request.getRequestDispatcher(
                    "/views/category-edit.jsp"
            ).forward(request, response);

            return;
        }

        Category category =
                categoryService.findById(id);

        if (category == null) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/categories"
            );
            return;
        }

        category.setCategoryname(
                categoryname.trim()
        );

        category.setImages(
                images == null
                        ? ""
                        : images.trim()
        );

        category.setStatus(status);

        categoryService.update(category);

        response.sendRedirect(
                request.getContextPath()
                        + "/categories?message=update_success"
        );
    }

    private void deleteCategory(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        int id = Integer.parseInt(
                request.getParameter("id")
        );

        categoryService.delete(id);

        response.sendRedirect(
                request.getContextPath()
                        + "/categories?message=delete_success"
        );
    }
}