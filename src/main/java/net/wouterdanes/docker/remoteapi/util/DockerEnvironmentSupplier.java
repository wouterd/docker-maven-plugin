package net.wouterdanes.docker.remoteapi.util;

import java.net.URI;

import com.google.common.base.Optional;

import net.wouterdanes.docker.provider.RemoteDockerProvider;

public abstract class DockerEnvironmentSupplier {

    protected Optional<URI> getDockerUriFromEnvironment() {
        String envDockerHost = System.getenv(RemoteDockerProvider.DOCKER_HOST_SYSTEM_ENV);
        if (envDockerHost == null) {
            return Optional.absent();
        }
        try {
            URI uri = URI.create(envDockerHost);
            return Optional.of(uri);
        } catch (IllegalArgumentException ignored) {
            return Optional.absent();
        }
    }

}
