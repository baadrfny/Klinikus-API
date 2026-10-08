package ma.klinikus.repository;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import ma.klinikus.model.DemandeExpertise;

public class DemandeExpertiseRepository {

    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("klinikusPU");

    public DemandeExpertise save(DemandeExpertise demandeExpertise) {
        EntityManager em = EMF.createEntityManager();
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
        EntityManager em = EMF.createEntityManager();
        try {
            String sql = "SELECT COUNT(*) FROM consultation WHERE id = :id";
            Number n = (Number) em.createNativeQuery(sql)
                    .setParameter("id", consultationId)
                    .getSingleResult();
            return n.longValue() > 0;
        } finally {
            em.close();
        }
    }

    public List<DemandeExpertise> consulterDemandesEnAtt(Long uid) {
        EntityManager em = EMF.createEntityManager();
        try {
            String jpql = "SELECT d FROM DemandeExpertise d JOIN FETCH d.specialiste s  WHERE s.utilisateurId.id = :uid";

            return em.createQuery(jpql, DemandeExpertise.class)
                    .setParameter("uid", uid)
                    .getResultList();
        } finally {
            em.close();
        }
    }

}