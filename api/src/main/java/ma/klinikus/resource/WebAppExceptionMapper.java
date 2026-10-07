package ma.klinikus.resource;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class WebAppExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Override
    public Response toResponse(WebApplicationException e) {
        int status = e.getResponse().getStatus();
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("status", status, "erreur", e.getMessage()))
                .build();
    }
}