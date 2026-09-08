package com.hcmute.jpa.dao;

import com.hcmute.jpa.config.JpaConfig;
import com.hcmute.jpa.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class UserDao implements IUserDao {

    @Override
    public void create(User user) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        boolean ownsTransaction = !JpaConfig.isTransactionActive();
        try {
            if (ownsTransaction) {
                transaction.begin();
            }
            entityManager.persist(user);
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
    public User findById(int id) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        boolean ownsEntityManager = !JpaConfig.isTransactionActive();
        try {
            return entityManager.find(User.class, id);
        } finally {
            if (ownsEntityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public User findByEmail(String email) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        boolean ownsEntityManager = !JpaConfig.isTransactionActive();
        try {
            TypedQuery<User> query = entityManager.createNamedQuery("User.findByEmail", User.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            if (ownsEntityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public User findByUsername(String username) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        boolean ownsEntityManager = !JpaConfig.isTransactionActive();
        try {
            TypedQuery<User> query = entityManager.createNamedQuery("User.findByUsername", User.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            if (ownsEntityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        boolean ownsEntityManager = !JpaConfig.isTransactionActive();
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
            query.setParameter("email", email);
            return query.getSingleResult() > 0;
        } finally {
            if (ownsEntityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        boolean ownsEntityManager = !JpaConfig.isTransactionActive();
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class);
            query.setParameter("username", username);
            return query.getSingleResult() > 0;
        } finally {
            if (ownsEntityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public void update(User user) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        boolean ownsTransaction = !JpaConfig.isTransactionActive();
        try {
            if (ownsTransaction) {
                transaction.begin();
            }
            entityManager.merge(user);
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
    public long countAllUsers() {
        EntityManager entityManager = JpaConfig.getEntityManager();
        boolean ownsEntityManager = !JpaConfig.isTransactionActive();
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(u) FROM User u", Long.class);
            return query.getSingleResult();
        } finally {
            if (ownsEntityManager) {
                entityManager.close();
            }
        }
    }
}
