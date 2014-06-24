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

import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * See <a href="http://docs.docker.io/reference/api/docker_remote_api_v1.10/#21-containers">
 * http://docs.docker.io/reference/api/docker_remote_api_v1.10/#create-a-container</a>
 */
@SuppressWarnings("unused")
public class ContainerCreateResponse {

    @JsonProperty("Id")
    private String id;
    @JsonProperty("Warnings")
    private List<String> warnings;

    public void setId(final String id) {
        this.id = id;
    }

    public void setWarnings(final List<String> warnings) {
        this.warnings = warnings;
    }

    public String getId() {
        return id;
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
}
