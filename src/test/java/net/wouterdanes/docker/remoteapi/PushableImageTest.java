package net.wouterdanes.docker.remoteapi;


import com.google.common.base.Optional;

import org.junit.Before;
import org.junit.Test;

import net.wouterdanes.docker.provider.model.PushableImage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PushableImageTest {

    private static final String IMAGE1 = "red";
    private static final String IMAGE2 = "blue";

    private static final String TAG1 = "oldest";
    private static final String TAG2 = "localhost:5000/fred/bluey:middlest";

    private PushableImage targetWithTag;
    private PushableImage targetWithoutTag;

    @Before
    public void setUp() throws Exception {
        targetWithTag = make(IMAGE1, TAG1);
        targetWithoutTag = make(IMAGE1, null);
    }

    @Test
    public void testConstruction() {
        assertExpected(Optional.fromNullable(TAG1), targetWithTag);
        assertExpected(Optional.<String> absent(), targetWithoutTag);
    }

    @Test
    public void testHashCode() {
        assertEquals(make(IMAGE1, TAG1).hashCode(), targetWithTag.hashCode());
        assertEquals(make(IMAGE1, null).hashCode(), targetWithoutTag.hashCode());
    }

    @Test
    public void testEquals() {
        // trivial cases
        assertTrue(targetWithTag.equals(targetWithTag));
        assertNotNull(targetWithTag);
        assertFalse(targetWithTag.equals(new Object()));

        // when equivalent
        assertTrue(targetWithTag.equals(make(IMAGE1, TAG1)));
        assertTrue(targetWithoutTag.equals(make(IMAGE1, null)));

        // when not equivalent
        // - Image not same
        assertFalse(targetWithTag.equals(make(IMAGE2, TAG1)));
        // - Reg not same
        assertFalse(targetWithTag.equals(make(IMAGE1, TAG2)));
        assertFalse(targetWithTag.equals(targetWithoutTag));
        assertFalse(targetWithoutTag.equals(targetWithTag));

    }

    private PushableImage make(String imageId, String registry) {
        return new PushableImage(imageId, Optional.fromNullable(registry));
    }

    private void assertExpected(Optional<String> expectedRegistry,
                                PushableImage actual) {
        assertEquals(PushableImageTest.IMAGE1, actual.getImageId());
        assertEquals(expectedRegistry, actual.getNameAndTag());
    }

}
