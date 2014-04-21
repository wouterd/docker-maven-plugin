package net.wouterdanes.docker.maven;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import net.wouterdanes.docker.provider.DockerProvider;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StartContainerMojoTest {

    private MavenProject mavenProject;

    @Before
    public void setUp() throws Exception {

        mavenProject = mock(MavenProject.class);
        Properties mavenProjectProperties = new Properties();
        when(mavenProject.getProperties()).thenReturn(mavenProjectProperties);

        List<ExposedPort> exposedPorts = Arrays.asList(
                new ExposedPort("tcp/8080", 1337, "172.42.123.10"),
                new ExposedPort("tcp/9000", 41329, "localhost")
        );
        when(FakeDockerProvider.instance.getExposedPorts("someId")).thenReturn(exposedPorts);

        Mockito.when(FakeDockerProvider.instance.startContainer(Matchers.any(ContainerStartConfiguration.class)))
                .thenReturn("someId");
        DockerProviderSupplier.registerProvider("fake", FakeDockerProvider.class);
    }

    @Test
    public void testThatMojoStartsAContainerOnTheProvider() throws Exception {
        ContainerStartConfiguration startConfiguration = new ContainerStartConfiguration();
        StartContainerMojo mojo = new StartContainerMojo(Arrays.asList(startConfiguration));

        mojo.setProject(mavenProject);
        mojo.setProviderName("fake");
        mojo.setPluginContext(new HashMap());

        mojo.execute();

        Mockito.verify(FakeDockerProvider.instance).startContainer(startConfiguration);
    }

    @Test
    public void testThatMojoExposesContainerPortsAsProperties() throws Exception {
        ContainerStartConfiguration startConfiguration = new ContainerStartConfiguration()
                .withId("ubuntu")
                .fromImage("debian");
        StartContainerMojo mojo = new StartContainerMojo(Arrays.asList(startConfiguration));

        mojo.setProject(mavenProject);
        mojo.setProviderName("fake");
        mojo.setPluginContext(new HashMap());

        mojo.execute();

        Properties properties = mavenProject.getProperties();
        assertEquals("172.42.123.10", properties.getProperty("docker.containers.ubuntu.ports.tcp/8080.host"));
        assertEquals("1337", properties.getProperty("docker.containers.ubuntu.ports.tcp/8080.port"));
        assertEquals("localhost", properties.getProperty("docker.containers.ubuntu.ports.tcp/9000.host"));
        assertEquals("41329", properties.getProperty("docker.containers.ubuntu.ports.tcp/9000.port"));
    }

    public static class FakeDockerProvider implements DockerProvider {

        private static FakeDockerProvider instance = mock(FakeDockerProvider.class);

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
    }
}
