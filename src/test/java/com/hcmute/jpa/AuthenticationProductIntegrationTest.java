package com.hcmute.jpa;

import com.hcmute.jpa.controller.ProductController;
import com.hcmute.jpa.controller.LogoutController;
import com.hcmute.jpa.filter.AuthenticationFilter;
import com.hcmute.jpa.entity.Product;
import com.hcmute.jpa.entity.Category;
import com.hcmute.jpa.service.IProductService;
import com.hcmute.jpa.service.ICategoryService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationProductIntegrationTest {

    private IProductService mockProductService;
    private ICategoryService mockCategoryService;
    private AuthenticationFilter authFilter;
    private ProductController productController;
    private LogoutController logoutController;

    private List<Product> productsDb;
    private List<Category> categoriesDb;

    @BeforeEach
    public void setUp() {
        productsDb = new ArrayList<>();
        categoriesDb = new ArrayList<>();

        Category c = new Category();
        c.setCategoryid(1);
        c.setCategoryname("Electronics");
        categoriesDb.add(c);

        Product p = new Product("Laptop", "Developer laptop", 1000.0, "laptop.jpg", 1, c);
        p.setProductid(101);
        productsDb.add(p);

        mockProductService = createMock(IProductService.class, (proxy, method, args) -> {
            String name = method.getName();
            if (name.equals("getAllProducts")) {
                return productsDb;
            } else if (name.equals("getProductById")) {
                int id = (Integer) args[0];
                return productsDb.stream().filter(prod -> prod.getProductid() == id).findFirst().orElse(null);
            } else if (name.equals("getProductsPage")) {
                int offset = (Integer) args[0];
                int limit = (Integer) args[1];
                int toIndex = Math.min(offset + limit, productsDb.size());
                if (offset > productsDb.size()) {
                    return new ArrayList<Product>();
                }
                return productsDb.subList(offset, toIndex);
            } else if (name.equals("countAllProducts")) {
                return (long) productsDb.size();
            }
            return null;
        });

        mockCategoryService = createMock(ICategoryService.class, (proxy, method, args) -> {
            String name = method.getName();
            if (name.equals("findAll")) {
                return categoriesDb;
            }
            return null;
        });

        authFilter = new AuthenticationFilter();
        productController = new ProductController(mockProductService, mockCategoryService);
        logoutController = new LogoutController();
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
        Map<String, Object> session = new HashMap<>();
        String redirectUrl = null;
        boolean forwarded = false;
        String forwardedPath = null;
        boolean invalidated = false;
        boolean hasSession = false;
        String servletPath;
        int status = 200;

        HttpServletRequest request;
        HttpServletResponse response;
        HttpSession httpSession;
        RequestDispatcher requestDispatcher;

        MockHttpContext(String servletPath) {
            this.servletPath = servletPath;

            httpSession = createMock(HttpSession.class, (proxy, method, args) -> {
                String methodName = method.getName();
                if (methodName.equals("setAttribute")) {
                    session.put((String) args[0], args[1]);
                    return null;
                } else if (methodName.equals("getAttribute")) {
                    return session.get((String) args[0]);
                } else if (methodName.equals("removeAttribute")) {
                    return session.remove((String) args[0]);
                } else if (methodName.equals("invalidate")) {
                    invalidated = true;
                    session.clear();
                    hasSession = false;
                    return null;
                }
                return null;
            });

            requestDispatcher = createMock(RequestDispatcher.class, (proxy, method, args) -> {
                if (method.getName().equals("forward")) {
                    forwarded = true;
                    return null;
                }
                return null;
            });

            request = createMock(HttpServletRequest.class, (proxy, method, args) -> {
                String methodName = method.getName();
                if (methodName.equals("getParameter")) {
                    return parameters.get((String) args[0]);
                } else if (methodName.equals("setAttribute")) {
                    attributes.put((String) args[0], args[1]);
                    return null;
                } else if (methodName.equals("getAttribute")) {
                    return attributes.get((String) args[0]);
                } else if (methodName.equals("getSession")) {
                    boolean create = args.length == 0 || (Boolean) args[0];
                    if (!hasSession && create) {
                        hasSession = true;
                    }
                    return hasSession ? httpSession : null;
                } else if (methodName.equals("getRequestDispatcher")) {
                    forwardedPath = (String) args[0];
                    return requestDispatcher;
                } else if (methodName.equals("getContextPath")) {
                    return "/JPAExercise-next";
                } else if (methodName.equals("getServletPath")) {
                    return servletPath;
                } else if (methodName.equals("setCharacterEncoding")) {
                    return null;
                }
                return null;
            });

            response = createMock(HttpServletResponse.class, (proxy, method, args) -> {
                String methodName = method.getName();
                if (methodName.equals("sendRedirect")) {
                    redirectUrl = (String) args[0];
                    return null;
                } else if (methodName.equals("setCharacterEncoding")) {
                    return null;
                } else if (methodName.equals("setStatus")) {
                    status = (Integer) args[0];
                    return null;
                }
                return null;
            });
        }
    }

    @Test
    public void testUnauthenticatedProductListIsPublic() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/product");
        // No session user ID

        boolean[] chainCalled = {false};
        FilterChain mockChain = createMock(FilterChain.class, (proxy, method, args) -> {
            if (method.getName().equals("doFilter")) {
                chainCalled[0] = true;
                return null;
            }
            return null;
        });

        authFilter.doFilter(ctx.request, ctx.response, mockChain);

        assertTrue(chainCalled[0], "Chain should be called for public product requests");
        assertNull(ctx.redirectUrl, "Public product requests should not redirect");
    }

    @Test
    public void testUnauthenticatedProductDetailIsPublic() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/products/detail");
        ctx.parameters.put("id", "101");

        boolean[] chainCalled = {false};
        FilterChain mockChain = createMock(FilterChain.class, (proxy, method, args) -> {
            if (method.getName().equals("doFilter")) {
                chainCalled[0] = true;
                return null;
            }
            return null;
        });

        authFilter.doFilter(ctx.request, ctx.response, mockChain);

        assertTrue(chainCalled[0], "Chain should be called for public detail requests");
        assertNull(ctx.redirectUrl, "Public detail requests should not redirect");
    }

    @Test
    public void testUnauthenticatedProductWriteStillRedirectsToLogin() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/products/add");

        boolean[] chainCalled = {false};
        FilterChain mockChain = createMock(FilterChain.class, (proxy, method, args) -> {
            if (method.getName().equals("doFilter")) {
                chainCalled[0] = true;
            }
            return null;
        });

        authFilter.doFilter(ctx.request, ctx.response, mockChain);

        assertFalse(chainCalled[0], "Unauthenticated product writes must remain protected");
        assertEquals("/JPAExercise-next/login", ctx.redirectUrl, "Product writes should redirect to login");
    }

    @Test
    public void testUnauthenticatedManagementProductListRedirectsToLogin() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/products");
        boolean[] chainCalled = {false};
        FilterChain mockChain = createMock(FilterChain.class, (proxy, method, args) -> {
            if (method.getName().equals("doFilter")) {
                chainCalled[0] = true;
            }
            return null;
        });

        authFilter.doFilter(ctx.request, ctx.response, mockChain);

        assertFalse(chainCalled[0], "Unauthenticated management listing must remain protected");
        assertEquals("/JPAExercise-next/login", ctx.redirectUrl);
    }

    @Test
    public void testAuthenticatedProductListSucceeds() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/product");
        ctx.hasSession = true;
        ctx.session.put("authenticatedUserId", 42);

        boolean[] chainCalled = {false};
        FilterChain mockChain = createMock(FilterChain.class, (proxy, method, args) -> {
            if (method.getName().equals("doFilter")) {
                chainCalled[0] = true;
                productController.doGet((HttpServletRequest) args[0], (HttpServletResponse) args[1]);
                return null;
            }
            return null;
        });

        authFilter.doFilter(ctx.request, ctx.response, mockChain);

        assertTrue(chainCalled[0], "Chain should be called for authenticated requests");
        assertNull(ctx.redirectUrl, "Should not redirect");
        assertTrue(ctx.forwarded, "Controller should forward request to view");
        assertEquals("/views/product-list.jsp", ctx.forwardedPath);
        assertEquals(101, ((List<Product>) ctx.attributes.get("products")).get(0).getProductid());
    }

    @Test
    public void testAuthenticatedProductDetailSucceeds() throws Exception {
        MockHttpContext ctx = new MockHttpContext("/products/detail");
        ctx.parameters.put("id", "101");
        ctx.hasSession = true;
        ctx.session.put("authenticatedUserId", 42);

        boolean[] chainCalled = {false};
        FilterChain mockChain = createMock(FilterChain.class, (proxy, method, args) -> {
            if (method.getName().equals("doFilter")) {
                chainCalled[0] = true;
                productController.doGet((HttpServletRequest) args[0], (HttpServletResponse) args[1]);
                return null;
            }
            return null;
        });

        authFilter.doFilter(ctx.request, ctx.response, mockChain);

        assertTrue(chainCalled[0], "Chain should be called for authenticated detail request");
        assertNull(ctx.redirectUrl, "Should not redirect");
        assertTrue(ctx.forwarded, "Controller should forward request to view");
        assertEquals("/views/product-detail.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("product"));
        assertEquals(101, ((Product) ctx.attributes.get("product")).getProductid());
    }

    @Test
    public void testLogoutInvalidatesSessionAndSubsequentRequestRedirectsToLogin() throws Exception {
        // 1. Initial State: Authenticated
        MockHttpContext ctx = new MockHttpContext("/logout");
        ctx.hasSession = true;
        ctx.session.put("authenticatedUserId", 42);

        // 2. Perform Logout
        logoutController.doGet(ctx.request, ctx.response);

        assertTrue(ctx.invalidated, "Session must be invalidated on logout");
        assertEquals("/JPAExercise-next/login", ctx.redirectUrl, "Logout should redirect to login");
        assertFalse(ctx.hasSession, "Session flag should be cleared");

        // 3. Subsequent Request: Access Public Product List
        MockHttpContext ctx2 = new MockHttpContext("/product");
        ctx2.hasSession = ctx.hasSession; // false
        ctx2.session = ctx.session;       // empty

        boolean[] chainCalled = {false};
        FilterChain mockChain = createMock(FilterChain.class, (proxy, method, args) -> {
            if (method.getName().equals("doFilter")) {
                chainCalled[0] = true;
                return null;
            }
            return null;
        });

        authFilter.doFilter(ctx2.request, ctx2.response, mockChain);

        assertTrue(chainCalled[0], "Public product list must remain accessible after logout");
        assertNull(ctx2.redirectUrl, "Public product list must not redirect after logout");
    }
}
