package net.wouterdanes.docker.provider;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class RemoteDockerProviderTest {
    @Before
    public void setUp() throws Exception {
        System.getProperties().remove(RemoteDockerProvider.DOCKER_HOST_PROPERTY);
        System.getProperties().remove(RemoteDockerProvider.DOCKER_PORT_PROPERTY);
    }

    @Test
    public void testThatDockerHostAndDockerPortOverride() throws Exception {

        System.setProperty(RemoteDockerProvider.DOCKER_HOST_PROPERTY, "lalahost");
        System.setProperty(RemoteDockerProvider.DOCKER_PORT_PROPERTY, "1337");

        RemoteDockerProvider provider = new RemoteDockerProvider();

        Assert.assertEquals(String.format("RemoteDockerProvider{host='lalahost', port=1337}"), provider.toString());

    }

    @Test
    public void testThatEnvironmentVariableOrDefaultIsPickedUp() throws Exception {

        String expectedHost = "127.0.0.1";
        int expectedPort = 4243;

        // Can't really mock this easily, so i went with parsing my own system env here
        // TODO: Add PowerMock
        String env = System.getenv(RemoteDockerProvider.DOCKER_HOST_SYSTEM_ENV);
        try {
            URI dockerUrl = URI.create(env);
            if ("tcp".equalsIgnoreCase(dockerUrl.getScheme())) {
                expectedHost = dockerUrl.getHost();
                expectedPort = dockerUrl.getPort();
            }
        } catch (NullPointerException | IllegalArgumentException ignored) {
        }

        RemoteDockerProvider provider = new RemoteDockerProvider();

        String expectedValue = String.format("RemoteDockerProvider{host='%s', port=%s}", expectedHost, expectedPort);
        org.junit.Assert.assertEquals(expectedValue, provider.toString());

    }
}
