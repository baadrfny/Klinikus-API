package ma.klinikus.filter;

import jakarta.ws.rs.core.SecurityContext;
import java.security.Principal;

public class SecurityContextImpl implements SecurityContext {

    private final UserPrincipal principal;
    private final boolean secure;

    public SecurityContextImpl(UserPrincipal principal, boolean secure) {
        this.principal = principal;
        this.secure = secure;
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return principal.getRole().equals(role);
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public String getAuthenticationScheme() {
        return BASIC_AUTH;
    }

    public static class UserPrincipal implements Principal {
        private final Long id;
        private final String email;
        private final String role;

        public UserPrincipal(Long id, String email, String role) {
            this.id = id;
            this.email = email;
            this.role = role;
        }

        public Long getId() {
            return id;
        }

        public String getRole() {
            return role;
        }

        @Override
        public String getName() {
            return email;
        }
    }
}