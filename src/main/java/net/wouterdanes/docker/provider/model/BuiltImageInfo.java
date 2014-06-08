package net.wouterdanes.docker.provider.model;

/**
 * This class holds information about an image that was built so that it can be references in the start goal and
 * removed in the stop goal.
 */
public class BuiltImageInfo {

    private final String startId;
    private final String imageId;
    private final boolean keep;

    public BuiltImageInfo(final String startId, final String imageId, final boolean keep) {
        this.startId = startId;
        this.imageId = imageId;
        this.keep = keep;
    }

    public String getStartId() {
        return startId;
    }

    public String getImageId() {
        return imageId;
    }

    public boolean shouldKeep() {
        return keep;
    }
}
