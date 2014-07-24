package net.wouterdanes.docker.maven;


import java.util.Arrays;
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
import net.wouterdanes.docker.provider.model.ImageTagConfiguration;
import net.wouterdanes.docker.provider.model.PushableImage;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

public class TagImageMojoTest {

    private static final String FAKE_PROVIDER_KEY = UUID.randomUUID().toString();
    private static final String IMAGEID = UUID.randomUUID().toString();
    private static final String STARTID = UUID.randomUUID().toString();
    private static final String NAMEANDTAG = UUID.randomUUID().toString();
    private static final String TAG1 = UUID.randomUUID().toString();
    private static final String TAG2 = UUID.randomUUID().toString();
    private static final String REGISTRY1 = UUID.randomUUID().toString();
    private static final String REGISTRY2 = UUID.randomUUID().toString();

    private TagImageMojo mojo = new TagImageMojo();

    private ImageBuildConfiguration mockImage;

    private ImageTagConfiguration mockTag;
    private List<ImageTagConfiguration> tags;

    @Before
    public void setUp() throws Exception {
        mojo.setPluginContext(new HashMap());

        FakeDockerProvider.instance = Mockito.mock(FakeDockerProvider.class);

        DockerProviderSupplier.registerProvider(FAKE_PROVIDER_KEY, FakeDockerProvider.class);
        DockerExceptionThrowingDockerProvider.class.newInstance();

        mockImage = Mockito.mock(ImageBuildConfiguration.class);
        Mockito.when(mockImage.getId()).thenReturn(STARTID);
        Mockito.when(mockImage.getNameAndTag()).thenReturn(NAMEANDTAG);

        mockTag = Mockito.mock(ImageTagConfiguration.class);
        Mockito.when(mockTag.getId()).thenReturn(STARTID);
        Mockito.when(mockTag.getTags()).thenReturn(Arrays.asList(TAG1, NAMEANDTAG, TAG2));

        tags = Collections.singletonList(mockTag);

        mojo.setImages(tags);
    }

    @After
    public void tearDown() throws Exception {
        DockerProviderSupplier.removeProvider(FAKE_PROVIDER_KEY);
    }

    @Test
    public void testThatImageTaggedButNotPushed() throws Exception {
        executeMojo(FAKE_PROVIDER_KEY);

        assertTrue(mojo.getPluginErrors().isEmpty());

        assertNImagesTagged(3);
        assertImageTagged(STARTID, TAG1);
        assertImageTagged(STARTID, NAMEANDTAG);
        assertImageTagged(STARTID, TAG2);

        assertImageNotEnqueuedForPush();
    }

    @Test
    public void testThatImageTaggedAndIsPushedWithNoRegistry() throws Exception {
        Mockito.when(mockTag.isPush()).thenReturn(true);

        executeMojo(FAKE_PROVIDER_KEY);

        assertTrue(mojo.getPluginErrors().isEmpty());

        assertNImagesTagged(3);
        assertImageTagged(STARTID, TAG1);
        assertImageTagged(STARTID, NAMEANDTAG);
        assertImageTagged(STARTID, TAG2);

        assertImageEnqueuedForPush(0, TAG1, null);
        assertImageEnqueuedForPush(1, NAMEANDTAG, null);
        assertImageEnqueuedForPush(2, TAG2, null);
    }

    @Test
    public void testThatImageTaggedAndIsPushedWithARegistry() throws Exception {
        Mockito.when(mockTag.isPush()).thenReturn(true);
        Mockito.when(mockTag.getRegistry()).thenReturn(REGISTRY1);

        executeMojo(FAKE_PROVIDER_KEY);

        assertTrue(mojo.getPluginErrors().isEmpty());

        assertNImagesTagged(3);
        assertImageTagged(STARTID, TAG1);
        assertImageTagged(STARTID, NAMEANDTAG);
        assertImageTagged(STARTID, TAG2);

        assertImageEnqueuedForPush(0, TAG1, REGISTRY1);
        assertImageEnqueuedForPush(1, NAMEANDTAG, REGISTRY1);
        assertImageEnqueuedForPush(2, TAG2, REGISTRY1);
    }

    @Test
    public void testThatImageTaggedAndIsPushedWithInheritedRegistry() throws Exception {
        Mockito.when(mockImage.isPush()).thenReturn(true);
        Mockito.when(mockImage.getRegistry()).thenReturn(REGISTRY2);

        mojo.registerBuiltImage(IMAGEID, mockImage);

        Mockito.when(mockTag.isPush()).thenReturn(true);

        executeMojo(FAKE_PROVIDER_KEY);

        assertTrue(mojo.getPluginErrors().isEmpty());

        assertNImagesTagged(3);
        assertImageTagged(IMAGEID, TAG1);
        assertImageTagged(IMAGEID, NAMEANDTAG);
        assertImageTagged(IMAGEID, TAG2);

        assertImageEnqueuedForPush(0, NAMEANDTAG, REGISTRY2);
        assertImageEnqueuedForPush(1, TAG1, REGISTRY2);
        assertImageEnqueuedForPush(2, TAG2, REGISTRY2);
    }

    @Test
    public void testThatImageTaggedAndIsPushedWithOverriddenRegistry() throws Exception {
        Mockito.when(mockImage.isPush()).thenReturn(true);
        Mockito.when(mockImage.getRegistry()).thenReturn(REGISTRY1);

        mojo.registerBuiltImage(IMAGEID, mockImage);

        Mockito.when(mockTag.isPush()).thenReturn(true);
        Mockito.when(mockTag.getRegistry()).thenReturn(REGISTRY2);

        executeMojo(FAKE_PROVIDER_KEY);

        assertTrue(mojo.getPluginErrors().isEmpty());

        assertNImagesTagged(3);
        assertImageTagged(IMAGEID, TAG1);
        assertImageTagged(IMAGEID, NAMEANDTAG);
        assertImageTagged(IMAGEID, TAG2);

        assertImageEnqueuedForPush(0, NAMEANDTAG, REGISTRY1);
        assertImageEnqueuedForPush(1, TAG1, REGISTRY2);
        assertImageEnqueuedForPush(2, NAMEANDTAG, REGISTRY2);
        assertImageEnqueuedForPush(3, TAG2, REGISTRY2);
    }

    @Test
    public void testThatImageTaggedAndIsPushedAndInheritedRegistryIsNull() throws Exception {
        mojo.registerBuiltImage(IMAGEID, mockImage);

        Mockito.when(mockTag.isPush()).thenReturn(true);

        executeMojo(FAKE_PROVIDER_KEY);

        assertTrue(mojo.getPluginErrors().isEmpty());

        assertNImagesTagged(3);
        assertImageTagged(IMAGEID, TAG1);
        assertImageTagged(IMAGEID, NAMEANDTAG);
        assertImageTagged(IMAGEID, TAG2);

        assertImageEnqueuedForPush(0, TAG1, null);
        assertImageEnqueuedForPush(1, NAMEANDTAG, null);
        assertImageEnqueuedForPush(2, TAG2, null);
    }

    @Test(expected = MojoFailureException.class)
    public void testThatTheMojoFailsIfTagRaisesError() throws Exception {
        executeMojo(DockerExceptionThrowingDockerProvider.PROVIDER_KEY);
    }

    private void executeMojo(String provider) throws MojoExecutionException, MojoFailureException {
        mojo.setProviderName(provider);
        mojo.execute();
    }

    private void assertNImagesTagged(final int count) {
        Mockito.verify(FakeDockerProvider.instance, times(count)).tagImage(any(String.class), any(String.class));
    }

    private void assertImageTagged(final String imageId, final String tag) {
        Mockito.verify(FakeDockerProvider.instance, times(1)).tagImage(imageId, tag);
    }

    private void assertImageNotEnqueuedForPush() {
        assertTrue(mojo.getImagesToPush().isEmpty());
    }

    private void assertImageEnqueuedForPush(int index, String expectedImageId, String expectedRegistry) {
        PushableImage actual = mojo.getImagesToPush().get(index);
        assertEquals(expectedImageId, actual.getImageId());
        assertEquals(expectedRegistry, actual.getRegistry().orNull());
    }

    public static class FakeDockerProvider extends AbstractFakeDockerProvider {

        private static FakeDockerProvider instance;

        @Override
        protected AbstractFakeDockerProvider getInstance() {
            return instance;
        }
    }

}
