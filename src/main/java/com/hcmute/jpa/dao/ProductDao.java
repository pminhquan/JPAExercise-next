package com.hcmute.jpa.dao;

import com.hcmute.jpa.config.JpaConfig;
import com.hcmute.jpa.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class ProductDao implements IProductDao {

    @Override
    public void create(Product product) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            entityManager.persist(product);
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
    public Product findById(int id) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        try {
            TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p JOIN FETCH p.category WHERE p.productid = :id", Product.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<Product> findAll() {
        EntityManager entityManager = JpaConfig.getEntityManager();
        try {
            TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p JOIN FETCH p.category ORDER BY p.productid DESC", Product.class);
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void update(Product product) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            entityManager.merge(product);
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
            Product product = entityManager.find(Product.class, id);
            if (product != null) {
                entityManager.remove(product);
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
    public List<Product> findNewest(int limit) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        try {
            TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p JOIN FETCH p.category ORDER BY p.productid DESC", Product.class);
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<Product> findPage(int offset, int limit) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        try {
            TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p JOIN FETCH p.category ORDER BY p.productid DESC", Product.class);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public long countAll() {
        EntityManager entityManager = JpaConfig.getEntityManager();
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(p) FROM Product p", Long.class);
            return query.getSingleResult();
        } finally {
            entityManager.close();
        }
    }
}
