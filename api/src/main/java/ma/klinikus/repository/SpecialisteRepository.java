package ma.klinikus.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import ma.klinikus.model.Specialiste;


public class SpecialisteRepository {

    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("klinikusPU");
     


    public List<Specialiste> findAll() {
    try (EntityManager em = emf.createEntityManager()) {
        return em.createQuery("SELECT s FROM Specialiste s", Specialiste.class)
                 .getResultList();
    }
}

    public Optional<Specialiste> findById(Long id) {
        try (EntityManager em = emf.createEntityManager()){
            return Optional.ofNullable(em.find(Specialiste.class, id));
        }
    }
}