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

package net.wouterdanes.docker.remoteapi.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

/**
 * Creates an image descriptor based on a passed image id or qualifier in the form ([registry]/[repo]/[image]:[tag])
 */
public class ImageDescriptor {

    private static final Pattern IMAGE_QUALIFIER = Pattern.compile("^"
            + "((?<registry>[\\w\\.\\-]+(:\\d+)?)/)??" // registry
            + "((?<repository>[\\w]+)/)?(?<image>[\\w]+)" // repository/image
            + "(:(?<tag>[\\w]+))?" // tag
            + "$");

    private final String id;
    private final String registry;
    private final String repository;
    private final String image;
    private final String tag;

    public ImageDescriptor(String id) {
        this.id = id;

        Matcher matcher = IMAGE_QUALIFIER.matcher(id);
        if (matcher.matches()) {
            this.registry = matcher.group("registry");
            this.repository = matcher.group("repository");
            this.image = matcher.group("image");
            this.tag = matcher.group("tag");
        } else {
            this.registry = null;
            this.repository = null;
            this.image = id;
            this.tag = null;
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

    public String getRepositoryAndImage() {
        StringBuilder buf = new StringBuilder();
        appendRepository(buf);
        appendImage(buf);
        return buf.toString();
    }

    public String getRepositoryImageAndTag() {
        StringBuilder buf = new StringBuilder();
        appendRepository(buf);
        appendImage(buf);
        appendTag(buf);
        return buf.toString();
    }

    private void appendRepository(StringBuilder buf) {
        if (!Strings.isNullOrEmpty(repository)) {
            buf.append(repository);
            buf.append('/');
        }
    }

    private void appendImage(StringBuilder buf) {
        buf.append(image);
    }

    private void appendTag(StringBuilder buf) {
        if (!Strings.isNullOrEmpty(tag)) {
            buf.append(':');
            buf.append(tag);
        }
    }

    public boolean hasRegistry() {
        return !Strings.isNullOrEmpty(registry);
    }

    public boolean hasTag() {
        return !Strings.isNullOrEmpty(tag);
    }

    @Override
    public String toString() {
        return "ImageDescriptor[id=" + id
                + ", registry=" + registry
                + ", repository=" + repository
                + ", image=" + image
                + ", tag=" + tag
                + "]";
    }



}
