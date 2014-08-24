package net.wouterdanes.docker.maven;

import com.google.common.base.Optional;

import net.wouterdanes.docker.remoteapi.exception.DockerException;

/**
 * Base class for Mojos that execute prior to the "verify" phase. In these Mojos,
 * {@link DockerException}s must be caught, suppressed but retained to be rethrown
 * during verification.
 */
public abstract class AbstractPreVerifyDockerMojo extends AbstractDockerMojo {

    @Override
    protected void handleDockerException(String message, DockerException e) {
        getLog().error(message, e);
        Optional<String> apiResponse = e.getApiResponse();
        if (apiResponse.isPresent()) {
            getLog().info(String.format("Api response:\n%s", apiResponse.get()));
        }
        registerPluginError(new DockerPluginError(getMojoGoalName(), message, e));
    }

    /**
     * For diagnostic purposes.
     *
     * @return the goal for this mojo
     */
    protected abstract String getMojoGoalName();

}
