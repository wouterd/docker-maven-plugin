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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.Security;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import com.google.common.io.BaseEncoding;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import net.wouterdanes.docker.remoteapi.exception.DockerException;
import net.wouterdanes.docker.remoteapi.exception.ImageNotFoundException;
import net.wouterdanes.docker.remoteapi.model.Credentials;
import net.wouterdanes.docker.remoteapi.util.HttpsHelper;

/**
 * This class is responsible for holding the shared functionality of all Docker remoteapi services.
 */
public abstract class BaseService {

    public static final String REGISTRY_AUTH_HEADER = "X-Registry-Auth";

    // required for "push" even if no credentials required
    private static final String REGISTRY_AUTH_NULL_VALUE = "null";

    private static final String TARGET_DOCKER_API_VERSION = "v1.12";
    private static final String ENV_DOCKER_TLS_VERIFY = "DOCKER_TLS_VERIFY";

    private final ObjectMapper objectMapper;
    private final WebTarget serviceEndPoint;
    private Credentials credentials = null;

    public BaseService(String dockerApiRoot, String endPointPath) {
        objectMapper = new ObjectMapper();
        // Only send properties that are actually set, default values are often wrong
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        // If the API changes, we might get new properties that we do not know
        DeserializationConfig deserializationConfig = objectMapper.getDeserializationConfig()
                .without(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setDeserializationConfig(deserializationConfig);
        serviceEndPoint = createDockerTarget(dockerApiRoot)
                .path(TARGET_DOCKER_API_VERSION)
                .path(endPointPath);
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    protected WebTarget getServiceEndPoint() {
        return serviceEndPoint;
    }

    protected String getRegistryAuthHeaderValue() {
        if (credentials == null) {
            return REGISTRY_AUTH_NULL_VALUE;
        }
        return BaseEncoding.base64().encode(toJson(credentials).getBytes(Charset.forName("UTF-8")));
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

    protected static void checkImageTargetingResponse(final String id, final Response.StatusType statusInfo) {
        if (statusInfo.getFamily() == Family.SUCCESSFUL) {
            // no error
            return;
        }

        switch (statusInfo.getStatusCode()) {
            case 404:
                throw new ImageNotFoundException(id);
            default:
                throw new DockerException(statusInfo.getReasonPhrase());
        }
    }

    protected static DockerException makeImageTargetingException(final String id, final WebApplicationException cause) {
        Response.StatusType statusInfo = cause.getResponse().getStatusInfo();
        switch (statusInfo.getStatusCode()) {
            case 404:
                return new ImageNotFoundException(id, cause);
            default:
                return new DockerException(statusInfo.getReasonPhrase(), cause);
        }
    }

    private static WebTarget createDockerTarget(final String dockerApiRoot) {
        String encrypted = System.getenv(ENV_DOCKER_TLS_VERIFY);
        if (!"1".equals(encrypted)) {
            return ClientBuilder.newClient()
                    .target("http://" + dockerApiRoot);
        }

        Security.addProvider(new BouncyCastleProvider());

        String certPath = System.getenv("DOCKER_CERT_PATH");
        if (certPath == null) {
            certPath = System.getProperty("USER_HOME") + File.separator + ".docker";
        }

        ensureThatCertificatesExist(certPath);

        KeyStore keyStore;
        KeyStore trustStore;
        try {
            keyStore = HttpsHelper.createKeyStore(certPath);
            trustStore = HttpsHelper.createTrustStore(certPath);
        } catch (Exception e) {
            throw new DockerException("Can't load docker certificates", e);
        }

        return ClientBuilder.newBuilder()
                .keyStore(keyStore, HttpsHelper.KEYSTORE_PWD)
                .trustStore(trustStore)
                .build()
                .target("https://" + dockerApiRoot);
    }

    private static void ensureThatCertificatesExist(final String certPath) {
        String[] files = {"ca.pem", "cert.pem", "key.pem"};
        for (String file : files) {
            Path path = Paths.get(certPath, file);
            boolean exists = Files.exists(path);
            if (!exists) {
                throw new DockerException(String.format("%s not found in cert path (%s), make sure that ca.pem, " +
                        "cert.pem and key.pem are available there.", file, certPath));
            }
        }
    }
}
