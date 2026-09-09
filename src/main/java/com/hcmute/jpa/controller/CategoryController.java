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

    // ponytail: Process-local write lock ensures atomic name-uniqueness check and write in a single JVM instance; multi-node deployments require a database-level UNIQUE constraint on CategoryName or distributed locking.
    private static final Object CATEGORY_WRITE_LOCK = new Object();

    private ICategoryService categoryService;

    public CategoryController() {
    }

    public CategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void init() {
        if (categoryService == null) {
            categoryService = new CategoryServiceImpl();
        }
    }

    @Override
    public void doGet(
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
                    response.sendRedirect(
                            request.getContextPath() + "/categories"
                    );
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
    public void doPost(
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

                case "delete":
                    deleteCategory(request, response);
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

        String idStr = request.getParameter("id");
        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(
                    request.getContextPath() + "/categories"
            );
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

        String statusStr = request.getParameter("status");
        int status;
        try {
            status = Integer.parseInt(statusStr);
        } catch (NumberFormatException e) {
            request.setAttribute(
                    "error",
                    "Invalid status value."
            );

            request.getRequestDispatcher(
                    "/views/category-add.jsp"
            ).forward(request, response);
            return;
        }

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

        if (categoryname.trim().length() > 100) {

            request.setAttribute(
                    "error",
                    "Category name must not exceed 100 characters."
            );

            request.getRequestDispatcher(
                    "/views/category-add.jsp"
            ).forward(request, response);

            return;
        }

        if (images != null && !images.trim().isEmpty()) {
            String trimmedImages = images.trim();
            if (trimmedImages.length() > 500) {
                request.setAttribute(
                        "error",
                        "Image path must not exceed 500 characters."
                );
                request.getRequestDispatcher(
                        "/views/category-add.jsp"
                ).forward(request, response);
                return;
            }
            if (trimmedImages.contains("..")) {
                request.setAttribute(
                        "error",
                        "Invalid image reference."
                );
                request.getRequestDispatcher(
                        "/views/category-add.jsp"
                ).forward(request, response);
                return;
            }
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

        boolean duplicateFound = false;
        synchronized (CATEGORY_WRITE_LOCK) {
            Category existingCategory = categoryService.findByName(categoryname.trim());
            if (existingCategory != null) {
                duplicateFound = true;
            } else {
                categoryService.insert(category);
            }
        }

        if (duplicateFound) {
            request.setAttribute(
                    "error",
                    "Category name already exists."
            );

            request.getRequestDispatcher(
                    "/views/category-add.jsp"
            ).forward(request, response);

            return;
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/categories?message=add_success"
        );
    }

    private void updateCategory(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, ServletException {

        String idStr = request.getParameter("categoryid");
        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(
                    request.getContextPath() + "/categories"
            );
            return;
        }

        String categoryname =
                request.getParameter("categoryname");

        String images =
                request.getParameter("images");

        String statusStr = request.getParameter("status");
        int status;
        try {
            status = Integer.parseInt(statusStr);
        } catch (NumberFormatException e) {
            Category category =
                    categoryService.findById(id);

            request.setAttribute(
                    "category",
                    category
            );

            request.setAttribute(
                    "error",
                    "Invalid status value."
            );

            request.getRequestDispatcher(
                    "/views/category-edit.jsp"
            ).forward(request, response);
            return;
        }

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

        if (categoryname.trim().length() > 100) {

            Category category =
                    categoryService.findById(id);

            request.setAttribute(
                    "category",
                    category
            );

            request.setAttribute(
                    "error",
                    "Category name must not exceed 100 characters."
            );

            request.getRequestDispatcher(
                    "/views/category-edit.jsp"
            ).forward(request, response);

            return;
        }

        if (images != null && !images.trim().isEmpty()) {
            String trimmedImages = images.trim();
            if (trimmedImages.length() > 500) {
                Category category =
                        categoryService.findById(id);
                request.setAttribute(
                        "category",
                        category
                );
                request.setAttribute(
                        "error",
                        "Image path must not exceed 500 characters."
                );
                request.getRequestDispatcher(
                        "/views/category-edit.jsp"
                ).forward(request, response);
                return;
            }
            if (trimmedImages.contains("..")) {
                Category category =
                        categoryService.findById(id);
                request.setAttribute(
                        "category",
                        category
                );
                request.setAttribute(
                        "error",
                        "Invalid image reference."
                );
                request.getRequestDispatcher(
                        "/views/category-edit.jsp"
                ).forward(request, response);
                return;
            }
        }

        boolean duplicateFound = false;
        boolean notFound = false;
        synchronized (CATEGORY_WRITE_LOCK) {
            Category existingCategory = categoryService.findByName(categoryname.trim());
            if (existingCategory != null && existingCategory.getCategoryid() != id) {
                duplicateFound = true;
            } else {
                Category category =
                        categoryService.findById(id);

                if (category == null) {
                    notFound = true;
                } else {
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
                }
            }
        }

        if (duplicateFound) {
            Category category =
                    categoryService.findById(id);

            request.setAttribute(
                    "category",
                    category
            );

            request.setAttribute(
                    "error",
                    "Category name already exists."
            );

            request.getRequestDispatcher(
                    "/views/category-edit.jsp"
            ).forward(request, response);

            return;
        }

        if (notFound) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/categories"
            );
            return;
        }

        response.sendRedirect(
                request.getContextPath()
                        + "/categories?message=update_success"
        );
    }

    private void deleteCategory(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            response.sendRedirect(
                    request.getContextPath() + "/categories?error=invalid_id"
            );
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(
                    request.getContextPath() + "/categories?error=invalid_id"
            );
            return;
        }

        Category category;
        try {
            category = categoryService.findById(id);
        } catch (Exception e) {
            response.sendRedirect(
                    request.getContextPath() + "/categories?error=delete_failed"
            );
            return;
        }

        if (category == null) {
            response.sendRedirect(
                    request.getContextPath() + "/categories?error=not_found"
            );
            return;
        }

        try {
            if (categoryService.isCategoryInUse(id)) {
                response.sendRedirect(
                        request.getContextPath() + "/categories?error=in_use"
                );
                return;
            }
        } catch (Exception e) {
            response.sendRedirect(
                    request.getContextPath() + "/categories?error=delete_failed"
            );
            return;
        }

        try {
            boolean success = categoryService.delete(id);
            if (success) {
                response.sendRedirect(
                        request.getContextPath() + "/categories?message=delete_success"
                );
            } else {
                response.sendRedirect(
                        request.getContextPath() + "/categories?error=not_found"
                );
            }
        } catch (Exception e) {
            response.sendRedirect(
                    request.getContextPath() + "/categories?error=delete_failed"
            );
        }
    }
}