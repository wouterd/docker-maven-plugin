package net.wouterdanes.docker.remoteapi.util;

import net.wouterdanes.docker.provider.RemoteDockerProvider;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Supplies the docker host from the system property
 * {@value net.wouterdanes.docker.provider.RemoteDockerProvider#DOCKER_HOST_PROPERTY}
 */
public final class DockerHostFromPropertySupplier implements Supplier<Optional<String>> {

    public static final DockerHostFromPropertySupplier INSTANCE = new DockerHostFromPropertySupplier();

    private DockerHostFromPropertySupplier() {    }

    @Override
    public Optional<String> get() {
        return Optional.ofNullable(System.getProperty(RemoteDockerProvider.DOCKER_HOST_PROPERTY));
    }
}
