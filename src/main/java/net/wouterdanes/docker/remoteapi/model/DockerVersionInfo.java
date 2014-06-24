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
