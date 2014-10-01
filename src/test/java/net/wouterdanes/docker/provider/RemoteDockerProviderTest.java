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

        Assert.assertEquals(String.format(provider.getClass().getName() + "{host='lalahost', port=1337}"), provider.toString());

    }

    @Test
    public void testThatEnvironmentVariableOrDefaultIsPickedUp() throws Exception {

        String expectedHost = "127.0.0.1";
        int expectedPort = 2375;

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

        String expectedValue = String.format(provider.getClass().getName() + "{host='%s', port=%s}", expectedHost, expectedPort);
        Assert.assertEquals(expectedValue, provider.toString());

    }
}
