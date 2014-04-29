package net.wouterdanes.docker.provider.model;

import java.io.File;
import java.util.List;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * This class is responsible for holding the configuration of a single docker image to be built by the
 * {@link net.wouterdanes.docker.maven.BuildImageMojo}
 */
public class ImageBuildConfiguration {

    @Parameter(required = true)
    private List<File> files;

    @Parameter(required = true)
    private String id;

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(final List<File> files) {
        this.files = files;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Checks if this is a valid configuration, every image build package should have a Dockerfile included.
     * @return <code>true</code> if this configuration can be built, <code>false</code> otherwise.
     */
    public boolean isValid() {
        for (File file : files) {
            if (file.getName().equals("Dockerfile")) {
                return true;
            }
        }
        return false;
    }
}
