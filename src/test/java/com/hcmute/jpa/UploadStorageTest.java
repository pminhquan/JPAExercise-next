package com.hcmute.jpa;

import com.hcmute.jpa.controller.UploadServlet;
import com.hcmute.jpa.util.UploadStorage;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class UploadStorageTest {

    private Path tempDir;
    private Path tempCatalinaBase;
    private Path tempPackagedUploads;
    private String savedAppUploadDir;
    private String savedCatalinaBase;

    @BeforeEach
    public void setUp() throws Exception {
        tempDir = Files.createTempDirectory("jpa_upload_test_");
        tempCatalinaBase = tempDir.resolve("catalina_base");
        Files.createDirectories(tempCatalinaBase);

        tempPackagedUploads = tempDir.resolve("packaged_webapp").resolve("uploads");
        Files.createDirectories(tempPackagedUploads);

        savedAppUploadDir = System.getProperty("app.upload.dir");
        savedCatalinaBase = System.getProperty("catalina.base");

        System.clearProperty("app.upload.dir");
        System.setProperty("catalina.base", tempCatalinaBase.toAbsolutePath().toString());
    }

    @AfterEach
    public void tearDown() {
        if (savedAppUploadDir != null) {
            System.setProperty("app.upload.dir", savedAppUploadDir);
        } else {
            System.clearProperty("app.upload.dir");
        }

        if (savedCatalinaBase != null) {
            System.setProperty("catalina.base", savedCatalinaBase);
        } else {
            System.clearProperty("catalina.base");
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createMock(Class<T> interfaceType, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                handler
        );
    }

    private ServletContext createMockServletContext(Path packagedUploadsPath) {
        return createMock(ServletContext.class, (proxy, method, args) -> {
            String name = method.getName();
            if ("getRealPath".equals(name)) {
                String path = (String) args[0];
                if ("/uploads".equals(path) || "uploads".equals(path)) {
                    return packagedUploadsPath != null ? packagedUploadsPath.toAbsolutePath().toString() : null;
                }
                return null;
            } else if ("getMimeType".equals(name)) {
                String file = (String) args[0];
                if (file.endsWith(".png")) return "image/png";
                if (file.endsWith(".jpg") || file.endsWith(".jpeg")) return "image/jpeg";
                if (file.endsWith(".webp")) return "image/webp";
                return null;
            }
            return null;
        });
    }

    @Test
    public void testExternalRootResolutionDefaultToCatalinaBase() {
        ServletContext context = createMockServletContext(tempPackagedUploads);
        Path root = UploadStorage.getUploadRoot(context);
        Path expected = tempCatalinaBase.resolve("uploads").toAbsolutePath().normalize();
        assertEquals(expected, root);
        assertTrue(Files.isDirectory(root));
    }

    @Test
    public void testExternalRootResolutionCustomSystemProperty() throws Exception {
        Path customDir = tempDir.resolve("custom_external_uploads");
        System.setProperty("app.upload.dir", customDir.toAbsolutePath().toString());

        ServletContext context = createMockServletContext(tempPackagedUploads);
        Path root = UploadStorage.getUploadRoot(context);
        assertEquals(customDir.toAbsolutePath().normalize(), root);
        assertTrue(Files.isDirectory(root));
    }

    @Test
    public void testStoreFileInSubfolders() throws Exception {
        ServletContext context = createMockServletContext(tempPackagedUploads);

        byte[] productBytes = "product dummy data".getBytes(StandardCharsets.UTF_8);
        String productStored = UploadStorage.storeFile(
                context,
                new ByteArrayInputStream(productBytes),
                "products",
                "test-product.png"
        );
        assertEquals("products/test-product.png", productStored);

        Path root = UploadStorage.getUploadRoot(context);
        Path productFile = root.resolve("products").resolve("test-product.png");
        assertTrue(Files.exists(productFile));
        assertArrayEquals(productBytes, Files.readAllBytes(productFile));

        byte[] avatarBytes = "avatar dummy data".getBytes(StandardCharsets.UTF_8);
        String avatarStored = UploadStorage.storeFile(
                context,
                new ByteArrayInputStream(avatarBytes),
                "avatars",
                "test-avatar.jpg"
        );
        assertEquals("avatars/test-avatar.jpg", avatarStored);

        Path avatarFile = root.resolve("avatars").resolve("test-avatar.jpg");
        assertTrue(Files.exists(avatarFile));
        assertArrayEquals(avatarBytes, Files.readAllBytes(avatarFile));
    }

    @Test
    public void testStoreFileValidationAndPathTraversalRejection() {
        ServletContext context = createMockServletContext(tempPackagedUploads);

        assertThrows(IllegalArgumentException.class, () ->
                UploadStorage.storeFile(context, null, "products", "valid.jpg")
        );

        assertThrows(IllegalArgumentException.class, () ->
                UploadStorage.storeFile(context, new ByteArrayInputStream(new byte[1]), "products", "")
        );

        assertThrows(IllegalArgumentException.class, () ->
                UploadStorage.storeFile(context, new ByteArrayInputStream(new byte[1]), "products", "../evil.png")
        );

        assertThrows(IllegalArgumentException.class, () ->
                UploadStorage.storeFile(context, new ByteArrayInputStream(new byte[1]), "products", "sub/evil.png")
        );

        assertThrows(IllegalArgumentException.class, () ->
                UploadStorage.storeFile(context, new ByteArrayInputStream(new byte[1]), "products", "sub\\evil.png")
        );
    }

    @Test
    public void testResolveForReadingBidirectional() throws Exception {
        ServletContext context = createMockServletContext(tempPackagedUploads);

        byte[] data = "dual resolution data".getBytes(StandardCharsets.UTF_8);
        UploadStorage.storeFile(
                context,
                new ByteArrayInputStream(data),
                "products",
                "item.png"
        );

        Path bySubfolder = UploadStorage.resolveForReading(context, "products/item.png");
        assertNotNull(bySubfolder);
        assertTrue(Files.isRegularFile(bySubfolder));

        Path byFlat = UploadStorage.resolveForReading(context, "item.png");
        assertNotNull(byFlat);
        assertEquals(bySubfolder, byFlat);

        assertNull(UploadStorage.resolveForReading(context, "../etc/passwd"));
        assertNull(UploadStorage.resolveForReading(context, "/products/item.png"));
        assertNull(UploadStorage.resolveForReading(context, "nonexistent.jpg"));
    }

    @Test
    public void testResolveForReadingPackagedFallback() throws Exception {
        ServletContext context = createMockServletContext(tempPackagedUploads);

        Path seedFile = tempPackagedUploads.resolve("seed-laptop.jpg");
        Files.write(seedFile, "packaged seed image".getBytes(StandardCharsets.UTF_8));

        Path resolved = UploadStorage.resolveForReading(context, "seed-laptop.jpg");
        assertNotNull(resolved);
        assertEquals(seedFile.toAbsolutePath().normalize(), resolved);
    }

    @Test
    public void testRedeploymentSafetyPreservesUploads() throws Exception {
        ServletContext context = createMockServletContext(tempPackagedUploads);

        byte[] imageBytes = "persisted across redeploys".getBytes(StandardCharsets.UTF_8);
        UploadStorage.storeFile(
                context,
                new ByteArrayInputStream(imageBytes),
                "avatars",
                "profile-redeploy.png"
        );

        // Simulate exploded WAR deletion on undeploy/redeploy
        Files.deleteIfExists(tempPackagedUploads);

        Path resolved = UploadStorage.resolveForReading(context, "avatars/profile-redeploy.png");
        assertNotNull(resolved, "External upload must survive webapp packaged dir deletion");
        assertArrayEquals(imageBytes, Files.readAllBytes(resolved));
    }

    @Test
    public void testDeleteFileSafetyAndContainment() throws Exception {
        ServletContext context = createMockServletContext(tempPackagedUploads);

        UploadStorage.storeFile(
                context,
                new ByteArrayInputStream("to delete".getBytes(StandardCharsets.UTF_8)),
                "avatars",
                "temp-to-del.jpg"
        );

        assertFalse(UploadStorage.deleteFile(context, "../something"));
        assertFalse(UploadStorage.deleteFile(context, "/avatars/temp-to-del.jpg"));

        boolean deleted = UploadStorage.deleteFile(context, "avatars/temp-to-del.jpg");
        assertTrue(deleted);
        assertNull(UploadStorage.resolveForReading(context, "avatars/temp-to-del.jpg"));

        // Verify that deleting never deletes packaged seed files
        Path packagedSeed = tempPackagedUploads.resolve("protected-seed.jpg");
        Files.write(packagedSeed, "seed".getBytes(StandardCharsets.UTF_8));
        boolean deletedSeed = UploadStorage.deleteFile(context, "protected-seed.jpg");
        assertFalse(deletedSeed);
        assertTrue(Files.exists(packagedSeed), "Packaged files must not be deleted by deleteFile");
    }

    @Test
    public void testCopyPackagedSeedImagesIdempotent() throws Exception {
        ServletContext context = createMockServletContext(tempPackagedUploads);

        Files.write(tempPackagedUploads.resolve("seed1.jpg"), "s1".getBytes(StandardCharsets.UTF_8));
        Files.write(tempPackagedUploads.resolve("seed2.png"), "s2".getBytes(StandardCharsets.UTF_8));

        int firstRun = UploadStorage.copyPackagedSeedImages(context);
        assertEquals(2, firstRun);

        Path root = UploadStorage.getUploadRoot(context);
        assertTrue(Files.exists(root.resolve("seed1.jpg")));
        assertTrue(Files.exists(root.resolve("seed2.png")));

        int secondRun = UploadStorage.copyPackagedSeedImages(context);
        assertEquals(0, secondRun, "Subsequent run must be idempotent and non-overwriting");
    }

    @Test
    public void testUploadServletServingValidImage() throws Exception {
        ServletContext context = createMockServletContext(tempPackagedUploads);

        byte[] payload = "image data binary".getBytes(StandardCharsets.UTF_8);
        UploadStorage.storeFile(
                context,
                new ByteArrayInputStream(payload),
                "products",
                "gadget.png"
        );

        UploadServlet servlet = new UploadServlet();
        ServletConfig config = createMock(ServletConfig.class, (proxy, method, args) -> {
            if ("getServletContext".equals(method.getName())) return context;
            return null;
        });
        servlet.init(config);

        HttpServletRequest request = createMock(HttpServletRequest.class, (proxy, method, args) -> {
            if ("getPathInfo".equals(method.getName())) return "/products/gadget.png";
            return null;
        });

        ByteArrayOutputStream responseOut = new ByteArrayOutputStream();
        Map<String, String> responseHeaders = new HashMap<>();
        int[] statusHolder = new int[]{200};

        ServletOutputStream servletOut = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }

            @Override
            public void write(int b) {
                responseOut.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) {
                responseOut.write(b, off, len);
            }
        };

        HttpServletResponse response = createMock(HttpServletResponse.class, (proxy, method, args) -> {
            String name = method.getName();
            if ("getOutputStream".equals(name)) return servletOut;
            if ("setContentType".equals(name)) {
                responseHeaders.put("Content-Type", (String) args[0]);
                return null;
            }
            if ("setHeader".equals(name)) {
                responseHeaders.put((String) args[0], (String) args[1]);
                return null;
            }
            if ("setContentLengthLong".equals(name)) {
                responseHeaders.put("Content-Length", String.valueOf(args[0]));
                return null;
            }
            if ("sendError".equals(name)) {
                statusHolder[0] = (Integer) args[0];
                return null;
            }
            return null;
        });

        Method doGet = UploadServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        assertEquals(200, statusHolder[0]);
        assertEquals("image/png", responseHeaders.get("Content-Type"));
        assertEquals("public, max-age=86400", responseHeaders.get("Cache-Control"));
        assertArrayEquals(payload, responseOut.toByteArray());
    }

    @Test
    public void testUploadServletNotFoundAndTraversal() throws Exception {
        ServletContext context = createMockServletContext(tempPackagedUploads);
        UploadServlet servlet = new UploadServlet();
        ServletConfig config = createMock(ServletConfig.class, (proxy, method, args) -> {
            if ("getServletContext".equals(method.getName())) return context;
            return null;
        });
        servlet.init(config);

        String[] badPaths = new String[]{null, "", "/", "/missing.jpg", "/../secret.txt", "/sub/../../evil.jpg"};

        for (String badPath : badPaths) {
            int[] statusHolder = new int[]{200};
            HttpServletRequest request = createMock(HttpServletRequest.class, (proxy, method, args) -> {
                if ("getPathInfo".equals(method.getName())) return badPath;
                return null;
            });
            HttpServletResponse response = createMock(HttpServletResponse.class, (proxy, method, args) -> {
                if ("sendError".equals(method.getName())) {
                    statusHolder[0] = (Integer) args[0];
                }
                return null;
            });

            Method doGet = UploadServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
            doGet.setAccessible(true);
            doGet.invoke(servlet, request, response);

            assertEquals(404, statusHolder[0], "Path " + badPath + " must respond with 404 Not Found");
        }
    }
}