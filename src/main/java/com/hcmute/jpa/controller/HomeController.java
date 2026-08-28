package com.hcmute.jpa.controller;

import com.hcmute.jpa.entity.Product;
import com.hcmute.jpa.service.IProductService;
import com.hcmute.jpa.service.ProductServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/home", "/"})
public class HomeController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private IProductService productService;

    public HomeController() {
    }

    public HomeController(IProductService productService) {
        this.productService = productService;
    }

    @Override
    public void init() {
        if (productService == null) {
            productService = new ProductServiceImpl();
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        List<Product> newestProducts = productService.getNewestProducts(10);
        request.setAttribute("newestProducts", newestProducts);

        request.getRequestDispatcher("/views/home.jsp").forward(request, response);
    }
}
