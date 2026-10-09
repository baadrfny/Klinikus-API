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
    @RolesAllowed({ "SPECIALISTE", "GENERALISTE" })
    public Response lister(
            @QueryParam("statut") String statut,
            @QueryParam("consultationId") Long consultationId,
            @Context SecurityContext securityContext) {

        UserPrincipal principal = (UserPrincipal) securityContext.getUserPrincipal();

        if ("SPECIALISTE".equals(principal.getRole())) {
            return Response.ok(demandesDuSpecialiste(statut, principal.getId())).build();
        }
        return Response.ok(service.listerParConsultation(consultationId)).build();
    }

    @PUT
    @Path("{id}/reponse")
    @RolesAllowed({ "SPECIALISTE" })
    public Response response(@PathParam("id") Long id, ReponseRequest request,
            @Context SecurityContext securityContext) {
        UserPrincipal principal = (UserPrincipal) securityContext.getUserPrincipal();
        return Response.ok(service.repondre(id, request, principal.getId())).build();
    }

    private java.util.List<DemandeExpertise> demandesDuSpecialiste(String statut, Long uid) {
        if (statut == null || statut.isBlank()) {
            throw new BadRequestException("Le statut est obligatoire");
        }
        if (!"EN_ATTENTE".equalsIgnoreCase(statut.trim())) {
            throw new BadRequestException("Statut non supporté");
        }
        return service.consulterDemandesEnAtt(uid);
    }
}