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

        try {
            transaction.begin();

            entityManager.persist(category);

            transaction.commit();

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw e;

        } finally {
            entityManager.close();
        }
    }

    @Override
    public void update(Category category) {

        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            entityManager.merge(category);

            transaction.commit();

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw e;

        } finally {
            entityManager.close();
        }
    }

    @Override
    public void delete(int id) {

        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            Category category =
                    entityManager.find(Category.class, id);

            if (category != null) {
                entityManager.remove(category);
            }

            transaction.commit();

        } catch (Exception e) {

            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw e;

        } finally {
            entityManager.close();
        }
    }

    @Override
    public Category findById(int id) {

        EntityManager entityManager = JpaConfig.getEntityManager();

        try {
            return entityManager.find(Category.class, id);

        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<Category> findAll() {

        EntityManager entityManager = JpaConfig.getEntityManager();

        try {
            TypedQuery<Category> query =
                    entityManager.createNamedQuery(
                            "Category.findAll",
                            Category.class
                    );

            return query.getResultList();

        } finally {
            entityManager.close();
        }
    }
}