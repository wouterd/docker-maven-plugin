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

package net.wouterdanes.docker.remoteapi;

import org.junit.Test;

import net.wouterdanes.docker.remoteapi.model.ImageDescriptor;
import static junit.framework.Assert.assertEquals;

public class ImageDescriptorTest {

    @Test
    public void testThatElementsAreDetected() throws Exception {
        assertDescriptor("ubuntu:precise", "ubuntu:precise", null, null, "ubuntu", "precise",
                "ubuntu", "ubuntu", "ubuntu:precise");
        assertDescriptor("ubuntu", "ubuntu", null, null, "ubuntu", null,
                "ubuntu", "ubuntu", "ubuntu");
        assertDescriptor("tutum.co/ubuntu", "tutum.co/ubuntu", "tutum.co", null, "ubuntu", null,
                "tutum.co/ubuntu", "ubuntu", "ubuntu");
        assertDescriptor("tutum.co/ubuntu:precise", "tutum.co/ubuntu:precise", "tutum.co", null, "ubuntu", "precise",
                "tutum.co/ubuntu", "ubuntu", "ubuntu:precise");
        assertDescriptor("wouter/ubuntu", "wouter/ubuntu", null, "wouter", "ubuntu", null,
                "wouter/ubuntu", "wouter/ubuntu", "wouter/ubuntu");
        assertDescriptor("tutum.co/wouter/ubuntu", "tutum.co/wouter/ubuntu", "tutum.co", "wouter", "ubuntu", null,
                "tutum.co/wouter/ubuntu", "wouter/ubuntu", "wouter/ubuntu");
        assertDescriptor("tutum.co/wouter/ubuntu:precise",
                "tutum.co/wouter/ubuntu:precise", "tutum.co", "wouter", "ubuntu", "precise",
                "tutum.co/wouter/ubuntu", "wouter/ubuntu", "wouter/ubuntu:precise");
        assertDescriptor("wouter/ubuntu:precise", "wouter/ubuntu:precise", null, "wouter", "ubuntu", "precise",
                "wouter/ubuntu", "wouter/ubuntu", "wouter/ubuntu:precise");
        assertDescriptor("abde1231adc", "abde1231adc", null, null, "abde1231adc", null,
                "abde1231adc", "abde1231adc", "abde1231adc");

        // support registries with port numbers
        assertDescriptor("tutum.co:5000/ubuntu",
                "tutum.co:5000/ubuntu", "tutum.co:5000", null, "ubuntu", null,
                "tutum.co:5000/ubuntu", "ubuntu", "ubuntu");
        assertDescriptor("tutum.co:5000/ubuntu:precise",
                "tutum.co:5000/ubuntu:precise", "tutum.co:5000", null, "ubuntu", "precise",
                "tutum.co:5000/ubuntu", "ubuntu", "ubuntu:precise");
        assertDescriptor("tutum.co:5000/wouter/ubuntu",
                "tutum.co:5000/wouter/ubuntu", "tutum.co:5000", "wouter", "ubuntu", null,
                "tutum.co:5000/wouter/ubuntu", "wouter/ubuntu", "wouter/ubuntu");
        assertDescriptor("tutum.co:5000/wouter/ubuntu:precise",
                "tutum.co:5000/wouter/ubuntu:precise", "tutum.co:5000", "wouter", "ubuntu", "precise",
                "tutum.co:5000/wouter/ubuntu", "wouter/ubuntu", "wouter/ubuntu:precise");
        // more complicated URL
        assertDescriptor("index_01-reg.tutum.co:5000/wouter/ubuntu:precise",
                "index_01-reg.tutum.co:5000/wouter/ubuntu:precise", "index_01-reg.tutum.co:5000", "wouter", "ubuntu", "precise",
                "index_01-reg.tutum.co:5000/wouter/ubuntu",  "wouter/ubuntu", "wouter/ubuntu:precise");

        // check for image names with "." and "-"
        assertDescriptor("localhost:5000/library/ubuntu-precise", "localhost:5000/library/ubuntu-precise",
                "localhost:5000", "library", "ubuntu-precise", null, "localhost:5000/library/ubuntu-precise",
                "library/ubuntu-precise", "library/ubuntu-precise");
        assertDescriptor("localhost:5000/library/ubuntu.precise", "localhost:5000/library/ubuntu.precise",
                "localhost:5000", "library", "ubuntu.precise", null, "localhost:5000/library/ubuntu.precise",
                "library/ubuntu.precise", "library/ubuntu.precise");

        // check for tags "." and "-" in them
        assertDescriptor("ubuntu:14.04", "ubuntu:14.04", null, null, "ubuntu", "14.04",
                "ubuntu", "ubuntu", "ubuntu:14.04");
        assertDescriptor("ubuntu:mark-2", "ubuntu:mark-2", null, null, "ubuntu", "mark-2",
                "ubuntu", "ubuntu", "ubuntu:mark-2");
    }

    private static void assertDescriptor(String qualifier,
            String id, String registry, String repository,
            String image, String tag, String registryRepoAndImage, String repoAndImage, String repoImageAndTag) {
        ImageDescriptor descriptor = new ImageDescriptor(qualifier);
        assertEquals("Id should be correct", id, descriptor.getId());
        assertEquals("Registry should be correct", registry, descriptor.getRegistry().orNull());
        assertEquals("Repository should be correct", repository, descriptor.getRepository().orNull());
        assertEquals("Image should be correct", image, descriptor.getImage());
        assertEquals("Tag should be correct", tag, descriptor.getTag().orNull());
        assertEquals("Repository+Image should be correct", repoAndImage, descriptor.getRepositoryAndImage());
        assertEquals("Repository+Image+Tag should be correct", repoImageAndTag, descriptor.getRepositoryImageAndTag());
        assertEquals("Registry, Repository+Image should be correct", registryRepoAndImage, descriptor.getRegistryRepositoryAndImage());
    }
}
