package ma.klinikus.repository;

import jakarta.persistence.EntityManager;
import ma.klinikus.model.Utilisateur;

import java.util.Optional;

public class UtilisateurRepository {

    public Optional<Utilisateur> findByEmail(String email) {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            return em.createQuery(
                    "SELECT u FROM Utilisateur u WHERE u.email = :email", Utilisateur.class)
                    .setParameter("email", email)
                    .getResultStream()
                    .findFirst();
        }
    }

    public Optional<Utilisateur> findById(Long id) {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            return Optional.ofNullable(em.find(Utilisateur.class, id));
        }
    }
}