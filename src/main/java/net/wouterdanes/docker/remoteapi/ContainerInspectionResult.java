package net.wouterdanes.docker.remoteapi;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Holds a part of the inspect result on a container that's needed to query exposed ports
 */
public class ContainerInspectionResult {

    @JsonProperty("NetworkSettings")
    private NetworkSettings networkSettings;

    public NetworkSettings getNetworkSettings() {
        return networkSettings;
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
