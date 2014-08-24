package net.wouterdanes.docker.provider;

import java.util.List;

import com.google.common.base.Optional;

import net.wouterdanes.docker.provider.model.ContainerStartConfiguration;
import net.wouterdanes.docker.provider.model.ExposedPort;
import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;
import net.wouterdanes.docker.remoteapi.exception.DockerException;
import net.wouterdanes.docker.remoteapi.model.ContainerInspectionResult;
import net.wouterdanes.docker.remoteapi.model.Credentials;

/**
 * A Mock {@link net.wouterdanes.docker.provider.DockerProvider} that only throws
 * {@link net.wouterdanes.docker.remoteapi.exception.DockerException}s
 *
 * If you want to use this provider in your tests, simply write:
 * <code>DockerExceptionThrowingDockerProvider.class.newInstance();</code>
 */
public class DockerExceptionThrowingDockerProvider implements DockerProvider {

    public static final String PROVIDER_KEY = DockerExceptionThrowingDockerProvider.class.getName();

    static {
        DockerProviderSupplier.registerProvider(PROVIDER_KEY, DockerExceptionThrowingDockerProvider.class);
    }

    @Override
    public void setCredentials(final Credentials credentials) {
    }

    @Override
    public ContainerInspectionResult startContainer(final ContainerStartConfiguration configuration) {
        throw new DockerException("Bad stuff");
    }

    @Override
    public void stopContainer(final String containerId) {
        throw new DockerException("Bad stuff");
    }

    @Override
    public void deleteContainer(final String containerId) {
        throw new DockerException("Bad stuff");
    }

    @Override
    public List<ExposedPort> getExposedPorts(final String containerId) {
        throw new DockerException("Bad stuff");
    }

    @Override
    public String buildImage(final ImageBuildConfiguration image) {
        throw new DockerException("Bad stuff");
    }

    @Override
    public void removeImage(final String imageId) {
        throw new DockerException("Bad stuff");
    }

    @Override
    public void pushImage(final String imageId, final Optional<String> nameAndTag) {
        throw new DockerException("Bad stuff");
    }

    @Override
    public void tagImage(final String imageId, final String nameAndTag) {
        throw new DockerException("Bad stuff");
    }

    @Override
    public String getLogs(final String containerId) {
        throw new DockerException("Bad stuff");
    }
}
