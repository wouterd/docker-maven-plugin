package net.wouterdanes.docker.provider.util;

import java.net.URI;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import net.wouterdanes.docker.provider.RemoteDockerProvider;

/**
 * Supplies the docker port from the environment variable
 * '{@value net.wouterdanes.docker.provider.RemoteDockerProvider#DOCKER_HOST_SYSTEM_ENV}'
 */
public final class DockerPortFromEnvironmentSupplier extends DockerEnvironmentSupplier
        implements Supplier<Optional<Integer>> {

    public static DockerPortFromEnvironmentSupplier INSTANCE = new DockerPortFromEnvironmentSupplier();

    private DockerPortFromEnvironmentSupplier() {    }

    @Override
    public Optional<Integer> get() {
        Optional<URI> dockerUriFromEnvironment = getDockerUriFromEnvironment();
        if (dockerUriFromEnvironment.isPresent()) {
            URI dockerUrl = dockerUriFromEnvironment.get();
            boolean isTcpSocket = RemoteDockerProvider.TCP_PROTOCOL.equalsIgnoreCase(dockerUrl.getScheme());
            return isTcpSocket ? Optional.fromNullable(dockerUrl.getPort()) : Optional.<Integer>absent();
        } else {
            return Optional.absent();
        }
    }
}
