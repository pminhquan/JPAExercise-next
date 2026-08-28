package com.hcmute.jpa.controller;

import com.hcmute.jpa.entity.Category;
import com.hcmute.jpa.entity.Product;
import com.hcmute.jpa.service.CategoryServiceImpl;
import com.hcmute.jpa.service.ICategoryService;
import com.hcmute.jpa.service.IProductService;
import com.hcmute.jpa.service.ProductServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/products", "/product", "/products/add", "/products/edit", "/products/delete", "/products/detail"})
public class ProductController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private IProductService productService;
    private ICategoryService categoryService;

    public ProductController() {
    }

    public ProductController(IProductService productService, ICategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @Override
    public void init() {
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

        String path = request.getServletPath();
        if (path == null) {
            path = "/products";
        }

        try {
            if ("/products/add".equals(path)) {
                showAddForm(request, response);
            } else if ("/products/edit".equals(path)) {
                showEditForm(request, response);
            } else if ("/products/detail".equals(path)) {
                showDetail(request, response);
            } else if ("/products".equals(path) || "/product".equals(path)) {
                listProducts(request, response);
            } else if ("/products/delete".equals(path)) {
                response.sendRedirect(request.getContextPath() + "/products");
            } else {
                response.sendRedirect(request.getContextPath() + "/products");
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/products");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();

        try {
            if ("/products/add".equals(path)) {
                insertProduct(request, response);
            } else if ("/products/edit".equals(path)) {
                updateProduct(request, response);
            } else if ("/products/delete".equals(path)) {
                deleteProduct(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/products");
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/products");
        }
    }

    private void listProducts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int limit = 6;
            int page = 1;
            String pageStr = request.getParameter("page");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                try {
                    page = Integer.parseInt(pageStr.trim());
                    if (page <= 0) {
                        page = 1;
                    }
                } catch (NumberFormatException e) {
                    page = 1;
                }
            }

            long totalProducts = productService.countAllProducts();
            int totalPages = (int) Math.ceil((double) totalProducts / limit);

            if (totalPages > 0 && page > totalPages) {
                page = totalPages;
            }

            int offset = (page - 1) * limit;
            List<Product> products = productService.getProductsPage(offset, limit);

            request.setAttribute("products", products);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalProducts", totalProducts);

            request.getRequestDispatcher("/views/product-list.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/products");
        }
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("error", "Product ID is missing.");
            request.getRequestDispatcher("/views/product-detail.jsp").forward(request, response);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("error", "Invalid Product ID format.");
            request.getRequestDispatcher("/views/product-detail.jsp").forward(request, response);
            return;
        }

        Product product = null;
        try {
            product = productService.getProductById(id);
        } catch (Exception e) {
            // handle database exception safely
        }

        if (product == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("error", "Product not found.");
            request.getRequestDispatcher("/views/product-detail.jsp").forward(request, response);
            return;
        }

        request.setAttribute("product", product);
        request.getRequestDispatcher("/views/product-detail.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Category> categories = categoryService.findAll();
            request.setAttribute("categories", categories);
            request.getRequestDispatcher("/views/product-add.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/products");
        }
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        Product product = null;
        try {
            product = productService.getProductById(id);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        if (product == null) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        try {
            List<Category> categories = categoryService.findAll();
            request.setAttribute("product", product);
            request.setAttribute("categories", categories);
            request.getRequestDispatcher("/views/product-edit.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/products");
        }
    }

    private void insertProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String productname = request.getParameter("productname");
        String priceStr = request.getParameter("price");
        String description = request.getParameter("description");
        String images = request.getParameter("images");
        String categoryidStr = request.getParameter("categoryid");
        String statusStr = request.getParameter("status");

        // Validation: non-blank name
        if (productname == null || productname.trim().isEmpty()) {
            forwardWithError(request, response, "Product name cannot be empty.", "/views/product-add.jsp");
            return;
        }

        // Validation: price valid number and > 0
        double price;
        try {
            if (priceStr == null || priceStr.trim().isEmpty()) {
                forwardWithError(request, response, "Price must be a valid number.", "/views/product-add.jsp");
                return;
            }
            price = Double.parseDouble(priceStr.trim());
            if (price <= 0 || Double.isNaN(price) || Double.isInfinite(price)) {
                forwardWithError(request, response, "Price must be greater than 0.", "/views/product-add.jsp");
                return;
            }
        } catch (NumberFormatException e) {
            forwardWithError(request, response, "Price must be a valid number.", "/views/product-add.jsp");
            return;
        }

        // Validation: category exists
        if (categoryidStr == null || categoryidStr.trim().isEmpty()) {
            forwardWithError(request, response, "Selected category does not exist.", "/views/product-add.jsp");
            return;
        }

        int categoryid;
        Category category = null;
        try {
            categoryid = Integer.parseInt(categoryidStr.trim());
            category = categoryService.findById(categoryid);
        } catch (NumberFormatException e) {
            forwardWithError(request, response, "Selected category does not exist.", "/views/product-add.jsp");
            return;
        }

        if (category == null) {
            forwardWithError(request, response, "Selected category does not exist.", "/views/product-add.jsp");
            return;
        }

        int status = 1;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                status = Integer.parseInt(statusStr.trim());
            } catch (NumberFormatException e) {
                // default status is 1
            }
        }

        Product product = new Product();
        product.setProductname(productname.trim());
        product.setPrice(price);
        product.setDescription(description == null ? "" : description.trim());
        product.setImages(images == null ? "" : images.trim());
        product.setCategory(category);
        product.setStatus(status);

        try {
            productService.createProduct(product);
            response.sendRedirect(request.getContextPath() + "/products?message=add_success");
        } catch (IllegalArgumentException e) {
            forwardWithError(request, response, e.getMessage(), "/views/product-add.jsp");
        } catch (Exception e) {
            forwardWithError(request, response, "Failed to create product due to a database error.", "/views/product-add.jsp");
        }
    }

    private void updateProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String productidStr = request.getParameter("productid");
        if (productidStr == null || productidStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        int productid;
        try {
            productid = Integer.parseInt(productidStr.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        Product existingProduct = null;
        try {
            existingProduct = productService.getProductById(productid);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        if (existingProduct == null) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        String productname = request.getParameter("productname");
        String priceStr = request.getParameter("price");
        String description = request.getParameter("description");
        String images = request.getParameter("images");
        String categoryidStr = request.getParameter("categoryid");
        String statusStr = request.getParameter("status");

        // Validation: non-blank name
        if (productname == null || productname.trim().isEmpty()) {
            request.setAttribute("product", existingProduct);
            forwardWithError(request, response, "Product name cannot be empty.", "/views/product-edit.jsp");
            return;
        }

        // Validation: price valid number and > 0
        double price;
        try {
            if (priceStr == null || priceStr.trim().isEmpty()) {
                request.setAttribute("product", existingProduct);
                forwardWithError(request, response, "Price must be a valid number.", "/views/product-edit.jsp");
                return;
            }
            price = Double.parseDouble(priceStr.trim());
            if (price <= 0 || Double.isNaN(price) || Double.isInfinite(price)) {
                request.setAttribute("product", existingProduct);
                forwardWithError(request, response, "Price must be greater than 0.", "/views/product-edit.jsp");
                return;
            }
        } catch (NumberFormatException e) {
            request.setAttribute("product", existingProduct);
            forwardWithError(request, response, "Price must be a valid number.", "/views/product-edit.jsp");
            return;
        }

        // Validation: category exists
        if (categoryidStr == null || categoryidStr.trim().isEmpty()) {
            request.setAttribute("product", existingProduct);
            forwardWithError(request, response, "Selected category does not exist.", "/views/product-edit.jsp");
            return;
        }

        int categoryid;
        Category category = null;
        try {
            categoryid = Integer.parseInt(categoryidStr.trim());
            category = categoryService.findById(categoryid);
        } catch (NumberFormatException e) {
            request.setAttribute("product", existingProduct);
            forwardWithError(request, response, "Selected category does not exist.", "/views/product-edit.jsp");
            return;
        }

        if (category == null) {
            request.setAttribute("product", existingProduct);
            forwardWithError(request, response, "Selected category does not exist.", "/views/product-edit.jsp");
            return;
        }

        int status = 1;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                status = Integer.parseInt(statusStr.trim());
            } catch (NumberFormatException e) {
                // default status is 1
            }
        }

        existingProduct.setProductname(productname.trim());
        existingProduct.setPrice(price);
        existingProduct.setDescription(description == null ? "" : description.trim());
        existingProduct.setImages(images == null ? "" : images.trim());
        existingProduct.setCategory(category);
        existingProduct.setStatus(status);

        try {
            productService.updateProduct(existingProduct);
            response.sendRedirect(request.getContextPath() + "/products?message=update_success");
        } catch (IllegalArgumentException e) {
            request.setAttribute("product", existingProduct);
            forwardWithError(request, response, e.getMessage(), "/views/product-edit.jsp");
        } catch (Exception e) {
            request.setAttribute("product", existingProduct);
            forwardWithError(request, response, "Failed to update product due to a database error.", "/views/product-edit.jsp");
        }
    }

    private void deleteProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        Product existingProduct = null;
        try {
            existingProduct = productService.getProductById(id);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        if (existingProduct == null) {
            response.sendRedirect(request.getContextPath() + "/products");
            return;
        }

        try {
            productService.deleteProduct(id);
            response.sendRedirect(request.getContextPath() + "/products?message=delete_success");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/products?error=delete_failed");
        }
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String errorMsg, String jspPath)
            throws ServletException, IOException {
        request.setAttribute("error", errorMsg);
        try {
            request.setAttribute("categories", categoryService.findAll());
        } catch (Exception ignored) {
        }
        request.getRequestDispatcher(jspPath).forward(request, response);
    }
}
