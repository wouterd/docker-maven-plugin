package net.wouterdanes.docker.provider;

import java.util.List;

import net.wouterdanes.docker.provider.model.ExposedNetworkInfo;
import org.apache.maven.plugin.logging.Log;

import net.wouterdanes.docker.provider.model.ContainerCommitConfiguration;
import net.wouterdanes.docker.provider.model.ContainerStartConfiguration;
import net.wouterdanes.docker.provider.model.ExposedPort;
import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;
import net.wouterdanes.docker.remoteapi.exception.DockerException;
import net.wouterdanes.docker.remoteapi.model.ContainerInspectionResult;
import net.wouterdanes.docker.remoteapi.model.Credentials;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

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
        // NOOP
    }

    @Override
    public ContainerInspectionResult startContainer(final ContainerStartConfiguration configuration) {
        throwBadException();
        return null;
    }

    @Override
    public void stopContainer(final String containerId) {
        throwBadException();
    }

    @Override
    public void deleteContainer(final String containerId) {
        throwBadException();
    }

    @Override
    public ExposedNetworkInfo getExposedNetworkInfo( final String containerId) {
        throwBadException();
        return null;
    }

    @Override
    public String buildImage(final ImageBuildConfiguration image) {
        throwBadException();
        return null;
    }

    @Override
    public String commitContainer(ContainerCommitConfiguration configuration) {
        throwBadException();
        return null;
    }

    @Override
    public void removeImage(final String imageId) {
        throwBadException();
    }

    @Override
    public void pushImage(final String nameAndTag) {
        throwBadException();
    }

    @Override
    public void tagImage(final String imageId, final String nameAndTag) {
        throwBadException();
    }

    @Override
    public String getLogs(final String containerId) {
        throwBadException();
        return null;
    }

    @Override
    public void setLogger(final Log logger) {
        // NOOP
    }

    @Override
    public void setRepositorySystem(RepositorySystem repositorySystem) {
        // NOOP
    }

    @Override
    public void setRepositorySystemSession(RepositorySystemSession repositorySystemSession) {
        // NOOP
    }

    @Override
    public void setRemoteRepositories(List<RemoteRepository> remoteRepositories) {
        // NOOP
    }

    private static void throwBadException() {
        throw new DockerException("Bad stuff");
    }
}
