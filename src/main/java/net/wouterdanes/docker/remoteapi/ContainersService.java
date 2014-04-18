package net.wouterdanes.docker.remoteapi;

import java.io.IOException;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * This class is responsible for talking to the Docker Remote API "containers" endpoint.<br/>
 * See <a href="http://docs.docker.io/reference/api/docker_remote_api_v1.10/#21-containers">
 *     http://docs.docker.io/reference/api/docker_remote_api_v1.10/#21-containers</a>
 */
public class ContainersService {

    private final WebTarget containersService;
    private final ObjectMapper objectMapper;

    public ContainersService(String dockerApiRoot) {
        containersService = ClientBuilder.newClient().target(dockerApiRoot).path("/containers");
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    }

    public void startContainer(String id, ContainerStartRequest configuration) {
        containersService
                .path(id)
                .path("/start")
                .request()
                .post(Entity.entity(toJson(configuration), MediaType.APPLICATION_JSON_TYPE));
    }

    public String createContainer(ContainerCreateRequest request) {
        WebTarget createEndPoint = containersService.path("/create");

        String createResponseStr = createEndPoint
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(toJson(request), MediaType.APPLICATION_JSON_TYPE), String.class);

        ContainerCreateResponse createResponse = toObject(createResponseStr, ContainerCreateResponse.class);
        return createResponse.getId();
    }

    private String toJson(Object obj) {

        try {

            return objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to Jsonify", e);
        }
    }

    private <T> T toObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot convert Json", e);
        }
    }
}
