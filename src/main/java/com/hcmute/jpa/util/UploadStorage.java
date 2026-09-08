package com.hcmute.jpa.util;

import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public final class UploadStorage {

    private static final Logger LOGGER = Logger.getLogger(UploadStorage.class.getName());

    public static final Set<String> ALLOWED_EXTENSIONS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "webp"))
    );

    private UploadStorage() {
    }

    public static Path getUploadRoot(ServletContext servletContext) {
        String customDir = System.getProperty("app.upload.dir");
        if (customDir == null || customDir.isBlank()) {
            customDir = System.getenv("APP_UPLOAD_DIR");
        }
        if (customDir != null && !customDir.isBlank()) {
            Path path = Paths.get(customDir.trim()).toAbsolutePath().normalize();
            ensureDirectoryExists(path);
            return path;
        }

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            Path path = Paths.get(catalinaBase, "uploads").toAbsolutePath().normalize();
            ensureDirectoryExists(path);
            return path;
        }

        String catalinaHome = System.getProperty("catalina.home");
        if (catalinaHome != null && !catalinaHome.isBlank()) {
            Path path = Paths.get(catalinaHome, "uploads").toAbsolutePath().normalize();
            ensureDirectoryExists(path);
            return path;
        }

        if (servletContext != null) {
            String realPath = servletContext.getRealPath("/uploads");
            if (realPath != null && !realPath.isBlank()) {
                Path path = Paths.get(realPath).toAbsolutePath().normalize();
                ensureDirectoryExists(path);
                return path;
            }
        }

        Path fallback = Paths.get(System.getProperty("user.home"), ".jpaexercise", "uploads").toAbsolutePath().normalize();
        ensureDirectoryExists(fallback);
        return fallback;
    }

    private static void ensureDirectoryExists(Path dir) {
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            LOGGER.warning("Could not create upload directory: " + dir + " (" + e.getMessage() + ")");
        }
    }

    public static String storeFile(ServletContext servletContext, InputStream inputStream, String subfolder, String fileName) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null.");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be empty.");
        }
        if (fileName.contains("/") || fileName.contains("\\") || fileName.contains("..") || fileName.contains("\0")) {
            throw new IllegalArgumentException("Invalid image filename.");
        }

        Path uploadRoot = getUploadRoot(servletContext);
        Path targetDir = (subfolder != null && !subfolder.isBlank()) ? uploadRoot.resolve(subfolder).normalize() : uploadRoot;
        if (!targetDir.startsWith(uploadRoot)) {
            throw new IOException("Upload target directory is invalid.");
        }
        Files.createDirectories(targetDir);

        Path targetFile = targetDir.resolve(fileName).normalize();
        if (!targetFile.getParent().equals(targetDir)) {
            throw new IOException("Upload target file path is invalid.");
        }

        Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);

        return (subfolder != null && !subfolder.isBlank()) ? (subfolder + "/" + fileName) : fileName;
    }

    public static boolean deleteFile(ServletContext servletContext, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return false;
        }
        if (relativePath.contains("..") || relativePath.contains("\0") || relativePath.startsWith("/") || relativePath.startsWith("\\")) {
            return false;
        }

        try {
            Path uploadRoot = getUploadRoot(servletContext);
            Path target = uploadRoot.resolve(relativePath).normalize();
            if (target.startsWith(uploadRoot) && Files.exists(target)) {
                Files.deleteIfExists(target);
                return true;
            }

            // Also check subfolders if relativePath was passed as a flat filename
            if (!relativePath.contains("/") && !relativePath.contains("\\")) {
                for (String sub : Arrays.asList("products", "avatars", "categories")) {
                    Path subTarget = uploadRoot.resolve(sub).resolve(relativePath).normalize();
                    if (subTarget.startsWith(uploadRoot) && Files.exists(subTarget)) {
                        Files.deleteIfExists(subTarget);
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warning("Unable to remove uploaded image: " + relativePath + " (" + e.getMessage() + ")");
        }
        return false;
    }

    public static Path resolveForReading(ServletContext servletContext, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        if (relativePath.contains("..") || relativePath.contains("\0") || relativePath.startsWith("/") || relativePath.startsWith("\\")) {
            return null;
        }

        Path uploadRoot = getUploadRoot(servletContext);
        Path direct = uploadRoot.resolve(relativePath).normalize();
        if (direct.startsWith(uploadRoot) && Files.isRegularFile(direct)) {
            return direct;
        }

        // Check subfolders if flat name was requested
        if (!relativePath.contains("/") && !relativePath.contains("\\")) {
            for (String sub : Arrays.asList("products", "avatars", "categories")) {
                Path subPath = uploadRoot.resolve(sub).resolve(relativePath).normalize();
                if (subPath.startsWith(uploadRoot) && Files.isRegularFile(subPath)) {
                    return subPath;
                }
            }
        }

        // Check flat in uploadRoot if subfolder was requested
        if (relativePath.contains("/") || relativePath.contains("\\")) {
            Path fileNameOnly = Paths.get(relativePath).getFileName();
            if (fileNameOnly != null) {
                Path flatUpload = uploadRoot.resolve(fileNameOnly).normalize();
                if (flatUpload.startsWith(uploadRoot) && Files.isRegularFile(flatUpload)) {
                    return flatUpload;
                }
            }
        }

        // Fallback to packaged WAR webapp uploads
        if (servletContext != null) {
            String packagedPath = servletContext.getRealPath("/uploads");
            if (packagedPath != null) {
                Path packagedRoot = Paths.get(packagedPath).toAbsolutePath().normalize();
                Path packagedFile = packagedRoot.resolve(relativePath).normalize();
                if (packagedFile.startsWith(packagedRoot) && Files.isRegularFile(packagedFile)) {
                    return packagedFile;
                }
                if (!relativePath.contains("/") && !relativePath.contains("\\")) {
                    for (String sub : Arrays.asList("products", "avatars", "categories")) {
                        Path subPath = packagedRoot.resolve(sub).resolve(relativePath).normalize();
                        if (subPath.startsWith(packagedRoot) && Files.isRegularFile(subPath)) {
                            return subPath;
                        }
                    }
                }
                if (relativePath.contains("/") || relativePath.contains("\\")) {
                    Path fileNameOnly = Paths.get(relativePath).getFileName();
                    if (fileNameOnly != null) {
                        Path flatPackaged = packagedRoot.resolve(fileNameOnly).normalize();
                        if (flatPackaged.startsWith(packagedRoot) && Files.isRegularFile(flatPackaged)) {
                            return flatPackaged;
                        }
                    }
                }
            }
        }

        return null;
    }

    public static int copyPackagedSeedImages(ServletContext servletContext) {
        if (servletContext == null) {
            return 0;
        }
        String packagedPath = servletContext.getRealPath("/uploads");
        if (packagedPath == null) {
            return 0;
        }
        Path packagedRoot = Paths.get(packagedPath).toAbsolutePath().normalize();
        Path uploadRoot = getUploadRoot(servletContext);
        if (!Files.isDirectory(packagedRoot) || packagedRoot.equals(uploadRoot) || uploadRoot.startsWith(packagedRoot)) {
            return 0;
        }

        int copied = 0;
        try (java.util.stream.Stream<Path> stream = Files.walk(packagedRoot)) {
            List<Path> files = stream.filter(Files::isRegularFile).toList();
            for (Path file : files) {
                Path relative = packagedRoot.relativize(file);
                Path dest = uploadRoot.resolve(relative).normalize();
                if (dest.startsWith(uploadRoot) && !Files.exists(dest)) {
                    Path parent = dest.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                    try {
                        Files.copy(file, dest);
                        copied++;
                    } catch (IOException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warning("Could not read packaged uploads directory: " + e.getMessage());
        }
        return copied;
    }
}
