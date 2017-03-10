package net.wouterdanes.docker.remoteapi.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Contains information from the "NetworkSettings" section of the inspection result, which may contain information about
 * the container's attachment to the default bridged network, or it may contain a mapping of attachments to custom
 * docker networks (or sometimes both, presumably).
 */
public class NetworkSettings
        extends Network
{
    /**
     * Docker network bridge name used by this container.
     */
    @JsonProperty( "Bridge" )
    private String bridge;

    /**
     * Ports exposed on the default bridged docker network for this container.
     */
    @JsonProperty( "Ports" )
    private Map<String, List<PortMappingInfo>> ports;

    /**
     * Mapping of custom-network-name to container attachment information for any custom docker networks used by the
     * container. This includes gateway, IP address, and IPPrefixLen, which I presume is another name for netmask.
     */
    @JsonProperty( "Networks" )
    private Map<String, Network> networks;

    public String getBridge()
    {
        return bridge;
    }

    public Map<String, List<PortMappingInfo>> getPorts()
    {
        return Collections.unmodifiableMap( ports );
    }

    public Map<String, Network> getNetworks()
    {
        return Collections.unmodifiableMap( networks );
    }

    public static class PortMappingInfo
    {
        @JsonProperty( "HostIp" )
        private String hostIp;

        @JsonProperty( "HostPort" )
        private int hostPort;

        public String getHostIp()
        {
            return hostIp;
        }

        public int getHostPort()
        {
            return hostPort;
        }
    }
}
