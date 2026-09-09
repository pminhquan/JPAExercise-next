package com.hcmute.jpa;

import com.hcmute.jpa.controller.CategoryController;
import com.hcmute.jpa.entity.Category;
import com.hcmute.jpa.service.ICategoryService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryControllerTest {

    private ICategoryService mockCategoryService;
    private List<Category> categoriesDb;
    private boolean insertCalled;
    private boolean updateCalled;
    private boolean deleteCalled;
    private int nextCategoryId = 1;
    private Path tempUploadDir;
    private String savedAppUploadDir;

    @BeforeEach
    public void setUp() throws IOException {
        tempUploadDir = Files.createTempDirectory("cat_ctrl_test_uploads_");
        savedAppUploadDir = System.getProperty("app.upload.dir");
        System.setProperty("app.upload.dir", tempUploadDir.toAbsolutePath().toString());

        categoriesDb = new ArrayList<>();
        insertCalled = false;
        updateCalled = false;
        deleteCalled = false;
        nextCategoryId = 1;

        Category c1 = new Category("Electronics", "laptop.jpg", 1);
        c1.setCategoryid(1);
        categoriesDb.add(c1);

        Category c2 = new Category("Books", "book.jpg", 1);
        c2.setCategoryid(2);
        categoriesDb.add(c2);

        Category c3 = new Category("ExternalCategory", "https://images.example.com/item.jpg", 1);
        c3.setCategoryid(3);
        categoriesDb.add(c3);

        mockCategoryService = createMock(ICategoryService.class, (proxy, method, args) -> {
            String name = method.getName();
            if ("findAll".equals(name)) {
                return categoriesDb;
            } else if ("findById".equals(name)) {
                int id = (Integer) args[0];
                return categoriesDb.stream().filter(c -> c.getCategoryid() == id).findFirst().orElse(null);
            } else if ("findByName".equals(name)) {
                String catName = (String) args[0];
                if (catName == null) return null;
                return categoriesDb.stream().filter(c -> c.getCategoryname().equalsIgnoreCase(catName.trim())).findFirst().orElse(null);
            } else if ("insert".equals(name)) {
                insertCalled = true;
                Category c = (Category) args[0];
                c.setCategoryid(nextCategoryId++);
                categoriesDb.add(c);
                return null;
            } else if ("update".equals(name)) {
                updateCalled = true;
                Category c = (Category) args[0];
                categoriesDb.removeIf(item -> item.getCategoryid() == c.getCategoryid());
                categoriesDb.add(c);
                return null;
            } else if ("isCategoryInUse".equals(name)) {
                int id = (Integer) args[0];
                return id == 1; // c1 is in use
            } else if ("delete".equals(name)) {
                deleteCalled = true;
                int id = (Integer) args[0];
                return categoriesDb.removeIf(item -> item.getCategoryid() == id);
            }
            return null;
        });
    }

    @AfterEach
    public void tearDown() {
        if (savedAppUploadDir != null) {
            System.setProperty("app.upload.dir", savedAppUploadDir);
        } else {
            System.clearProperty("app.upload.dir");
        }

        if (tempUploadDir != null && Files.exists(tempUploadDir)) {
            try (Stream<Path> stream = Files.walk(tempUploadDir)) {
                stream.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (Exception ignored) {
                            }
                        });
            } catch (Exception ignored) {
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createMock(Class<T> interfaceType, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                handler
        );
    }

    private static class MockHttpContext {
        Map<String, String> parameters = new HashMap<>();
        Map<String, Object> attributes = new HashMap<>();
        Map<String, Part> parts = new HashMap<>();
        boolean throwOnGetPart = false;
        Throwable getPartException = null;
        String contentType = null;
        String redirectUrl = null;
        boolean forwarded = false;
        String forwardedPath = null;

        HttpServletRequest request;
        HttpServletResponse response;
        RequestDispatcher requestDispatcher;

        MockHttpContext() {
            requestDispatcher = createMock(RequestDispatcher.class, (proxy, method, args) -> {
                if ("forward".equals(method.getName())) {
                    forwarded = true;
                    return null;
                }
                return null;
            });

            request = createMock(HttpServletRequest.class, (proxy, method, args) -> {
                String methodName = method.getName();
                if ("getContentType".equals(methodName)) {
                    if (contentType != null) {
                        return contentType;
                    }
                    if (!parts.isEmpty() || throwOnGetPart || getPartException != null) {
                        return "multipart/form-data; boundary=----WebKitFormBoundaryXYZ";
                    }
                    return "application/x-www-form-urlencoded";
                } else if ("getParameter".equals(methodName)) {
                    return parameters.get((String) args[0]);
                } else if ("setAttribute".equals(methodName)) {
                    attributes.put((String) args[0], args[1]);
                    return null;
                } else if ("getAttribute".equals(methodName)) {
                    return attributes.get((String) args[0]);
                } else if ("getRequestDispatcher".equals(methodName)) {
                    forwardedPath = (String) args[0];
                    return requestDispatcher;
                } else if ("getContextPath".equals(methodName)) {
                    return "/JPAExercise-next";
                } else if ("setCharacterEncoding".equals(methodName)) {
                    return null;
                } else if ("getPart".equals(methodName)) {
                    if (throwOnGetPart) {
                        throw new IllegalStateException("Size limit exceeded");
                    }
                    if (getPartException != null) {
                        if (getPartException instanceof IOException) {
                            throw (IOException) getPartException;
                        }
                        if (getPartException instanceof ServletException) {
                            throw (ServletException) getPartException;
                        }
                        if (getPartException instanceof RuntimeException) {
                            throw (RuntimeException) getPartException;
                        }
                        throw new ServletException(getPartException);
                    }
                    return parts.get((String) args[0]);
                } else if ("getServletContext".equals(methodName)) {
                    return null;
                }
                return null;
            });

            response = createMock(HttpServletResponse.class, (proxy, method, args) -> {
                String methodName = method.getName();
                if ("sendRedirect".equals(methodName)) {
                    redirectUrl = (String) args[0];
                    return null;
                } else if ("setCharacterEncoding".equals(methodName)) {
                    return null;
                }
                return null;
            });
        }
    }

    private static Part createMockPart(String submittedFileName, byte[] content, String contentType) {
        return createMock(Part.class, (proxy, method, args) -> {
            String name = method.getName();
            if ("getSubmittedFileName".equals(name)) {
                return submittedFileName;
            } else if ("getSize".equals(name)) {
                return content == null ? 0L : (long) content.length;
            } else if ("getInputStream".equals(name)) {
                return new java.io.ByteArrayInputStream(content != null ? content : new byte[0]);
            } else if ("getContentType".equals(name)) {
                return contentType;
            }
            return null;
        });
    }

    private static Part createMockPartWithStream(String submittedFileName, long size, java.io.InputStream inputStream, String contentType) {
        return createMock(Part.class, (proxy, method, args) -> {
            String name = method.getName();
            if ("getSubmittedFileName".equals(name)) {
                return submittedFileName;
            } else if ("getSize".equals(name)) {
                return size;
            } else if ("getInputStream".equals(name)) {
                return inputStream;
            } else if ("getContentType".equals(name)) {
                return contentType;
            }
            return null;
        });
    }

    @Test
    public void testInsertCategorySuccess() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "insert");
        ctx.parameters.put("categoryname", "Smartphones");
        ctx.parameters.put("images", "phone.jpg");
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertTrue(insertCalled);
        assertEquals("/JPAExercise-next/categories?message=add_success", ctx.redirectUrl);
    }

    @Test
    public void testInsertCategoryBlankNameFails() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "insert");
        ctx.parameters.put("categoryname", "   ");
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(insertCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/category-add.jsp", ctx.forwardedPath);
        assertEquals("Category name cannot be empty.", ctx.attributes.get("error"));
    }

    @Test
    public void testInsertCategoryDuplicateNameFails() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "insert");
        ctx.parameters.put("categoryname", "electronics"); // Case-insensitive duplicate of Electronics
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(insertCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/category-add.jsp", ctx.forwardedPath);
        assertEquals("Category name already exists.", ctx.attributes.get("error"));
    }

    @Test
    public void testInsertCategoryNameExceeds100CharsFails() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "insert");
        ctx.parameters.put("categoryname", "C".repeat(101));
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(insertCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/category-add.jsp", ctx.forwardedPath);
        assertEquals("Category name must not exceed 100 characters.", ctx.attributes.get("error"));
    }

    @Test
    public void testInsertCategoryPathTraversalImageFails() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "insert");
        ctx.parameters.put("categoryname", "Valid Category");
        ctx.parameters.put("images", "../../etc/passwd");
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(insertCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/category-add.jsp", ctx.forwardedPath);
        assertEquals("Invalid image reference.", ctx.attributes.get("error"));
    }

    @Test
    public void testUpdateCategoryDuplicateNameFails() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "update");
        ctx.parameters.put("categoryid", "2"); // Category 2 updating to Category 1's name
        ctx.parameters.put("categoryname", "Electronics");
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/category-edit.jsp", ctx.forwardedPath);
        assertEquals("Category name already exists.", ctx.attributes.get("error"));
    }

    @Test
    public void testUpdateCategorySameNameAllowed() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "update");
        ctx.parameters.put("categoryid", "1"); // Category 1 keeping its own name
        ctx.parameters.put("categoryname", "Electronics");
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertTrue(updateCalled);
        assertEquals("/JPAExercise-next/categories?message=update_success", ctx.redirectUrl);
    }

    @Test
    public void testInsertCategoryImageExceeds500CharsFails() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "insert");
        ctx.parameters.put("categoryname", "Valid Category");
        ctx.parameters.put("images", "a".repeat(501));
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(insertCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/category-add.jsp", ctx.forwardedPath);
        assertEquals("Image path must not exceed 500 characters.", ctx.attributes.get("error"));
    }

    @Test
    public void testUpdateCategoryImageExceeds500CharsFails() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "update");
        ctx.parameters.put("categoryid", "1");
        ctx.parameters.put("categoryname", "Electronics");
        ctx.parameters.put("images", "a".repeat(501));
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/category-edit.jsp", ctx.forwardedPath);
        assertEquals("Image path must not exceed 500 characters.", ctx.attributes.get("error"));
        assertNotNull(ctx.attributes.get("category"));
    }

    @Test
    public void testUpdateCategoryNameExceeds100CharsFails() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "update");
        ctx.parameters.put("categoryid", "1");
        ctx.parameters.put("categoryname", "C".repeat(101));
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/category-edit.jsp", ctx.forwardedPath);
        assertEquals("Category name must not exceed 100 characters.", ctx.attributes.get("error"));
        assertNotNull(ctx.attributes.get("category"));
    }

    @Test
    public void testConcurrentCategoryInsertAtomicity() throws Exception {
        CountDownLatch thread1InsideLock = new CountDownLatch(1);
        CountDownLatch thread1Proceed = new CountDownLatch(1);
        AtomicInteger insertCount = new AtomicInteger(0);
        List<Category> sharedDb = Collections.synchronizedList(new ArrayList<>(categoriesDb));

        ICategoryService concurrentService = createMock(ICategoryService.class, (proxy, method, args) -> {
            String name = method.getName();
            if ("findByName".equals(name)) {
                String catName = (String) args[0];
                if (catName == null) return null;
                synchronized (sharedDb) {
                    return sharedDb.stream()
                            .filter(c -> c.getCategoryname().equalsIgnoreCase(catName.trim()))
                            .findFirst()
                            .orElse(null);
                }
            } else if ("insert".equals(name)) {
                thread1InsideLock.countDown();
                try {
                    assertTrue(thread1Proceed.await(5, TimeUnit.SECONDS), "Thread 1 timed out waiting for proceed signal");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                insertCount.incrementAndGet();
                Category c = (Category) args[0];
                synchronized (sharedDb) {
                    c.setCategoryid(nextCategoryId++);
                    sharedDb.add(c);
                }
                return null;
            }
            return null;
        });

        CategoryController controller = new CategoryController(concurrentService);

        MockHttpContext ctx1 = new MockHttpContext();
        ctx1.parameters.put("action", "insert");
        ctx1.parameters.put("categoryname", "ConcurrentItem");
        ctx1.parameters.put("images", "item.jpg");
        ctx1.parameters.put("status", "1");

        MockHttpContext ctx2 = new MockHttpContext();
        ctx2.parameters.put("action", "insert");
        ctx2.parameters.put("categoryname", "ConcurrentItem");
        ctx2.parameters.put("images", "item.jpg");
        ctx2.parameters.put("status", "1");

        AtomicReference<Throwable> thread1Error = new AtomicReference<>();
        AtomicReference<Throwable> thread2Error = new AtomicReference<>();

        Thread t1 = new Thread(() -> {
            try {
                controller.doPost(ctx1.request, ctx1.response);
            } catch (Throwable t) {
                thread1Error.set(t);
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                controller.doPost(ctx2.request, ctx2.response);
            } catch (Throwable t) {
                thread2Error.set(t);
            }
        });

        t1.start();
        assertTrue(thread1InsideLock.await(5, TimeUnit.SECONDS), "Thread 1 did not reach insert within timeout");

        t2.start();

        long deadline = System.currentTimeMillis() + 5000;
        while (t2.getState() != Thread.State.BLOCKED) {
            if (System.currentTimeMillis() > deadline) {
                fail("Thread 2 failed to block on CATEGORY_WRITE_LOCK. Current state: " + t2.getState());
            }
            Thread.yield();
        }

        thread1Proceed.countDown();

        t1.join(5000);
        t2.join(5000);

        assertNull(thread1Error.get(), "Thread 1 threw an exception");
        assertNull(thread2Error.get(), "Thread 2 threw an exception");

        assertEquals(1, insertCount.get(), "Expected exactly one insert to succeed");
        assertEquals("/JPAExercise-next/categories?message=add_success", ctx1.redirectUrl);

        assertTrue(ctx2.forwarded, "Thread 2 should have forwarded on duplicate error");
        assertEquals("/views/category-add.jsp", ctx2.forwardedPath);
        assertEquals("Category name already exists.", ctx2.attributes.get("error"));
    }

    @Test
    public void testInsertCategoryWithUploadedImage() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "insert");
        ctx.parameters.put("categoryname", "Monitors");
        ctx.parameters.put("status", "1");
        ctx.parts.put("image", createMockPart("monitor.png", new byte[]{1, 2, 3}, "image/png"));

        controller.doPost(ctx.request, ctx.response);

        assertTrue(insertCalled);
        assertEquals("/JPAExercise-next/categories?message=add_success", ctx.redirectUrl);
        Category saved = categoriesDb.stream().filter(c -> "Monitors".equals(c.getCategoryname())).findFirst().orElse(null);
        assertNotNull(saved);
        assertTrue(saved.getImages().startsWith("categories/"));
        assertTrue(saved.getImages().endsWith(".png"));
    }

    @Test
    public void testUpdateCategoryWithImageReplacement() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "update");
        ctx.parameters.put("categoryid", "1");
        ctx.parameters.put("categoryname", "Electronics");
        ctx.parameters.put("status", "1");
        ctx.parts.put("image", createMockPart("new-elec.jpg", new byte[]{4, 5, 6}, "image/jpeg"));

        controller.doPost(ctx.request, ctx.response);

        assertTrue(updateCalled);
        assertEquals("/JPAExercise-next/categories?message=update_success", ctx.redirectUrl);
        Category updated = categoriesDb.stream().filter(c -> c.getCategoryid() == 1).findFirst().orElse(null);
        assertNotNull(updated);
        assertTrue(updated.getImages().startsWith("categories/"));
        assertTrue(updated.getImages().endsWith(".jpg"));
    }

    @Test
    public void testUpdateCategoryWithoutNewImageRetainsOld() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "update");
        ctx.parameters.put("categoryid", "1");
        ctx.parameters.put("categoryname", "Electronics Renamed");
        ctx.parameters.put("status", "1");
        // Neither image upload part nor images text parameter is provided

        controller.doPost(ctx.request, ctx.response);

        assertTrue(updateCalled);
        assertEquals("/JPAExercise-next/categories?message=update_success", ctx.redirectUrl);
        Category updated = categoriesDb.stream().filter(c -> c.getCategoryid() == 1).findFirst().orElse(null);
        assertNotNull(updated);
        assertEquals("laptop.jpg", updated.getImages());
    }

    @Test
    public void testInsertCategoryWithoutImageDefaultsToBlank() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "insert");
        ctx.parameters.put("categoryname", "Laptop");
        ctx.parameters.put("status", "1");
        // No image upload

        controller.doPost(ctx.request, ctx.response);

        assertTrue(insertCalled);
        Category saved = categoriesDb.stream().filter(c -> "Laptop".equals(c.getCategoryname())).findFirst().orElse(null);
        assertNotNull(saved);
        assertEquals("", saved.getImages());
    }

    @Test
    public void testUpdateCategoryReplacingExternalUrlImageSucceeds() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "update");
        ctx.parameters.put("categoryid", "3");
        ctx.parameters.put("categoryname", "ExternalCategory");
        ctx.parameters.put("status", "1");
        ctx.parts.put("image", createMockPart("replaced.png", new byte[]{10, 20, 30}, "image/png"));

        controller.doPost(ctx.request, ctx.response);

        assertTrue(updateCalled);
        assertEquals("/JPAExercise-next/categories?message=update_success", ctx.redirectUrl);
        Category updated = categoriesDb.stream().filter(c -> c.getCategoryid() == 3).findFirst().orElse(null);
        assertNotNull(updated);
        assertTrue(updated.getImages().startsWith("categories/"));
        assertTrue(updated.getImages().endsWith(".png"));
    }

    @Test
    public void testInsertCategoryPersistenceFailureCleansUpStoredImage() throws Exception {
        mockCategoryService = createMock(ICategoryService.class, (proxy, method, args) -> {
            String name = method.getName();
            if ("findByName".equals(name)) return null;
            if ("insert".equals(name)) throw new RuntimeException("Database error");
            return null;
        });
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "insert");
        ctx.parameters.put("categoryname", "FailingCategory");
        ctx.parameters.put("status", "1");
        ctx.parts.put("image", createMockPart("orphan.png", new byte[]{1, 2, 3}, "image/png"));

        controller.doPost(ctx.request, ctx.response);

        assertTrue(ctx.forwarded);
        assertEquals("/views/category-add.jsp", ctx.forwardedPath);
        Path catFolder = tempUploadDir.resolve("categories");
        if (Files.exists(catFolder)) {
            try (Stream<Path> s = Files.list(catFolder)) {
                assertEquals(0, s.count(), "Orphaned image must be deleted on persistence failure");
            }
        }
    }

    @Test
    public void testUpdateCategoryPersistenceFailureCleansUpStoredImage() throws Exception {
        Category existing = new Category("Existing", "old.jpg", 1);
        existing.setCategoryid(10);
        mockCategoryService = createMock(ICategoryService.class, (proxy, method, args) -> {
            String name = method.getName();
            if ("findById".equals(name)) return existing;
            if ("findByName".equals(name)) return null;
            if ("update".equals(name)) throw new RuntimeException("Database update error");
            return null;
        });
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "update");
        ctx.parameters.put("categoryid", "10");
        ctx.parameters.put("categoryname", "Existing");
        ctx.parameters.put("status", "1");
        ctx.parts.put("image", createMockPart("orphan-update.png", new byte[]{1, 2, 3}, "image/png"));

        controller.doPost(ctx.request, ctx.response);

        assertTrue(ctx.forwarded);
        assertEquals("/views/category-edit.jsp", ctx.forwardedPath);
        Path catFolder = tempUploadDir.resolve("categories");
        if (Files.exists(catFolder)) {
            try (Stream<Path> s = Files.list(catFolder)) {
                assertEquals(0, s.count(), "Orphaned image must be deleted on update persistence failure");
            }
        }
    }

    @Test
    public void testInsertCategoryUnsupportedFileTypeFails() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "insert");
        ctx.parameters.put("categoryname", "Hacking Tools");
        ctx.parameters.put("status", "1");
        ctx.parts.put("image", createMockPart("malware.exe", new byte[]{1, 2, 3}, "application/octet-stream"));

        controller.doPost(ctx.request, ctx.response);

        assertFalse(insertCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/category-add.jsp", ctx.forwardedPath);
        assertEquals("Only JPG, JPEG, PNG and WEBP images are allowed.", ctx.attributes.get("error"));
    }

    @Test
    public void testInsertCategoryOversizedUploadFails() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "insert");
        ctx.parameters.put("categoryname", "Oversized Item");
        ctx.parameters.put("status", "1");
        ctx.throwOnGetPart = true;

        controller.doPost(ctx.request, ctx.response);

        assertFalse(insertCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/category-add.jsp", ctx.forwardedPath);
        assertEquals("Image file exceeds maximum allowed size of 5 MB.", ctx.attributes.get("error"));
    }

    @Test
    public void testInsertCategoryPartialStreamWriteFailureCleansUpOrphanFile() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.parameters.put("action", "insert");
        ctx.parameters.put("categoryname", "PartialStreamCategory");
        ctx.parameters.put("status", "1");

        java.io.InputStream failingStream = new java.io.InputStream() {
            private int bytesRead = 0;

            @Override
            public int read() throws IOException {
                if (bytesRead >= 16) {
                    throw new IOException("Simulated network disconnection after partial read");
                }
                bytesRead++;
                return 0x55;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (bytesRead >= 16) {
                    throw new IOException("Simulated network disconnection after partial read");
                }
                int toRead = Math.min(len, 16 - bytesRead);
                if (toRead <= 0) {
                    throw new IOException("Simulated network disconnection after partial read");
                }
                Arrays.fill(b, off, off + toRead, (byte) 0x55);
                bytesRead += toRead;
                return toRead;
            }
        };

        ctx.parts.put("image", createMockPartWithStream("partial.jpg", 1024L, failingStream, "image/jpeg"));

        controller.doPost(ctx.request, ctx.response);

        assertFalse(insertCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/category-add.jsp", ctx.forwardedPath);
        assertEquals("Unable to upload image file.", ctx.attributes.get("error"));

        Path catFolder = tempUploadDir.resolve("categories");
        if (Files.exists(catFolder)) {
            try (Stream<Path> s = Files.list(catFolder)) {
                assertEquals(0, s.count(), "Isolated categories directory must be empty after partial stream write failure");
            }
        }
    }

    @Test
    public void testInsertCategoryGetPartIoExceptionFails() throws Exception {
        assertAddParserFailureShowsError(new IOException("Multipart stream read failure"));
    }

    @Test
    public void testInsertCategoryGetPartServletExceptionFails() throws Exception {
        assertAddParserFailureShowsError(new ServletException("Multipart parser failure"));
    }

    @Test
    public void testUpdateCategoryGetPartIoExceptionFails() throws Exception {
        assertEditParserFailureShowsError(new IOException("Multipart stream read failure"));
    }

    @Test
    public void testUpdateCategoryGetPartServletExceptionFails() throws Exception {
        assertEditParserFailureShowsError(new ServletException("Multipart parser failure"));
    }

    @Test
    public void testInsertCategoryNonMultipartUsesImagesParameter() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.contentType = "application/x-www-form-urlencoded";
        ctx.parameters.put("action", "insert");
        ctx.parameters.put("categoryname", "NonMultipartCat");
        ctx.parameters.put("images", "phone.jpg");
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertTrue(insertCalled);
        assertEquals("/JPAExercise-next/categories?message=add_success", ctx.redirectUrl);
        Category saved = categoriesDb.stream().filter(c -> "NonMultipartCat".equals(c.getCategoryname())).findFirst().orElse(null);
        assertNotNull(saved);
        assertEquals("phone.jpg", saved.getImages());
    }

    @Test
    public void testUpdateCategoryMultipartWithoutNewImageRetainsOld() throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.contentType = "multipart/form-data; boundary=----WebKitFormBoundaryXYZ";
        ctx.parameters.put("action", "update");
        ctx.parameters.put("categoryid", "1");
        ctx.parameters.put("categoryname", "Electronics Renamed");
        ctx.parameters.put("status", "1");
        // empty parts

        controller.doPost(ctx.request, ctx.response);

        assertTrue(updateCalled);
        assertEquals("/JPAExercise-next/categories?message=update_success", ctx.redirectUrl);
        Category updated = categoriesDb.stream().filter(c -> c.getCategoryid() == 1).findFirst().orElse(null);
        assertNotNull(updated);
        assertEquals("laptop.jpg", updated.getImages());
    }

    private void assertAddParserFailureShowsError(Throwable exception) throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.contentType = "multipart/form-data; boundary=----WebKitFormBoundaryXYZ";
        ctx.parameters.put("action", "insert");
        ctx.parameters.put("categoryname", "ParserFailCat");
        ctx.parameters.put("status", "1");
        ctx.getPartException = exception;

        controller.doPost(ctx.request, ctx.response);

        assertFalse(insertCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/category-add.jsp", ctx.forwardedPath);
        assertEquals("Unable to upload image file.", ctx.attributes.get("error"));
    }

    private void assertEditParserFailureShowsError(Throwable exception) throws Exception {
        CategoryController controller = new CategoryController(mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();
        ctx.contentType = "multipart/form-data; boundary=----WebKitFormBoundaryXYZ";
        ctx.parameters.put("action", "update");
        ctx.parameters.put("categoryid", "1");
        ctx.parameters.put("categoryname", "ParserFailEdit");
        ctx.parameters.put("status", "1");
        ctx.getPartException = exception;

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/category-edit.jsp", ctx.forwardedPath);
        assertEquals("Unable to upload image file.", ctx.attributes.get("error"));
        Category existing = categoriesDb.stream().filter(c -> c.getCategoryid() == 1).findFirst().orElse(null);
        assertNotNull(existing);
        assertEquals("laptop.jpg", existing.getImages());
    }
}
