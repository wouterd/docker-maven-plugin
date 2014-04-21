package net.wouterdanes.docker.maven;

/**
 * This class stores information about an exposed port on a docker container
 */
public class ExposedPort {
    private final String containerPort;
    private final int externalPort;
    private final String host;

    public ExposedPort(final String containerPort, final int externalPort, final String host) {
        this.containerPort = containerPort;
        this.externalPort = externalPort;
        this.host = host;
    }

    public String getContainerPort() {
        return containerPort;
    }

    public int getExternalPort() {
        return externalPort;
    }

    public String getHost() {
        return host;
    }
}
