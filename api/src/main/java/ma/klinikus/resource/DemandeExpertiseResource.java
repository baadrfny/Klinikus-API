package ma.klinikus.resource;

import ma.klinikus.filter.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ma.klinikus.model.DemandeExpertise;
import ma.klinikus.service.DemandeExpertiseService;
import ma.klinikus.service.DemandeExpertiseService.CreerDemandeRequest;
import jakarta.ws.rs.core.SecurityContext;
import ma.klinikus.filter.SecurityContextImpl.UserPrincipal;

@Path("/demandes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DemandeExpertiseResource {

    private final DemandeExpertiseService service = new DemandeExpertiseService();

    @POST
    @RolesAllowed({ "GENERALISTE" })
    public Response creer(CreerDemandeRequest request) {
        DemandeExpertise d = service.creer(request);
        return Response.status(Response.Status.CREATED).entity(d).build();
    }

    @GET
    @Path("/attente")
    @RolesAllowed({ "SPECIALISTE" })
    public Response consulterDemandesEnAtt(@Context SecurityContext securityContext) {

        UserPrincipal principal = (UserPrincipal) securityContext.getUserPrincipal();

        Long uid = principal.getId();

        return Response.ok(service.consulterDemandesEnAtt(uid)).build();
    }
}