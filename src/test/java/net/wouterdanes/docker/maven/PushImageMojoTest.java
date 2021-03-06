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

import net.wouterdanes.docker.provider.AbstractFakeDockerProvider;
import net.wouterdanes.docker.provider.DockerProviderSupplier;
import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class PushImageMojoTest {

    private final String fakeProviderKey = UUID.randomUUID().toString();
    private PushImageMojo mojo;

    @Before
    public void setUp() throws Exception {

        FakeDockerProvider.instance = mock(FakeDockerProvider.class);
        DockerProviderSupplier.registerProvider(fakeProviderKey, FakeDockerProvider.class);

        mojo = new PushImageMojo();
        mojo.setPluginContext(new HashMap());

        mojo.setProviderName(fakeProviderKey);
    }

    @After
    public void tearDown() throws Exception {

        DockerProviderSupplier.removeProvider(fakeProviderKey);

    }

    @Test(expected = MojoFailureException.class)
    public void testThatImagesThatHaveNoNameCauseAnError() throws Exception {

        mojo.enqueueForPushing("some-image-id", Optional.empty());
        mojo.enqueueForPushing("another-image-id", new ImageBuildConfiguration());
        mojo.execute();

    }

    @Test
    public void testThatNoImagesArePushedWhenThereAreNoImagesMarkedToPush() throws Exception {

        mojo.execute();

        verify(FakeDockerProvider.instance, never()).pushImage(Matchers.<String>any());

    }

    public static class FakeDockerProvider extends AbstractFakeDockerProvider {

        private static FakeDockerProvider instance;

        @Override
        protected AbstractFakeDockerProvider getInstance() {
            return instance;
        }
    }
}