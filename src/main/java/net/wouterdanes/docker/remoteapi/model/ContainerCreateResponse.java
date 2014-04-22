package net.wouterdanes.docker.remoteapi.model;

import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * See <a href="http://docs.docker.io/reference/api/docker_remote_api_v1.10/#21-containers">
 * http://docs.docker.io/reference/api/docker_remote_api_v1.10/#create-a-container</a>
 */
@SuppressWarnings("unused")
public class ContainerCreateResponse {

    @JsonProperty("Id")
    private String id;
    @JsonProperty("Warnings")
    private List<String> warnings;

    public void setId(final String id) {
        this.id = id;
    }

    public void setWarnings(final List<String> warnings) {
        this.warnings = warnings;
    }

    public String getId() {
        return id;
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
}
