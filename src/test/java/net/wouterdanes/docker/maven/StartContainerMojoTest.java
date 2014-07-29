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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import net.wouterdanes.docker.provider.AbstractFakeDockerProvider;
import net.wouterdanes.docker.provider.DockerExceptionThrowingDockerProvider;
import net.wouterdanes.docker.provider.DockerProviderSupplier;
import net.wouterdanes.docker.provider.model.ContainerStartConfiguration;
import net.wouterdanes.docker.provider.model.ExposedPort;
import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StartContainerMojoTest {

    private static final String FAKE_PROVIDER_KEY = UUID.randomUUID().toString();

    private final MavenProject mavenProject = mock(MavenProject.class);
    private final MojoExecution mojoExecution = new MojoExecution(null, "start-containers", "some-id");

    @Before
    public void setUp() throws Exception {
        Properties mavenProjectProperties = new Properties();
        when(mavenProject.getProperties()).thenReturn(mavenProjectProperties);

        FakeDockerProvider.instance = mock(FakeDockerProvider.class);

        Mockito.when(FakeDockerProvider.instance.startContainer(Matchers.any(ContainerStartConfiguration.class)))
                .thenReturn("someId");
        DockerProviderSupplier.registerProvider(FAKE_PROVIDER_KEY, FakeDockerProvider.class);

        DockerExceptionThrowingDockerProvider.class.newInstance();
    }

    @After
    public void tearDown() throws Exception {
        DockerProviderSupplier.removeProvider(FAKE_PROVIDER_KEY);
    }

    @Test
    public void testThatMojoStartsAContainerOnTheProvider() throws Exception {
        ContainerStartConfiguration startConfiguration = new ContainerStartConfiguration();
        StartContainerMojo mojo = createMojo(startConfiguration);

        mojo.execute();

        verify(FakeDockerProvider.instance).startContainer(startConfiguration);

        assert mojo.getPluginErrors().isEmpty();
    }

    @Test
    public void testThatMojoExposesContainerPortsAsProperties() throws Exception {
        List<ExposedPort> exposedPorts = Arrays.asList(
                new ExposedPort("tcp/8080", 1337, "172.42.123.10"),
                new ExposedPort("tcp/9000", 41329, "localhost")
        );
        when(FakeDockerProvider.instance.getExposedPorts("someId")).thenReturn(exposedPorts);

        ContainerStartConfiguration startConfiguration = new ContainerStartConfiguration()
                .withId("ubuntu").fromImage("debian");

        StartContainerMojo mojo = createMojo(startConfiguration);

        mojo.execute();

        Properties properties = mavenProject.getProperties();
        assertEquals("172.42.123.10", properties.getProperty("docker.containers.ubuntu.ports.tcp/8080.host"));
        assertEquals("1337", properties.getProperty("docker.containers.ubuntu.ports.tcp/8080.port"));
        assertEquals("localhost", properties.getProperty("docker.containers.ubuntu.ports.tcp/9000.host"));
        assertEquals("41329", properties.getProperty("docker.containers.ubuntu.ports.tcp/9000.port"));

        assert mojo.getPluginErrors().isEmpty();
    }

    @Test
    public void testThatMojoStartsBuiltImageWhenReferencedById() throws Exception {
        ImageBuildConfiguration imageConfig = new ImageBuildConfiguration();
        imageConfig.setId("built-image");

        ContainerStartConfiguration startConfiguration = new ContainerStartConfiguration()
                .fromImage("built-image").withId("someId");

        StartContainerMojo mojo = createMojo(startConfiguration);
        mojo.registerBuiltImage("the-image-id", imageConfig);

        mojo.execute();

        ArgumentCaptor<ContainerStartConfiguration> captor = ArgumentCaptor.forClass(ContainerStartConfiguration.class);
        verify(FakeDockerProvider.instance).startContainer(captor.capture());

        ContainerStartConfiguration passedValue = captor.getValue();
        assertEquals("the-image-id", passedValue.getImage());

        assert mojo.getPluginErrors().isEmpty();
    }

    @Test
    public void testThatMojoDoesNotStartWhenSkipped() throws Exception {
        ContainerStartConfiguration startConfiguration = new ContainerStartConfiguration();
        StartContainerMojo mojo = createMojo(startConfiguration);
        mojo.setSkip(true);

        mojo.execute();

        verify(FakeDockerProvider.instance, never()).startContainer(startConfiguration);
        assert mojo.getPluginErrors().isEmpty();
    }

    @Test
    public void testThatAnErrorIsRegisteredWhenStartingAContainerFails() throws Exception {
        StartContainerMojo mojo = createMojo(new ContainerStartConfiguration(),
                DockerExceptionThrowingDockerProvider.PROVIDER_KEY);

        mojo.execute();

        assert !mojo.getPluginErrors().isEmpty();
    }

    @Test
    public void testThatMojoAddsAnErrorWhenThereIsDuplicateContainerIds() throws Exception {
        ContainerStartConfiguration startConfiguration = new ContainerStartConfiguration()
                .withId("duplicate-id");

        StartContainerMojo mojo = createMojo(Arrays.asList(startConfiguration, startConfiguration), FAKE_PROVIDER_KEY);

        mojo.execute();

        assert !mojo.getPluginErrors().isEmpty();
        verify(FakeDockerProvider.instance, never()).startContainer(any(ContainerStartConfiguration.class));
    }

    private StartContainerMojo createMojo(final ContainerStartConfiguration startConfiguration) {
        return createMojo(startConfiguration, FAKE_PROVIDER_KEY);
    }

    private StartContainerMojo createMojo(final ContainerStartConfiguration startConfiguration, String provider) {
        return createMojo(Arrays.asList(startConfiguration), provider);
    }

    private StartContainerMojo createMojo(List<ContainerStartConfiguration> startConfigurations, String provider) {
        StartContainerMojo mojo = new StartContainerMojo(startConfigurations);
        mojo.setProject(mavenProject);
        mojo.setProviderName(provider);
        mojo.setPluginContext(new HashMap());
        mojo.setMojoExecution(mojoExecution);

        return mojo;
    }

    public static class FakeDockerProvider extends AbstractFakeDockerProvider {
        private static FakeDockerProvider instance;

        @Override
        protected AbstractFakeDockerProvider getInstance() {
            return instance;
        }
    }
}
