package net.wouterdanes.docker.remoteapi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import net.wouterdanes.docker.remoteapi.exception.DockerException;
import net.wouterdanes.docker.remoteapi.model.DockerVersionInfo;

/**
 * The class act as an interface to the "root" Remote Docker API with some "misc" service end points.
 */
public class MiscService extends BaseService {

    private static final Pattern BUILD_IMAGE_ID_EXTRACTION_PATTERN =
            Pattern.compile(".*Successfully built ([0-9a-f]+).*", Pattern.DOTALL);

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

    public String buildImage(byte[] tarArchive) {
        String jsonStream = getServiceEndPoint()
                .path("/build")
                .queryParam("q", true)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(tarArchive, "application/tar"), String.class);

        Matcher matcher = BUILD_IMAGE_ID_EXTRACTION_PATTERN.matcher(jsonStream);
        if (!matcher.matches()) {
            throw new DockerException("Can't obtain ID from build output stream.", jsonStream);
        }

        return matcher.group(1);
    }

}