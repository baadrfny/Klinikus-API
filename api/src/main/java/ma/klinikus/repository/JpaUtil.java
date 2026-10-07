package ma.klinikus.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JpaUtil {

    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("klinikusPU");

    private JpaUtil() {
    }

    public static EntityManager createEntityManager() {
        return EMF.createEntityManager();
    }
}