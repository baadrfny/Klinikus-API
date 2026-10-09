package ma.klinikus.resource;

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ma.klinikus.filter.RolesAllowed;
import ma.klinikus.model.Specialiste;
import ma.klinikus.service.SpecialisteService;

@Path("/specialistes")
@RolesAllowed({ "GENERALISTE", "INFIRMIER" })
public class SpecialisteResource {

    private final SpecialisteService service = new SpecialisteService();

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") Long id) {
        return service.trouverParId(id)
                .map(s -> Response.ok(s).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("message", "Specialiste introuvable")).build());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response lister() {
        List<Specialiste> specialistes = service.touverTous();
        return Response.ok(specialistes).build();
    }

    @GET
    @Path("/specialite")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listerParSpecialite(
            @QueryParam("specialite") String specialite) {

        try {
            List<Specialiste> specialistes = service.trouverParSpecialite(specialite);

            return Response.ok(specialistes).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Spécialité invalide"))
                    .build();
        }
    }

}