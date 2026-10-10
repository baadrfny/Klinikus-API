package ma.klinikus;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

@ApplicationPath("/api")
public class RestApplication extends ResourceConfig {

    public RestApplication() {
        packages("ma.klinikus");
        register(RolesAllowedDynamicFeature.class);
    }
}