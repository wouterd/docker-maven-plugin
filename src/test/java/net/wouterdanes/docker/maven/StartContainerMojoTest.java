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
import net.wouterdanes.docker.provider.DockerExceptionThrowingDockerProvider;
import net.wouterdanes.docker.provider.DockerProviderSupplier;
import net.wouterdanes.docker.provider.model.ContainerStartConfiguration;
import net.wouterdanes.docker.provider.model.ExposedPort;
import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;
import net.wouterdanes.docker.remoteapi.model.ContainerInspectionResult;
import net.wouterdanes.docker.remoteapi.model.ContainerLink;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class StartContainerMojoTest {

    private static final String FAKE_PROVIDER_KEY = UUID.randomUUID().toString();

    private final MavenProject mavenProject = mock(MavenProject.class);
    private final MojoExecution mojoExecution = new MojoExecution(null, "start-containers", "some-id");

    @Before
    public void setUp() throws Exception {
        Properties mavenProjectProperties = new Properties();
        when(mavenProject.getProperties()).thenReturn(mavenProjectProperties);

        FakeDockerProvider.instance = mock(FakeDockerProvider.class);

        ContainerInspectionResult inspectionResult = mock(ContainerInspectionResult.class);
        when(inspectionResult.getId()).thenReturn("someId");

        when(FakeDockerProvider.instance.startContainer(Matchers.any(ContainerStartConfiguration.class)))
                .thenReturn(inspectionResult);
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

    @Test
    public void testThatMojoAddsErrorWhenContainerIsLinkedThatIsNotStartedBefore() throws Exception {

        ContainerStartConfiguration container1 = new ContainerStartConfiguration()
                .withId("container1")
                .withLink(new ContainerLink().toContainer("container2").withAlias("db"));

        ContainerStartConfiguration container2 = new ContainerStartConfiguration()
                .withId("container2");

        StartContainerMojo mojo = createMojo(Arrays.asList(container1, container2), FAKE_PROVIDER_KEY);

        mojo.execute();

        assert !mojo.getPluginErrors().isEmpty();

    }

    @Test
    public void testThatMojoAddsErrorWhenContainerIsLinkedToNotExistingContainer() throws Exception {

        ContainerStartConfiguration container1 = new ContainerStartConfiguration()
                .withId("container1")
                .withLink(new ContainerLink().toContainer("container2").withAlias("db"));

        StartContainerMojo mojo = createMojo(container1, FAKE_PROVIDER_KEY);

        mojo.execute();

        assert !mojo.getPluginErrors().isEmpty();

    }

    @Test
    public void testThatMojoAsksForLogsAndDoesNotLogAnErrorWhenLogsIndicateServerStarted() throws Exception {

        ContainerStartConfiguration container = new ContainerStartConfiguration()
                .fromImage("some-image")
                .waitForStartup("hello world!")
                .withId("some-container")
                .withStartupTimeout(1);

        when(FakeDockerProvider.instance.getLogs("someId")).thenReturn("Oh hello world!");

        StartContainerMojo mojo = createMojo(container);

        mojo.execute();

        verify(FakeDockerProvider.instance, atLeastOnce()).getLogs("someId");
        assert mojo.getPluginErrors().isEmpty();

    }

    @Test
    public void testThatMojoAddsErrorWhenContainerDoesNotFinishStartupInTime() throws Exception {

        ContainerStartConfiguration container = new ContainerStartConfiguration()
                .fromImage("some-image")
                .waitForStartup("hello world!")
                .withId("some-container")
                .withStartupTimeout(1);

        when(FakeDockerProvider.instance.getLogs("someId")).thenReturn("Oh dear, something went wrong!");

        StartContainerMojo mojo = createMojo(container);

        mojo.execute();

        verify(FakeDockerProvider.instance, atLeastOnce()).getLogs("someId");
        assert !mojo.getPluginErrors().isEmpty();

    }

    @Test
    public void testThatMojoWaitsLongEnoughForTheStartupToFinish() throws Exception {

        ContainerStartConfiguration container = new ContainerStartConfiguration()
                .fromImage("some-image")
                .waitForStartup("hello world!")
                .withId("some-container")
                .withStartupTimeout(2);

        final ThreadLocal<Long> startTime = new ThreadLocal<>();

        when(FakeDockerProvider.instance.startContainer(container)).then(new Answer<ContainerInspectionResult>() {
            @Override
            public ContainerInspectionResult answer(final InvocationOnMock invocation) throws Throwable {
                startTime.set(System.currentTimeMillis());
                ContainerInspectionResult inspectionResult = mock(ContainerInspectionResult.class);
                when(inspectionResult.getId()).thenReturn("someId");
                return inspectionResult;
            }
        });

        when(FakeDockerProvider.instance.getLogs("someId")).then(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                boolean logsAvailable = startTime.get() != null && startTime.get() + 1000 <= System.currentTimeMillis();
                return logsAvailable ? "Well... hello world!" : null;
            }
        });

        StartContainerMojo mojo = createMojo(container);

        mojo.execute();

        verify(FakeDockerProvider.instance, atLeastOnce()).getLogs("someId");
        assert mojo.getPluginErrors().isEmpty();

    }

    @Test
    public void testThatContainerWaitsForLinkedContainerToStart() throws Exception {

        ContainerStartConfiguration linked = new ContainerStartConfiguration()
                .withId("linked")
                .waitForStartup("there");

        final ContainerStartConfiguration parent = new ContainerStartConfiguration()
                .withId("parent")
                .withLink(new ContainerLink()
                                .toContainer("linked")
                                .withAlias("database")
                );

        when(FakeDockerProvider.instance.getLogs("linked")).thenReturn("", "", "there");

        ContainerInspectionResult linkedContainerInspectionResult = mock(ContainerInspectionResult.class);
        when(linkedContainerInspectionResult.getId()).thenReturn("linked");
        when(FakeDockerProvider.instance.startContainer(linked)).thenReturn(linkedContainerInspectionResult);

        when(FakeDockerProvider.instance.startContainer(parent)).then(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                verify(FakeDockerProvider.instance, times(3)).getLogs("linked");
                return new ContainerInspectionResult();
            }
        });

        StartContainerMojo mojo = createMojo(Arrays.asList(linked, parent), FAKE_PROVIDER_KEY);

        mojo.execute();

    }

    @Test
    public void testThatMojoStartsAContainerOnTheProviderWithEnvironmentVariables() throws Exception {
    	Map<String, String> env = new HashMap<>();
    	env.put("TEST_KEY", "test value");
    	
        ContainerStartConfiguration startConfiguration = new ContainerStartConfiguration()
        	.withEnv(env);
        StartContainerMojo mojo = createMojo(startConfiguration);

        mojo.execute();
        
        ArgumentCaptor<ContainerStartConfiguration> captor = ArgumentCaptor.forClass(ContainerStartConfiguration.class);
        verify(FakeDockerProvider.instance).startContainer(captor.capture());

        assert mojo.getPluginErrors().isEmpty();
        ContainerStartConfiguration passedValue = captor.getValue();
        assertNotNull(passedValue.getEnv());
        assertEquals("test value", passedValue.getEnv().get("TEST_KEY"));
    }

    @Test
    public void testThatConfiguredHostnameGetsPassedToDocker() throws Exception {
        ContainerStartConfiguration configuration = new ContainerStartConfiguration().withHostname("test-hostname");
        StartContainerMojo mojo = createMojo(configuration);

        mojo.execute();

        ArgumentCaptor<ContainerStartConfiguration> captor = ArgumentCaptor.forClass(ContainerStartConfiguration.class);
        verify(FakeDockerProvider.instance).startContainer(captor.capture());

        assert mojo.getPluginErrors().isEmpty();

        ContainerStartConfiguration passedValue = captor.getValue();
        assertEquals("test-hostname", passedValue.getHostname());
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
