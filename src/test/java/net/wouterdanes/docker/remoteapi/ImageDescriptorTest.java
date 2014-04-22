package net.wouterdanes.docker.remoteapi;

import org.junit.Test;

import net.wouterdanes.docker.remoteapi.model.ImageDescriptor;
import static junit.framework.Assert.assertEquals;

public class ImageDescriptorTest {

    @Test
    public void testThatElementsAreDetected() throws Exception {
        assertDescriptor("ubuntu:precise", "ubuntu:precise", null, null, "ubuntu", "precise");
        assertDescriptor("ubuntu", "ubuntu", null, null, "ubuntu", null);
        assertDescriptor("tutum.co/ubuntu", "tutum.co/ubuntu", "tutum.co", null, "ubuntu", null);
        assertDescriptor("tutum.co/ubuntu:precise", "tutum.co/ubuntu:precise", "tutum.co", null, "ubuntu", "precise");
        assertDescriptor("wouter/ubuntu", "wouter/ubuntu", null, "wouter", "ubuntu", null);
        assertDescriptor("tutum.co/wouter/ubuntu", "tutum.co/wouter/ubuntu", "tutum.co", "wouter", "ubuntu", null);
        assertDescriptor("tutum.co/wouter/ubuntu:precise",
                "tutum.co/wouter/ubuntu:precise", "tutum.co", "wouter", "ubuntu", "precise");
        assertDescriptor("wouter/ubuntu:precise", "wouter/ubuntu:precise", null, "wouter", "ubuntu", "precise");
        assertDescriptor("abde1231adc", "abde1231adc", null, null, "abde1231adc", null);
    }


    private static void assertDescriptor(String qualifier, String id, String registry, String repository,
                                  String image, String tag) {
        ImageDescriptor descriptor = new ImageDescriptor(qualifier);
        assertEquals("Id should be correct", id, descriptor.getId());
        assertEquals("Registry should be correct", registry, descriptor.getRegistry());
        assertEquals("Repository should be correct", repository, descriptor.getRepository());
        assertEquals("Image should be correct", image, descriptor.getImage());
        assertEquals("Tag should be correct", tag, descriptor.getTag());
    }
}
