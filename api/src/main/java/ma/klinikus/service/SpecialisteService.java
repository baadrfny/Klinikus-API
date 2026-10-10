package ma.klinikus.service;

import java.util.Comparator;
import java.util.List;

import jakarta.ws.rs.BadRequestException;
import ma.klinikus.model.Specialiste;
import ma.klinikus.model.enums.Specialite;
import ma.klinikus.repository.SpecialisteRepository;

public class SpecialisteService {

    private final SpecialisteRepository repository = new SpecialisteRepository();

    public List<Specialiste> trouverParSpecialite(String specialite) {
        Specialite specialiteEnum;

        try {
            specialiteEnum = Specialite.valueOf(specialite.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Spécialité invalide");
        }

        return repository.findAll()
                .stream()
                .filter(s -> s.getSpecialite() == specialiteEnum)
                .sorted(Comparator.comparing(Specialiste::getTarif))
                .toList();
    }
}