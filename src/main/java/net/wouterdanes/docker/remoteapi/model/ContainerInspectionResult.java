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

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Holds a part of the inspect result on a container that's needed to query exposed ports
 */
@SuppressWarnings("unused")
public class ContainerInspectionResult {

    @JsonProperty("Id")
    private String id;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Created")
    private Calendar createdAt;
    @JsonProperty("Path")
    private String path;
    @JsonProperty("Args")
    private List<String> args;

    @JsonProperty("Config")
    private Config config;

    @JsonProperty("NetworkSettings")
    private NetworkSettings networkSettings;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Calendar getCreatedAt() {
        return createdAt;
    }

    public String getPath() {
        return path;
    }

    public List<String> getArgs() {
        return args;
    }

    public Config getConfig() {
        return config;
    }

    public NetworkSettings getNetworkSettings() {
        return networkSettings;
    }

    public static class Config {
        @JsonProperty("Hostname")
        private String hostname;
        @JsonProperty("Domainname")
        private String domainName;
        @JsonProperty("User")
        private String user;
        @JsonProperty("Memory")
        private Long memory;
        @JsonProperty("MemorySwap")
        private Long memorySwap;
        @JsonProperty("AttachStdin")
        private Boolean attachStdin;
        @JsonProperty("AttachStdout")
        private Boolean attachStdout;
        @JsonProperty("AttachStderr")
        private Boolean attachStderr;
        @JsonProperty("ExposedPorts")
        private Map<String, Map> exposedPorts;
        @JsonProperty("Tty")
        private Boolean tty;
        @JsonProperty("OpenStdin")
        private Boolean openStdin;
        @JsonProperty("StdinOnce")
        private Boolean stdinOnce;
        @JsonProperty("Env")
        private List<String> env;
        @JsonProperty("Cmd")
        private List<String> cmd;
        @JsonProperty("Image")
        private String image;
        @JsonProperty("WorkingDir")
        private String workingDir;
        @JsonProperty("Entrypoint")
        private List<String> entrypoint;

        public String getHostname() {
            return hostname;
        }

        public String getDomainName() {
            return domainName;
        }

        public String getUser() {
            return user;
        }

        public Long getMemory() {
            return memory;
        }

        public Long getMemorySwap() {
            return memorySwap;
        }

        public Boolean getAttachStdin() {
            return attachStdin;
        }

        public Boolean getAttachStdout() {
            return attachStdout;
        }

        public Boolean getAttachStderr() {
            return attachStderr;
        }

        public Map<String, Map> getExposedPorts() {
            return Collections.unmodifiableMap(exposedPorts);
        }

        public Boolean getTty() {
            return tty;
        }

        public Boolean getOpenStdin() {
            return openStdin;
        }

        public Boolean getStdinOnce() {
            return stdinOnce;
        }

        public List<String> getEnv() {
            return env;
        }

        public List<String> getCmd() {
            return cmd;
        }

        public String getImage() {
            return image;
        }

        public String getWorkingDir() {
            return workingDir;
        }

        public List<String> getEntrypoint() {
            return entrypoint;
        }
    }

    public static class NetworkSettings {
        @JsonProperty("Bridge")
        private String bridge;
        @JsonProperty("Gateway")
        private String gateway;
        @JsonProperty("IPAddress")
        private String ipAddress;
        @JsonProperty("IPPrefixLen")
        private int ipPrefixLen;
        @JsonProperty("Ports")
        private Map<String, List<PortMappingInfo>> ports;

        public String getBridge() {
            return bridge;
        }

        public String getGateway() {
            return gateway;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public int getIpPrefixLen() {
            return ipPrefixLen;
        }

        public Map<String, List<PortMappingInfo>> getPorts() {
            return Collections.unmodifiableMap(ports);
        }

        public static class PortMappingInfo {
            @JsonProperty("HostIp")
            private String hostIp;
            @JsonProperty("HostPort")
            private int hostPort;

            public String getHostIp() {
                return hostIp;
            }

            public int getHostPort() {
                return hostPort;
            }
        }
    }
}
