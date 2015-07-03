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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.base.Optional;

import net.wouterdanes.docker.remoteapi.model.ImageDescriptor;

/**
 * This class is responsible for talking to the Docker Remote API "images" endpoint.<br> See <a
 * href="http://docs.docker.io/reference/api/docker_remote_api_v1.12/#22-images">
 * http://docs.docker.io/reference/api/docker_remote_api_v1.12/#22-images</a>
 */
public class ImagesService extends BaseService {

    public ImagesService(String dockerApiRoot) {
        super(dockerApiRoot, "/images");
    }

    public String pullImage(final String image) {
        ImageDescriptor descriptor = new ImageDescriptor(image);

        WebTarget target = getServiceEndPoint()
                .path("create");

        target = target.queryParam("fromImage", descriptor.getRegistryRepositoryAndImage());

        if (descriptor.getTag().isPresent()) {
            target = target.queryParam("tag", descriptor.getTag().get());
        }

        return target.request()
                .header(REGISTRY_AUTH_HEADER, getRegistryAuthHeaderValue())
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(null, String.class);
    }

    public String pushImage(String nameAndTag) {
        try {
            WebTarget target = createPushRequestFromTag(nameAndTag);

            return target.request()
                    .header(REGISTRY_AUTH_HEADER, getRegistryAuthHeaderValue())
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .post(null, String.class);

        } catch (WebApplicationException e) {
            throw makeImageTargetingException(nameAndTag, e);

        }
    }

    private WebTarget createPushRequestFromTag(final String nameAndTag) {
        ImageDescriptor descriptor = new ImageDescriptor(nameAndTag);
        WebTarget target = getServiceEndPoint()
                .path(descriptor.getRegistryRepositoryAndImage())
                .path("push");

        if (descriptor.getTag().isPresent()) {
            target = target.queryParam("tag", descriptor.getTag().get());
        }

        return target;
    }

    public void tagImage(final String imageId, final String nameAndTag) {
        ImageDescriptor descriptor = new ImageDescriptor(nameAndTag);

        WebTarget target = getServiceEndPoint()
                .path(imageId)
                .path("tag")
                .queryParam("repo", descriptor.getRegistryRepositoryAndImage())
                .queryParam("force", 1);

        Optional<String> targetTag = descriptor.getTag();
        if (targetTag.isPresent()) {
            target = target.queryParam("tag", targetTag.get());
        }

        Response response = target.request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(null);

        Response.StatusType statusInfo = response.getStatusInfo();

        response.close();

        checkImageTargetingResponse(imageId, statusInfo);
    }

    public void deleteImage(final String imageId) {
        try {
            getServiceEndPoint()
                    .path(imageId)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .delete(String.class);
        } catch (WebApplicationException e) {
            throw makeImageTargetingException("Cannot remove image", e);
        }
    }
}
