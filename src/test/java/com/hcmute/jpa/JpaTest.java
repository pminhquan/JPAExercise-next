package com.hcmute.jpa;

import com.hcmute.jpa.config.JpaConfig;
import com.hcmute.jpa.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.Test;

public class JpaTest {

    @Test
    public void testInsertCategory() {

        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            Category category = new Category();
            category.setCategoryname("Laptop");
            category.setImages("laptop.jpg");
            category.setStatus(1);

            entityManager.persist(category);

            transaction.commit();

            System.out.println("==============================");
            System.out.println("INSERT CATEGORY SUCCESS");
            System.out.println("Category ID: " + category.getCategoryid());
            System.out.println("==============================");

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            e.printStackTrace();
            throw e;

        } finally {

            entityManager.close();
            JpaConfig.close();
        }
    }
}