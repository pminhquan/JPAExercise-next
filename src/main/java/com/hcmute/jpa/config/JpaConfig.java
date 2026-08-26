package com.hcmute.jpa.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaConfig {

    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY;

    static {
        try {
            ENTITY_MANAGER_FACTORY =
                    Persistence.createEntityManagerFactory(
                            "jpa-hibernate-sqlserver"
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