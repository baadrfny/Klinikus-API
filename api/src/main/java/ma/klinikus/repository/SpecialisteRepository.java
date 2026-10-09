package ma.klinikus.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import ma.klinikus.model.Specialiste;



public class SpecialisteRepository {


    public List<Specialiste> findAll() {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            return em.createQuery(
                    "SELECT s FROM Specialiste s JOIN FETCH s.utilisateurId",
                    Specialiste.class)
                    .getResultList();
        }
    }

    public Optional<Specialiste> findById(Long id) {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            return em.createQuery(
                    "SELECT s FROM Specialiste s JOIN FETCH s.utilisateurId WHERE s.id = :id",
                    Specialiste.class)
                    .setParameter("id", id)
                    .getResultStream().findFirst();
        }
    }


    public List<Specialiste> findBySpecialite(String specialite) {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            return em.createQuery(
                    "SELECT s FROM Specialiste s JOIN FETCH s.utilisateurId WHERE s.specialite = :specialite",
                    Specialiste.class)
                    .setParameter("specialite", specialite)
                    .getResultList();
        }
    }
    
}