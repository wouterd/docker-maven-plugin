package net.wouterdanes.docker.provider;

import net.wouterdanes.docker.maven.ContainerStartConfiguration;

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

}
