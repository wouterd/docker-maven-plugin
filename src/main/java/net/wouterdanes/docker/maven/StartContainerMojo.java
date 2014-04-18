package net.wouterdanes.docker.maven;

import java.util.List;

import javax.inject.Inject;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import net.wouterdanes.docker.provider.DockerProvider;

/**
 * This class is responsible for starting docking containers in the pre-integration phase of the maven build. The goal
 * is called "start-containers"
 */
@Mojo(defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, name = "start-containers", threadSafe = true)
public class StartContainerMojo extends AbstractMojo {

    @Parameter(required = true)
    private List<ContainerStartConfiguration> containers;

    @Parameter(defaultValue = "remote", property = "docker.provider", required = true)
    private String providerName;

    @Inject
    public StartContainerMojo(final List<ContainerStartConfiguration> containers, final String providerName) {
        this.containers = containers;
        this.providerName = providerName;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        DockerProvider provider = new DockerProviderSupplier(providerName).get();

        for (ContainerStartConfiguration configuration : containers) {
            String containerId = provider.startContainer(configuration);
            getLog().info(String.format("Started container with id '%s'", containerId));
        }
    }

}
