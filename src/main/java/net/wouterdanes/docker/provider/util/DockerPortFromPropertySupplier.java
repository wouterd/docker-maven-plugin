package net.wouterdanes.docker.provider.util;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

import net.wouterdanes.docker.provider.RemoteDockerProvider;

/**
 * Supplies the docker host from the system property
 * {@value net.wouterdanes.docker.provider.RemoteDockerProvider#DOCKER_HOST_PROPERTY}
 */
public final class DockerPortFromPropertySupplier implements Supplier<Optional<Integer>> {

    public static final DockerPortFromPropertySupplier INSTANCE = new DockerPortFromPropertySupplier();

    private DockerPortFromPropertySupplier() {    }

    @Override
    public Optional<Integer> get() {
        Optional<String> port = Optional.fromNullable(System.getProperty(RemoteDockerProvider.DOCKER_PORT_PROPERTY));
        return port.isPresent() ? Optional.of(new Integer(port.get())) : Optional.<Integer>absent();
    }
}
