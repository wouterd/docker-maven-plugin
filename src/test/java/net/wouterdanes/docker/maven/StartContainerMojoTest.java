package net.wouterdanes.docker.maven;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import net.wouterdanes.docker.provider.DockerProvider;

public class StartContainerMojoTest {

    @Test
    public void testThatMojoStartsAContainerOnTheProvider() throws Exception {
        DockerProvider fakeProvider = Mockito.mock(DockerProvider.class);
        Mockito.when(fakeProvider.startContainer(Matchers.any(ContainerStartConfiguration.class))).thenReturn("someID");

        StartContainerMojo mojo = new StartContainerMojo(fakeProvider);

        ContainerStartConfiguration startConfiguration = new ContainerStartConfiguration();
        mojo.setContainers(Arrays.asList(startConfiguration));

        mojo.execute();

        Mockito.verify(fakeProvider).startContainer(startConfiguration);
    }
}
