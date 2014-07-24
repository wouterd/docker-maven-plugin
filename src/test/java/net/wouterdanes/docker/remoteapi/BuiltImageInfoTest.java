package net.wouterdanes.docker.remoteapi;

import static org.junit.Assert.assertEquals;

import net.wouterdanes.docker.provider.model.BuiltImageInfo;
import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;

import org.junit.Test;

import com.google.common.base.Optional;

public class BuiltImageInfoTest {

    private static final boolean KEEP = true;
    private static final boolean DONT_KEEP = false;

    private static final boolean PUSH = true;
    private static final boolean DONT_PUSH = false;

    private static final String IMAGEID = "x56543d5";
    private static final String STARTID = "start";
    private static final String REGISTRYID = "registry";

    private static final Optional<String> NON_NULL_REGISTRY = Optional.fromNullable(REGISTRYID);
    private static final Optional<String> NULL_REGISTRY = Optional.absent();

    @Test
    public void testConstruction() {
        assertExpected(NON_NULL_REGISTRY, DONT_KEEP, makeTarget(REGISTRYID, DONT_KEEP, DONT_PUSH));
        assertExpected(NON_NULL_REGISTRY, KEEP, makeTarget(REGISTRYID, DONT_KEEP, PUSH));
        assertExpected(NON_NULL_REGISTRY, KEEP, makeTarget(REGISTRYID, KEEP, PUSH));
        assertExpected(NON_NULL_REGISTRY, KEEP, makeTarget(REGISTRYID, KEEP, DONT_PUSH));

        assertExpected(NULL_REGISTRY, DONT_KEEP, makeTarget(null, DONT_KEEP, DONT_KEEP));
    }

    private BuiltImageInfo makeTarget(String registry, boolean keep, boolean push) {
        return new BuiltImageInfo(IMAGEID, makeConfiguration(registry, keep, push));
    }

    private ImageBuildConfiguration makeConfiguration(String registry, boolean keep, boolean push) {
        ImageBuildConfiguration config = new ImageBuildConfiguration();
        config.setId(STARTID);
        config.setKeep(keep);
        config.setPush(push);
        config.setRegistry(registry);
        return config;
    }

    private void assertExpected(Optional<String> expectedRegistry,
            boolean expectedKeep,
            BuiltImageInfo actual) {
        assertEquals(IMAGEID, actual.getImageId());
        assertEquals(STARTID, actual.getStartId());
        assertEquals(expectedRegistry, actual.getRegistry());
        assertEquals(expectedKeep, actual.shouldKeepAfterStopping());
    }

}
