package net.wouterdanes.docker.remoteapi.model;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * This class wraps the "version" response of the docker api.
 */
public class DockerVersionInfo {

    @JsonProperty("ApiVersion")
    private String apiVersion;

    @JsonProperty("Arch")
    private String architecture;

    @JsonProperty("GitCommit")
    private String gitCommitHash;

    @JsonProperty("GoVersion")
    private String goVersion;

    @JsonProperty("KernelVersion")
    private String kernelVersion;

    @JsonProperty("Os")
    private String os;

    @JsonProperty("Version")
    private String dockerVersion;

    public String getApiVersion() {
        return apiVersion;
    }

    public String getArchitecture() {
        return architecture;
    }

    public String getGitCommitHash() {
        return gitCommitHash;
    }

    public String getGoVersion() {
        return goVersion;
    }

    public String getKernelVersion() {
        return kernelVersion;
    }

    public String getOs() {
        return os;
    }

    public String getDockerVersion() {
        return dockerVersion;
    }
}
