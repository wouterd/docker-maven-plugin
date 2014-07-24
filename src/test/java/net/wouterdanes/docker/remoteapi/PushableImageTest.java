package net.wouterdanes.docker.remoteapi;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.wouterdanes.docker.provider.model.PushableImage;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;

public class PushableImageTest {

    private static final String IMAGE1 = "red";
    private static final String IMAGE2 = "blue";

    private static final String REG1 = "tutum.co";
    private static final String REG2 = "localhost:5000";

    private PushableImage targetWithReg;
    private PushableImage targetWithoutReg;

    @Before
    public void setUp() throws Exception {
        targetWithReg = make(IMAGE1, REG1);
        targetWithoutReg = make(IMAGE1, null);
    }

    @Test
    public void testConstruction() {
        assertExpected(IMAGE1, Optional.fromNullable(REG1), targetWithReg);
        assertExpected(IMAGE1, Optional.<String> absent(), targetWithoutReg);
    }

    @Test
    public void testHashCode() {
        assertEquals(make(IMAGE1, REG1).hashCode(), targetWithReg.hashCode());
        assertEquals(make(IMAGE1, null).hashCode(), targetWithoutReg.hashCode());
    }

    @Test
    public void testEquals() {
        // trivial cases
        assertTrue(targetWithReg.equals(targetWithReg));
        assertFalse(targetWithReg.equals(null));
        assertFalse(targetWithReg.equals(new Object()));

        // when equivalent
        assertTrue(targetWithReg.equals(make(IMAGE1, REG1)));
        assertTrue(targetWithoutReg.equals(make(IMAGE1, null)));

        // when not equivalent
        // - Image not same
        assertFalse(targetWithReg.equals(make(IMAGE2, REG1)));
        // - Reg not same
        assertFalse(targetWithReg.equals(make(IMAGE1, REG2)));
        assertFalse(targetWithReg.equals(targetWithoutReg));
        assertFalse(targetWithoutReg.equals(targetWithReg));

    }

    private PushableImage make(String imageId, String registry) {
        return new PushableImage(imageId, Optional.fromNullable(registry));
    }

    private void assertExpected(String expectedImageId, Optional<String> expectedRegistry,
            PushableImage actual) {
        assertEquals(expectedImageId, actual.getImageId());
        assertEquals(expectedRegistry, actual.getRegistry());
    }

}
