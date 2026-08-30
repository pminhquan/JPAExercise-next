package com.hcmute.jpa.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaConfig {

    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY;
    private static final ThreadLocal<EntityManager> CURRENT_ENTITY_MANAGER = new ThreadLocal<>();

    static {
        try {
            java.util.Map<String, String> properties = new java.util.HashMap<>();

            String dbUrl = System.getProperty("jakarta.persistence.jdbc.url");
            if (dbUrl == null) {
                dbUrl = System.getenv("DB_URL");
            }
            if (dbUrl == null) {
                dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=jakartaJPA;encrypt=true;trustServerCertificate=true";
            }
            properties.put("jakarta.persistence.jdbc.url", dbUrl);

            String dbUser = System.getProperty("jakarta.persistence.jdbc.user");
            if (dbUser == null) {
                dbUser = System.getenv("DB_USER");
            }
            if (dbUser == null) {
                dbUser = "sa";
            }
            properties.put("jakarta.persistence.jdbc.user", dbUser);

            String dbPassword = System.getProperty("jakarta.persistence.jdbc.password");
            if (dbPassword == null) {
                dbPassword = System.getenv("DB_PASSWORD");
            }
            if (dbPassword == null) {
                throw new IllegalStateException("Database password is not configured. Please set the DB_PASSWORD environment variable or the jakarta.persistence.jdbc.password system property.");
            }
            properties.put("jakarta.persistence.jdbc.password", dbPassword);

            ENTITY_MANAGER_FACTORY =
                    Persistence.createEntityManagerFactory(
                            "jpa-hibernate-sqlserver",
                            properties
                    );
        } catch (Exception e) {
            System.err.println("Cannot create EntityManagerFactory");
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    private JpaConfig() {
    }

    public static EntityManager getEntityManager() {
        EntityManager currentEntityManager = CURRENT_ENTITY_MANAGER.get();
        return currentEntityManager != null
                ? currentEntityManager
                : ENTITY_MANAGER_FACTORY.createEntityManager();
    }

    public static boolean isTransactionActive() {
        EntityManager currentEntityManager = CURRENT_ENTITY_MANAGER.get();
        return currentEntityManager != null
                && currentEntityManager.getTransaction().isActive();
    }

    public static void beginTransaction() {
        if (CURRENT_ENTITY_MANAGER.get() != null) {
            throw new IllegalStateException("A transaction is already active on this thread");
        }

        EntityManager entityManager = ENTITY_MANAGER_FACTORY.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            CURRENT_ENTITY_MANAGER.set(entityManager);
        } catch (RuntimeException e) {
            entityManager.close();
            throw e;
        }
    }

    public static void commitTransaction() {
        requireCurrentEntityManager().getTransaction().commit();
    }

    public static void rollbackTransaction() {
        EntityManager entityManager = CURRENT_ENTITY_MANAGER.get();
        if (entityManager != null
                && entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }
    }

    public static void endTransaction() {
        EntityManager entityManager = CURRENT_ENTITY_MANAGER.get();
        if (entityManager != null) {
            try {
                if (entityManager.isOpen()) {
                    entityManager.close();
                }
            } finally {
                CURRENT_ENTITY_MANAGER.remove();
            }
        }
    }

    private static EntityManager requireCurrentEntityManager() {
        EntityManager entityManager = CURRENT_ENTITY_MANAGER.get();
        if (entityManager == null) {
            throw new IllegalStateException("No transaction is active on this thread");
        }
        return entityManager;
    }

    public static void close() {
        if (ENTITY_MANAGER_FACTORY != null
                && ENTITY_MANAGER_FACTORY.isOpen()) {

            ENTITY_MANAGER_FACTORY.close();
        }
    }
}
