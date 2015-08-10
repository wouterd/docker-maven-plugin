package net.wouterdanes.docker.maven;

import net.wouterdanes.docker.provider.model.ContainerCommitConfiguration;
import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;
import net.wouterdanes.docker.remoteapi.exception.DockerException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;
import java.util.Optional;

@Mojo(name = "commit-container", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST, threadSafe = true,
        instantiationStrategy = InstantiationStrategy.PER_LOOKUP)
public class CommitContainerMojo extends AbstractPreVerifyDockerMojo {
    @Parameter(required = true)
    private List<ContainerCommitConfiguration> containers;

    public void setConfiguration(final List<ContainerCommitConfiguration> containers) {
        this.containers = containers;
    }

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        if (containers == null || containers.isEmpty()) {
            getLog().warn("No containers specified.");
            return;
        }
        for (ContainerCommitConfiguration container : containers) {
            commitContainer(container);
        }
    }

    protected void commitContainer(ContainerCommitConfiguration container) throws MojoFailureException {
        getLog().info(String.format("Creating image for configuration '%s'", container));
        String containerId = container.getId();
        Optional<StartedContainerInfo> containerInfo = getInfoForContainerStartId(containerId);
        if (containerInfo.isPresent()) {
            try {
                //replace container name by its actual id.
                String startedContainerId = containerInfo.get().getContainerInfo().getId();
                container.setId(startedContainerId);

                String imageId = getDockerProvider().commitContainer(container);
                getLog().info(String.format("Image '%s' created from container '%s'", imageId, container.getId()));

                //Register the resulting image so it can be pushed
                ImageBuildConfiguration imageBuildConfiguration = new ImageBuildConfiguration();
                imageBuildConfiguration.setId(containerId);
                imageBuildConfiguration.setNameAndTag(container.getRepo() + ":" + container.getTag());
                imageBuildConfiguration.setPush(container.isPush());
                registerBuiltImage(imageId, imageBuildConfiguration);

            } catch (DockerException e) {
                String errorMessage = String.format("Image '%s:%s' could not be created from container '%s'", container.getRepo(), container.getTag(), container.getId());
                handleDockerException(errorMessage, e);
            }
        } else {
            String message = String.format("No container found for id '%s'", containerId);
            registerPluginError(new DockerPluginError("commit-container", message));
            getLog().warn(message);
        }
    }

    @Override
    protected String getMojoGoalName() {
        return "commit-container";
    }

}
