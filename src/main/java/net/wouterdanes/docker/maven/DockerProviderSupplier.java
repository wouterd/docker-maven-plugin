package net.wouterdanes.docker.maven;

import com.google.common.base.Supplier;

import net.wouterdanes.docker.provider.DockerProvider;
import net.wouterdanes.docker.provider.RemoteDockerProvider;

/**
 * This class creates a docker provider based on the passed name when the get() method is called.
 * Used in for example {@link com.google.common.base.Optional#or(com.google.common.base.Supplier)}
 */
public class DockerProviderSupplier implements Supplier<DockerProvider> {

    private final String providerName;

    public DockerProviderSupplier(final String providerName) {
        this.providerName = providerName;
    }

    @Override
    public DockerProvider get() {
        if ("remote".equals(providerName)) {
            return new RemoteDockerProvider();
        }
        throw new IllegalStateException(String.format("No provider known by name '%s'", providerName));
    }
}
