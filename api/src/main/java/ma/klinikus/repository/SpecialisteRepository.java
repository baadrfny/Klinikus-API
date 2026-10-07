package ma.clinique.api.repository;

import jakarta.persistence.EntityManager;
import ma.clinique.api.model.Specialiste;
import ma.clinique.api.util.JPAUtil; // à adapter à ton socle

import java.util.List;
import java.util.Optional;

public class SpecialisteRepository {

    public List<Specialiste> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT s FROM Specialiste s", Specialiste.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Specialiste> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Specialiste.class, id));
        } finally {
            em.close();
        }
    }
}