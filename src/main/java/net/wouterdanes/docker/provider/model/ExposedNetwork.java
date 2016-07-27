package net.wouterdanes.docker.provider.model;

import net.wouterdanes.docker.remoteapi.model.Network;

/**
 * Contains information about a custom docker network used by the container. When using these exposed networks, simply
 * access the EXPOSE'd ports from the Dockerfile in combination with the exposed IP address in order to access the
 * service.
 *
 * These are especially useful in continuous integration environments, where containers may be used to run tests against
 * the containers started by this plugin.
 *
 * Used in a container "container_id", and ExposedNetwork with name="ci" and ipAddress "172.18.0.3", this will be
 * translated into a Maven project property of:
 * <pre>
 *     docker.containers.container_id.nets.ci=172.18.0.3
 * </pre>
 */
public class ExposedNetwork
{
    /**
     * The name of the custom network.
     */
    private String name;

    /**
     * The container's IP address on the custom network.
     */
    private String ipAddress;

    public ExposedNetwork( String name, String ipAddress )
    {
        this.name = name;
        this.ipAddress = ipAddress;
    }

    public String getName()
    {
        return name;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }
}
