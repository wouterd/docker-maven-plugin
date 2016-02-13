package net.wouterdanes.docker.provider.model;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.Optional;

public class MavenArtifact {

    /**
     * The {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>} of the artifact to resolve.
     */
    @Parameter(required = true)
    private String dependency;

    @Parameter
    private String dest;

    public String getDependency() {
        return dependency;
    }

    public void setDependency(String dependency) {
        this.dependency = dependency;
    }

    public Optional<String> getDest() {
        return Optional.ofNullable(dest);
    }

    public void setDest(String dest) {
        this.dest = dest;
    }
}
