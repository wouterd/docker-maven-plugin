/*
    Copyright 2014 Wouter Danes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

*/

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