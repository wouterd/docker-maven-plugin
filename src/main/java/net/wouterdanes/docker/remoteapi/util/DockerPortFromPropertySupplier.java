package net.wouterdanes.docker.remoteapi.util;

import net.wouterdanes.docker.provider.RemoteDockerProvider;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Supplies the docker host from the system property
 * {@value net.wouterdanes.docker.provider.RemoteDockerProvider#DOCKER_HOST_PROPERTY}
 */
public final class DockerPortFromPropertySupplier implements Supplier<Optional<Integer>> {

    public static final DockerPortFromPropertySupplier INSTANCE = new DockerPortFromPropertySupplier();

    private DockerPortFromPropertySupplier() {    }

    @Override
    public Optional<Integer> get() {
        Optional<String> port = Optional.ofNullable(System.getProperty(RemoteDockerProvider.DOCKER_PORT_PROPERTY));
        return port.isPresent() ? Optional.of(Integer.valueOf(port.get())) : Optional.empty();
    }
}
