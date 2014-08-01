package net.wouterdanes.docker.maven;

import net.wouterdanes.docker.remoteapi.exception.DockerException;

import com.google.common.base.Optional;

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
     */
    protected abstract String getMojoGoalName();

}
