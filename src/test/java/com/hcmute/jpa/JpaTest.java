package com.hcmute.jpa;

import com.hcmute.jpa.config.JpaConfig;
import com.hcmute.jpa.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JpaTest {

    @Test
    public void testInsertCategory() {

        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        Category category = null;
        Throwable mainException = null;

        try {
            transaction.begin();

            category = new Category();
            category.setCategoryname("Laptop");
            category.setImages("laptop.jpg");
            category.setStatus(1);

            entityManager.persist(category);

            transaction.commit();

            System.out.println("==============================");
            System.out.println("INSERT CATEGORY SUCCESS");
            System.out.println("Category ID: " + category.getCategoryid());
            System.out.println("==============================");

        } catch (Throwable e) {
            mainException = e;
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;

        } finally {
            if (category != null && category.getCategoryid() > 0) {
                int catId = category.getCategoryid();
                EntityTransaction cleanupTx = entityManager.getTransaction();
                try {
                    // Prep: clear EntityManager and ensure any preparation errors are propagated
                    entityManager.clear();

                    cleanupTx.begin();

                    // Check if any product references this category; if so, fail cleanup rather than bulk deleting
                    long count = entityManager.createQuery("SELECT COUNT(p) FROM Product p WHERE p.category.categoryid = :catId", Long.class)
                                              .setParameter("catId", catId)
                                              .getSingleResult();
                    if (count > 0) {
                        throw new IllegalStateException("Cleanup failed: Category has referencing products: " + count);
                    }

                    Category toRemove = entityManager.find(Category.class, catId);
                    if (toRemove != null) {
                        entityManager.remove(toRemove);
                    }
                    cleanupTx.commit();

                    // Verification: prove that the category is absent
                    entityManager.clear();
                    Category checked = entityManager.find(Category.class, catId);
                    assertNull(checked, "Category should be deleted after test cleanup");

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
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }
}
