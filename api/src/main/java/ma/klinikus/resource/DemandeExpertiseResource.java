package ma.klinikus.resource;

import ma.klinikus.filter.RolesAllowed;
import ma.klinikus.filter.SecurityContextImpl.UserPrincipal;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import ma.klinikus.model.DemandeExpertise;
import ma.klinikus.service.DemandeExpertiseService;
import ma.klinikus.service.DemandeExpertiseService.CreerDemandeRequest;
import ma.klinikus.service.DemandeExpertiseService.ReponseRequest;

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
    @RolesAllowed({ "SPECIALISTE" })
    public Response consulterDemandes(
            @QueryParam("statut") String statut,
            @Context SecurityContext securityContext) {

        UserPrincipal principal = (UserPrincipal) securityContext.getUserPrincipal();

        Long uid = principal.getId();

        if (statut == null || statut.isBlank()) {
            throw new BadRequestException("Le statut est obligatoire");
        }

        if (!"EN_ATTENTE".equalsIgnoreCase(statut.trim())) {
            throw new BadRequestException("Statut non supporté");
        }

        return Response.ok(service.consulterDemandesEnAtt(uid)).build();
    }


    
    @PUT
    @Path("{id}/reponse")
    @RolesAllowed({ "SPECIALISTE" })

    public Response response(@PathParam("id") Long id, ReponseRequest request,
            @Context SecurityContext securityContext) {
        UserPrincipal principal = (UserPrincipal) securityContext.getUserPrincipal();
        return Response.ok(service.repondre(id, request, principal.getId())).build();
    }

    @GET
    @RolesAllowed({ "GENERALISTE" })
    public Response lister(@QueryParam("consultationId") Long consultationId) {
        return Response.ok(service.listerParConsultation(consultationId)).build();
    }
}