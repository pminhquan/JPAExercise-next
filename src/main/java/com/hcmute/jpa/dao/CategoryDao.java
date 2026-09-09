package com.hcmute.jpa.dao;

import com.hcmute.jpa.config.JpaConfig;
import com.hcmute.jpa.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class CategoryDao implements ICategoryDao {

    @Override
    public void insert(Category category) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        boolean ownsTransaction = !JpaConfig.isTransactionActive();

        try {
            if (ownsTransaction) {
                transaction.begin();
            }

            entityManager.persist(category);

            if (ownsTransaction) {
                transaction.commit();
            }

        } catch (Exception e) {
            if (ownsTransaction && transaction.isActive()) {
                transaction.rollback();
            }

            throw e;

        } finally {
            if (ownsTransaction) {
                entityManager.close();
            }
        }
    }

    @Override
    public void update(Category category) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        boolean ownsTransaction = !JpaConfig.isTransactionActive();

        try {
            if (ownsTransaction) {
                transaction.begin();
            }

            entityManager.merge(category);

            if (ownsTransaction) {
                transaction.commit();
            }

        } catch (Exception e) {
            if (ownsTransaction && transaction.isActive()) {
                transaction.rollback();
            }

            throw e;

        } finally {
            if (ownsTransaction) {
                entityManager.close();
            }
        }
    }

    @Override
    public boolean delete(int id) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        boolean ownsTransaction = !JpaConfig.isTransactionActive();
        boolean deleted = false;

        try {
            if (ownsTransaction) {
                transaction.begin();
            }

            Category category =
                    entityManager.find(Category.class, id);

            if (category != null) {
                entityManager.remove(category);
                deleted = true;
            }

            if (ownsTransaction) {
                transaction.commit();
            }
            return deleted;

        } catch (Exception e) {
            if (ownsTransaction && transaction.isActive()) {
                transaction.rollback();
            }

            throw e;

        } finally {
            if (ownsTransaction) {
                entityManager.close();
            }
        }
    }

    @Override
    public Category findById(int id) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        boolean ownsEntityManager = !JpaConfig.isTransactionActive();

        try {
            return entityManager.find(Category.class, id);

        } finally {
            if (ownsEntityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public List<Category> findAll() {
        EntityManager entityManager = JpaConfig.getEntityManager();
        boolean ownsEntityManager = !JpaConfig.isTransactionActive();

        try {
            TypedQuery<Category> query =
                    entityManager.createNamedQuery(
                            "Category.findAll",
                            Category.class
                    );

            return query.getResultList();

        } finally {
            if (ownsEntityManager) {
                entityManager.close();
            }
        }
     }

    @Override
    public boolean isCategoryInUse(int categoryId) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        boolean ownsEntityManager = !JpaConfig.isTransactionActive();
        try {
            String jpql = "SELECT COUNT(p) FROM Product p WHERE p.category.categoryid = :categoryId";
            Long count = entityManager.createQuery(jpql, Long.class)
                    .setParameter("categoryId", categoryId)
                    .getSingleResult();
            return count > 0;
        } finally {
            if (ownsEntityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public Category findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        EntityManager entityManager = JpaConfig.getEntityManager();
        boolean ownsEntityManager = !JpaConfig.isTransactionActive();
        try {
            String jpql = "SELECT c FROM Category c WHERE LOWER(TRIM(c.categoryname)) = LOWER(:name)";
            List<Category> results = entityManager.createQuery(jpql, Category.class)
                    .setParameter("name", name.trim())
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            if (ownsEntityManager) {
                entityManager.close();
            }
        }
    }
}