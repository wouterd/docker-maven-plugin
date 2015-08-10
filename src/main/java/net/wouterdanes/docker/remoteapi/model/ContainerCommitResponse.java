package net.wouterdanes.docker.remoteapi.model;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * See <a href="http://docs.docker.io/reference/api/docker_remote_api_v1.10/#23-misc">
 * http://docs.docker.io/reference/api/docker_remote_api_v1.10/#create-a-new-image-from-a-containers-changes</a>
 */
public class ContainerCommitResponse {

    @JsonProperty("Id")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
