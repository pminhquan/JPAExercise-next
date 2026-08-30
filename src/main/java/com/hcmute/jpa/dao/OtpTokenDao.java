package com.hcmute.jpa.dao;

import com.hcmute.jpa.config.JpaConfig;
import com.hcmute.jpa.entity.OtpPurpose;
import com.hcmute.jpa.entity.OtpToken;
import com.hcmute.jpa.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.sql.Timestamp;
import java.util.List;

public class OtpTokenDao implements IOtpTokenDao {

    @Override
    public void create(OtpToken token) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        boolean ownsTransaction = !JpaConfig.isTransactionActive();
        try {
            if (ownsTransaction) {
                transaction.begin();
            }
            entityManager.persist(token);
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
    public void update(OtpToken token) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        boolean ownsTransaction = !JpaConfig.isTransactionActive();
        try {
            if (ownsTransaction) {
                transaction.begin();
            }
            entityManager.merge(token);
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
    public OtpToken findById(int id) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        boolean ownsEntityManager = !JpaConfig.isTransactionActive();
        try {
            return entityManager.find(OtpToken.class, id);
        } finally {
            if (ownsEntityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public OtpToken findLatestValidByUserAndPurpose(User user, OtpPurpose purpose) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        boolean ownsEntityManager = !JpaConfig.isTransactionActive();
        try {
            TypedQuery<OtpToken> query = entityManager.createNamedQuery("OtpToken.findLatestValid", OtpToken.class);
            query.setParameter("user", user);
            query.setParameter("purpose", purpose);
            query.setParameter("now", new Timestamp(System.currentTimeMillis()));
            query.setMaxResults(1);
            List<OtpToken> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            if (ownsEntityManager) {
                entityManager.close();
            }
        }
    }

    @Override
    public void invalidateExistingByUserAndPurpose(User user, OtpPurpose purpose) {
        EntityManager entityManager = JpaConfig.getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        boolean ownsTransaction = !JpaConfig.isTransactionActive();
        try {
            if (ownsTransaction) {
                transaction.begin();
            }
            entityManager.createQuery(
                "UPDATE OtpToken t SET t.used = true WHERE t.user = :user AND t.purpose = :purpose AND t.used = false")
                .setParameter("user", user)
                .setParameter("purpose", purpose)
                .executeUpdate();
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
}
