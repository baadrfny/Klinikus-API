package ma.klinikus.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import ma.klinikus.model.DemandeExpertise;

public class DemandeExpertiseRepository {

    public DemandeExpertise save(DemandeExpertise demandeExpertise) {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            try {
                em.getTransaction().begin();
                em.persist(demandeExpertise);
                em.getTransaction().commit();
                return demandeExpertise;
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive())
                    em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public boolean consultationExists(Long consultationId) {

        try (EntityManager em = JpaUtil.createEntityManager()) {
            String sql = "SELECT COUNT(*) FROM consultation WHERE id = :id";
            Number n = (Number) em.createNativeQuery(sql)
                    .setParameter("id", consultationId)
                    .getSingleResult();
            return n.longValue() > 0;
        }
    }

    public DemandeExpertise update(DemandeExpertise demande) {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            try {
                em.getTransaction().begin();
                DemandeExpertise merged = em.merge(demande);
                em.getTransaction().commit();
                return merged;
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive())
                    em.getTransaction().rollback();
                throw e;
            }
        }
    }

    private List<DemandeExpertise> findWhere(String condition, String param, Object value) {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            return em.createQuery(
                            "SELECT d FROM DemandeExpertise d " +
                                    "JOIN FETCH d.specialiste s JOIN FETCH s.utilisateurId " +
                                    "WHERE " + condition,
                            DemandeExpertise.class)
                    .setParameter(param, value)
                    .getResultList();
        }
    }

    public List<DemandeExpertise> findBySpecialiste(Long uid) {
        return findWhere("s.utilisateurId.id = :uid", "uid", uid);
    }

    public Optional<DemandeExpertise> findDemandeById(Long id) {
        return findWhere("d.id = :id", "id", id).stream().findFirst();
    }

    public List<DemandeExpertise> findByConsultationId(Long consultationId) {
        return findWhere("d.consultationId = :cid ORDER BY d.dateCreation DESC", "cid", consultationId);
    }

}