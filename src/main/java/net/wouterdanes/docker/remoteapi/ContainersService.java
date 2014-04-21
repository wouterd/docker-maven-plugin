package net.wouterdanes.docker.remoteapi;

import net.wouterdanes.docker.remoteapi.exception.ContainerNotFoundException;
import net.wouterdanes.docker.remoteapi.exception.DockerException;
import net.wouterdanes.docker.remoteapi.exception.ImageNotFoundException;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class is responsible for talking to the Docker Remote API "containers" endpoint.<br/> See <a
 * href="http://docs.docker.io/reference/api/docker_remote_api_v1.10/#21-containers">
 * http://docs.docker.io/reference/api/docker_remote_api_v1.10/#21-containers</a>
 */
public class ContainersService extends BaseService {

    public ContainersService(String dockerApiRoot) {
        super(dockerApiRoot, "/containers");
    }

    public String createContainer(ContainerCreateRequest request) {
        WebTarget createEndPoint = getServiceEndPoint().path("/create");

        String createResponseStr;
        try {
            createResponseStr = createEndPoint
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.entity(toJson(request), MediaType.APPLICATION_JSON_TYPE), String.class);
        } catch (WebApplicationException e) {
            Response.StatusType statusInfo = e.getResponse().getStatusInfo();
            switch (statusInfo.getStatusCode()) {
                case 404:
                    throw new ImageNotFoundException(request.getImage(), e);
                default:
                    throw new DockerException(e);
            }
        }

        ContainerCreateResponse createResponse = toObject(createResponseStr, ContainerCreateResponse.class);
        return createResponse.getId();
    }

    public void startContainer(String id, ContainerStartRequest configuration) {
        Response response = getServiceEndPoint()
                .path(id)
                .path("/start")
                .request()
                .post(Entity.entity(toJson(configuration), MediaType.APPLICATION_JSON_TYPE));

        Response.StatusType statusInfo = response.getStatusInfo();
        response.close();

        checkContainerTargetingResponse(id, statusInfo);
    }

    public void killContainer(String id) {
        Response response = getServiceEndPoint()
                .path(id)
                .path("/kill")
                .request()
                .method(HttpMethod.POST);

        Response.StatusType statusInfo = response.getStatusInfo();
        response.close();

        checkContainerTargetingResponse(id, statusInfo);
    }

    public void deleteContainer(String id) {
        Response response = getServiceEndPoint()
                .path(id)
                .request()
                .delete();

        Response.StatusType statusInfo = response.getStatusInfo();
        response.close();

        checkContainerTargetingResponse(id, statusInfo);
    }

    private static void checkContainerTargetingResponse(final String id, final Response.StatusType statusInfo) {
        switch (statusInfo.getStatusCode()) {
            case 404:
                throw new ContainerNotFoundException(id);
            case 500:
                throw new DockerException(statusInfo.getReasonPhrase());
        }
    }

}
