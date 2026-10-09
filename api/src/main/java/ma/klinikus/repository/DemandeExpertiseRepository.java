package ma.klinikus.repository;

import java.util.Optional;

import jakarta.persistence.EntityManager;
import ma.klinikus.model.DemandeExpertise;

public class DemandeExpertiseRepository {

    public DemandeExpertise save(DemandeExpertise demandeExpertise) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(demandeExpertise);
            em.getTransaction().commit();
            return demandeExpertise;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean consultationExists(Long consultationId) {

        try (EntityManager em = JpaUtil.createEntityManager();) {
            String sql = "SELECT COUNT(*) FROM consultation WHERE id = :id";
            Number n = (Number) em.createNativeQuery(sql)
                    .setParameter("id", consultationId)
                    .getSingleResult();
            return n.longValue() > 0;
        }
    }

    public Optional<DemandeExpertise> findDemandeById(Long id) {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            return em.createQuery(
                    "SELECT d FROM DemandeExpertise d JOIN FETCH d.specialiste WHERE d.id = :id",
                    DemandeExpertise.class)
                    .setParameter("id", id)
                    .getResultStream().findFirst();
        }
    }

    public Optional<Long> findProprietaire(Long demandeId) {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            return em.createQuery(
                    "SELECT s.utilisateurId.id FROM DemandeExpertise d JOIN d.specialiste s WHERE d.id = :id",
                    Long.class)
                    .setParameter("id", demandeId)
                    .getResultStream().findFirst();
        }
    }

    public DemandeExpertise update(DemandeExpertise demande) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            em.getTransaction().begin();
            DemandeExpertise merged = em.merge(demande);
            em.getTransaction().commit();
            return merged;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

}