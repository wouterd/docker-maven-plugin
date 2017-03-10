package net.wouterdanes.docker.provider.model;

import java.util.Collections;
import java.util.List;

/**
 * Contains information about node presence on custom networks, along with ports "exposed" to the main docker host.
 */
public class ExposedNetworkInfo
{
    /**
     * Port mappings on the docker host.
     */
    private List<ExposedPort> exposedPorts;

    /**
     * IP Address and other information about the container on a set of custom docker networks. When using these, it's
     * not necessary to have a port mapping; simply reference the ports from the EXPOSE line in the Dockerfile in
     * combination with the IP address of the container on that network.
     */
    private List<ExposedNetwork> exposedNetworks;

    public List<ExposedPort> getExposedPorts()
    {
        return exposedPorts == null ? Collections.emptyList() : exposedPorts;
    }

    public List<ExposedNetwork> getExposedNetworks()
    {
        return exposedNetworks == null ? Collections.emptyList() : exposedNetworks;
    }

    public ExposedNetworkInfo withExposedNetworks( List<ExposedNetwork> exposedNetworks )
    {
        this.exposedNetworks = exposedNetworks;
        return this;
    }

    public ExposedNetworkInfo withExposedPorts( List<ExposedPort> exposedPorts )
    {
        this.exposedPorts = exposedPorts;
        return this;
    }
}
