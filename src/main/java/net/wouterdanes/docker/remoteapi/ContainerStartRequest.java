package net.wouterdanes.docker.remoteapi;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * See <a href="http://docs.docker.io/reference/api/docker_remote_api_v1.10/#21-containers">
 * http://docs.docker.io/reference/api/docker_remote_api_v1.10/#start-a-container</a>
 */
@SuppressWarnings("unused")
public class ContainerStartRequest {

    @JsonProperty("Binds")
    private List<String> binds;
    @JsonProperty("LxcConf")
    private Map<String, String> lxcConf;
    @JsonProperty("PortBindings")
    private Map<String, List<Map<String, String>>> portBindings;
    @JsonProperty("PublishAllPorts")
    private boolean publishAllPorts = false;
    @JsonProperty("Privileged")
    private boolean privileged = false;

    public ContainerStartRequest withBinds(List<String> binds) {
        this.binds = binds;
        return this;
    }

    public ContainerStartRequest withLxcConf(Map<String, String> lxcConf) {
        this.lxcConf = lxcConf;
        return this;
    }

    public ContainerStartRequest withPortBindings(Map<String, List<Map<String, String>>> portBindings) {
        this.portBindings = portBindings;
        return this;
    }

    public ContainerStartRequest withAllPortsPublished() {
        this.publishAllPorts = true;
        return this;
    }

    public ContainerStartRequest makePrivileged() {
        this.privileged = true;
        return this;
    }

    public List<String> getBinds() {
        return binds;
    }

    public Map<String, String> getLxcConf() {
        return lxcConf;
    }

    public Map<String, List<Map<String, String>>> getPortBindings() {
        return portBindings;
    }

    public boolean isPublishAllPorts() {
        return publishAllPorts;
    }

    public boolean isPrivileged() {
        return privileged;
    }
}
