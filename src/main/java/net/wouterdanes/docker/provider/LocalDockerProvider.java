package net.wouterdanes.docker.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.wouterdanes.docker.provider.model.ContainerStartConfiguration;
import net.wouterdanes.docker.provider.model.ExposedPort;
import net.wouterdanes.docker.remoteapi.model.ContainerInspectionResult;
import net.wouterdanes.docker.remoteapi.model.ContainerStartRequest;

/**
 * A Docker provider for the remote http api on a locally running docker.
 * This provider won't publish ports to the host, but will give host + port combinations directly to the container.
 * You should only use this provider if you can directly access the created containers via their IP address on the
 * docker host.
 */
public class LocalDockerProvider extends RemoteApiBasedDockerProvider {

    private static final Pattern TCP_PORT_MATCHER = Pattern.compile("([0-9]+)/tcp");

    public LocalDockerProvider() {
        super();
    }

    @Override
    public String startContainer(final ContainerStartConfiguration configuration) {
        return super.startContainer(configuration, new ContainerStartRequest());
    }

    @Override
    public List<ExposedPort> getExposedPorts(final String containerId) {
        ContainerInspectionResult containerInspectionResult = getContainersService().inspectContainer(containerId);
        if (containerInspectionResult.getNetworkSettings().getPorts().isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> ports = containerInspectionResult.getConfig().getExposedPorts().keySet();
        String containerIp = containerInspectionResult.getNetworkSettings().getIpAddress();
        List<ExposedPort> exposedPorts = new ArrayList<>(ports.size());
        for (String port : ports) {
            Matcher matcher = TCP_PORT_MATCHER.matcher(port);
            if (matcher.matches()) {
                int tcpPort = Integer.parseInt(matcher.group(1));
                exposedPorts.add(new ExposedPort(port, tcpPort, containerIp));
            }
        }
        return exposedPorts;
    }
}
