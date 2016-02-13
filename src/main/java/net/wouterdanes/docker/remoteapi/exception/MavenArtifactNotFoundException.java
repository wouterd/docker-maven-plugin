package net.wouterdanes.docker.remoteapi.exception;

public class MavenArtifactNotFoundException extends DockerException {

    public MavenArtifactNotFoundException(String artifactName) {
        super(makeMessage(artifactName));
    }

    public MavenArtifactNotFoundException(String artifactName, Throwable cause) {
        super(makeMessage(artifactName), cause);
    }

    private static String makeMessage(String artifactName) {
        return String.format("Maven artifact '%s' not found.", artifactName);
    }

}
