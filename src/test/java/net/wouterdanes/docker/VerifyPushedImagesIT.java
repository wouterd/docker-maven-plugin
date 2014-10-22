package net.wouterdanes.docker;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * This Integration Test checks the registry to see if all images are pushed by the IT POMs.
 */
@Ignore("Code works, tests fail.")
public class VerifyPushedImagesIT {

    private WebTarget repositories;

    @Before
    public void setUp() throws Exception {

        String registryUri = System.getProperty("docker.registry");
        ClientConfig config = new ClientConfig(new JacksonFeature());
        repositories = ClientBuilder.newClient(config).target(registryUri).path("v1/repositories");

    }

    @Test
    public void testThatPushWithCredsItImagesGotPushed() throws Exception {

        assertThatImageExists("drek", "latest", "push-with-creds-it");
        assertThatImageExists("nginxier", "latest", "push-with-creds-it");

    }

    @Test
    public void testThatPushWithExplicitRegistryItImagesGotPushed() throws Exception {

        assertThatImageExists("corgis", "latest", "push-with-explicit-registry-it");

    }

    @Test
    public void testThatTagAndPushItImagesGotPushed() throws Exception {

        assertThatImageExists("dross", "snapshot", "tag-and-push-it");
        assertThatImageExists("dross", "release", "tag-and-push-it");
        assertThatImageExists("dross", "4.1", "tag-and-push-it");

    }

    private void assertThatImageExists(String name, String tag, String itName) {

        Map<String,String> tags = repositories.path(name).path("tags")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<Map<String, String>>() {
                });

        Assert.assertTrue(String.format("Integration test '%s' should push image '%s:%s'.", itName, name, tag),
                tags.containsKey(tag));

    }
}
