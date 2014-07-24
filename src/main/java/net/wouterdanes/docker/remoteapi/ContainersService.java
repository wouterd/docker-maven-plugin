/*
    Copyright 2014 Wouter Danes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

*/

package net.wouterdanes.docker.remoteapi;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.wouterdanes.docker.remoteapi.exception.ContainerNotFoundException;
import net.wouterdanes.docker.remoteapi.exception.DockerException;
import net.wouterdanes.docker.remoteapi.model.ContainerCreateRequest;
import net.wouterdanes.docker.remoteapi.model.ContainerCreateResponse;
import net.wouterdanes.docker.remoteapi.model.ContainerInspectionResult;
import net.wouterdanes.docker.remoteapi.model.ContainerStartRequest;

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
        String createResponseStr;
        try {
            createResponseStr = getServiceEndPoint()
                    .path("/create")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.entity(toJson(request), MediaType.APPLICATION_JSON_TYPE), String.class);
        } catch (WebApplicationException e) {
            throw makeImageTargetingException(request.getImage(), e);
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

    public ContainerInspectionResult inspectContainer(final String containerId) {
        String json = getServiceEndPoint()
                .path(containerId)
                .path("json")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        return toObject(json, ContainerInspectionResult.class);
    }
}
