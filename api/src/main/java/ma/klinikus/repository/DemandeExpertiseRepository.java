package ma.klinikus.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import ma.klinikus.model.DemandeExpertise;
import ma.klinikus.model.enums.Priorite;
import ma.klinikus.model.enums.StatutDemande;

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

    public List<DemandeExpertise> consulterDemandesEnAtt(Long uid) {
        EntityManager em = EMF.createEntityManager();

        try {
            String jpql = """
                    SELECT d
                    FROM DemandeExpertise d
                    JOIN FETCH d.specialiste s
                    WHERE s.utilisateurId.id = :uid
                      AND d.statut = :statut
                    ORDER BY CASE
                        WHEN d.priorite = :urgente THEN 0
                        WHEN d.priorite = :normale THEN 1
                        ELSE 2
                    END
                    """;

            return em.createQuery(jpql, DemandeExpertise.class)
                    .setParameter("uid", uid)
                    .setParameter("statut", StatutDemande.EN_ATTENTE)
                    .setParameter("urgente", Priorite.URGENTE)
                    .setParameter("normale", Priorite.NORMALE)
                    .getResultList();

        } finally {
            em.close();
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