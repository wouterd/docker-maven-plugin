package net.wouterdanes.docker.provider;

import net.wouterdanes.docker.provider.model.ContainerCommitConfiguration;
import net.wouterdanes.docker.provider.model.ContainerStartConfiguration;
import net.wouterdanes.docker.provider.model.ExposedNetworkInfo;
import net.wouterdanes.docker.provider.model.ExposedPort;
import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;
import net.wouterdanes.docker.remoteapi.model.ContainerInspectionResult;
import net.wouterdanes.docker.remoteapi.model.Credentials;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import java.util.List;

/**
 * Utility class to create mock docker providers, extend this and implement the getInstance() method, then create
 * a static field in the extending class that can be modified by the test and that hold the actual mock.
 */
public abstract class AbstractFakeDockerProvider implements DockerProvider {

    private final AbstractFakeDockerProvider proxy;

    public AbstractFakeDockerProvider() {
        proxy = getInstance();
    }

    protected abstract AbstractFakeDockerProvider getInstance();

    @Override
    public ContainerInspectionResult startContainer(final ContainerStartConfiguration configuration) {
        return proxy.startContainer(configuration);
    }

    @Override
    public void stopContainer(final String containerId) {
        proxy.stopContainer(containerId);
    }

    @Override
    public void deleteContainer(final String containerId) {
        proxy.deleteContainer(containerId);
    }

    @Override
    public ExposedNetworkInfo getExposedNetworkInfo( final String containerId) {
        return proxy.getExposedNetworkInfo(containerId);
    }

    @Override
    public String buildImage(final ImageBuildConfiguration image) {
        return proxy.buildImage(image);
    }

    @Override
    public String commitContainer(ContainerCommitConfiguration configuration) {
        return proxy.commitContainer(configuration);
    }

    @Override
    public void removeImage(final String imageId) {
        proxy.removeImage(imageId);
    }

    @Override
    public void pushImage(final String nameAndTag) {
        proxy.pushImage(nameAndTag);
    }

    @Override
    public void tagImage(final String imageId, final String nameAndTag) {
        proxy.tagImage(imageId, nameAndTag);
    }

    @Override
    public void setCredentials(Credentials credentials) {
        proxy.setCredentials(credentials);
    }

    @Override
    public String getLogs(final String containerId) {
        return proxy.getLogs(containerId);
    }

    @Override
    public void setLogger(final Log logger) {
        proxy.setLogger(logger);
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
}
