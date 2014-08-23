package net.wouterdanes.docker.remoteapi.model;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * Holds the information for a link to another docker container.
 */
public class ContainerLink {
  @Parameter(required = true)
  private String containerId;
  @Parameter(required = true)
  private String containerAlias;

  public ContainerLink toContainer(String containerId) {
    this.containerId = containerId;
    return this;
  }

  public ContainerLink withAlias(String alias) {
    this.containerAlias = alias;
    return this;
  }

  public String getContainerId() {
    return containerId;
  }

  /**
   * Returns the configured alias for this container
   *
   * @return the linked container's alias
   */
  public String getContainerAlias() {
    return containerAlias;
  }
}
