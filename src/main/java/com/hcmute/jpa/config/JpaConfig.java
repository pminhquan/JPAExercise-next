package com.hcmute.jpa.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaConfig {

    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY;

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
        return ENTITY_MANAGER_FACTORY.createEntityManager();
    }

    public static void close() {
        if (ENTITY_MANAGER_FACTORY != null
                && ENTITY_MANAGER_FACTORY.isOpen()) {

            ENTITY_MANAGER_FACTORY.close();
        }
    }
}