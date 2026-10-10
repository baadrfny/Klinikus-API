package ma.klinikus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.security.RolesAllowed;
import ma.klinikus.service.SpecialisteService;

@Path("/specialistes")
@RolesAllowed({ "GENERALISTE" })
public class SpecialisteResource {

    private final SpecialisteService service = new SpecialisteService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listerParSpecialite(@QueryParam("specialite") String specialite) {
        return Response.ok(service.trouverParSpecialite(specialite)).build();
    }
}