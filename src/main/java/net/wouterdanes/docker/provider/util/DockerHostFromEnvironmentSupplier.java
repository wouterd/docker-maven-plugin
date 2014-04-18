package net.wouterdanes.docker.provider.util;

import java.net.URI;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import net.wouterdanes.docker.provider.RemoteDockerProvider;

/**
 * Supplies the docker host from the environment variable
 * '{@value net.wouterdanes.docker.provider.RemoteDockerProvider#DOCKER_HOST_SYSTEM_ENV}'
 */
public final class DockerHostFromEnvironmentSupplier extends DockerEnvironmentSupplier
        implements Supplier<Optional<String>> {

    public static DockerHostFromEnvironmentSupplier INSTANCE = new DockerHostFromEnvironmentSupplier();

    private DockerHostFromEnvironmentSupplier() {    }

    @Override
    public Optional<String> get() {
        Optional<URI> dockerUriFromEnvironment = getDockerUriFromEnvironment();
        if (!dockerUriFromEnvironment.isPresent()) {
            return Optional.absent();
        }
        URI dockerUrl = dockerUriFromEnvironment.get();
        boolean isTcpSocket = RemoteDockerProvider.TCP_PROTOCOL.equalsIgnoreCase(dockerUrl.getScheme());
        return isTcpSocket ? Optional.fromNullable(dockerUrl.getHost()) : Optional.<String>absent();
    }
}
