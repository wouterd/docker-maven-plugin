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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import net.wouterdanes.docker.provider.model.BuiltImageInfo;
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
        for (BuiltImageInfo image : getBuiltImages()) {
            if (image.shouldKeepAfterStopping()) {
                getLog().info(String.format("Keeping image %s", image.getImageId()));
                continue;
            }

            getLog().info(String.format("Removing image '%s' (%s) ...", image.getImageId(), image.getStartId()));

            try {
                getDockerProvider().removeImage(image.getImageId());
            } catch (DockerException e) {
                getLog().error("Failed to remove image", e);
            }
        }
    }
}
