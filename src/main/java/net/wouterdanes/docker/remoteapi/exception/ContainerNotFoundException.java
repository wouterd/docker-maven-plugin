package net.wouterdanes.docker.remoteapi.exception;


public class ContainerNotFoundException extends DockerException {
    public ContainerNotFoundException(final String id) {
        super(String.format("Container '%s' not found.", id));
    }
}
