package net.wouterdanes.docker.remoteapi.util;

import net.wouterdanes.docker.provider.RemoteDockerProvider;

import java.net.URI;
import java.util.Optional;

public abstract class DockerEnvironmentSupplier {

    protected Optional<URI> getDockerUriFromEnvironment() {
        String envDockerHost = System.getenv(RemoteDockerProvider.DOCKER_HOST_SYSTEM_ENV);
        if (envDockerHost == null) {
            return Optional.empty();
        }
        try {
            URI uri = URI.create(envDockerHost);
            return Optional.of(uri);
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

}
