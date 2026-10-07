package ma.klinikus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import ma.klinikus.filter.RolesAllowed;
import ma.klinikus.filter.SecurityContextImpl.UserPrincipal;
import ma.klinikus.repository.UtilisateurRepository;

import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final UtilisateurRepository utilisateurRepository = new UtilisateurRepository();

    @POST
    @Path("/login")
    @RolesAllowed({ "INFIRMIER", "GENERALISTE", "SPECIALISTE" })
    public Response login(@Context SecurityContext securityContext) {
        return Response.ok(identity(securityContext)).build();
    }

    @GET
    @Path("/me")
    @RolesAllowed({ "INFIRMIER", "GENERALISTE", "SPECIALISTE" })
    public Response me(@Context SecurityContext securityContext) {
        return Response.ok(identity(securityContext)).build();
    }

    private Map<String, Object> identity(SecurityContext securityContext) {
        UserPrincipal principal = (UserPrincipal) securityContext.getUserPrincipal();
        String nom = utilisateurRepository.findById(principal.getId())
                .map(u -> u.getNom())
                .orElse("");
        return Map.of(
                "id", principal.getId(),
                "nom", nom,
                "email", principal.getName(),
                "role", principal.getRole());
    }
}