package net.wouterdanes.docker.remoteapi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Optional;

import net.wouterdanes.docker.remoteapi.exception.DockerException;
import net.wouterdanes.docker.remoteapi.model.DockerVersionInfo;

/**
 * The class act as an interface to the "root" Remote Docker API with some "misc" service end points.
 */
public class MiscService extends BaseService {

    private static final Pattern BUILD_IMAGE_ID_EXTRACTION_PATTERN =
            Pattern.compile(".*Successfully built ([0-9a-f]+).*", Pattern.DOTALL);

    private static final Pattern BUILD_IMAGE_INTERMEDIATE_IDS_EXTRACTION_PATTERN =
            Pattern.compile(".*?Running in ([0-9a-f]+).*?", Pattern.DOTALL);

    private final ContainersService containersService;

    public MiscService(final String dockerApiRoot) {
        super(dockerApiRoot, "/");
        containersService = new ContainersService(dockerApiRoot);
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
     * Builds an image based on the passed tar archive. Optionally names & tags the image
     * @param tarArchive the tar archive to use as a source for the image
     * @param name the name and optional tag of the image.
     * @return the ID of the created image
     */
    public String buildImage(byte[] tarArchive, Optional<String> name) {
        String jsonStream = getServiceEndPoint()
                .path("/build")
                .queryParam("q", true)
                .queryParam("t", name.orNull())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(tarArchive, "application/tar"), String.class);

        Matcher intermediates = BUILD_IMAGE_INTERMEDIATE_IDS_EXTRACTION_PATTERN.matcher(jsonStream);
        while (intermediates.find()) {
            String intermediateId = intermediates.group(1);
            containersService.deleteContainer(intermediateId);
        }

        Matcher matcher = BUILD_IMAGE_ID_EXTRACTION_PATTERN.matcher(jsonStream);
        if (!matcher.matches()) {
            throw new DockerException("Can't obtain ID from build output stream.", jsonStream);
        }
        return matcher.group(1);
    }

}