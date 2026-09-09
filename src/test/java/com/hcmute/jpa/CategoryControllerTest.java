package com.hcmute.jpa;

import com.hcmute.jpa.controller.CategoryController;
import com.hcmute.jpa.entity.Category;
import com.hcmute.jpa.service.ICategoryService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryControllerTest {

    private ICategoryService mockCategoryService;
    private List<Category> categoriesDb;
    private boolean insertCalled;
    private boolean updateCalled;
    private boolean deleteCalled;
    private int nextCategoryId = 1;

    @BeforeEach
    public void setUp() {
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
                if ("getParameter".equals(methodName)) {
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
}
