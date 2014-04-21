package net.wouterdanes.docker.remoteapi;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;

/**
 * This class is responsible for holding the shared functionality of all Docker remoteapi services.
 */
public abstract class BaseService {

    private final ObjectMapper objectMapper;
    private final WebTarget serviceEndPoint;

    public BaseService(String dockerApiRoot, String endPointPath) {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        serviceEndPoint = ClientBuilder.newClient().target(dockerApiRoot).path(endPointPath);
    }

    public WebTarget getServiceEndPoint() {
        return serviceEndPoint;
    }

    protected String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to Jsonify", e);
        }
    }

    protected <T> T toObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot convert Json", e);
        }
    }
}
