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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;
import net.wouterdanes.docker.remoteapi.exception.DockerException;
import net.wouterdanes.docker.remoteapi.model.ContainerCommitResponse;
import net.wouterdanes.docker.remoteapi.model.DockerVersionInfo;
import org.glassfish.jersey.uri.UriComponent;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class act as an interface to the "root" Remote Docker API with some "misc" service end points.
 */
public class MiscService extends BaseService {

    private static final Pattern BUILD_IMAGE_ID_EXTRACTION_PATTERN =
            Pattern.compile(".*sha256:([0-9a-f]+).*", Pattern.DOTALL);

    public MiscService(final String dockerApiRoot) {
        super(dockerApiRoot, "/");
    }

    /**
     * Returns the Docker version information
     *
     * @return a {@link DockerVersionInfo} instance describing this docker installation.
     */
    public DockerVersionInfo getVersionInfo() {
        String json = getServiceEndPoint()
                .path("/version")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        return toObject(json, DockerVersionInfo.class);
    }

    /**
     * Create a new image from a container's changes
     *
     * @param container source container
     * @param repo repository
     * @param tag tag
     * @param comment commit message
     * @param author author (e.g., "John Hannibal Smith &lt;hannibal@a-team.com&gt;")
     *
     * @return the ID of the created image
     */
    public String commitContainer(
            String container,
            Optional<String> repo,
            Optional<String> tag,
            Optional<String> comment,
            Optional<String> author) {

        WebTarget request = getServiceEndPoint()
                .path("/commit")
                .queryParam("container", container)
                .queryParam("repo", repo.orElse(null))
                .queryParam("tag", tag.orElse(null))
                .queryParam("comment", comment.orElse(null))
                .queryParam("author", author.orElse(null));

        String json = request
                .request(MediaType.APPLICATION_JSON_TYPE)
                .method(HttpMethod.POST, String.class);

        ContainerCommitResponse result = toObject(json, ContainerCommitResponse.class);

        return result.getId();
    }

    /**
     * Builds an image based on the passed tar archive. Optionally names &amp; tags the image
     *
     * @param tarArchive        the tar archive to use as a source for the image
     * @param name              the name and optional tag of the image.
     * @param buildArguments    a list of optional build arguments made available to the Dockerfile.
     * @return the ID of the created image
     */
    public String buildImage(byte[] tarArchive, Optional<String> name, Optional<String> buildArguments) {
        Response response = getServiceEndPoint()
                .path("/build")
                .queryParam("q", true)
                .queryParam("t", name.orElse(null))
                .queryParam("buildargs", UriComponent.encode(buildArguments.orElse(null), UriComponent.Type.QUERY_PARAM_SPACE_ENCODED))
                .queryParam("forcerm")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(REGISTRY_AUTH_HEADER, getRegistryAuthHeaderValue())
                .post(Entity.entity(tarArchive, "application/tar"));

        InputStream inputStream = (InputStream) response.getEntity();

        String imageId = parseSteamForImageId(inputStream);

        if (imageId == null) {
            throw new DockerException("Can't obtain ID from build output stream.");
        }

        return imageId;
    }

    private static String parseSteamForImageId(final InputStream inputStream) {
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(isr);

        JsonStreamParser parser = new JsonStreamParser(reader);

        String imageId = null;

        while (parser.hasNext()) {
            JsonElement element = parser.next();
            JsonObject object = element.getAsJsonObject();
            if (object.has("stream")) {
                String text = object.get("stream").getAsString();
                System.out.print(text);
                Matcher matcher = BUILD_IMAGE_ID_EXTRACTION_PATTERN.matcher(text);
                if (matcher.matches()) {
                    imageId = matcher.group(1);
                }
            }
            if (object.has("status")) {
                System.out.println(object.get("status").getAsString());
            }
            if (object.has("error")) {
                System.err.println("ERROR: " + object.get("error").getAsString());
            }
        }
        return imageId;
    }

}
