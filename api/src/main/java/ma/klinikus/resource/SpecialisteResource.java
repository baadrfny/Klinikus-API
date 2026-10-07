package ma.klinikus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ma.klinikus.model.Specialiste;
import ma.klinikus.repository.SpecialisteRepository;

import java.util.List;

@Path("/specialistes")
public class SpecialisteResource {

    private final SpecialisteRepository repository = new SpecialisteRepository();

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") Long id){
        return repository.findById(id).map(s -> Response.ok(s).build())
                                      .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }


}