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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import net.wouterdanes.docker.provider.DockerProvider;
import net.wouterdanes.docker.provider.model.BuiltImageInfo;
import net.wouterdanes.docker.provider.model.ContainerStartConfiguration;
import net.wouterdanes.docker.provider.model.ExposedPort;
import net.wouterdanes.docker.remoteapi.exception.DockerException;
import net.wouterdanes.docker.remoteapi.model.ContainerInspectionResult;
import net.wouterdanes.docker.remoteapi.model.ContainerLink;

/**
 * This class is responsible for starting docking containers in the pre-integration phase of the maven build. The goal
 * is called "start-containers"
 */
@Mojo(defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, name = "start-containers",
        threadSafe = true, instantiationStrategy = InstantiationStrategy.PER_LOOKUP)
public class StartContainerMojo extends AbstractPreVerifyDockerMojo {

    @Parameter(required = true)
    private List<ContainerStartConfiguration> containers;

    @Inject
    public StartContainerMojo(List<ContainerStartConfiguration> containers) {
        this.containers = containers;
    }

    @Component
    private MavenProject project;

    @Component
    private MojoExecution mojoExecution;

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        if (hasDuplicateIds() || hasInvalidLinks()) {
            return;
        }
        DockerProvider provider = getDockerProvider();
        for (ContainerStartConfiguration configuration : containers) {
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
        for (ExposedPort exposedPort : exposedPorts) {
            String prefix = String.format("docker.containers.%s.ports.%s.",
                    configuration.getId(), exposedPort.getContainerPort());
            addPropertyToProject(prefix + "host", exposedPort.getHost());
            addPropertyToProject(prefix + "port", String.valueOf(exposedPort.getExternalPort()));
        }
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
            String name = Collections2.filter(getStartedContainers(), new Predicate<StartedContainerInfo>() {
                @Override
                public boolean apply(final StartedContainerInfo input) {
                    return input.getContainerId().equals(containerId);
                }
            }).iterator().next().getContainerInfo().getName();
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