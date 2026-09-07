package com.hcmute.jpa.controller;

import com.hcmute.jpa.entity.User;
import com.hcmute.jpa.service.IUserService;
import com.hcmute.jpa.service.UserServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@WebServlet(name = "ProfileController", urlPatterns = "/profile")
@MultipartConfig(
        fileSizeThreshold = 0,
        maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 6L * 1024 * 1024
)
public class ProfileController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String PROFILE_VIEW =
            "/WEB-INF/views/profile.jsp";

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp");

    private IUserService userService;

    public ProfileController() {
    }

    public ProfileController(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public void init() {
        if (userService == null) {
            userService = new UserServiceImpl();
        }
    }

    @Override
    public void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        Integer userId = getAuthenticatedUserId(request);

        if (userId == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        User user = userService.findById(userId);

        if (user == null) {
            clearAuthentication(request);

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        request.setAttribute("user", user);

        getServletContext()
                .getRequestDispatcher(PROFILE_VIEW)
                .forward(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        Integer userId = getAuthenticatedUserId(request);

        if (userId == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        User user = userService.findById(userId);

        if (user == null) {
            clearAuthentication(request);

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        String fullname = trim(request.getParameter("fullname"));
        String phone = trim(request.getParameter("phone"));

        /*
         * Preserve submitted text when validation fails.
         * This does NOT change the database until updateProfile succeeds.
         */
        user.setFullname(fullname);
        user.setPhone(phone);

        if (fullname != null && fullname.length() > 100) {
            forwardWithError(
                    request,
                    response,
                    user,
                    "Full name must not exceed 100 characters."
            );
            return;
        }

        if (phone != null && !phone.isEmpty()) {

            if (phone.length() > 30 || !isValidPhone(phone)) {
                forwardWithError(
                        request,
                        response,
                        user,
                        "Invalid phone number."
                );
                return;
            }
        }

        Part imagePart;

        try {

            String contentType = request.getContentType();

            if (contentType == null ||
                    !contentType.toLowerCase(Locale.ROOT)
                            .startsWith("multipart/")) {

                forwardWithError(
                        request,
                        response,
                        user,
                        "Invalid profile form submission."
                );
                return;
            }

            imagePart = request.getPart("images");

        } catch (IllegalStateException e) {

            forwardWithError(
                    request,
                    response,
                    user,
                    "Image file exceeds maximum allowed size of 5 MB."
            );
            return;

        } catch (ServletException | IOException e) {

            forwardWithError(
                    request,
                    response,
                    user,
                    "Unable to process uploaded image."
            );
            return;
        }

        String oldImage = user.getImages();
        String finalImage = oldImage;
        String newlyUploadedImage = null;

        try {

            newlyUploadedImage =
                    storeImage(request, imagePart);

            if (newlyUploadedImage != null) {
                finalImage = newlyUploadedImage;
            }

        } catch (IllegalArgumentException e) {

            forwardWithError(
                    request,
                    response,
                    user,
                    e.getMessage()
            );
            return;

        } catch (IOException e) {

            forwardWithError(
                    request,
                    response,
                    user,
                    "Unable to save uploaded image."
            );
            return;
        }

        boolean updated;

        try {

            updated = userService.updateProfile(
                    userId,
                    fullname,
                    phone,
                    finalImage
            );

        } catch (Exception e) {

            updated = false;
        }

        if (!updated) {

            if (newlyUploadedImage != null) {
                deleteStoredImage(
                        request,
                        newlyUploadedImage
                );
            }

            user.setImages(oldImage);

            forwardWithError(
                    request,
                    response,
                    user,
                    "Failed to update profile."
            );

            return;
        }

        User updatedUser = userService.findById(userId);
        if (updatedUser != null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute("account", updatedUser);
            }
        }

        response.sendRedirect(
                request.getContextPath() + "/profile"
        );
    }

    private Integer getAuthenticatedUserId(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return null;
        }

        Object value =
                session.getAttribute(
                        "authenticatedUserId"
                );

        if (!(value instanceof Number)) {
            return null;
        }

        return ((Number) value).intValue();
    }

    private void clearAuthentication(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session != null) {
            session.removeAttribute(
                    "authenticatedUserId"
            );
        }
    }

    private String trim(String value) {

        if (value == null) {
            return null;
        }

        return value.trim();
    }

    private boolean isValidPhone(String phone) {

        if (phone == null || phone.isBlank()) {
            return true;
        }

        return phone.matches(
                "^[+]?[0-9\\s\\-().]{3,30}$"
        );
    }

    private void forwardWithError(
            HttpServletRequest request,
            HttpServletResponse response,
            User user,
            String error)
            throws ServletException, IOException {

        request.setAttribute("user", user);
        request.setAttribute("error", error);

        getServletContext()
                .getRequestDispatcher(PROFILE_VIEW)
                .forward(request, response);
    }

    private String storeImage(
            HttpServletRequest request,
            Part imagePart)
            throws IOException {

        if (imagePart == null ||
                imagePart.getSize() <= 0) {

            return null;
        }

        String submittedFileName =
                imagePart.getSubmittedFileName();

        if (submittedFileName == null ||
                submittedFileName.isBlank()) {

            return null;
        }

        if (submittedFileName.contains("/") ||
                submittedFileName.contains("\\") ||
                submittedFileName.contains("..")) {

            throw new IllegalArgumentException(
                    "Invalid image filename."
            );
        }

        String safeOriginalName =
                Paths.get(submittedFileName)
                        .getFileName()
                        .toString();

        int dot =
                safeOriginalName.lastIndexOf('.');

        if (dot <= 0 ||
                dot == safeOriginalName.length() - 1) {

            throw new IllegalArgumentException(
                    "Unsupported image type."
            );
        }

        String extension =
                safeOriginalName
                        .substring(dot + 1)
                        .toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS
                .contains(extension)) {

            throw new IllegalArgumentException(
                    "Only JPG, JPEG, PNG and WEBP images are allowed."
            );
        }

        String uploadRoot =
                getServletContext()
                        .getRealPath("/uploads");

        if (uploadRoot == null) {

            throw new IOException(
                    "Upload directory is unavailable."
            );
        }

        Path uploadDirectory =
                Paths.get(uploadRoot)
                        .toAbsolutePath()
                        .normalize();

        Files.createDirectories(
                uploadDirectory
        );

        String generatedFileName =
                UUID.randomUUID()
                        + "."
                        + extension;

        Path target =
                uploadDirectory
                        .resolve(generatedFileName)
                        .normalize();

        if (!target.getParent()
                .equals(uploadDirectory)) {

            throw new IOException(
                    "Invalid upload target."
            );
        }

        try (InputStream input =
                     imagePart.getInputStream()) {

            Files.copy(input, target);
        }

        return generatedFileName;
    }

    private void deleteStoredImage(
            HttpServletRequest request,
            String fileName) {

        if (fileName == null ||
                fileName.isBlank()) {

            return;
        }

        if (fileName.contains("/") ||
                fileName.contains("\\") ||
                fileName.contains("..")) {

            return;
        }

        try {

            String uploadRoot =
                    getServletContext()
                            .getRealPath("/uploads");

            if (uploadRoot == null) {
                return;
            }

            Path uploadDirectory =
                    Paths.get(uploadRoot)
                            .toAbsolutePath()
                            .normalize();

            Path target =
                    uploadDirectory
                            .resolve(fileName)
                            .normalize();

            if (target.getParent()
                    .equals(uploadDirectory)) {

                Files.deleteIfExists(target);
            }

        } catch (IOException ignored) {
        }
    }
}