package net.wouterdanes.docker.remoteapi.util;

import net.wouterdanes.docker.provider.RemoteDockerProvider;

import java.net.URI;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Supplies the docker port from the environment variable
 * '{@value net.wouterdanes.docker.provider.RemoteDockerProvider#DOCKER_HOST_SYSTEM_ENV}'
 */
public final class DockerPortFromEnvironmentSupplier extends DockerEnvironmentSupplier
        implements Supplier<Optional<Integer>> {

    public static final DockerPortFromEnvironmentSupplier INSTANCE = new DockerPortFromEnvironmentSupplier();

    private DockerPortFromEnvironmentSupplier() {    }

    @Override
    public Optional<Integer> get() {
        Optional<URI> dockerUriFromEnvironment = getDockerUriFromEnvironment();
        if (!dockerUriFromEnvironment.isPresent()) {
            return Optional.empty();
        }
        URI dockerUrl = dockerUriFromEnvironment.get();
        boolean isTcpSocket = RemoteDockerProvider.TCP_PROTOCOL.equalsIgnoreCase(dockerUrl.getScheme());
        return isTcpSocket ? Optional.ofNullable(dockerUrl.getPort()) : Optional.empty();
    }
}
