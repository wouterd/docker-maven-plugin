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

/**
 * This class holds information about an image that was built so that it can be references in the start goal and
 * removed in the stop goal.
 */
public class BuiltImageInfo {

    private final String startId;
    private final String imageId;
    private final String nameAndTag;
    private final boolean keep;
    private final boolean push;

    public BuiltImageInfo(final String imageId, ImageBuildConfiguration imageConfig) {
        this.imageId = imageId;
        this.startId = imageConfig.getId();
        this.nameAndTag = imageConfig.getNameAndTag();
        this.keep = imageConfig.isKeep();
        this.push = imageConfig.isPush();
    }

    public String getStartId() {
        return startId;
    }

    public String getImageId() {
        return imageId;
    }

    public String getNameAndTag() {
        return nameAndTag;
    }

    public boolean shouldKeep() {
        return keep || push;
    }

    public boolean shouldPush() {
        return push;
    }

}
