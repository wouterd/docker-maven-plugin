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

package net.wouterdanes.docker.provider.model;

import java.util.Objects;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Holds information about an image (or tag thereof) to be pushed at a later stage.
 */
public class PushableImage {

    private final String imageId;
    private final Optional<String> registry;

    public PushableImage(final String imageId, final Optional<String> registry) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(imageId), "Image id was null or empty");
        Preconditions.checkNotNull(registry, "Registry was null");

        this.imageId = imageId;
        this.registry = registry;
    }

    public String getImageId() {
        return imageId;
    }

    public Optional<String> getRegistry() {
        return registry;
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageId, registry);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PushableImage)) {
            return false;
        }

        PushableImage other = (PushableImage) obj;
        return imageId.equals(other.getImageId()) && registry.equals(other.getRegistry());
    }

    @Override
    public String toString() {
        return "PushableImage["
                + "imageId=" + imageId
                + ", registry=" + registry.or("<Unspecified>")
                + "]";
    }

}
