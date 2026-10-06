package ma.clinique.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "specialiste")
public class Specialiste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "utilisateur_id", nullable = false, unique = true)
    private Long utilisateurId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Specialite specialite;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tarif; // en DH

    public Specialiste() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }

    public Specialite getSpecialite() { return specialite; }
    public void setSpecialite(Specialite specialite) { this.specialite = specialite; }

    public BigDecimal getTarif() { return tarif; }
    public void setTarif(BigDecimal tarif) { this.tarif = tarif; }
}