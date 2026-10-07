package ma.klinikus.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import ma.klinikus.model.Utilisateur;
import ma.klinikus.service.AuthService;

import java.util.Arrays;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    private final AuthService authService = new AuthService();

    @Override
    public void filter(ContainerRequestContext ctx) {
        RolesAllowed rolesAllowed = findRolesAllowed();
        if (rolesAllowed == null) {
            return; 
        }

        Utilisateur user = authService.authenticate(ctx.getHeaderString(HttpHeaders.AUTHORIZATION));

        String role = String.valueOf(user.getRole());
        SecurityContextImpl.UserPrincipal principal = new SecurityContextImpl.UserPrincipal(user.getId(),
                user.getEmail(), role);
        ctx.setSecurityContext(new SecurityContextImpl(principal, ctx.getSecurityContext().isSecure()));

        if (!Arrays.asList(rolesAllowed.value()).contains(role)) {
            throw new ForbiddenException("Accès refusé : rôle non autorisé pour cet endpoint");
        }
    }

    private RolesAllowed findRolesAllowed() {
        RolesAllowed onMethod = resourceInfo.getResourceMethod().getAnnotation(RolesAllowed.class);
        if (onMethod != null) {
            return onMethod;
        }
        return resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class);
    }
}