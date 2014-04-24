package net.wouterdanes.docker.remoteapi.exception;

import com.google.common.base.Optional;

public class DockerException extends RuntimeException {

    private Optional<String> apiResponse = Optional.absent();

    public DockerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DockerException(final Throwable e) {
        super("Docker internal error occurred", e);
    }

    public DockerException(final String message) {
        super(message);
    }

    public DockerException(final String message, final String apiResponse) {
        super(message);
        this.apiResponse = Optional.of(apiResponse);
    }

    public Optional<String> getApiResponse() {
        return apiResponse;
    }
}
