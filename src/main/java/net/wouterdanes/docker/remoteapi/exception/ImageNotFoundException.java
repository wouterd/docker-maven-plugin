package net.wouterdanes.docker.remoteapi.exception;

public class ImageNotFoundException extends DockerException {
    public ImageNotFoundException(String imageName, Throwable cause) {
        super(String.format("Image '%s' not found.", imageName), cause);
    }
}
