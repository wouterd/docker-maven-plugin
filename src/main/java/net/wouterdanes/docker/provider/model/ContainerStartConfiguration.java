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

import org.apache.maven.plugins.annotations.Parameter;

/**
 * This class is responsible for holding the start configuration of a docker container<br/> See <a
 * href="http://docs.docker.io/reference/api/docker_remote_api_v1.10/#21-containers">
 * http://docs.docker.io/reference/api/docker_remote_api_v1.10/#start-a-container</a>
 */
@SuppressWarnings("unused")
public class ContainerStartConfiguration {

    @Parameter(required = true)
    private String image;
    @Parameter(required = true)
    private String id;

    /**
     * Set the image name or id to use and returns the object so you can chain from/with statements.
     *
     * @param image the image name or id
     * @return this object
     */
    public ContainerStartConfiguration fromImage(String image) {
        this.image = image;
        return this;
    }

    public ContainerStartConfiguration withId(String id) {
        this.id = id;
        return this;
    }

    public String getImage() {
        return image;
    }

    public String getId() {
        return id != null ? id : image;
    }
}
