package net.wouterdanes.docker.remoteapi.model;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * When used by itself, as an entry value from NetworkSettings -&gt; Networks section of the inspection result, this
 * will contain information about the container's network configuration on a custom docker network. However, it is also
 * the parent class of {@link NetworkSettings}, which contains the container's network configuration on the default
 * docker bridged network.
 */
public class Network
{
    @JsonProperty( "Gateway" )
    private String gateway;

    @JsonProperty( "IPAddress" )
    private String ipAddress;

    @JsonProperty( "IPPrefixLen" )
    private int ipPrefixLen;

    public String getGateway()
    {
        return gateway;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public int getIpPrefixLen()
    {
        return ipPrefixLen;
    }

}
