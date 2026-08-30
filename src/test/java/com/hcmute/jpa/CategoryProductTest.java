package com.hcmute.jpa;

import com.hcmute.jpa.config.JpaConfig;
import com.hcmute.jpa.entity.Category;
import com.hcmute.jpa.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryProductTest {

    @Test
    public void testCategoryProductRelationship() {
        EntityManager em = JpaConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        Category category = null;
        Product product = null;
        Throwable mainException = null;

        try {
            tx.begin();

            // Create Category
            category = new Category();
            category.setCategoryname("Test Category");
            category.setImages("test_cat.jpg");
            category.setStatus(1);
            em.persist(category);

            // Create Product
            product = new Product();
            product.setProductname("Test Product");
            product.setDescription("Test Description");
            product.setPrice(99.99);
            product.setImages("test_prod.jpg");
            product.setStatus(1);
            product.setCategory(category);
            em.persist(product);

            tx.commit();

            int categoryId = category.getCategoryid();
            int productId = product.getProductid();

            em.clear(); // Force reload from database

            // Find category and verify products list
            Category foundCategory = em.find(Category.class, categoryId);
            assertNotNull(foundCategory);
            assertNotNull(foundCategory.getProducts());
            assertFalse(foundCategory.getProducts().isEmpty());

            Product foundProductInList = foundCategory.getProducts().get(0);
            assertEquals("Test Product", foundProductInList.getProductname());
            assertEquals(productId, foundProductInList.getProductid());

            // Find product and verify category association
            Product foundProduct = em.find(Product.class, productId);
            assertNotNull(foundProduct);
            assertNotNull(foundProduct.getCategory());
            assertEquals("Test Category", foundProduct.getCategory().getCategoryname());

        } catch (Throwable e) {
            mainException = e;
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            if (category != null && category.getCategoryid() > 0) {
                int catId = category.getCategoryid();
                int prodId = (product != null) ? product.getProductid() : 0;
                EntityTransaction cleanupTx = em.getTransaction();
                try {
                    // Prep: clear EntityManager and ensure any preparation errors are propagated
                    em.clear();

                    cleanupTx.begin();

                    // 1. Delete only the captured product ID
                    if (prodId > 0) {
                        Product toRemoveProduct = em.find(Product.class, prodId);
                        if (toRemoveProduct != null) {
                            em.remove(toRemoveProduct);
                        }
                    }

                    // 2. Check if any OTHER products remain for the category
                    long count = em.createQuery("SELECT COUNT(p) FROM Product p WHERE p.category.categoryid = :catId AND p.productid <> :prodId", Long.class)
                                   .setParameter("catId", catId)
                                   .setParameter("prodId", prodId)
                                   .getSingleResult();
                    if (count > 0) {
                        throw new IllegalStateException("Cleanup failed: Category has unexpected referencing products: " + count);
                    }

                    // 3. Delete category
                    Category toRemoveCategory = em.find(Category.class, catId);
                    if (toRemoveCategory != null) {
                        em.remove(toRemoveCategory);
                    }
                    cleanupTx.commit();

                    // Verification: prove that the rows are absent from DB
                    em.clear();
                    if (prodId > 0) {
                        assertNull(em.find(Product.class, prodId), "Product should be deleted after test cleanup");
                    }
                    assertNull(em.find(Category.class, catId), "Category should be deleted after test cleanup");

                } catch (Throwable ex) {
                    if (cleanupTx.isActive()) {
                        try {
                            cleanupTx.rollback();
                        } catch (Exception rex) {
                            ex.addSuppressed(rex);
                        }
                    }
                    if (mainException != null) {
                        mainException.addSuppressed(ex);
                    } else {
                        if (ex instanceof RuntimeException) {
                            throw (RuntimeException) ex;
                        } else if (ex instanceof Error) {
                            throw (Error) ex;
                        } else {
                            throw new RuntimeException("Cleanup verification failed", ex);
                        }
                    }
                }
            }
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Test
    public void testCategoryDeletionFailsWithReferencedProducts() {
        com.hcmute.jpa.dao.ICategoryDao categoryDao = new com.hcmute.jpa.dao.CategoryDao();
        EntityManager em = JpaConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        Category category = null;
        Product product = null;
        Throwable mainException = null;

        try {
            tx.begin();

            // Create Category
            category = new Category();
            category.setCategoryname("Deletion Test Category");
            category.setImages("test_del_cat.jpg");
            category.setStatus(1);
            em.persist(category);

            // Create Product linked to Category
            product = new Product();
            product.setProductname("Deletion Test Product");
            product.setDescription("Test Desc");
            product.setPrice(10.0);
            product.setImages("test_del_prod.jpg");
            product.setStatus(1);
            product.setCategory(category);
            em.persist(product);

            tx.commit();

            final int catId = category.getCategoryid();
            final int prodId = product.getProductid();

            // Attempt to delete category via actual DAO delete path
            // Because cascade = CascadeType.REMOVE is disabled, this should throw an exception due to foreign key constraint violation.
            assertThrows(Exception.class, () -> {
                categoryDao.delete(catId);
            });

            // Verify both category and product still exist in database
            em.clear();
            Category stillCategory = em.find(Category.class, catId);
            Product stillProduct = em.find(Product.class, prodId);

            assertNotNull(stillCategory);
            assertNotNull(stillProduct);

        } catch (Throwable e) {
            mainException = e;
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            if (category != null && category.getCategoryid() > 0) {
                int catId = category.getCategoryid();
                int prodId = (product != null) ? product.getProductid() : 0;
                EntityTransaction cleanupTx = em.getTransaction();
                try {
                    // Prep: clear EntityManager and ensure any preparation errors are propagated
                    em.clear();

                    cleanupTx.begin();

                    // 1. Delete only the captured product ID
                    if (prodId > 0) {
                        Product loadedProd = em.find(Product.class, prodId);
                        if (loadedProd != null) {
                            em.remove(loadedProd);
                        }
                    }

                    // 2. Check if any OTHER products remain for the category
                    long count = em.createQuery("SELECT COUNT(p) FROM Product p WHERE p.category.categoryid = :catId AND p.productid <> :prodId", Long.class)
                                   .setParameter("catId", catId)
                                   .setParameter("prodId", prodId)
                                   .getSingleResult();
                    if (count > 0) {
                        throw new IllegalStateException("Cleanup failed: Category has unexpected referencing products: " + count);
                    }

                    // 3. Delete category
                    Category loadedCat = em.find(Category.class, catId);
                    if (loadedCat != null) {
                        em.remove(loadedCat);
                    }
                    cleanupTx.commit();

                    // Verification: prove that the rows are absent from DB
                    em.clear();
                    if (prodId > 0) {
                        assertNull(em.find(Product.class, prodId), "Product should be deleted after test cleanup");
                    }
                    assertNull(em.find(Category.class, catId), "Category should be deleted after test cleanup");

                } catch (Throwable ex) {
                    if (cleanupTx.isActive()) {
                        try {
                            cleanupTx.rollback();
                        } catch (Exception rex) {
                            ex.addSuppressed(rex);
                        }
                    }
                    if (mainException != null) {
                        mainException.addSuppressed(ex);
                    } else {
                        if (ex instanceof RuntimeException) {
                            throw (RuntimeException) ex;
                        } else if (ex instanceof Error) {
                            throw (Error) ex;
                        } else {
                            throw new RuntimeException("Cleanup verification failed", ex);
                        }
                    }
                }
            }
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}
