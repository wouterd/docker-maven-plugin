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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Creates an image descriptor based on a passed image id or qualifier in the form ([registry]/[repo]/[image]:[tag])
 */
public class ImageDescriptor {

    private static final Pattern IMAGE_QUALIFIER = Pattern.compile("^"
            + "((?<registry>[\\w\\.\\-]+(:\\d+)?)/)??" // registry
            + "((?<repository>[\\w]+)/)?(?<image>[\\w]+)" // repository/image
            + "(:(?<tag>[\\w\\.\\-]+))?" // tag
            + "$");

    private final String id;
    private final Optional<String> registry;
    private final Optional<String> repository;
    private final String image;
    private final Optional<String> tag;

    public ImageDescriptor(String id) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "Id was null or empty");

        this.id = id;

        Matcher matcher = IMAGE_QUALIFIER.matcher(id);
        if (matcher.matches()) {
            // because Optional.orNull(x) cannot handle null values of x
            this.registry = Optional.fromNullable(matcher.group("registry"));
            this.repository = Optional.fromNullable(matcher.group("repository"));
            this.image = matcher.group("image");
            this.tag = Optional.fromNullable(matcher.group("tag"));
        } else {
            this.registry = Optional.absent();
            this.repository = Optional.absent();
            this.image = id;
            this.tag = Optional.absent();
        }
    }

    public String getId() {
        return id;
    }

    public Optional<String> getRegistry() {
        return registry;
    }

    public Optional<String> getRepository() {
        return repository;
    }

    public String getImage() {
        return image;
    }

    public Optional<String> getTag() {
        return tag;
    }

    public String getRegistryRepositoryAndImage() {
        StringBuilder buf = new StringBuilder();
        appendRegistry(buf);
        appendRepository(buf);
        appendImage(buf);
        return buf.toString();
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

    private void appendRegistry(StringBuilder buf) {
        if (registry.isPresent()) {
            buf.append(registry.get());
            buf.append('/');
        }
    }

    private void appendRepository(StringBuilder buf) {
        if (repository.isPresent()) {
            buf.append(repository.get());
            buf.append('/');
        }
    }

    private void appendImage(StringBuilder buf) {
        buf.append(image);
    }

    private void appendTag(StringBuilder buf) {
        if (tag.isPresent()) {
            buf.append(':');
            buf.append(tag.get());
        }
    }

    @Override
    public String toString() {
        return "ImageDescriptor[id=" + id
                + ", registry=" + registry.orNull()
                + ", repository=" + repository.orNull()
                + ", image=" + image
                + ", tag=" + tag.orNull()
                + "]";
    }



}
