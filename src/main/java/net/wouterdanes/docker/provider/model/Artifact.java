package net.wouterdanes.docker.provider.model;

import java.io.File;

import com.google.common.base.Optional;

import org.apache.maven.plugins.annotations.Parameter;

public class Artifact {
    @Parameter(required = true)
    private File file;

    @Parameter
    private String dest;

    public Optional<String> getDest() {
        return Optional.fromNullable(dest);
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
