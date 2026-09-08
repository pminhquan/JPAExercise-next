package com.hcmute.jpa.controller;

import com.hcmute.jpa.util.UploadStorage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.logging.Logger;

@WebServlet(urlPatterns = {"/uploads/*"}, loadOnStartup = 1)
public class UploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(UploadServlet.class.getName());

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            UploadStorage.copyPackagedSeedImages(getServletContext());
        } catch (Exception e) {
            LOGGER.warning("Could not sync packaged seed images: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.trim().isEmpty() || "/".equals(pathInfo.trim())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String relativePath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (relativePath.contains("..") || relativePath.contains("\0") || relativePath.contains("\\")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Path file = UploadStorage.resolveForReading(getServletContext(), relativePath);
        if (file == null || !Files.isRegularFile(file)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mimeType = getServletContext().getMimeType(file.getFileName().toString());
        if (mimeType == null) {
            String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                mimeType = "image/jpeg";
            } else if (name.endsWith(".png")) {
                mimeType = "image/png";
            } else if (name.endsWith(".webp")) {
                mimeType = "image/webp";
            } else if (name.endsWith(".gif")) {
                mimeType = "image/gif";
            } else {
                mimeType = "application/octet-stream";
            }
        }

        response.setContentType(mimeType);
        response.setContentLengthLong(Files.size(file));
        response.setHeader("Cache-Control", "public, max-age=86400");

        try (InputStream in = Files.newInputStream(file);
             OutputStream out = response.getOutputStream()) {
            in.transferTo(out);
        }
    }
}
