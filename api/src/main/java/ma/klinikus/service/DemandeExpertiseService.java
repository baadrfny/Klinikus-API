package ma.klinikus.service;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import ma.klinikus.model.DemandeExpertise;
import ma.klinikus.model.Specialiste;
import ma.klinikus.model.enums.Priorite;
import ma.klinikus.model.enums.StatutDemande;
import ma.klinikus.repository.DemandeExpertiseRepository;
import ma.klinikus.repository.SpecialisteRepository;
import ma.klinikus.service.DemandeExpertiseService.ReponseRequest;

import java.util.Arrays;
import java.util.List;

public class DemandeExpertiseService {

    private final DemandeExpertiseRepository demandeRepository = new DemandeExpertiseRepository();
    private final SpecialisteRepository specialisteRepository = new SpecialisteRepository();

    public DemandeExpertise creer(CreerDemandeRequest req) {
        if (req == null)
            throw new BadRequestException("Corps de la requête manquant");
        if (req.consultationId() == null)
            throw new BadRequestException("consultationId est obligatoire");
        if (req.specialisteId() == null)
            throw new BadRequestException("specialisteId est obligatoire");
        if (req.question() == null || req.question().isBlank())
            throw new BadRequestException("La question ne peut pas être vide");

        Priorite priorite = parsePriorite(req.priorite());

        if (!demandeRepository.consultationExists(req.consultationId()))
            throw new NotFoundException("Consultation introuvable : " + req.consultationId());

        Specialiste specialiste = specialisteRepository.findById(req.specialisteId())
                .orElseThrow(() -> new NotFoundException("Spécialiste introuvable : " + req.specialisteId()));

        DemandeExpertise d = new DemandeExpertise();
        d.setConsultationId(req.consultationId());
        d.setSpecialiste(specialiste);
        d.setQuestion(req.question().trim());
        d.setPriorite(priorite);
        d.setStatut(StatutDemande.EN_ATTENTE);
        return demandeRepository.save(d);
    }

    private Priorite parsePriorite(String value) {
        if (value == null || value.isBlank())
            throw new BadRequestException("La priorité est obligatoire");
        try {
            return Priorite.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Priorité invalide. Valeurs : " + Arrays.toString(Priorite.values()));
        }
    }

    public record CreerDemandeRequest(Long consultationId, Long specialisteId,
            String question, String priorite) {
    }

    public DemandeExpertise repondre(Long id, ReponseRequest req, Long utilisateurId) {
        DemandeExpertise d = demandeRepository.findDemandeById(id)
                .orElseThrow(() -> new NotFoundException("Demande introuvable : " + id));

        Long proprietaire = demandeRepository.findProprietaire(id).orElse(null);
        if (proprietaire == null || !proprietaire.equals(utilisateurId))
            throw new ForbiddenException("Cette demande est adressee a un autre specialiste");

        if (d.getStatut() == StatutDemande.TERMINEE)
            throw new ClientErrorException("Demande deja traitee", Response.Status.CONFLICT);

        if (req == null)
            throw new BadRequestException("Corps de la requete manquant");
        if (req.avis() == null || req.avis().isBlank())
            throw new BadRequestException("L'avis ne peut pas etre vide");
        if (req.recommandations() == null || req.recommandations().isBlank())
            throw new BadRequestException("Les recommandations ne peuvent pas etre vides");

        d.setAvis(req.avis().trim());
        d.setRecommandations(req.recommandations().trim());
        d.setStatut(StatutDemande.TERMINEE);
        return demandeRepository.update(d);
    }

    public List<DemandeExpertise> listerParConsultation(Long consultationId) {
        if (consultationId == null)
            throw new BadRequestException("consultationId est obligatoire");
        if (!demandeRepository.consultationExists(consultationId))
            throw new NotFoundException("Consultation introuvable : " + consultationId);
        return demandeRepository.findByConsultationId(consultationId);
    }

    public record ReponseRequest(String avis, String recommandations) {
    }
}