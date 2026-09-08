package com.hcmute.jpa;

import com.hcmute.jpa.controller.AdminDashboardController;
import com.hcmute.jpa.entity.Category;
import com.hcmute.jpa.entity.Product;
import com.hcmute.jpa.service.ICategoryService;
import com.hcmute.jpa.service.IProductService;
import com.hcmute.jpa.service.IUserService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AdminDashboardControllerTest {

    private IUserService mockUserService;
    private IProductService mockProductService;
    private ICategoryService mockCategoryService;

    private List<Product> mockRecentProducts;
    private List<Category> mockCategories;
    private boolean countAllUsersCalled;
    private boolean countAllProductsCalled;
    private boolean findAllCategoriesCalled;
    private boolean getNewestProductsCalled;
    private int recentLimitPassed;

    @BeforeEach
    public void setUp() {
        countAllUsersCalled = false;
        countAllProductsCalled = false;
        findAllCategoriesCalled = false;
        getNewestProductsCalled = false;
        recentLimitPassed = 0;

        mockRecentProducts = new ArrayList<>();
        Category cat = new Category();
        cat.setCategoryid(1);
        cat.setCategoryname("Electronics");

        for (int i = 1; i <= 5; i++) {
            Product p = new Product("Product " + i, "Desc " + i, 100.0 * i, "img" + i + ".jpg", 1, cat);
            p.setProductid(i);
            mockRecentProducts.add(p);
        }

        mockCategories = new ArrayList<>();
        mockCategories.add(cat);
        Category cat2 = new Category();
        cat2.setCategoryid(2);
        cat2.setCategoryname("Books");
        mockCategories.add(cat2);

        mockUserService = createMock(IUserService.class, (proxy, method, args) -> {
            if ("countAllUsers".equals(method.getName())) {
                countAllUsersCalled = true;
                return 42L;
            }
            return null;
        });

        mockProductService = createMock(IProductService.class, (proxy, method, args) -> {
            if ("countAllProducts".equals(method.getName())) {
                countAllProductsCalled = true;
                return 15L;
            }
            if ("getNewestProducts".equals(method.getName())) {
                getNewestProductsCalled = true;
                recentLimitPassed = (Integer) args[0];
                return mockRecentProducts;
            }
            return null;
        });

        mockCategoryService = createMock(ICategoryService.class, (proxy, method, args) -> {
            if ("findAll".equals(method.getName())) {
                findAllCategoriesCalled = true;
                return mockCategories;
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
        Map<String, Object> attributes = new HashMap<>();
        boolean forwarded = false;
        String forwardedPath = null;
        String requestEncoding = null;
        String responseEncoding = null;

        HttpServletRequest request;
        HttpServletResponse response;
        RequestDispatcher requestDispatcher;

        MockHttpContext() {
            requestDispatcher = createMock(RequestDispatcher.class, (proxy, method, args) -> {
                if (method.getName().equals("forward")) {
                    forwarded = true;
                    return null;
                }
                return null;
            });

            request = createMock(HttpServletRequest.class, (proxy, method, args) -> {
                String methodName = method.getName();
                if (methodName.equals("setAttribute")) {
                    attributes.put((String) args[0], args[1]);
                    return null;
                } else if (methodName.equals("getAttribute")) {
                    return attributes.get((String) args[0]);
                } else if (methodName.equals("getRequestDispatcher")) {
                    forwardedPath = (String) args[0];
                    return requestDispatcher;
                } else if (methodName.equals("setCharacterEncoding")) {
                    requestEncoding = (String) args[0];
                    return null;
                }
                return null;
            });

            response = createMock(HttpServletResponse.class, (proxy, method, args) -> {
                if (method.getName().equals("setCharacterEncoding")) {
                    responseEncoding = (String) args[0];
                    return null;
                }
                return null;
            });
        }
    }

    @Test
    public void testDoGetBindsAllDashboardAttributesAndForwards() throws Exception {
        AdminDashboardController controller = new AdminDashboardController(mockUserService, mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext();

        controller.doGet(ctx.request, ctx.response);

        assertTrue(countAllUsersCalled, "countAllUsers must be called");
        assertTrue(countAllProductsCalled, "countAllProducts must be called");
        assertTrue(findAllCategoriesCalled, "findAll must be called on categoryService");
        assertTrue(getNewestProductsCalled, "getNewestProducts must be called on productService");
        assertTrue(recentLimitPassed > 0, "Limit passed to getNewestProducts must be positive");

        assertEquals(42L, ctx.attributes.get("totalUsers"), "totalUsers attribute must match user count");
        assertEquals(15L, ctx.attributes.get("totalProducts"), "totalProducts attribute must match product count");
        assertEquals(2L, ctx.attributes.get("totalCategories"), "totalCategories attribute must match categories size");
        assertEquals(mockRecentProducts, ctx.attributes.get("recentProducts"), "recentProducts attribute must match recent products list");

        assertTrue(ctx.forwarded, "Request must be forwarded");
        assertEquals("/WEB-INF/views/admin-dashboard.jsp", ctx.forwardedPath, "Forward path must be /WEB-INF/views/admin-dashboard.jsp");
        assertEquals("UTF-8", ctx.requestEncoding, "Request character encoding must be UTF-8");
        assertEquals("UTF-8", ctx.responseEncoding, "Response character encoding must be UTF-8");
    }

    @Test
    public void testInitInitializesDefaultServices() {
        AdminDashboardController controller = new AdminDashboardController();
        assertDoesNotThrow(() -> controller.init());
    }
}
