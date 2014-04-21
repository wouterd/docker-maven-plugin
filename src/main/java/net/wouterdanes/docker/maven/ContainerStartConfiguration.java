package net.wouterdanes.docker.maven;

/**
 * This class is responsible for holding the start configuration of a docker container<br/> See <a
 * href="http://docs.docker.io/reference/api/docker_remote_api_v1.10/#21-containers">
 * http://docs.docker.io/reference/api/docker_remote_api_v1.10/#start-a-container</a>
 */
@SuppressWarnings("unused")
public class ContainerStartConfiguration {

    private String image;
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
