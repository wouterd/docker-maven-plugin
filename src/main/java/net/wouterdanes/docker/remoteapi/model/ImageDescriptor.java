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
