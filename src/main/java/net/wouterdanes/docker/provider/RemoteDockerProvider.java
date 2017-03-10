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

package net.wouterdanes.docker.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.wouterdanes.docker.provider.model.ContainerStartConfiguration;
import net.wouterdanes.docker.provider.model.ExposedNetwork;
import net.wouterdanes.docker.provider.model.ExposedNetworkInfo;
import net.wouterdanes.docker.provider.model.ExposedPort;
import net.wouterdanes.docker.remoteapi.model.ContainerInspectionResult;
import net.wouterdanes.docker.remoteapi.model.ContainerStartRequest;
import net.wouterdanes.docker.remoteapi.model.Network;
import net.wouterdanes.docker.remoteapi.model.NetworkSettings;

/**
 * This class is responsible for providing a docker interface with a remote (not running on localhost) docker host. It
 * can be configured by setting an environment variable {@value #DOCKER_HOST_SYSTEM_ENV }, like in the client. Or you
 * can specify the host and port on the command line like such:
 * <pre>-D{@value #DOCKER_HOST_PROPERTY}=[host] -D{@value #DOCKER_PORT_PROPERTY}=[port]</pre>
 *
 * The provider defaults to {@value #TCP_PROTOCOL}://{@value #DEFAULT_DOCKER_HOST}:{@value #DEFAULT_DOCKER_PORT}
 */
public class RemoteDockerProvider extends RemoteApiBasedDockerProvider {

    public RemoteDockerProvider() {
        super();
    }

    @Override
    public ContainerInspectionResult startContainer(final ContainerStartConfiguration configuration) {
        ContainerStartRequest startRequest = new ContainerStartRequest()
                .withAllPortsPublished()
                .withLinks(configuration.getLinks());

        return super.startContainer(configuration, startRequest);
    }

    @Override
    public ExposedNetworkInfo getExposedNetworkInfo( final String containerId) {
        ContainerInspectionResult containerInspectionResult = getContainersService().inspectContainer(containerId);
        if (containerInspectionResult.getNetworkSettings().getPorts().isEmpty()) {
            return new ExposedNetworkInfo();
        }
        NetworkSettings networkSettings = containerInspectionResult.getNetworkSettings();
        Map<String, List<NetworkSettings.PortMappingInfo>> ports =
                networkSettings.getPorts();

        List<ExposedPort> exposedPorts = new ArrayList<>();
        for (Map.Entry<String, List<NetworkSettings.PortMappingInfo>> port : ports.entrySet()) {
            String exposedPort = port.getKey();
            if ( port.getValue() == null || port.getValue().isEmpty() )
            {
                System.out.printf( "Exposed ports list is empty / missing for: '%s'\n", exposedPort );
                continue;
            }

            int hostPort = port.getValue().get(0).getHostPort();
            exposedPorts.add(new ExposedPort(exposedPort, hostPort, getHost()));
        }

        List<ExposedNetwork> nets = new ArrayList<>();
        Map<String, Network> networks = networkSettings.getNetworks();
        if ( networks != null )
        {
            networks.forEach( (name, net) -> nets.add( new ExposedNetwork( name, net.getIpAddress() )));
        }

        return new ExposedNetworkInfo().withExposedPorts( exposedPorts ).withExposedNetworks( nets );
    }

}
