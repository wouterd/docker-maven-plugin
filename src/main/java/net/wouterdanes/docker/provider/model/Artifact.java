package net.wouterdanes.docker.provider.model;

import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Optional;

public class Artifact {
    @Parameter(required = true)
    private File file;

    @Parameter
    private String dest;

    public Optional<String> getDest() {
        return Optional.ofNullable(dest);
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
