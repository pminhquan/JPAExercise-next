package com.hcmute.jpa;

import com.hcmute.jpa.config.JpaConfig;
import com.hcmute.jpa.dao.CategoryDao;
import com.hcmute.jpa.dao.ICategoryDao;
import com.hcmute.jpa.entity.Category;
import com.hcmute.jpa.entity.Product;
import com.hcmute.jpa.service.IProductService;
import com.hcmute.jpa.service.ProductServiceImpl;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductServiceTest {

    private static IProductService productService;
    private static ICategoryDao categoryDao;
    private static Category testCategory;
    
    private final List<Integer> productIdsToCleanup = new ArrayList<>();

    @BeforeAll
    public static void setUp() {
        productService = new ProductServiceImpl();
        categoryDao = new CategoryDao();

        // Create a persistent category for testing
        testCategory = new Category();
        testCategory.setCategoryname("CRUD Category");
        testCategory.setImages("crud.jpg");
        testCategory.setStatus(1);
        categoryDao.insert(testCategory);
    }

    @AfterAll
    public static void tearDown() {
        if (testCategory != null) {
            categoryDao.delete(testCategory.getCategoryid());
        }

    }

    @AfterEach
    public void cleanupProducts() {
        for (int id : productIdsToCleanup) {
            try {
                productService.deleteProduct(id);
            } catch (Exception e) {
                System.err.println("Test cleanup failure: could not delete product " + id);
                throw new RuntimeException("Reliable cleanup failed for product id " + id, e);
            }
        }
        productIdsToCleanup.clear();
    }

    @Test
    @Order(1)
    public void testCreateAndFindProduct() {
        Product product = new Product();
        product.setProductname("Gaming Laptop");
        product.setDescription("High performance gaming laptop");
        product.setPrice(1200.00);
        product.setImages("laptop.jpg");
        product.setStatus(1);
        product.setCategory(testCategory);

        productService.createProduct(product);
        productIdsToCleanup.add(product.getProductid());
        assertTrue(product.getProductid() > 0);

        Product found = productService.getProductById(product.getProductid());
        assertNotNull(found);
        assertEquals("Gaming Laptop", found.getProductname());
        assertEquals(1200.00, found.getPrice());
        assertEquals(testCategory.getCategoryid(), found.getCategory().getCategoryid());

        // Exercise getAllProducts and assert the created fixture is present
        List<Product> all = productService.getAllProducts();
        boolean foundInAll = all.stream().anyMatch(p -> p.getProductid() == product.getProductid());
        assertTrue(foundInAll, "Created product should be present in getAllProducts()");

        // Delete product
        productService.deleteProduct(product.getProductid());
        productIdsToCleanup.remove((Integer) product.getProductid());

        // Assert getProductById returns null after delete
        assertNull(productService.getProductById(product.getProductid()), "Product must be null after deletion");
    }

    @Test
    @Order(2)
    public void testValidationBlankName() {
        Product product = new Product();
        product.setProductname("   ");
        product.setPrice(100);
        product.setCategory(testCategory);

        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(product));
    }

    @Test
    @Order(3)
    public void testValidationPriceLessThanOrEqualToZero() {
        Product product = new Product();
        product.setProductname("Valid Name");
        product.setPrice(0);
        product.setCategory(testCategory);

        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(product));

        product.setPrice(-50);
        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(product));
    }

    @Test
    @Order(4)
    public void testValidationFractionalPrice() {
        Product product = new Product();
        product.setProductname("Valid Name");
        product.setPrice(19.99);
        product.setCategory(testCategory);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct(product));
        assertEquals("Price must be a whole number", error.getMessage());
    }

    @Test
    @Order(5)
    public void testValidationNonexistentCategory() {
        Category nonexistent = new Category();
        nonexistent.setCategoryid(999999);

        Product product = new Product();
        product.setProductname("Valid Name");
        product.setPrice(100);
        product.setCategory(nonexistent);

        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(product));
    }

    @Test
    @Order(6)
    public void testUpdateProductAndPreserveCategory() {
        Product product = new Product();
        product.setProductname("Original Name");
        product.setPrice(100);
        product.setCategory(testCategory);

        productService.createProduct(product);
        productIdsToCleanup.add(product.getProductid());

        Product updated = new Product();
        updated.setProductid(product.getProductid());
        updated.setProductname("Updated Name");
        updated.setPrice(150);
        updated.setCategory(null);

        productService.updateProduct(updated);

        Product found = productService.getProductById(product.getProductid());
        assertEquals("Updated Name", found.getProductname());
        assertEquals(150.00, found.getPrice());
        assertNotNull(found.getCategory());
        assertEquals(testCategory.getCategoryid(), found.getCategory().getCategoryid());

        productService.deleteProduct(product.getProductid());
        productIdsToCleanup.remove((Integer) product.getProductid());
    }

    @Test
    @Order(7)
    public void testFindAllNewestAndPagination() {
        Product p1 = new Product("A", "Desc", 10.0, "img.jpg", 1, testCategory);
        Product p2 = new Product("B", "Desc", 20.0, "img.jpg", 1, testCategory);
        Product p3 = new Product("C", "Desc", 30.0, "img.jpg", 1, testCategory);

        long beforeCount = productService.countAllProducts();

        productService.createProduct(p1);
        productIdsToCleanup.add(p1.getProductid());
        
        productService.createProduct(p2);
        productIdsToCleanup.add(p2.getProductid());
        
        productService.createProduct(p3);
        productIdsToCleanup.add(p3.getProductid());

        // Assert countAll against exact before/after expectation
        long afterCount = productService.countAllProducts();
        assertEquals(beforeCount + 3, afterCount, "Exact countAll verification failed");

        // findNewest
        List<Product> newest = productService.getNewestProducts(2);
        assertEquals(2, newest.size());
        assertEquals(p3.getProductid(), newest.get(0).getProductid());
        assertEquals(p2.getProductid(), newest.get(1).getProductid());

        // Pagination: test non-overlapping pages and assert no duplicate/missing IDs
        List<Product> page1 = productService.getProductsPage(0, 2);
        assertEquals(2, page1.size());
        assertEquals(p3.getProductid(), page1.get(0).getProductid());
        assertEquals(p2.getProductid(), page1.get(1).getProductid());

        List<Product> page2 = productService.getProductsPage(2, 2);
        assertTrue(page2.size() >= 1);
        assertEquals(p1.getProductid(), page2.get(0).getProductid());

        // Assert no duplicate IDs across the page boundary
        for (Product prod1 : page1) {
            for (Product prod2 : page2) {
                assertNotEquals(prod1.getProductid(), prod2.getProductid(), 
                    "Duplicate product ID found across page boundary");
            }
        }

        productService.deleteProduct(p1.getProductid());
        productIdsToCleanup.remove((Integer) p1.getProductid());
        
        productService.deleteProduct(p2.getProductid());
        productIdsToCleanup.remove((Integer) p2.getProductid());
        
        productService.deleteProduct(p3.getProductid());
        productIdsToCleanup.remove((Integer) p3.getProductid());
    }

    @Test
    @Order(8)
    public void testValidationNaNAndInfinities() {
        // Create validation
        Product product = new Product();
        product.setProductname("NaN Test Product");
        product.setCategory(testCategory);

        product.setPrice(Double.NaN);
        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(product));

        product.setPrice(Double.POSITIVE_INFINITY);
        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(product));

        product.setPrice(Double.NEGATIVE_INFINITY);
        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(product));

        // Update validation
        Product valid = new Product("Valid Update", "Desc", 10.0, "img.jpg", 1, testCategory);
        productService.createProduct(valid);
        productIdsToCleanup.add(valid.getProductid());

        Product toUpdate = new Product();
        toUpdate.setProductid(valid.getProductid());
        toUpdate.setProductname("Valid Update");
        toUpdate.setCategory(testCategory);

        toUpdate.setPrice(Double.NaN);
        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(toUpdate));

        toUpdate.setPrice(Double.POSITIVE_INFINITY);
        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(toUpdate));

        toUpdate.setPrice(Double.NEGATIVE_INFINITY);
        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(toUpdate));

        productService.deleteProduct(valid.getProductid());
        productIdsToCleanup.remove((Integer) valid.getProductid());
    }

    @Test
    @Order(9)
    public void testUpdateProductValidationCategory() {
        Product valid = new Product("Valid Update Cat", "Desc", 10.0, "img.jpg", 1, testCategory);
        productService.createProduct(valid);
        productIdsToCleanup.add(valid.getProductid());

        // 1. Update with transient category (id <= 0) - should reject
        Category transientCategory = new Category();
        transientCategory.setCategoryid(0);

        Product updateTransient = new Product();
        updateTransient.setProductid(valid.getProductid());
        updateTransient.setProductname("Valid Update Cat");
        updateTransient.setPrice(10.0);
        updateTransient.setCategory(transientCategory);

        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(updateTransient));

        Product checkTransient = productService.getProductById(valid.getProductid());
        assertEquals(testCategory.getCategoryid(), checkTransient.getCategory().getCategoryid(), 
            "Category should remain unchanged after rejected transient update");

        // 2. Update with nonexistent positive category id - should reject
        Category nonexistentCategory = new Category();
        nonexistentCategory.setCategoryid(999999);

        Product updateNonexistent = new Product();
        updateNonexistent.setProductid(valid.getProductid());
        updateNonexistent.setProductname("Valid Update Cat");
        updateNonexistent.setPrice(10.0);
        updateNonexistent.setCategory(nonexistentCategory);

        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(updateNonexistent));

        Product checkNonexistent = productService.getProductById(valid.getProductid());
        assertEquals(testCategory.getCategoryid(), checkNonexistent.getCategory().getCategoryid(),
            "Category should remain unchanged after rejected nonexistent category update");

        productService.deleteProduct(valid.getProductid());
        productIdsToCleanup.remove((Integer) valid.getProductid());
    }

    @Test
    @Order(10)
    public void testExplicitAcceptanceCoverage() {
        // 1. create with null Category rejects
        Product pNullCat = new Product();
        pNullCat.setProductname("Null Cat Test");
        pNullCat.setPrice(100);
        pNullCat.setCategory(null);
        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(pNullCat));

        // 2. create with transient Category id <= 0 rejects
        Product pTransientCat = new Product();
        pTransientCat.setProductname("Transient Cat Test");
        pTransientCat.setPrice(100);
        Category transientCat = new Category();
        transientCat.setCategoryid(0);
        pTransientCat.setCategory(transientCat);
        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(pTransientCat));

        // Create a valid product for update testing
        Product valid = new Product("Valid Init", "Desc", 10.0, "img.jpg", 1, testCategory);
        productService.createProduct(valid);
        productIdsToCleanup.add(valid.getProductid());

        // 3. update with blank Product name rejects
        Product toUpdateBlankName = new Product();
        toUpdateBlankName.setProductid(valid.getProductid());
        toUpdateBlankName.setProductname("   ");
        toUpdateBlankName.setPrice(10.0);
        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(toUpdateBlankName));

        // 4. update with price 0 and negative rejects
        Product toUpdateZeroPrice = new Product();
        toUpdateZeroPrice.setProductid(valid.getProductid());
        toUpdateZeroPrice.setProductname("Valid Init");
        toUpdateZeroPrice.setPrice(0);
        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(toUpdateZeroPrice));

        Product toUpdateNegativePrice = new Product();
        toUpdateNegativePrice.setProductid(valid.getProductid());
        toUpdateNegativePrice.setProductname("Valid Init");
        toUpdateNegativePrice.setPrice(-20.0);
        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(toUpdateNegativePrice));

        // 5. update with a valid different persisted Category succeeds and changes to that category
        Category secondCategory = new Category();
        secondCategory.setCategoryname("Second Category");
        secondCategory.setImages("sec.jpg");
        secondCategory.setStatus(1);
        categoryDao.insert(secondCategory);

        try {
            Product toUpdateDifferentCat = new Product();
            toUpdateDifferentCat.setProductid(valid.getProductid());
            toUpdateDifferentCat.setProductname("Valid Init");
            toUpdateDifferentCat.setPrice(10.0);
            toUpdateDifferentCat.setCategory(secondCategory);

            productService.updateProduct(toUpdateDifferentCat);

            Product found = productService.getProductById(valid.getProductid());
            assertNotNull(found);
            assertNotNull(found.getCategory());
            assertEquals(secondCategory.getCategoryid(), found.getCategory().getCategoryid(), 
                "Category should be successfully changed to secondCategory");
        } finally {
            productService.deleteProduct(valid.getProductid());
            productIdsToCleanup.remove((Integer) valid.getProductid());
            categoryDao.delete(secondCategory.getCategoryid());
        }
    }

    @Test
    @Order(11)
    public void testFindNewestOrdersByCreatedAt() {
        Product pA = new Product("A", "Desc", 10.0, "img.jpg", 1, testCategory);
        Product pB = new Product("B", "Desc", 20.0, "img.jpg", 1, testCategory);

        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
        pA.setCreatedAt(new java.sql.Timestamp(now.getTime() + 120000));
        pB.setCreatedAt(new java.sql.Timestamp(now.getTime() + 60000));

        productService.createProduct(pA);
        productIdsToCleanup.add(pA.getProductid());

        productService.createProduct(pB);
        productIdsToCleanup.add(pB.getProductid());

        List<Product> newest = productService.getNewestProducts(2);

        assertEquals(pA.getProductid(), newest.get(0).getProductid(), "Product with later createdAt must be first");
        assertEquals(pB.getProductid(), newest.get(1).getProductid(), "Product with earlier createdAt must be second");

        productService.deleteProduct(pA.getProductid());
        productIdsToCleanup.remove((Integer) pA.getProductid());

        productService.deleteProduct(pB.getProductid());
        productIdsToCleanup.remove((Integer) pB.getProductid());
    }
}
