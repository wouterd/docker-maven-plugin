package net.wouterdanes.docker.remoteapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * See <a href="http://docs.docker.io/reference/api/docker_remote_api_v1.10/#21-containers">
 * http://docs.docker.io/reference/api/docker_remote_api_v1.10/#create-a-container</a>
 */
@SuppressWarnings("unused")
public class ContainerCreateRequest {

    @JsonProperty("Hostname")
    private String hostname;
    @JsonProperty("User")
    private String user;
    @JsonProperty("Memory")
    private Long memory;
    @JsonProperty("Cmd")
    private List<String> cmd;
    @JsonProperty("Image")
    private String image;

    public String getHostname() {
        return hostname;
    }

    public String getUser() {
        return user;
    }

    public Long getMemory() {
        return memory;
    }

    public List<String> getCmd() {
        return cmd;
    }

    public String getImage() {
        return image;
    }

    public ContainerCreateRequest withHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public ContainerCreateRequest withUser(String user) {
        this.user = user;
        return this;
    }

    public ContainerCreateRequest withMemory(long memory) {
        this.memory = memory;
        return this;
    }

    public ContainerCreateRequest withCommand(String command) {
        this.cmd = Arrays.asList(command);
        return this;
    }

    public ContainerCreateRequest withCommands(List<String> commands) {
        this.cmd = new ArrayList<>(commands);
        return this;
    }

    public ContainerCreateRequest fromImage(String image) {
        this.image = image;
        return this;
    }

}
