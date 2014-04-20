package net.wouterdanes.docker.maven;

import java.util.List;

import javax.inject.Inject;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import net.wouterdanes.docker.provider.DockerProvider;
import net.wouterdanes.docker.remoteapi.exception.DockerException;

/**
 * This class is responsible for starting docking containers in the pre-integration phase of the maven build. The goal
 * is called "start-containers"
 */
@Mojo(defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, name = "start-containers", threadSafe = true)
public class StartContainerMojo extends AbstractDockerMojo {

    @Parameter(required = true)
    private List<ContainerStartConfiguration> containers;

    @Inject
    public StartContainerMojo(final List<ContainerStartConfiguration> containers) {
        this.containers = containers;
    }

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        for (ContainerStartConfiguration configuration : containers) {
            DockerProvider provider = getDockerProvider();
            try {
                String containerId = provider.startContainer(configuration);
                getLog().info(String.format("Started container with id '%s'", containerId));
                registerStartedContainer(containerId);
            } catch (DockerException e) {
                getLog().error("Failed to start container", e);
            }
        }
    }

}
