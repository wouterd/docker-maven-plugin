package net.wouterdanes.docker.maven;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;

import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ImageBuildConfigurationTest {

    @Test
    public void testThatConfigurationDetectsAValidConfiguration() throws Exception {
        File dockerFile = new File("/var/tmp/Dockerfile");
        File otherFile = new File("/var/tmp/some_other_file");

        ImageBuildConfiguration buildConfiguration = new ImageBuildConfiguration();
        buildConfiguration.setFiles(Arrays.asList(dockerFile, otherFile));

        assertTrue(buildConfiguration.isValid());
    }

    @Test
    public void testThatConfigurationDetectsAnInvalidConfiguration() throws Exception {
        File someFile = new File("/var/tmp/somefile");
        File anotherFile = new File("/log/stash");

        ImageBuildConfiguration buildConfiguration = new ImageBuildConfiguration();
        buildConfiguration.setFiles(Arrays.asList(someFile, anotherFile));

        assertFalse(buildConfiguration.isValid());
    }
}