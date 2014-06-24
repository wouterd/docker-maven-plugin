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
import javax.ws.rs.core.MediaType;

import net.wouterdanes.docker.remoteapi.exception.DockerException;
import net.wouterdanes.docker.remoteapi.model.ImageDescriptor;

/**
 * This class is responsible for talking to the Docker Remote API "images" endpoint.<br/> See <a
 * href="http://docs.docker.io/reference/api/docker_remote_api_v1.10/#22-images">
 * http://docs.docker.io/reference/api/docker_remote_api_v1.10/#22-images</a>
 */
public class ImagesService extends BaseService {

    public ImagesService(String dockerApiRoot) {
        super(dockerApiRoot, "/images");
    }

    public String pullImage(String image) {

        ImageDescriptor descriptor = new ImageDescriptor(image);

        return getServiceEndPoint()
                .path("create")
                .queryParam("fromImage", descriptor.getImage())
                .queryParam("repo", descriptor.getRepository())
                .queryParam("tag", descriptor.getTag())
                .queryParam("registry", descriptor.getRegistry())
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(null, String.class);
    }

    public void deleteImage(final String imageId) {
        try {
            getServiceEndPoint()
                    .path(imageId)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .delete(String.class);
        } catch (WebApplicationException e) {
            throw new DockerException("Cannot remove image", e);
        }
    }
}
