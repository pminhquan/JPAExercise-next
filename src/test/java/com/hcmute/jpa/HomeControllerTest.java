package com.hcmute.jpa;

import com.hcmute.jpa.controller.HomeController;
import com.hcmute.jpa.entity.Category;
import com.hcmute.jpa.entity.Product;
import com.hcmute.jpa.service.IProductService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class HomeControllerTest {

    private IProductService mockProductService;
    private List<Product> mockProductsList;
    private boolean getNewestProductsCalled;
    private int limitPassed;

    @BeforeEach
    public void setUp() {
        getNewestProductsCalled = false;
        limitPassed = 0;
        mockProductsList = new ArrayList<>();

        Category cat = new Category();
        cat.setCategoryid(1);
        cat.setCategoryname("Test Category");

        for (int i = 1; i <= 10; i++) {
            Product p = new Product("Product " + i, "Desc " + i, 10.0 * i, "img" + i + ".jpg", 1, cat);
            p.setProductid(i);
            mockProductsList.add(p);
        }

        mockProductService = createMock(IProductService.class, (proxy, method, args) -> {
            if (method.getName().equals("getNewestProducts")) {
                getNewestProductsCalled = true;
                limitPassed = (Integer) args[0];
                return mockProductsList;
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
                    return null;
                }
                return null;
            });

            response = createMock(HttpServletResponse.class, (proxy, method, args) -> {
                if (method.getName().equals("setCharacterEncoding")) {
                    return null;
                }
                return null;
            });
        }
    }

    @Test
    public void testGetHomeBindsNewestProductsAndForwards() throws Exception {
        HomeController controller = new HomeController(mockProductService);
        MockHttpContext ctx = new MockHttpContext();

        controller.doGet(ctx.request, ctx.response);

        assertTrue(getNewestProductsCalled, "getNewestProducts must be called");
        assertEquals(10, limitPassed, "limit passed to getNewestProducts must be 10");
        assertTrue(ctx.forwarded, "Request must be forwarded");
        assertEquals("/views/home.jsp", ctx.forwardedPath, "Forward path must be /views/home.jsp");
        assertNotNull(ctx.attributes.get("newestProducts"), "newestProducts attribute must not be null");
        assertEquals(mockProductsList, ctx.attributes.get("newestProducts"), "Bound newestProducts must match service output");
    }
}
