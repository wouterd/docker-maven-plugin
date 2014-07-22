package net.wouterdanes.docker.maven;

import net.wouterdanes.docker.remoteapi.exception.DockerException;

/**
 * This class holds plugin execution errors that are tested for in the verify goal of this plugin.
 * This class is immutable and should be initialized via the constructor.
 */
public class DockerPluginError {

    private final String goal;
    private final String message;
    private final DockerException exception;

    public DockerPluginError(final String goal, final String message, final DockerException exception) {
        this.goal = goal;
        this.message = message;
        this.exception = exception;
    }

    public String getGoal() {
        return goal;
    }

    public String getMessage() {
        return message;
    }

    public DockerException getException() {
        return exception;
    }
}
