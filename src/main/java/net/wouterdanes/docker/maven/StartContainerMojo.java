/*
    Copyright 2014 Wouter Danes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

*/

package net.wouterdanes.docker.maven;

import net.wouterdanes.docker.provider.DockerProvider;
import net.wouterdanes.docker.provider.model.BuiltImageInfo;
import net.wouterdanes.docker.provider.model.ContainerStartConfiguration;
import net.wouterdanes.docker.provider.model.ExposedPort;
import net.wouterdanes.docker.remoteapi.exception.DockerException;
import net.wouterdanes.docker.remoteapi.model.ContainerInspectionResult;
import net.wouterdanes.docker.remoteapi.model.ContainerLink;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import javax.inject.Inject;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class is responsible for starting docking containers in the pre-integration phase of the maven build. The goal
 * is called "start-containers"
 */
@Mojo(defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, name = "start-containers",
        threadSafe = true, instantiationStrategy = InstantiationStrategy.PER_LOOKUP)
public class StartContainerMojo extends AbstractPreVerifyDockerMojo {

    @Parameter(required = true)
    private List<ContainerStartConfiguration> containers;

    @Parameter
    private boolean forceCleanup;

    @Inject
    public StartContainerMojo(List<ContainerStartConfiguration> containers) {
        this.containers = containers;
    }

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution mojoExecution;

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        if (hasDuplicateIds() || hasInvalidLinks()) {
            return;
        }
        DockerProvider provider = getDockerProvider();
        for (ContainerStartConfiguration configuration : containers) {
            for (ContainerLink link : configuration.getLinks()) {
                String linkedContainerId = link.getContainerId();
                ContainerStartConfiguration startConfiguration = getContainerStartConfiguration(linkedContainerId);
                if (startConfiguration.getWaitForStartup() != null) {
                    waitForContainerToFinishStartup(startConfiguration);
                }
            }
            replaceImageWithBuiltImageIdIfInternalId(configuration);
            replaceLinkedContainerIdsWithStartedNames(configuration);
            try {
                getLog().info(String.format("Starting container '%s'..", configuration.getId()));
                ContainerInspectionResult container = provider.startContainer(configuration);
                String containerId = container.getId();
                List<ExposedPort> exposedPorts = provider.getExposedPorts(containerId);
                exposePortsToProject(configuration, exposedPorts);
                getLog().info(String.format("Started container with id '%s'", containerId));
                registerStartedContainer(configuration.getId(), container);
            } catch (DockerException e) {
                String message = String.format("Failed to start container '%s'", configuration.getId());
                handleDockerException(message, e);
            }
        }
        getLog().debug("Properties after exposing ports: " + project.getProperties());
        waitForContainersToFinishStartup();
        if (forceCleanup) {
            addShutdownHookToCleanUpContainers();
        }
    }

    /** Avoid dangling containers if the build is interrupted (e.g. via Ctrl+C) before the StopContainer mojo runs. */
    private void addShutdownHookToCleanUpContainers()
    {
        getLog().info("Started containers will be forcibly cleaned up when the build finishes");
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                cleanUpStartedContainers();
            }
        }));
    }

    private ContainerStartConfiguration getContainerStartConfiguration(String id) {
        return containers.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("No container with ID '%s'", id)));
    }

    private void waitForContainersToFinishStartup() {
        containers.stream()
                .filter(input -> input.getWaitForStartup() != null)
                .forEach(this::waitForContainerToFinishStartup);
    }

    private void waitForContainerToFinishStartup(final ContainerStartConfiguration container) {
        Pattern pattern = Pattern.compile(container.getWaitForStartup());
        Optional<StartedContainerInfo> startedContainerInfo = getInfoForContainerStartId(container.getId());
        if (!startedContainerInfo.isPresent()) {
            return;
        }
        StartedContainerInfo containerInfo = startedContainerInfo.get();
        String containerId = containerInfo.getContainerInfo().getId();
        long maxWait = System.currentTimeMillis() + 1000 * container.getStartupTimeout();
        boolean finished = false;
        while (System.currentTimeMillis() <= maxWait) {
            String logs = getDockerProvider().getLogs(containerId);
            if (logs != null && pattern.matcher(logs).find()) {
                getLog().info(String.format("Container '%s' has completed startup", container.getId()));
                finished = true;
                break;
            }
            try {
                getLog().info(String.format("Waiting for container '%s' to finish startup (max %s sec.)",
                        container.getId(), container.getStartupTimeout()));
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
                break;
            }
        }
        if (!finished) {
            String message = String.format("Container %s did not finish startup in time", container.getId());
            registerPluginError(new DockerPluginError(getMojoGoalName(), message));
            getLog().error(message);
        }
    }

    private boolean hasInvalidLinks() {
        List<String> containerIds = new ArrayList<>();
        boolean hasInvalidLinks = false;
        for (ContainerStartConfiguration configuration : containers) {
            List<ContainerLink> links = configuration.getLinks();
            for (ContainerLink link : links) {
                if (!containerIds.contains(link.getContainerId())) {
                    String message = String.format("Container '%s' tries to link to container '%s' that is not started " +
                            "before this container.", configuration.getId(), link.getContainerId());
                    getLog().error(message);
                    registerPluginError(new DockerPluginError(mojoExecution.getGoal(), message));
                    hasInvalidLinks = true;
                }
            }
            containerIds.add(configuration.getId());
        }
        return hasInvalidLinks;
    }

    private boolean hasDuplicateIds() {
        Set<String> ids = new HashSet<>(containers.size());
        for (ContainerStartConfiguration configuration : containers) {
            if (ids.contains(configuration.getId())) {
                String message = String.format("Container ID '%s' used twice, Container IDs should be unique!",
                        configuration.getId());
                getLog().error(message);
                registerPluginError(new DockerPluginError(mojoExecution.getGoal(), message));
                return true;
            }
            ids.add(configuration.getId());
        }
        return false;
    }

    private void exposePortsToProject(ContainerStartConfiguration configuration, List<ExposedPort> exposedPorts) {
        exposedPorts.parallelStream().forEach(port -> {
            String prefix = String.format("docker.containers.%s.ports.%s.",
                    configuration.getId(), port.getContainerPort());
            addPropertyToProject(prefix + "host", port.getHost());
            addPropertyToProject(prefix + "port", String.valueOf(port.getExternalPort()));
        });
    }

    private void replaceImageWithBuiltImageIdIfInternalId(ContainerStartConfiguration configuration) {
        Optional<BuiltImageInfo> builtImage = getBuiltImageForStartId(configuration.getImage());
        if (builtImage.isPresent()) {
            configuration.fromImage(builtImage.get().getImageId());
        }
    }

    private void replaceLinkedContainerIdsWithStartedNames(final ContainerStartConfiguration configuration) {
        for (ContainerLink link : configuration.getLinks()) {
            final String containerId = link.getContainerId();
            String name = getStartedContainers().stream()
                    .filter(input -> input.getContainerId().equals(containerId))
                    .findFirst()
                    .get().getContainerInfo().getName();

            link.toContainer(name);
        }
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public void setMojoExecution(final MojoExecution mojoExecution) {
        this.mojoExecution = mojoExecution;
    }

    private void addPropertyToProject(String key, String value) {
        getLog().info(String.format("Setting property '%s' to '%s'", key, value));
        project.getProperties().setProperty(key, value);
    }

    @Override
    protected String getMojoGoalName() {
        return "start-containers";
    }
}