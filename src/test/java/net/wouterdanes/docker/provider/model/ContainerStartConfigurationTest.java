package net.wouterdanes.docker.provider.model;

import org.junit.Test;

import net.wouterdanes.docker.remoteapi.model.ContainerLink;

public class ContainerStartConfigurationTest {

    @Test
    public void testThatGetEnvReturnsEmptyEnvWhenEnvNotInitialized() throws Exception {

        ContainerStartConfiguration configuration = new ContainerStartConfiguration();

        assert configuration.getEnv().isEmpty();

    }

    @Test
    public void testThatCallingWithLinksTwiceAddsToFirstLinks() throws Exception {

        ContainerLink link1 = new ContainerLink();
        ContainerLink link2 = new ContainerLink();
        ContainerStartConfiguration configuration = new ContainerStartConfiguration()
                .withLinks(link1)
                .withLinks(link2);

        assert configuration.getLinks().contains(link1);
        assert configuration.getLinks().contains(link2);
    }

    @Test
    public void testThatGetLinksReturnsEmptyListWhenNotInitialized() throws Exception {

        ContainerStartConfiguration configuration = new ContainerStartConfiguration();

        assert configuration.getLinks().isEmpty();

    }
}