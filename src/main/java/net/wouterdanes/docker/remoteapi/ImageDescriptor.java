package net.wouterdanes.docker.remoteapi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates an image descriptor based on a passed image id or qualifier in the form ([registry]/[repo]/[image]:[tag])
 */
public class ImageDescriptor {

    private static final Pattern IMAGE_QUALIFIER = Pattern.compile("^(([\\w\\.]+)/)??(([\\w]+)/)?([\\w]+)(:([\\w]+))?$");

    private final String id;
    private String registry;
    private String repository;
    private String image;
    private String tag;

    public ImageDescriptor(String id) {
        this.id = id;

        this.registry = null;
        this.repository = null;
        this.image = null;
        this.tag = null;

        Matcher matcher = IMAGE_QUALIFIER.matcher(id);
        if (matcher.matches()) {
            this.registry = matcher.group(2);
            this.repository = matcher.group(4);
            this.image = matcher.group(5);
            this.tag = matcher.group(7);
        }
    }

    public String getId() {
        return id;
    }

    public String getRegistry() {
        return registry;
    }

    public String getRepository() {
        return repository;
    }

    public String getImage() {
        return image;
    }

    public String getTag() {
        return tag;
    }
}
