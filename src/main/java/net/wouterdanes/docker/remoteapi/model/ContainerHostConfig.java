package net.wouterdanes.docker.remoteapi.model;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Models some useful configuration points in the HostConfig section of the create-container command in the Docker
 * Remote API.
 */
public class ContainerHostConfig
{
    /**
     * When using a custom docker network, this will be the name of the network. Otherwise, you probably don't mean to
     * use it. When used, it will cause an {@link net.wouterdanes.docker.provider.model.ExposedNetwork} instance to be
     * added to the {@link net.wouterdanes.docker.provider.model.ExposedNetworkInfo}.
     */
    @JsonProperty("NetworkMode")
    private String networkMode;

    public String getNetworkMode()
    {
        return networkMode;
    }

    public ContainerHostConfig withNetworkMode( String networkMode )
    {
        this.networkMode = networkMode;
        return this;
    }
}
