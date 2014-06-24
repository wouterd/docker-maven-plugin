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

package net.wouterdanes.docker.provider;

import java.util.List;

import net.wouterdanes.docker.provider.model.ContainerStartConfiguration;
import net.wouterdanes.docker.provider.model.ExposedPort;
import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;

/**
 * This interface represents an implementation that provides Docker functionality. Examples are:
 * <ul>
 *     <li>Local Docker via Unix Socket</li>
 *     <li>Local Docker via TCP Socket</li>
 *     <li>Remote (Boot2Docker) via TCP Socket</li>
 *     <li>tutum.co</li>
 * </ul>
 */
public interface DockerProvider {

    /**
     * Starts a docker container and returns the ID of the started container
     * @param configuration the configuration parameters
     * @return the ID of the started container
     */
    String startContainer(ContainerStartConfiguration configuration);

    /**
     * Stops a docker container
     * @param containerId the Id of the container to stop
     */
    void stopContainer(String containerId);

    /**
     * Delete a docker container
     * @param containerId the Id of the container to delete
     */
    void deleteContainer(String containerId);

    /**
     * Returns a list of ports exposed by the container, including information on how to reach them
     * @param containerId the Id of the container
     * @return {@link List} of {@link net.wouterdanes.docker.provider.model.ExposedPort}s
     */
    List<ExposedPort> getExposedPorts(String containerId);

    /**
     * Builds a new Docker Image based on the passed configuration and returns the id of the newly created image.
     * @param image the image configuration to use
     * @return the id of the new Docker Image
     */
    String buildImage(ImageBuildConfiguration image);

    /**
     * Removes an image from docker
     * @param imageId the Id of the images to remove
     */
    void removeImage(String imageId);
}
