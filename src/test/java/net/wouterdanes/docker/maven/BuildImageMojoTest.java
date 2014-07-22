package net.wouterdanes.docker.maven;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import net.wouterdanes.docker.provider.AbstractFakeDockerProvider;
import net.wouterdanes.docker.provider.DockerExceptionThrowingDockerProvider;
import net.wouterdanes.docker.provider.DockerProviderSupplier;
import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;
import static org.mockito.Matchers.any;

public class BuildImageMojoTest {

    private static final String FAKE_PROVIDER_KEY = UUID.randomUUID().toString();
    private BuildImageMojo mojo = new BuildImageMojo();

    @Before
    public void setUp() throws Exception {
        mojo.setPluginContext(new HashMap());
        FakeDockerProvider.instance = Mockito.mock(FakeDockerProvider.class);

        DockerProviderSupplier.registerProvider(FAKE_PROVIDER_KEY, FakeDockerProvider.class);
        Class.forName(DockerExceptionThrowingDockerProvider.class.getName());
    }

    @After
    public void tearDown() throws Exception {
        DockerProviderSupplier.removeProvider(FAKE_PROVIDER_KEY);
    }

    @Test(expected = MojoExecutionException.class)
    public void testThatTheMojoFailsIfAnInvalidImageExists() throws Exception {

        ImageBuildConfiguration image = Mockito.mock(ImageBuildConfiguration.class);
        Mockito.when(image.isValid()).thenReturn(false);
        List<ImageBuildConfiguration> images = Collections.singletonList(image);
        mojo.setImages(images);

        executeMojo(FAKE_PROVIDER_KEY);

    }

    @Test
    public void testThatTheMojoBuildsAtLeastOneImageWhenAllImagesAreValid() throws Exception {

        ImageBuildConfiguration image = Mockito.mock(ImageBuildConfiguration.class);
        Mockito.when(image.isValid()).thenReturn(true);
        List<ImageBuildConfiguration> images = Collections.singletonList(image);
        mojo.setImages(images);

        executeMojo(FAKE_PROVIDER_KEY);

        Mockito.verify(FakeDockerProvider.instance, Mockito.atLeastOnce()).buildImage(any(ImageBuildConfiguration.class));

    }

    @Test
    public void testThatTheMojoLogsAnErrorWhenBuildingAnImageFails() throws Exception {

        ImageBuildConfiguration image = Mockito.mock(ImageBuildConfiguration.class);
        Mockito.when(image.isValid()).thenReturn(true);
        List<ImageBuildConfiguration> images = Collections.singletonList(image);
        mojo.setImages(images);

        executeMojo(DockerExceptionThrowingDockerProvider.PROVIDER_KEY);

        assert !mojo.getPluginErrors().isEmpty();

    }

    private void executeMojo(String provider) throws MojoExecutionException, MojoFailureException {
        mojo.setProviderName(provider);
        mojo.execute();
    }

    public static class FakeDockerProvider extends AbstractFakeDockerProvider {

        private static FakeDockerProvider instance;

        @Override
        protected AbstractFakeDockerProvider getInstance() {
            return instance;
        }
    }

}