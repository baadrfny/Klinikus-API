package ma.klinikus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import ma.klinikus.model.Specialiste;
import ma.klinikus.repository.SpecialisteRepository;

import java.util.List;

@Path("/specialistes")
public class SpecialisteResource {

    private final SpecialisteRepository repository = new SpecialisteRepository();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Specialiste> lister() {
        return repository.findAll();
    }
}