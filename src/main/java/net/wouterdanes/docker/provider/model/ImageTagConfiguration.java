/*
    Copyright 2014 Lachlan Coote

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

import java.util.List;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * This class is responsible for holding the configuration to assign one or more tags to a single
 * docker image within the {@link net.wouterdanes.docker.maven.TagImageMojo}
 */
public class ImageTagConfiguration {

    @Parameter(required = true)
    private String id;

    @Parameter(required = true)
    private List<String> tags;

    @Parameter(defaultValue = "false")
    private boolean push;

    @Parameter
    private String registry;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isPush() {
        return push;
    }

    public void setPush(boolean push) {
        this.push = push;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

}
