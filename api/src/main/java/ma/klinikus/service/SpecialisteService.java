package ma.klinikus.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import ma.klinikus.model.Specialiste;
import ma.klinikus.model.enums.Specialite;
import ma.klinikus.repository.SpecialisteRepository;

public class SpecialisteService {

    private final SpecialisteRepository repository = new SpecialisteRepository();

    public List<Specialiste> touverTous(){
        return repository.findAll();
    }

    public Optional<Specialiste> trouverParId(Long id) {
        return repository.findById(id);
    }

    public List<Specialiste> trouverParSpecialite(String specialite) {
    Specialite specialiteEnum;

    try {
        specialiteEnum = Specialite.valueOf(
                specialite.trim().toUpperCase()
        );
    } catch (IllegalArgumentException | NullPointerException e) {
        throw new IllegalArgumentException("Spécialité invalide");
    }

    return repository.findAll()
            .stream()
            .filter(s -> s.getSpecialite() == specialiteEnum)
            .sorted(Comparator.comparing(Specialiste::getTarif))
            .toList();
}
}