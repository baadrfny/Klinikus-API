package ma.klinikus.service;

import ma.klinikus.model.Specialiste;
import ma.klinikus.repository.SpecialisteRepository;

import java.util.List;
import java.util.Optional;

public class SpecialisteService {

    private final SpecialisteRepository repository = new SpecialisteRepository();

    public List<Specialiste> touverTous(){
        return repository.findAll();
    }

    public Optional<Specialiste> trouverParId(Long id) {
        return repository.findById(id);
    }

    public List<Specialiste> touverParSpecialite(String specialite) {
        return repository.findBySpecialite(specialite);
    }
}