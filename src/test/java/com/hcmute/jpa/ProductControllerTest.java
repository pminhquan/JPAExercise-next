package com.hcmute.jpa;

import com.hcmute.jpa.controller.ProductController;
import com.hcmute.jpa.entity.Category;
import com.hcmute.jpa.entity.Product;
import com.hcmute.jpa.service.ICategoryService;
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

public class ProductControllerTest {

    private IProductService mockProductService;
    private ICategoryService mockCategoryService;

    private List<Product> productsDb;
    private List<Category> categoriesDb;
    private int nextProductId = 1;
    private int nextCategoryId = 1;

    // Track calls
    private boolean createProductCalled;
    private boolean updateProductCalled;
    private boolean deleteProductCalled;

    @BeforeEach
    public void setUp() {
        productsDb = new ArrayList<>();
        categoriesDb = new ArrayList<>();
        nextProductId = 1;
        nextCategoryId = 1;

        createProductCalled = false;
        updateProductCalled = false;
        deleteProductCalled = false;

        // Stub Category DB
        Category c1 = new Category();
        c1.setCategoryid(10);
        c1.setCategoryname("Electronics");
        c1.setStatus(1);
        categoriesDb.add(c1);

        Category c2 = new Category();
        c2.setCategoryid(20);
        c2.setCategoryname("Books");
        c2.setStatus(1);
        categoriesDb.add(c2);

        // Stub Product DB
        Product p1 = new Product("Laptop", "Tech device", 1000.0, "laptop.jpg", 1, c1);
        p1.setProductid(101);
        productsDb.add(p1);

        mockCategoryService = createMock(ICategoryService.class, (proxy, method, args) -> {
            String name = method.getName();
            if (name.equals("findAll")) {
                return categoriesDb;
            } else if (name.equals("findById")) {
                int id = (Integer) args[0];
                return categoriesDb.stream().filter(c -> c.getCategoryid() == id).findFirst().orElse(null);
            }
            return null;
        });

        mockProductService = createMock(IProductService.class, (proxy, method, args) -> {
            String name = method.getName();
            if (name.equals("getAllProducts")) {
                return productsDb;
            } else if (name.equals("getProductById")) {
                int id = (Integer) args[0];
                return productsDb.stream().filter(p -> p.getProductid() == id).findFirst().orElse(null);
            } else if (name.equals("createProduct")) {
                createProductCalled = true;
                Product p = (Product) args[0];
                p.setProductid(nextProductId++);
                productsDb.add(p);
                return null;
            } else if (name.equals("updateProduct")) {
                updateProductCalled = true;
                Product p = (Product) args[0];
                productsDb.removeIf(prod -> prod.getProductid() == p.getProductid());
                productsDb.add(p);
                return null;
            } else if (name.equals("deleteProduct")) {
                deleteProductCalled = true;
                int id = (Integer) args[0];
                productsDb.removeIf(prod -> prod.getProductid() == id);
                return null;
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
        String servletPath = "/products";

        HttpServletRequest request;
        HttpServletResponse response;
        RequestDispatcher requestDispatcher;

        MockHttpContext(String servletPath) {
            this.servletPath = servletPath;

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
                }
                return null;
            });
        }
    }

    @Test
    public void testGetProductsList() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products");

        controller.doGet(ctx.request, ctx.response);

        assertTrue(ctx.forwarded);
        assertEquals("/views/product-list.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("products"));
        assertEquals(productsDb, ctx.attributes.get("products"));
    }

    @Test
    public void testGetProductAddForm() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/add");

        controller.doGet(ctx.request, ctx.response);

        assertTrue(ctx.forwarded);
        assertEquals("/views/product-add.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("categories"));
        assertEquals(categoriesDb, ctx.attributes.get("categories"));
    }

    @Test
    public void testPostProductAddSuccess() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/add");
        ctx.parameters.put("productname", "Smartphone");
        ctx.parameters.put("price", "499.99");
        ctx.parameters.put("description", "A nice smartphone");
        ctx.parameters.put("images", "phone.jpg");
        ctx.parameters.put("categoryid", "10");
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertTrue(createProductCalled);
        assertEquals("/JPAExercise-next/products?message=add_success", ctx.redirectUrl);
    }

    @Test
    public void testPostProductAddBlankNameValidation() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/add");
        ctx.parameters.put("productname", "   ");
        ctx.parameters.put("price", "499.99");
        ctx.parameters.put("categoryid", "10");
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(createProductCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/product-add.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("error"));
        assertEquals(categoriesDb, ctx.attributes.get("categories"));
    }

    @Test
    public void testPostProductAddPriceZeroValidation() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/add");
        ctx.parameters.put("productname", "Smartphone");
        ctx.parameters.put("price", "0");
        ctx.parameters.put("categoryid", "10");
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(createProductCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/product-add.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("error"));
    }

    @Test
    public void testPostProductAddCategoryNonexistentValidation() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/add");
        ctx.parameters.put("productname", "Smartphone");
        ctx.parameters.put("price", "299.99");
        ctx.parameters.put("categoryid", "99"); // nonexistent category ID
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(createProductCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/product-add.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("error"));
    }

    @Test
    public void testGetProductEditFormSuccess() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/edit");
        ctx.parameters.put("id", "101");

        controller.doGet(ctx.request, ctx.response);

        assertTrue(ctx.forwarded);
        assertEquals("/views/product-edit.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("product"));
        assertEquals(101, ((Product) ctx.attributes.get("product")).getProductid());
        assertNotNull(ctx.attributes.get("categories"));
    }

    @Test
    public void testGetProductEditFormMissingIdSafe() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/edit");
        // No ID param

        controller.doGet(ctx.request, ctx.response);

        assertFalse(ctx.forwarded);
        assertEquals("/JPAExercise-next/products", ctx.redirectUrl);
    }

    @Test
    public void testGetProductEditFormNonexistentIdSafe() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/edit");
        ctx.parameters.put("id", "9999"); // Non-existent product ID

        controller.doGet(ctx.request, ctx.response);

        assertFalse(ctx.forwarded);
        assertEquals("/JPAExercise-next/products", ctx.redirectUrl);
    }

    @Test
    public void testPostProductEditSuccess() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/edit");
        ctx.parameters.put("productid", "101");
        ctx.parameters.put("productname", "New Laptop Name");
        ctx.parameters.put("price", "999.00");
        ctx.parameters.put("description", "Updated desc");
        ctx.parameters.put("images", "new_laptop.jpg");
        ctx.parameters.put("categoryid", "10");
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertTrue(updateProductCalled);
        assertEquals("/JPAExercise-next/products?message=update_success", ctx.redirectUrl);
    }

    @Test
    public void testPostProductEditMissingIdSafe() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/edit");
        ctx.parameters.put("productname", "New Laptop Name");
        ctx.parameters.put("price", "999.00");
        ctx.parameters.put("categoryid", "10");
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateProductCalled);
        assertEquals("/JPAExercise-next/products", ctx.redirectUrl);
    }

    @Test
    public void testPostProductDeleteSuccess() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/delete");
        ctx.parameters.put("id", "101");

        controller.doPost(ctx.request, ctx.response);

        assertTrue(deleteProductCalled);
        assertEquals("/JPAExercise-next/products?message=delete_success", ctx.redirectUrl);
        assertTrue(productsDb.isEmpty());
    }

    @Test
    public void testPostProductDeleteMissingIdSafe() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/delete");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(deleteProductCalled);
        assertEquals("/JPAExercise-next/products", ctx.redirectUrl);
    }

    @Test
    public void testProductDeletionPostOnly() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);

        // Test GET delete -> Should redirect to list view and NOT call delete
        MockHttpContext getCtx = new MockHttpContext("/products/delete");
        getCtx.parameters.put("id", "101");
        controller.doGet(getCtx.request, getCtx.response);

        assertFalse(deleteProductCalled);
        assertEquals("/JPAExercise-next/products", getCtx.redirectUrl);
    }

    @Test
    public void testGetProductEditFormInvalidIdSafe() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/edit");
        ctx.parameters.put("id", "invalid-id");

        controller.doGet(ctx.request, ctx.response);

        assertFalse(ctx.forwarded);
        assertEquals("/JPAExercise-next/products", ctx.redirectUrl);
    }

    @Test
    public void testPostProductDeleteInvalidIdSafe() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/delete");
        ctx.parameters.put("id", "not-a-number");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(deleteProductCalled);
        assertEquals("/JPAExercise-next/products", ctx.redirectUrl);
    }

    @Test
    public void testPostProductDeleteNonexistentIdSafe() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/delete");
        ctx.parameters.put("id", "9999");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(deleteProductCalled);
        assertEquals("/JPAExercise-next/products", ctx.redirectUrl);
    }

    @Test
    public void testPostProductEditInvalidIdSafe() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/edit");
        ctx.parameters.put("productid", "not-a-number");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateProductCalled);
        assertEquals("/JPAExercise-next/products", ctx.redirectUrl);
    }

    @Test
    public void testPostProductEditNonexistentIdSafe() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/edit");
        ctx.parameters.put("productid", "9999");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateProductCalled);
        assertEquals("/JPAExercise-next/products", ctx.redirectUrl);
    }

    @Test
    public void testPostProductEditBlankNameValidation() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/edit");
        ctx.parameters.put("productid", "101");
        ctx.parameters.put("productname", "   ");
        ctx.parameters.put("price", "999.00");
        ctx.parameters.put("categoryid", "10");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateProductCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/product-edit.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("error"));
        assertNotNull(ctx.attributes.get("product"));
        assertEquals(categoriesDb, ctx.attributes.get("categories"));
    }

    @Test
    public void testPostProductEditPriceZeroValidation() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/edit");
        ctx.parameters.put("productid", "101");
        ctx.parameters.put("productname", "Valid Name");
        ctx.parameters.put("price", "0");
        ctx.parameters.put("categoryid", "10");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateProductCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/product-edit.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("error"));
        assertNotNull(ctx.attributes.get("product"));
    }

    @Test
    public void testPostProductEditPriceInvalidStringValidation() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/edit");
        ctx.parameters.put("productid", "101");
        ctx.parameters.put("productname", "Valid Name");
        ctx.parameters.put("price", "invalid-price");
        ctx.parameters.put("categoryid", "10");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateProductCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/product-edit.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("error"));
    }

    @Test
    public void testPostProductEditCategoryNonexistentValidation() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/edit");
        ctx.parameters.put("productid", "101");
        ctx.parameters.put("productname", "Valid Name");
        ctx.parameters.put("price", "500.00");
        ctx.parameters.put("categoryid", "9999");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(updateProductCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/product-edit.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("error"));
    }

    @Test
    public void testPostProductAddInvalidPriceStringValidation() throws Exception {
        ProductController controller = new ProductController(mockProductService, mockCategoryService);
        MockHttpContext ctx = new MockHttpContext("/products/add");
        ctx.parameters.put("productname", "Smartphone");
        ctx.parameters.put("price", "not-a-number");
        ctx.parameters.put("categoryid", "10");
        ctx.parameters.put("status", "1");

        controller.doPost(ctx.request, ctx.response);

        assertFalse(createProductCalled);
        assertTrue(ctx.forwarded);
        assertEquals("/views/product-add.jsp", ctx.forwardedPath);
        assertNotNull(ctx.attributes.get("error"));
    }
}
