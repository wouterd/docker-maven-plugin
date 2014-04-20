package net.wouterdanes.docker.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import net.wouterdanes.docker.remoteapi.exception.DockerException;

/**
 * This class is responsible for stopping the docker containers that were started by the plugin. The goal
 * is called "stop-containers" and it's executed in the "post-integration-test" phase.
 */
@Mojo(name = "stop-containers", threadSafe = true, defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class StopContainerMojo extends AbstractDockerMojo {
    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        for (String containerId : getStartedContainers()) {
            getLog().info(String.format("Stopping container '%s'..", containerId));
            try {
                getDockerProvider().stopContainer(containerId);
            } catch (DockerException e) {
                getLog().error("Failed to stop container", e);
            }
        }
        for (String containerId : getStartedContainers()) {
            getLog().info(String.format("Deleting container '%s'..", containerId));
            try {
                getDockerProvider().deleteContainer(containerId);
            } catch (DockerException e) {
                getLog().error("Failed to delete container", e);
            }
        }
    }
}
