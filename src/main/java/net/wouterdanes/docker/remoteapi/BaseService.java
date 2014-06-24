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

package net.wouterdanes.docker.remoteapi;

import java.io.IOException;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * This class is responsible for holding the shared functionality of all Docker remoteapi services.
 */
public abstract class BaseService {

    public static final String TARGET_DOCKER_API_VERSION = "v1.10";
    private final ObjectMapper objectMapper;
    private final WebTarget serviceEndPoint;

    public BaseService(String dockerApiRoot, String endPointPath) {
        objectMapper = new ObjectMapper();
        // Only send properties that are actually set, default values are often wrong
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        // If the API changes, we might get new properties that we do not know
        DeserializationConfig deserializationConfig = objectMapper.getDeserializationConfig()
                .without(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setDeserializationConfig(deserializationConfig);
        serviceEndPoint = ClientBuilder.newClient()
                .target(dockerApiRoot)
                .path(TARGET_DOCKER_API_VERSION)
                .path(endPointPath);
    }

    protected WebTarget getServiceEndPoint() {
        return serviceEndPoint;
    }

    protected String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to Jsonify", e);
        }
    }

    protected <T> T toObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot convert Json", e);
        }
    }
}
