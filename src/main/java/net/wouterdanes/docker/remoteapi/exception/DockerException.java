package net.wouterdanes.docker.remoteapi.exception;

public class DockerException extends RuntimeException {
    public DockerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DockerException(final Throwable e) {
        super("Docker internal error occurred", e);
    }

    public DockerException(final String message) {
        super(message);
    }
}
