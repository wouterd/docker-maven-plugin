package net.wouterdanes.docker.provider;

import net.wouterdanes.docker.maven.ContainerStartConfiguration;
import net.wouterdanes.docker.provider.util.DockerHostFromEnvironmentSupplier;
import net.wouterdanes.docker.provider.util.DockerHostFromPropertySupplier;
import net.wouterdanes.docker.provider.util.DockerPortFromEnvironmentSupplier;
import net.wouterdanes.docker.provider.util.DockerPortFromPropertySupplier;

/**
 * This class is responsible for providing a docker interface with a remote (not running on localhost) docker host It
 * can be configured by setting an environment variable {@value #DOCKER_HOST_SYSTEM_ENV }, like in the client. Or you
 * can specify the host and port on the command line like such:
 * <pre>-D{@value #DOCKER_HOST_PROPERTY}=[host] -D{@value #DOCKER_PORT_PROPERTY}=[port]</pre>
 * <p/>
 * The provider defaults to {@value #TCP_PROTOCOL}://{@value #DEFAULT_DOCKER_HOST}:{@value #DEFAULT_DOCKER_PORT}
 */
public class RemoteDockerProvider implements DockerProvider {

    public static final String DOCKER_HOST_SYSTEM_ENV = "DOCKER_HOST";
    public static final String DOCKER_HOST_PROPERTY = "docker.host";
    public static final String DOCKER_PORT_PROPERTY = "docker.port";

    public static final String TCP_PROTOCOL = "tcp";
    public static final int DEFAULT_DOCKER_PORT = 4243;
    public static final String DEFAULT_DOCKER_HOST = "127.0.0.1";

    private final String host;
    private final int port;

    public RemoteDockerProvider() {

        host = DockerHostFromPropertySupplier.INSTANCE.get()
                .or(DockerHostFromEnvironmentSupplier.INSTANCE.get())
                .or(DEFAULT_DOCKER_HOST);

        port = DockerPortFromPropertySupplier.INSTANCE.get()
                .or(DockerPortFromEnvironmentSupplier.INSTANCE.get())
                .or(DEFAULT_DOCKER_PORT);

    }

    @Override
    public String startContainer(final ContainerStartConfiguration configuration) {
        return null;
    }

    @Override
    public String toString() {
        return "RemoteDockerProvider{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
