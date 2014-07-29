package net.wouterdanes.docker.maven;

import com.google.common.base.Optional;

import net.wouterdanes.docker.remoteapi.exception.DockerException;

/**
 * This class holds plugin execution errors that are tested for in the verify goal of this plugin.
 * This class is immutable and should be initialized via the constructor.
 */
public class DockerPluginError {

    private final String pluginGoal;
    private final String message;
    private final Optional<DockerException> exception;

    public DockerPluginError(final String pluginGoal, final String message, final DockerException exception) {
        this(pluginGoal, message, Optional.of(exception));
    }

    public DockerPluginError(final String pluginGoal, final String message) {
        this(pluginGoal, message, Optional.<DockerException>absent());
    }

    public DockerPluginError(final String pluginGoal, final String message, final Optional<DockerException> exception) {
        this.pluginGoal = pluginGoal;
        this.message = message;
        this.exception = exception;
    }

    public String getPluginGoal() {
        return pluginGoal;
    }

    public String getMessage() {
        return message;
    }

    public Optional<DockerException> getException() {
        return exception;
    }
}
