package net.wouterdanes.docker.remoteapi;

import javax.ws.rs.core.MediaType;

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
}
