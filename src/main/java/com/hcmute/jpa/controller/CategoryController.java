package com.hcmute.jpa.controller;

import com.hcmute.jpa.entity.Category;
import com.hcmute.jpa.service.CategoryServiceImpl;
import com.hcmute.jpa.service.ICategoryService;
import com.hcmute.jpa.util.UploadStorage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

@MultipartConfig(
        fileSizeThreshold = 0,
        maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 6L * 1024 * 1024
)
@WebServlet("/categories")
public class CategoryController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

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

    private String storeImage(HttpServletRequest request) throws IOException, ServletException {
        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("multipart/form-data")) {
            return null;
        }

        Part imagePart = request.getPart("image");
        if (imagePart == null) {
            imagePart = request.getPart("images");
        }

        if (imagePart == null) {
            return null;
        }

        String submittedFileName = imagePart.getSubmittedFileName();
        if (submittedFileName == null || submittedFileName.trim().isEmpty()) {
            return null;
        }
        if (imagePart.getSize() <= 0) {
            throw new IllegalArgumentException("Image file cannot be empty.");
        }
        if (submittedFileName.contains("/") || submittedFileName.contains("\\") || submittedFileName.contains("..")) {
            throw new IllegalArgumentException("Invalid image filename.");
        }

        String fileName = Paths.get(submittedFileName).getFileName().toString();
        if (!fileName.equals(submittedFileName)) {
            throw new IllegalArgumentException("Invalid image filename.");
        }

        int dot = fileName.lastIndexOf('.');
        if (dot <= 0 || dot == fileName.length() - 1) {
            throw new IllegalArgumentException("Unsupported image type.");
        }
        String extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Only JPG, JPEG, PNG and WEBP images are allowed.");
        }

        String generatedFileName = UUID.randomUUID() + "." + extension;
        String targetPath = "categories/" + generatedFileName;
        try (InputStream input = imagePart.getInputStream()) {
            return UploadStorage.storeFile(request.getServletContext(), input, "categories", generatedFileName);
        } catch (IOException | RuntimeException e) {
            deleteStoredImage(request, targetPath);
            throw e;
        }
    }

    private void deleteStoredImage(HttpServletRequest request, String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        String trimmed = fileName.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return;
        }
        if (!trimmed.startsWith("categories/")) {
            return;
        }
        if (trimmed.contains("..") || trimmed.contains("\0") || trimmed.contains("\\")) {
            return;
        }
        UploadStorage.deleteFile(request.getServletContext(), trimmed);
    }

    private void insertCategory(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, ServletException {

        String categoryname = request.getParameter("categoryname");
        String statusStr = request.getParameter("status");

        int status;
        try {
            status = Integer.parseInt(statusStr);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid status value.");
            request.getRequestDispatcher("/views/category-add.jsp").forward(request, response);
            return;
        }

        if (categoryname == null || categoryname.trim().isEmpty()) {
            request.setAttribute("error", "Category name cannot be empty.");
            request.getRequestDispatcher("/views/category-add.jsp").forward(request, response);
            return;
        }

        if (categoryname.trim().length() > 100) {
            request.setAttribute("error", "Category name must not exceed 100 characters.");
            request.getRequestDispatcher("/views/category-add.jsp").forward(request, response);
            return;
        }

        String storedImage = null;
        try {
            storedImage = storeImage(request);
        } catch (IllegalStateException e) {
            request.setAttribute("error", "Image file exceeds maximum allowed size of 5 MB.");
            request.getRequestDispatcher("/views/category-add.jsp").forward(request, response);
            return;
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/views/category-add.jsp").forward(request, response);
            return;
        } catch (IOException | ServletException e) {
            request.setAttribute("error", "Unable to upload image file.");
            request.getRequestDispatcher("/views/category-add.jsp").forward(request, response);
            return;
        }

        String images;
        if (storedImage != null) {
            images = storedImage;
        } else {
            String imagesParam = request.getParameter("images");
            if (imagesParam == null || imagesParam.isBlank()) {
                imagesParam = request.getParameter("image");
            }
            if (imagesParam != null && !imagesParam.trim().isEmpty()) {
                String trimmedImages = imagesParam.trim();
                if (trimmedImages.length() > 500) {
                    request.setAttribute("error", "Image path must not exceed 500 characters.");
                    request.getRequestDispatcher("/views/category-add.jsp").forward(request, response);
                    return;
                }
                if (trimmedImages.contains("..")) {
                    request.setAttribute("error", "Invalid image reference.");
                    request.getRequestDispatcher("/views/category-add.jsp").forward(request, response);
                    return;
                }
                images = trimmedImages;
            } else {
                images = "";
            }
        }

        Category category = new Category();
        category.setCategoryname(categoryname.trim());
        category.setImages(images);
        category.setStatus(status);

        boolean duplicateFound = false;
        try {
            synchronized (CATEGORY_WRITE_LOCK) {
                Category existingCategory = categoryService.findByName(categoryname.trim());
                if (existingCategory != null) {
                    duplicateFound = true;
                } else {
                    categoryService.insert(category);
                }
            }
        } catch (IllegalArgumentException e) {
            deleteStoredImage(request, storedImage);
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/views/category-add.jsp").forward(request, response);
            return;
        } catch (Exception e) {
            deleteStoredImage(request, storedImage);
            request.setAttribute("error", "Failed to add category due to a database error.");
            request.getRequestDispatcher("/views/category-add.jsp").forward(request, response);
            return;
        }

        if (duplicateFound) {
            deleteStoredImage(request, storedImage);
            request.setAttribute("error", "Category name already exists.");
            request.getRequestDispatcher("/views/category-add.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/categories?message=add_success");
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
            response.sendRedirect(request.getContextPath() + "/categories");
            return;
        }

        String categoryname = request.getParameter("categoryname");
        String statusStr = request.getParameter("status");

        int status;
        try {
            status = Integer.parseInt(statusStr);
        } catch (NumberFormatException e) {
            Category category = categoryService.findById(id);
            request.setAttribute("category", category);
            request.setAttribute("error", "Invalid status value.");
            request.getRequestDispatcher("/views/category-edit.jsp").forward(request, response);
            return;
        }

        if (categoryname == null || categoryname.trim().isEmpty()) {
            Category category = categoryService.findById(id);
            request.setAttribute("category", category);
            request.setAttribute("error", "Category name cannot be empty.");
            request.getRequestDispatcher("/views/category-edit.jsp").forward(request, response);
            return;
        }

        if (categoryname.trim().length() > 100) {
            Category category = categoryService.findById(id);
            request.setAttribute("category", category);
            request.setAttribute("error", "Category name must not exceed 100 characters.");
            request.getRequestDispatcher("/views/category-edit.jsp").forward(request, response);
            return;
        }

        String storedImage = null;
        try {
            storedImage = storeImage(request);
        } catch (IllegalStateException e) {
            Category category = categoryService.findById(id);
            request.setAttribute("category", category);
            request.setAttribute("error", "Image file exceeds maximum allowed size of 5 MB.");
            request.getRequestDispatcher("/views/category-edit.jsp").forward(request, response);
            return;
        } catch (IllegalArgumentException e) {
            Category category = categoryService.findById(id);
            request.setAttribute("category", category);
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/views/category-edit.jsp").forward(request, response);
            return;
        } catch (IOException | ServletException e) {
            Category category = categoryService.findById(id);
            request.setAttribute("category", category);
            request.setAttribute("error", "Unable to upload image file.");
            request.getRequestDispatcher("/views/category-edit.jsp").forward(request, response);
            return;
        }

        String finalImage;
        if (storedImage != null) {
            finalImage = storedImage;
        } else {
            String imagesParam = request.getParameter("images");
            if (imagesParam != null && !imagesParam.trim().isEmpty()) {
                String trimmedImages = imagesParam.trim();
                if (trimmedImages.length() > 500) {
                    Category category = categoryService.findById(id);
                    request.setAttribute("category", category);
                    request.setAttribute("error", "Image path must not exceed 500 characters.");
                    request.getRequestDispatcher("/views/category-edit.jsp").forward(request, response);
                    return;
                }
                if (trimmedImages.contains("..")) {
                    Category category = categoryService.findById(id);
                    request.setAttribute("category", category);
                    request.setAttribute("error", "Invalid image reference.");
                    request.getRequestDispatcher("/views/category-edit.jsp").forward(request, response);
                    return;
                }
                finalImage = trimmedImages;
            } else {
                // Retain existing image when edit upload is empty
                Category category = categoryService.findById(id);
                finalImage = (category != null && category.getImages() != null) ? category.getImages() : "";
            }
        }

        boolean duplicateFound = false;
        boolean notFound = false;
        String oldImageToDelete = null;

        try {
            synchronized (CATEGORY_WRITE_LOCK) {
                Category dupCheck = categoryService.findByName(categoryname.trim());
                if (dupCheck != null && dupCheck.getCategoryid() != id) {
                    duplicateFound = true;
                } else {
                    Category existingCategory = categoryService.findById(id);
                    if (existingCategory == null) {
                        notFound = true;
                    } else {
                        String oldImage = existingCategory.getImages();
                        existingCategory.setCategoryname(categoryname.trim());
                        existingCategory.setImages(finalImage);
                        existingCategory.setStatus(status);
                        categoryService.update(existingCategory);

                        if (storedImage != null && oldImage != null && !oldImage.isBlank() && !oldImage.equals(storedImage)) {
                            oldImageToDelete = oldImage;
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            deleteStoredImage(request, storedImage);
            Category category = categoryService.findById(id);
            request.setAttribute("category", category);
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/views/category-edit.jsp").forward(request, response);
            return;
        } catch (Exception e) {
            deleteStoredImage(request, storedImage);
            Category category = categoryService.findById(id);
            request.setAttribute("category", category);
            request.setAttribute("error", "Failed to update category due to a database error.");
            request.getRequestDispatcher("/views/category-edit.jsp").forward(request, response);
            return;
        }

        if (duplicateFound) {
            deleteStoredImage(request, storedImage);
            Category category = categoryService.findById(id);
            request.setAttribute("category", category);
            request.setAttribute("error", "Category name already exists.");
            request.getRequestDispatcher("/views/category-edit.jsp").forward(request, response);
            return;
        }

        if (notFound) {
            deleteStoredImage(request, storedImage);
            response.sendRedirect(request.getContextPath() + "/categories");
            return;
        }

        if (oldImageToDelete != null) {
            deleteStoredImage(request, oldImageToDelete);
        }

        response.sendRedirect(request.getContextPath() + "/categories?message=update_success");
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