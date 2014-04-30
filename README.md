docker-maven-plugin
===================

[![Build Status](https://travis-ci.org/wouterd/docker-maven-plugin.svg?branch=master)](https://travis-ci.org/wouterd/docker-maven-plugin)

A maven plugin to manage docker containers and images for integration tests.

# Current Functionality:
- Start a container in the pre-integration-test phase based on an image:
      - Known on the docker host by a name
      - Available in a repository
- Shut down containers in the post-integration-test phase that were started in the pre-integration-test phase
- Supply information to the project during the integration-test phase about:
      - Images that were built
      - Containers that were started
- Build a docker image from a bunch of source files in package and pre-integration-test phases
      - Allow built containers to be started in the pre-integration phase
- Docker provider for "local docker" via tcp
- Docker provider for "remote docker" via tcp (boot2docker/vm/server/localhost via tcp)

# Usage

Current release version: `1.0`
Current snapshot version: `1.1-SNAPSHOT`

## Example

      <plugin>
        <groupId>net.wouterdanes.docker</groupId>
        <artifactId>docker-maven-plugin</artifactId>
        <version>1.0</version>
        <executions>
          <execution>
            <id>build</id>
            <goals>
              <goal>build-images</goal>
            </goals>
            <configuration>
              <images>
                <image>
                  <id>nginx</id>
                  <files>
                    <file>${project.basedir}/src/test/resources/Dockerfile</file>
                  </files>
                </image>
              </images>
            </configuration>
          </execution>
          <execution>
            <id>start</id>
            <configuration>
              <containers>
                <container>
                  <id>Debian</id>
                  <image>debian:wheezy</image>
                </container>
                <container>
                  <id>BusyBox</id>
                  <image>busybox</image>
                </container>
                <container>
                  <id>cache</id>
                  <image>nginx</image>
                </container>
              </containers>
            </configuration>
            <goals>
              <goal>start-containers</goal>
            </goals>
          </execution>
          <execution>
            <id>stop</id>
            <goals>
              <goal>stop-containers</goal>
            </goals>
          </execution>
        </executions>
      </plugin>

The above pom.xml element includes the plugin and starts builds an image from the project. Then it starts some containers
in the pre-integration-test phase, including the built container and stops those in the post-integration-test phase.
Under `<configuration>` add some containers. By giving them an `id`, you can reference them later and the ID is also
used in the port mapping properties. The `<image>` tag specifies the docker image to start.

By default, all exposed ports are published on the host. The following two properties are set per exposed port:
- docker.containers.[id].ports.[portname].host (f.ex 'docker.containers.id.cache.ports.80/tcp.host')
- docker.containers.[id].ports.[portname].port (f.ex 'docker.containers.id.cache.ports.80/tcp.port')

You can pass those project properties over to your integration test and use them to connect to your application.

The plugin will connect to a docker instance over HTTP, linux socket support will be added after 1.0. It will look up
the host/port of docker in the following way:
- It will grab host and port from docker.host and docker.port set by -Ddocker.host and -Ddocker.port on the command line
- Else it will try to parse the DOCKER_HOST system environment variable
- Finally it will default to 127.0.0.1:4243

## Using a SNAPSHOT version
The releases of this plugin are deployed to maven central, the SNAPSHOT versions are automatically deployed to the Sonatype OSS repository. To be able to use the SNAPSHOT versions of this plugin, add the following repository to your project POM or settings.xml:

      <pluginRepository>
            <id>sonatype-oss-snapshots</id>
            <name>Sonatype OSS Snapshots</name>
            <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
      </pluginRepository>
      
## Enabling the Remote Api on the Docker Daemon
Normally, docker accepts commands via unix sockets, by default this is /var/run/docker.sock. This plugin uses the REST API that is also packaged with docker, but needs to be enabled. You can enable this by adding a -H option to the daemon startup command, see http://docs.docker.io/reference/commandline/cli/#daemon. To bind the REST API to port 4243 (default) that only listens to the local interface, add this to your daemon startup: `-H tcp://127.0.0.1:4243`

# Boot2docker-cli
Boot2docker-cli exposes two interfaces on the boot2docker VM. There's a host-only network and a "public network". The VM
also exposes port 4243 on localhost for the docker API. You should specify the IP of `eth1`, the host-only network
interface. Else, the published ports won't be mapped to the right IP.

# Docker version
The docker remote API implementation is based on the 1.10 docker api version. The plugin should work with docker 1.10 and
up. It could also work with lower versions of docker, but it won't, because I specifically target the 1.10 API to prevent
strange errors from occurring.

# Docker providers
Currently the plugin supports two types of docker "providers", which both connect to docker via the remote API
(HTTP REST), unix sockets are not yet supported:
* remote (default), which publishes all ports to the host system and returns `docker_host:dynamic_port` as the port
    mappings for all exposed ports on containers
* local, which doesn't publish any ports to the host and returns `container_ip:exposed_port` as the port mappings for
    all exposed ports on containers

The remote provider works for both dockers running on the same system as the client as well as boot2docker or VM based
dockers. Just make sure DOCKER_HOST or docker.host points to the IP that is on the host-only network or that has all
dynamic docker ports exposed (49xxx). The local provider works when the docker containers are reachable from the client
through their IP address, so for example when the client runs on the docker host. Local is also a nice mode to use when
consumers of your containers need to connect on the "real port" and cannot connect to a "dynamic port".

You can specify the docker provider using the system property `docker.provider`, either in the pom or via the command
line using -D, for example: `mvn clean verify -Prun-its -Ddocker.provider=local`

# Dependencies:

* [Google Guava](https://code.google.com/p/guava-libraries/) for Optional and Supplier, because it's too early for Java8
* [Jersey Client](https://jersey.java.net/) for a light weight API to do rest calls.
* [Jackson](http://jackson.codehaus.org/) for parsing / creating JSON
* [Apache Commons Compress](http://commons.apache.org/proper/commons-compress/) for creating the tar.gz archive needed to build a docker image

# Architecture principles
* The plugin needs to work in CI server environments, so it needs to make sure there are no port collisions and multiple
    builds can run on the same server in parallel. Also, docker images and containers it creates need to have unique names
    and/or ids.
* Multiple "docker providers" need to be supported and pluggable

# Future functionality
- [ ] Linking containers
- [ ] Commit containers instead of cleaning them up when the integration tests have failed
- [ ] Create some kind of "wait for stuff to initialize" step: for example, Tomcat starting up.
- [ ] Add support for linux sockets for "local docker" provider
- [ ] Add support for Tutum.co
- [ ] Create a feature complete docker remote api for Java
- [ ] Support multiple (all) versions of the Docker Remote API
- [ ] Push containers to a docker registry in the deploy phase
