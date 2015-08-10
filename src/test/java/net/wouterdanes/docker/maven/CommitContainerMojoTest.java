package net.wouterdanes.docker.maven;

import com.google.common.base.Optional;
import net.wouterdanes.docker.provider.AbstractFakeDockerProvider;
import net.wouterdanes.docker.provider.DockerProviderSupplier;
import net.wouterdanes.docker.provider.model.ContainerCommitConfiguration;
import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

/**
 * Created by eli on 30/06/15.
 */
public class CommitContainerMojoTest {

    private final String fakeProviderKey = UUID.randomUUID().toString();
    private CommitContainerMojo mojo;

    @Before
    public void setUp() throws Exception {

        FakeDockerProvider.instance = mock(FakeDockerProvider.class);
        DockerProviderSupplier.registerProvider(fakeProviderKey, FakeDockerProvider.class);

        mojo = new CommitContainerMojo();
        mojo.setPluginContext(new HashMap());

        mojo.setProviderName(fakeProviderKey);
    }

    @After
    public void tearDown() throws Exception {

        DockerProviderSupplier.removeProvider(fakeProviderKey);

    }

    @Test
    public void testNoConfigurationSpecified() throws Exception {

        mojo.execute();
        Mockito.verify(FakeDockerProvider.instance, Mockito.never()).commitContainer(any(ContainerCommitConfiguration.class));

        mojo.setConfiguration(new ArrayList<ContainerCommitConfiguration>());
        mojo.execute();
        Mockito.verify(FakeDockerProvider.instance, Mockito.never()).commitContainer(any(ContainerCommitConfiguration.class));
    }

    @Test
    public void testNoIdSpecified() throws Exception {

        List<ContainerCommitConfiguration> list = new ArrayList<>();
        list.add(new ContainerCommitConfiguration());
        mojo.setConfiguration(list);

        mojo.execute();

        Mockito.verify(FakeDockerProvider.instance, Mockito.never()).commitContainer(any(ContainerCommitConfiguration.class));
    }

    public static class FakeDockerProvider extends AbstractFakeDockerProvider {

        private static FakeDockerProvider instance;

        @Override
        protected AbstractFakeDockerProvider getInstance() {
            return instance;
        }
    }
}
