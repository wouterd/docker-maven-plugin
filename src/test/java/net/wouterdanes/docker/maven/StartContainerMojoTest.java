package net.wouterdanes.docker.maven;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import net.wouterdanes.docker.provider.DockerProvider;
import net.wouterdanes.docker.provider.DockerProviderSupplier;
import net.wouterdanes.docker.provider.model.ContainerStartConfiguration;
import net.wouterdanes.docker.provider.model.ExposedPort;
import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StartContainerMojoTest {

    private MavenProject mavenProject;

    @Before
    public void setUp() throws Exception {

        mavenProject = mock(MavenProject.class);
        Properties mavenProjectProperties = new Properties();
        when(mavenProject.getProperties()).thenReturn(mavenProjectProperties);

        FakeDockerProvider.instance = mock(FakeDockerProvider.class);

        Mockito.when(FakeDockerProvider.instance.startContainer(Matchers.any(ContainerStartConfiguration.class)))
                .thenReturn("someId");
        DockerProviderSupplier.registerProvider("fake", FakeDockerProvider.class);
    }

    @Test
    public void testThatMojoStartsAContainerOnTheProvider() throws Exception {
        ContainerStartConfiguration startConfiguration = new ContainerStartConfiguration();
        StartContainerMojo mojo = createMojo(startConfiguration);

        mojo.execute();

        verify(FakeDockerProvider.instance).startContainer(startConfiguration);
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
    }

    @Test
    public void testThatMojoStartsBuiltImageWhenReferencedById() throws Exception {
        ContainerStartConfiguration startConfiguration = new ContainerStartConfiguration()
                .fromImage("built-image").withId("someId");

        StartContainerMojo mojo = createMojo(startConfiguration);
        mojo.registerBuiltImage("built-image", "the-image-id");

        mojo.execute();

        ArgumentCaptor<ContainerStartConfiguration> captor = ArgumentCaptor.forClass(ContainerStartConfiguration.class);
        verify(FakeDockerProvider.instance).startContainer(captor.capture());

        ContainerStartConfiguration passedValue = captor.getValue();
        assertEquals("the-image-id", passedValue.getImage());
    }

    private StartContainerMojo createMojo(final ContainerStartConfiguration startConfiguration) {
        StartContainerMojo mojo = new StartContainerMojo(Arrays.asList(startConfiguration));

        mojo.setProject(mavenProject);
        mojo.setProviderName("fake");
        mojo.setPluginContext(new HashMap());
        return mojo;
    }

    public static class FakeDockerProvider implements DockerProvider {

        private static FakeDockerProvider instance;

        private final FakeDockerProvider proxy;

        public FakeDockerProvider() {
            proxy = instance;
        }

        @Override
        public String startContainer(final ContainerStartConfiguration configuration) {
            return proxy.startContainer(configuration);
        }

        @Override
        public void stopContainer(final String containerId) {
            proxy.stopContainer(containerId);
        }

        @Override
        public void deleteContainer(final String containerId) {
            proxy.deleteContainer(containerId);
        }

        @Override
        public List<ExposedPort> getExposedPorts(final String containerId) {
            return proxy.getExposedPorts(containerId);
        }

        @Override
        public String buildImage(final ImageBuildConfiguration image) {
            return proxy.buildImage(image);
        }

        @Override
        public void removeImage(final String imageId) {
            proxy.removeImage(imageId);
        }
    }
}
