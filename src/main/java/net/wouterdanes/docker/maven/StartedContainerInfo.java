package net.wouterdanes.docker.maven;

import net.wouterdanes.docker.remoteapi.model.ContainerInspectionResult;

/**
 * Holds the information for a started container: it's starting id and an inspection result just after starting.
 */
public class StartedContainerInfo {

    private final String containerId;
    private final ContainerInspectionResult containerInfo;

    public StartedContainerInfo(final String containerId, final ContainerInspectionResult containerInfo) {
        this.containerId = containerId;
        this.containerInfo = containerInfo;
    }

    public String getContainerId() {
        return containerId;
    }

    public ContainerInspectionResult getContainerInfo() {
        return containerInfo;
    }
}
