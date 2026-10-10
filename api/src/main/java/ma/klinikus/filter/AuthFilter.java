package ma.klinikus.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import ma.klinikus.model.Utilisateur;
import ma.klinikus.service.AuthService;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    private final AuthService authService = new AuthService();

    @Override
    public void filter(ContainerRequestContext ctx) {
        Utilisateur user = authService.authenticate(ctx.getHeaderString(HttpHeaders.AUTHORIZATION));

        ctx.setSecurityContext(new SecurityContextImpl(
                new SecurityContextImpl.UserPrincipal(user.getId(), user.getEmail(), String.valueOf(user.getRole())),
                ctx.getSecurityContext().isSecure()));
    }
}