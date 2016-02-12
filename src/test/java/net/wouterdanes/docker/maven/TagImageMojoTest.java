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
import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;
import net.wouterdanes.docker.provider.model.ImageTagConfiguration;
import net.wouterdanes.docker.provider.model.PushableImage;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

public class TagImageMojoTest {

    private static final String FAKE_PROVIDER_KEY = UUID.randomUUID().toString();

    private static final String IMAGEID = "imageId-apple";
    private static final String STARTID = "startId-turnip";
    private static final String NAMEANDTAG = "namdAndTag-durian";
    private static final String TAG1 = "tag1-chocko";
    private static final String TAG2 = "tag2-rambutan";
    private static final String REGISTRY1 = "reg1-orange";
    private static final String REGISTRY2 = "reg2-quince";

    private static final String REG1_NAMEANDTAG = REGISTRY1 + "/" + NAMEANDTAG;
    private static final String REG1_TAG1 = REGISTRY1 + "/" + TAG1;
    private static final String REG1_TAG2 = REGISTRY1 + "/" + TAG2;
    private static final String REG2_NAMEANDTAG = REGISTRY2 + "/" + NAMEANDTAG;
    private static final String REG2_TAG1 = REGISTRY2 + "/" + TAG1;
    private static final String REG2_TAG2 = REGISTRY2 + "/" + TAG2;

    private TagImageMojo mojo = new TagImageMojo();

    private ImageBuildConfiguration mockImage;

    private ImageTagConfiguration mockTag;

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

        List<ImageTagConfiguration> tags = Collections.singletonList(mockTag);

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

        assertImageEnqueuedForPush(0, STARTID, TAG1);
        assertImageEnqueuedForPush(1, STARTID, NAMEANDTAG);
        assertImageEnqueuedForPush(2, STARTID, TAG2);
    }

    @Test
    public void testThatImageTaggedAndIsPushedWithARegistry() throws Exception {
        Mockito.when(mockTag.isPush()).thenReturn(true);
        Mockito.when(mockTag.getRegistry()).thenReturn(REGISTRY1);

        executeMojo(FAKE_PROVIDER_KEY);

        assertTrue(mojo.getPluginErrors().isEmpty());

        assertNImagesTagged(6);
        assertImageTagged(STARTID, TAG1);
        assertImageTagged(STARTID, NAMEANDTAG);
        assertImageTagged(STARTID, TAG2);
        assertImageTagged(STARTID, REG1_NAMEANDTAG);
        assertImageTagged(STARTID, REG1_TAG1);
        assertImageTagged(STARTID, REG1_TAG2);

        assertImageEnqueuedForPush(0, STARTID, REG1_TAG1);
        assertImageEnqueuedForPush(1, STARTID, REG1_NAMEANDTAG);
        assertImageEnqueuedForPush(2, STARTID, REG1_TAG2);
    }

    @Test
    public void testThatImageTaggedAndIsPushedWithInheritedRegistry() throws Exception {
        Mockito.when(mockImage.isPush()).thenReturn(true);
        Mockito.when(mockImage.getRegistry()).thenReturn(REGISTRY2);

        mojo.setProviderName(FAKE_PROVIDER_KEY);
        mojo.registerBuiltImage(IMAGEID, mockImage);

        Mockito.when(mockTag.isPush()).thenReturn(true);

        mojo.execute();

        assertTrue(mojo.getPluginErrors().isEmpty());

        assertNImagesTagged(7); // called 7 times but only 6 unique calls made
        assertImageTagged(IMAGEID, NAMEANDTAG);
        assertImageTagged(IMAGEID, TAG1);
        assertImageTagged(IMAGEID, TAG2);
        assertImageTagged(IMAGEID, REG2_NAMEANDTAG, 2);
        assertImageTagged(IMAGEID, REG2_TAG1);
        assertImageTagged(IMAGEID, REG2_TAG2);

        assertImageEnqueuedForPush(0, IMAGEID, REG2_NAMEANDTAG);
        assertImageEnqueuedForPush(1, IMAGEID, REG2_TAG1);
        assertImageEnqueuedForPush(2, IMAGEID, REG2_TAG2);
    }

    @Test
    public void testThatImageTaggedAndIsPushedWithOverriddenRegistry() throws Exception {
        Mockito.when(mockImage.isPush()).thenReturn(true);
        Mockito.when(mockImage.getRegistry()).thenReturn(REGISTRY1);

        mojo.setProviderName(FAKE_PROVIDER_KEY);
        mojo.registerBuiltImage(IMAGEID, mockImage);

        Mockito.when(mockTag.isPush()).thenReturn(true);
        Mockito.when(mockTag.getRegistry()).thenReturn(REGISTRY2);

        mojo.execute();

        assertTrue(mojo.getPluginErrors().isEmpty());

        assertNImagesTagged(7);
        assertImageTagged(IMAGEID, REG1_NAMEANDTAG);
        assertImageTagged(IMAGEID, TAG1);
        assertImageTagged(IMAGEID, NAMEANDTAG);
        assertImageTagged(IMAGEID, TAG2);
        assertImageTagged(IMAGEID, REG2_NAMEANDTAG);
        assertImageTagged(IMAGEID, REG2_TAG1);
        assertImageTagged(IMAGEID, REG2_TAG2);

        assertImageEnqueuedForPush(0, IMAGEID, REG1_NAMEANDTAG);
        assertImageEnqueuedForPush(1, IMAGEID, REG2_TAG1);
        assertImageEnqueuedForPush(2, IMAGEID, REG2_NAMEANDTAG);
        assertImageEnqueuedForPush(3, IMAGEID, REG2_TAG2);
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

        assertImageEnqueuedForPush(0, IMAGEID, TAG1);
        assertImageEnqueuedForPush(1, IMAGEID, NAMEANDTAG);
        assertImageEnqueuedForPush(2, IMAGEID, TAG2);
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
        assertImageTagged(imageId, tag, 1);
    }

    private void assertImageTagged(final String imageId, final String tag, final int numTimes) {
        Mockito.verify(FakeDockerProvider.instance, times(numTimes)).tagImage(imageId, tag);
    }

    private void assertImageNotEnqueuedForPush() {
        assertTrue(mojo.getImagesToPush().isEmpty());
    }

    private void assertImageEnqueuedForPush(int index, String expectedImageId, String expectedNameAndTag) {
        PushableImage actual = mojo.getImagesToPush().get(index);
        assertEquals(expectedImageId, actual.getImageId());
        assertEquals(expectedNameAndTag, actual.getNameAndTag().orElse(null));
    }

    public static class FakeDockerProvider extends AbstractFakeDockerProvider {

        private static FakeDockerProvider instance;

        @Override
        protected AbstractFakeDockerProvider getInstance() {
            return instance;
        }
    }

}
