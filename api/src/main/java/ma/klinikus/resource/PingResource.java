package ma.klinikus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/ping")
public class PingResource {

    @GET
    public Response ping() {
        return Response.ok("Klinikus API is running").build();
    }
}