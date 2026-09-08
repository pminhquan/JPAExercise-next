package com.hcmute.jpa.controller;

import com.hcmute.jpa.entity.Category;
import com.hcmute.jpa.entity.Product;
import com.hcmute.jpa.service.CategoryServiceImpl;
import com.hcmute.jpa.service.ICategoryService;
import com.hcmute.jpa.service.IProductService;
import com.hcmute.jpa.service.IUserService;
import com.hcmute.jpa.service.ProductServiceImpl;
import com.hcmute.jpa.service.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/admin/dashboard"})
public class AdminDashboardController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_RECENT_PRODUCTS_LIMIT = 5;

    private IUserService userService;
    private IProductService productService;
    private ICategoryService categoryService;

    public AdminDashboardController() {
    }

    public AdminDashboardController(IUserService userService, IProductService productService, ICategoryService categoryService) {
        this.userService = userService;
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @Override
    public void init() {
        if (userService == null) {
            userService = new UserServiceImpl();
        }
        if (productService == null) {
            productService = new ProductServiceImpl();
        }
        if (categoryService == null) {
            categoryService = new CategoryServiceImpl();
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        long totalUsers = userService.countAllUsers();
        long totalProducts = productService.countAllProducts();
        List<Category> categories = categoryService.findAll();
        long totalCategories = categories != null ? categories.size() : 0L;
        List<Product> recentProducts = productService.getNewestProducts(DEFAULT_RECENT_PRODUCTS_LIMIT);

        request.setAttribute("totalUsers", totalUsers);
        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("totalCategories", totalCategories);
        request.setAttribute("recentProducts", recentProducts);

        request.getRequestDispatcher("/WEB-INF/views/admin-dashboard.jsp").forward(request, response);
    }
}
