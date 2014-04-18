package net.wouterdanes.docker.provider.util;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import net.wouterdanes.docker.provider.RemoteDockerProvider;

/**
 * Supplies the docker host from the system property
 * {@value net.wouterdanes.docker.provider.RemoteDockerProvider#DOCKER_HOST_PROPERTY}
 */
public final class DockerHostFromPropertySupplier implements Supplier<Optional<String>> {

    public static final DockerHostFromPropertySupplier INSTANCE = new DockerHostFromPropertySupplier();

    private DockerHostFromPropertySupplier() {    }

    @Override
    public Optional<String> get() {
        return Optional.fromNullable(System.getProperty(RemoteDockerProvider.DOCKER_HOST_PROPERTY));
    }
}
