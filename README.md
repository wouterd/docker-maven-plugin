docker-maven-plugin
===================

[![Build Status](https://travis-ci.org/wouterd/docker-maven-plugin.svg?branch=master)](https://travis-ci.org/wouterd/docker-maven-plugin)

A maven plugin to create, test and publish docker containers and images for maven projects. Can also be used to integration test your application using docker containers.

# About this document
As the documentation evolves with different plugin versions, be sure that before you read on that:
- You check that you are watching the right tag
- Switch to the right tag to read the right documentation.

The README of the master branch will cover the current development version and not cover the current released version.

# Donations
If you love this plugin and use it a lot and want to donate something for my time spent maintaining this project, use the following link: [Pay pal](https://www.paypal.me/WouterDanes). Donations are much appreciated!

# Minimum versions required for the plugin to work

- Minimum required java version: 8
- Minimum required maven version: 3.1.1
- Minimum required docker daemon version: 1.9 (Remote API v1.21)

# Current Functionality:
- Build a docker image from a bunch of source files in package and pre-integration-test phases
      - Allow built containers to be started in the pre-integration phase
- Start a container in the pre-integration-test phase based on an image:
      - Known on the docker host by a name
      - Available in a repository
- Wait for a container to initialize by checking for a phrase in the stderr/stdout of the container
- Link containers (same as docker run --link)
- Commit the state of a running container to a new image
- Shut down containers in the post-integration-test phase that were started in the pre-integration-test phase
- Supply information to the project during the integration-test phase about:
      - Images that were built
      - Containers that were started
- Verifies the build in the "verify" phase which tests if anything upto the integration test phase failed.
- Assign release tags to a docker image in the install phase
- Push docker images to a public or private image registry in the deploy phase
- Docker provider for "local docker" via tcp
- Docker provider for "remote docker" via tcp (boot2docker/vm/server/localhost via tcp)

# Usage

Current release version: `5.0.0`

Current snapshot version: `5.0.1-SNAPSHOT`

## Example
The following is a snippet from the pom for one of the integration test projects in this project. You can find the full
pom.xml here: [pom.xml](https://github.com/wouterd/docker-maven-plugin/blob/master/src/it/sample-api-mongo-it/pom.xml)

    <plugin>
      <groupId>net.wouterdanes.docker</groupId>
      <artifactId>docker-maven-plugin</artifactId>
      <version>5.0.0</version>
      <executions>
        <execution>
          <id>package</id>
          <goals>
            <goal>build-images</goal>
          </goals>
          <configuration>
            <images>
              <image>
                <id>app</id>
                <dockerFile>${project.basedir}/src/main/docker/Dockerfile</dockerFile>
                <artifacts>
                  <artifact>
                    <file>${project.build.directory}/discuss-jar-with-dependencies.jar</file>
                  </artifact>
                </artifacts>
              </image>
            </images>
          </configuration>
        </execution>
        <execution>
          <id>start</id>
          <goals>
            <goal>start-containers</goal>
          </goals>
          <configuration>
            <!-- You can set forceCleanup to true to stop and remove started containers
                 at the end of the build even if the stop-containers goal is not executed
                 (useful for preventing Ctrl+C causing dangling containers) -->
            <forceCleanup>false</forceCleanup>
            <containers>
              <container>
                <id>mongo</id>
                <image>mongo:2.6</image>
                <waitForStartup>waiting for connections on port 27017</waitForStartup>
              </container>
              <container>
                <id>app</id>
                <image>app</image>
                <links>
                  <link>
                    <containerId>mongo</containerId>
                    <containerAlias>mongo</containerAlias>
                  </link>
                </links>
                <env>
                  <APP_MESSAGE>Hello, world!</APP_MESSAGE>
                </env>
                <waitForStartup>Ratpack started for</waitForStartup>
              </container>
              <container>
                <id>app2</id>
                <image>app</image>
                <links>
                  <link>
                    <containerId>mongo</containerId>
                    <containerAlias>mongo</containerAlias>
                  </link>
                </links>
                <env>
                  <APP_MESSAGE>I am, so I message</APP_MESSAGE>
                </env>
                <waitForStartup>Ratpack started for</waitForStartup>
              </container>
            </containers>
          </configuration>
        </execution>
        <execution>
          <id>commit</id>
          <configuration>
            <containers>
              <container>
                <id>app2</id>
                <repo>me/app</repo>
                <tag>1.0</tag>
                <push>true</push>
              </container>
            </containers>
          </configuration>
          <goals>
            <goal>commit-container</goal>
          </goals>
        </execution>
        <execution>
          <id>stop</id>
          <goals>
            <goal>stop-containers</goal>
          </goals>
        </execution>
        <execution>
          <id>verify</id>
          <goals>
            <goal>verify</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

The above pom.xml element includes the plugin and starts builds an image from the project. Then it starts some containers
in the pre-integration-test phase, including the built container and stops those in the post-integration-test phase.
Under `<configuration>` add some containers. By giving them an `id`, you can reference them later and the ID is also
used in the port mapping properties. The `<image>` tag specifies the docker image to start.

By default, all exposed ports are published on the host. The following two properties are set per exposed port:
- docker.containers.[id].ports.[portname].host (f.ex 'docker.containers.id.app.ports.80/tcp.host')
- docker.containers.[id].ports.[portname].port (f.ex 'docker.containers.id.app.ports.80/tcp.port')

You can pass those project properties over to your integration test and use them to connect to your application.

The plugin will connect to a docker instance over HTTP, linux socket support will be added after 1.0. It will look up
the host/port of docker in the following way:
- It will grab host and port from docker.host and docker.port set by -Ddocker.host and -Ddocker.port on the command line
- Else it will try to parse the DOCKER_HOST system environment variable
- Finally it will default to 127.0.0.1:2375

## Environment Variables

Environment variables can be passed to containers using the following configuration syntax:

            <container>
                <id>app</id>
                <image>app</image>
                <env>
                    <VARIABLE_NAME>variable value<VARIABLE_NAME>
                <env>
            </container>

## Setting the hostname

The hostname can be set by adding a <hostname> element to a container configuration.

            <container>
                <id>app</id>
                <image>app</image>
                <hostname>appserver1</hostname>
            </container>

## Setting the hostname

The macAddress can be set by adding a <macAddress> element to a container configuration.

            <container>
                <id>app</id>
                <image>app</image>
                <macAddress>12:34:56:78:9a:bc</macAddress>
            </container>

## Linking containers

Containers can be linked, similar to the `--link name:alias` parameter of the `docker run` command.
The configuration snippet looks as follows:

            <container>
                <id>app</id>
                <image>app</image>
                <links>
                    <link>
                        <containerId>mongo</containerId>
                        <containerAlias>mongo</containerAlias>
                    </link>
                </links>
            </container>

The containerId is the id specified in another `<container>` definition. It will be replaced with the container name of
 the started container when the plugin is executed. The containerAlias is the name of the container being linked inside
 the container that links the container. It's also the hostname of the linked container for the linking container. In
 the case of the above XML snippet, I can now reach the mongodb instance using `mongo:27017` as the connection string.

## Wait for a container to finish starting up

You might want to wait for your application to finish initialization before you start running integration tests. The
plugin allows you to do a global regular expression find on the stdout + stderr of your container to see if the
container has finished initialization. To check if a tomcat container has started up, you could configure the following:

        <container>
            <id>app-server</id>
            <image>myAppServer</image>
            <waitForStartup>Server startup in</waitForStartup>
        </container>

The `<waitForStartup/>` tag can contain any valid java regular expression.

## `build-images` goal
The `build-images` goal allows you to build a docker image based on a list of files, one of which must be a `Dockerfile`.
Below is an example snippet.

          <execution>
            <id>build</id>
            <goals>
              <goal>build-images</goal>
            </goals>
            <configuration>
              <images>
                <image>
                  <id>nginx</id>
                  <dockerFile>${project.basedir}/src/test/resources/Dockerfile</dockerFile>
                  <artifacts>
                    <artifact>
                      <file>${project.basedir}/src/test/resources/nginx.conf</file>
                      <dest>etc/nginx/nginx.conf</dest>
                    </artifact>
                    <artifact>
                      <file>${project.basedir}/src/test/resources/sites-available/</file>
                      <dest>etc/nginx/sites-available/</dest>
                    </artifact>
                  </artifacts>
                  <mavenArtifacts>
                    <mavenArtifact>
                      <dependency>biz.hochguertel.javaeetutorial.jaxrs:hello:war:1.4</dependency>
                      <dest>test/rest.war</dest>
                    </mavenArtifact>
                  </mavenArtifacts>
                  <buildArguments>
                    <foo>bar</foo>
                  </buildArguments>
                  <keep>true</keep>
                  <push>true</push>
                  <registry>mydocker-registry.corp.com:5000</registry>
                  <nameAndTag>wouterd/my-nginx:1.0</nameAndTag>
                </image>
              </images>
            </configuration>
          </execution>

The configuration works as follows:
- `<images>` contains a list of images to build as `<image>` elements
- `<id>` for an image specifies the ID you want to use to reference this image in the plugin, for example when starting
    a container based on a built image.
  `<dockerFile>` specifies which file to use a the Dockerfile
- `<artifacts>` contains a list of files to add to the container as `<artifact>` elements. For each `<artifact>` element
    you can specify:
    - `file`: pointing to the file to add to the tar ball sent to the docker daemon
    - `dest`: path in the tar ball where you want the file, you can refer to it using the same path in an ADD statement.
- `<buildArguments>` contains a list of build arguments that can be used in your Dockerfile
    you can specify:
    - `tag_name`: The name of the tag will be used as the name of the build argument e.g. <foo> will create a build argument called `foo` that can be used in your dockerfile as `$foo`.
    - `tag_value`: The value of the tag will be used as the value of the build argument e.g. <foo>bar</foo> will create a build argument called `foo` with the value `bar` that can be used in your dockerfile as `$foo`.
- `<mavenArtifacts>` contains a list of Maven dependencies to add to the container as `<mavenArtifact>` elements. For each `<mavenArtifact>` element
    you can specify:
    - `dependency`: A maven dependency in the format \<groupId\>:\<artifactId\>[:\<extension\>[:\<classifier\>]]:\<version\>
    - `dest`: path in the tar ball where you want the maven dependency put, you can refer to it using the same path in an ADD statement.    
- `<keep>` (defaults to false) specifies whether or not the plugin should keep this image or delete it after executing
    the maven build. If false, the image will be deleted as part of the `stop-containers` goal.
- `<nameAndTag>` specifies the name and tag for this image, especially useful when keeping the built images. It can be in one of the
    "standard" docker formats: `repository:tag`; or `registry/repository:tag`.
- `<push>` (defaults to false) specifies whether or not the plugin should push this image to a Docker image registry. If true, the
    image will be pushed as part of the `push-images` goal. Additionally, when true, the `<keep>` property is ignored and
    the image will be retained after the container is stopped.
- `<registry>` captures the host name and port of a private Docker registry, to which the image should be pushed, optional.

## `stop-containers` goal
The `stop-containers` goal allows you to stop and clean up any containers that were created with the plugin. The `logs`
parameter allows you to specify a folder where the logs of the containres need to go. They will be saved as
`[container name].log`. If you don't specify a folder, no logs will be saved.

## `commit-containers` goal
The `commit-containers` goals allows you to persist the state of a container to an image. Below is an example snippet.

          <execution>
            <id>commit</id>
            <goals>
              <goal>commit-containers</goal>
            </goals>
            <configuration>
              <containers>
                <container>
                  <id>app2</id>
                  <repo>me/app</repo>
                  <tag>${maven.build.timestamp}</tag>
                  <comment>Daily build</comment>
                  <author>Jenkins</author>
                  <push>true</push>
                </container>
               </containers>
            </configuration>
          </execution>

The configuration works as follows:
- `<containers>` contains a list of containers to build as `<container>` elements
- `<id>` specifies the ID of a previously started container.
- `<repo>` specifies the repository to push the resulting image to.
- `<tag>` specifies the tag for the image, especially useful when pushing the built image to a repository.
- `<comment>` commit message
- `<author>` author (e.g., “John Hannibal Smith <hannibal@a-team.com>“)
- `<push>` (defaults to false) specifies whether or not the plugin should push the tagged image to a Docker image registry.

## `tag-images` goal
The `tag-images` goal allows you to assign additional tags to images and optionally flag those tags to be pushed to a Docker image registry in a subsequent
`push-images` execution.
Below is an example snippet.

          <execution>
            <id>release</id>
            <goals>
              <goal>tag-images</goal>
            </goals>
            <configuration>
              <images>
                <image>
                  <id>nginx</id>
                  <tags>
                  	<tag>goonwarrior/my-nginx:1.0</tag>
                  	<tag>goonwarrior/my-nginx:latest</tag>
                  </tags>
                  <push>true</push>
                  <registry>mydocker-registry.corp.com:5000</registry>
                </image>
              </images>
            </configuration>
          </execution>

The configuration works as follows:
- `<images>` contains a list of images to build as `<image>` elements
- `<id>` specifies the ID of a previously built image.
- `<tags>` contains a list of repository, name and/or tags to assign to the image as `<tag>` elements, Each can be in one of
	the "standard" docker formats: `repository:tag`; or `registry/repository:tag`.
- `<push>` (defaults to false) specifies whether or not the plugin should push the tagged image to a Docker image registry.
- `<registry>` captures the host name and port of a private Docker registry, to which the image should be pushed, optional.

## `push-images` goal
The `push-images` goal allows you to push any marked images that were built in a prior execution of the
`build-images` goal to a Docker image registry.

Pushing an image to a private registry (that is, a registry other than https://registry.hub.docker.com/) can
be specified in one of two ways.

1. Embedded in the `<nameAndTag>`

          e.g.
          `<nameAndTag>myregistry.corpdomain.net:5000/repo:tag</nameAndTag>`

1. Separately via `<registry>` parameter of the image or incorporated into the `<nameAndTag>`.

          e.g.
          `<nameAndTag>repo:tag</nameAndTag>`
          `<registry>myregistry.corpdomain.net</registry>`

These 2 configurations behave slightly differently. In the former, the image is associated with a single
long tag  and all references to subsequent references to that image (e.g. in `FROM` statement in a Dockerfile)
need to reference the full string. In the latter case, 2 tags are registered, one long, one short, enabling
access to the more concise form.

If the registry is omitted, then https://registry.hub.docker.com/ is assumed.

## Credentials
Some registries (including https://registry.hub.docker.com/) will require user credentials to perform
specific operations. The plugin provides a means to specify these credentials however, at this time
they are only used when pushing images. These credentials can be specified in multiple ways.

### Credentials within plugin configuration or populated indirectly by Maven properties

- `<userName>`, Docker registry user name, defaults to the value of `docker.userName`.
- `<password>`, Docker registry user password (in plain text), defaults to the value of `docker.password`.
- `<email>`, Docker registry user email address, defaults to the value of `docker.email`.

### Credentials in Maven's global `settings.xml`

Define a new server in `~/.m2/settings.xml`

        <servers>
            <server>
                <id>docker.mycompany.com</id>
                <username>...</username>
                <password>...</password>
                <configuration>
                    <email>...</email>
                </configuration>
              </server>
        </servers>

Refer to this server in the plugin configuration

        <plugin>
            <groupId>net.wouterdanes.docker</groupId>
            <artifactId>docker-maven-plugin</artifactId>
            <version>4.1.1</version>
            <configuration>
                <serverId>docker.mycompany.com</serverId>
            </configuration>
            <executions>
                ...
            </executions>
        </plugin>

Priority is given to credentials defined in plugin configuration over the ones from `settings.xml`.


## Using a SNAPSHOT version
The releases of this plugin are deployed to maven central, the SNAPSHOT versions are automatically deployed to the Sonatype OSS repository. To be able to use the SNAPSHOT versions of this plugin, add the following repository to your project POM or settings.xml:

      <pluginRepository>
            <id>sonatype-oss-snapshots</id>
            <name>Sonatype OSS Snapshots</name>
            <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
      </pluginRepository>

## Enabling the Remote Api on the Docker Daemon
Normally, docker accepts commands via unix sockets, by default this is /var/run/docker.sock. This plugin uses the REST
API that is also packaged with docker, but needs to be enabled. You can enable this by adding a -H option to the daemon
startup command, see http://docs.docker.io/reference/commandline/cli/#daemon. To bind the REST API to port 2375 (default)
that only listens to the local interface, add this to your daemon startup: `-H tcp://127.0.0.1:2375`

## Skipping execution of the plugin or phases
To skip execution of the plugin, you can set the docker.skip property to true. This can be useful when you want to skip
running tests, like: `mvn clean verify -Ddocker.skip=true -DskipTests`.
Each individual execution can be skipped or the plugin as a whole can be skipped by configuring the `<skip>` property
on the `<configuration>` element of the plugin or an execution.
Adding the following profile to your pom.xml will skip the whole plugin when the `skipTests` property is set:

        <profile>
            <id>skip-docker-plugin-execution</id>
            <activation>
                <property>skipTests</property>
            </activation>
            <properties>
                <docker.skip>true</docker.skip>
            </properties>
        </profile>

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

## HTTPS support
As per Docker 1.3, the docker daemon can be protected with SSL encryption. For this to work, the client needs to have
some certificates and a private key. These by default reside in ~/.docker. Boot2docker now by default since version 1.3
enables SSL encryption. The plugin supports this. As long as you can reach the daemon by doing `docker ps` on the command
line, the plugin should pick up the right environment variables. The environment variables that control this behavior are:

* `DOCKER_HOST` specifies where docker lives, for example: `tcp://192.168.59.103:2376`.
* `DOCKER_TLS_VERIFY` specifies whether SSL encryption is on, the value `1` denotes that SSL encryption is enabled.
* `DOCKER_CERT_PATH` points to the folder containing the needed `ca.pem`, `cert.pem` and `key.pem`. If not specified,
    this defaults to `~/.docker`.

# Dependencies:

* [Jersey Client](https://jersey.java.net/) for a light weight API to do rest calls.
* [Jackson](http://jackson.codehaus.org/) for parsing / creating JSON
* [Apache Commons Compress](http://commons.apache.org/proper/commons-compress/) for creating the tar.gz archive needed to build a docker image

# Building the project

To build the project, you will need Maven and Java8.

To build the project and run all the tests, run:

        mvn clean verify -Prun-its

This will run the build including all integration tests. You should run this at least once before submitting a PR.
To just run unit tests, run:

        mvn clean verify

# Architecture principles
* The plugin needs to work in CI server environments, so it needs to make sure there are no port collisions and multiple
    builds can run on the same server in parallel. Also, docker images and containers it creates need to have unique names
    and/or ids.
* Multiple "docker providers" need to be supported and pluggable

# Future functionality
- [ ] Commit containers instead of cleaning them up when the integration tests have failed
- [ ] Add support for linux sockets for "local docker" provider
- [ ] Add support for Tutum.co
- [ ] Create a feature complete docker remote api for Java
- [ ] Support multiple (all) versions of the Docker Remote API
