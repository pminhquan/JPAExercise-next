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

        try {
            tx.begin();

            // Create Category
            Category category = new Category();
            category.setCategoryname("Test Category");
            category.setImages("test_cat.jpg");
            category.setStatus(1);
            em.persist(category);

            // Create Product
            Product product = new Product();
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

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Test
    public void testCategoryDeletionFailsWithReferencedProducts() {
        com.hcmute.jpa.dao.ICategoryDao categoryDao = new com.hcmute.jpa.dao.CategoryDao();
        EntityManager em = JpaConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        Category category = null;
        Product product = null;

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

            // Cleanup test data to maintain transaction isolation/no leftover data.
            // We must delete the product first, then the category.
            EntityTransaction cleanupTx = em.getTransaction();
            cleanupTx.begin();
            Product loadedProd = em.find(Product.class, prodId);
            if (loadedProd != null) {
                em.remove(loadedProd);
            }
            Category loadedCat = em.find(Category.class, catId);
            if (loadedCat != null) {
                em.remove(loadedCat);
            }
            cleanupTx.commit();

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            // Cleanup in case of failure
            if (category != null && category.getCategoryid() > 0) {
                try {
                    EntityTransaction cleanupTx = em.getTransaction();
                    cleanupTx.begin();
                    Product loadedProd = em.find(Product.class, product.getProductid());
                    if (loadedProd != null) {
                        em.remove(loadedProd);
                    }
                    Category loadedCat = em.find(Category.class, category.getCategoryid());
                    if (loadedCat != null) {
                        em.remove(loadedCat);
                    }
                    cleanupTx.commit();
                } catch (Exception ex) {
                    // Ignore cleanup failure in exception handler
                }
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
