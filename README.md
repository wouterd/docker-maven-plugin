docker-maven-plugin
===================

[![Build Status](https://travis-ci.org/wouterd/docker-maven-plugin.svg?branch=master)](https://travis-ci.org/wouterd/docker-maven-plugin)

A maven plugin to manage docker containers and images for integration tests.

# Architecture principles
* The plugin needs to work in CI server environments, so it needs to make sure there are no port collisions and multiple builds can run on the same server in parallel. Also, docker images and containers it creates need to have unique names and/or ids.
* Multiple "docker providers" need to be supported and pluggable

# Musts for 1.0

- [x] Start a container in the pre-integration-test phase based on an image:
  - [x] Known on the docker host by a name
  - [x] Available in a repository
- [x] Shut down containers in the post-integration-test phase that were started in the pre-integration-test phase
- [ ] Supply information to the project during the integration-test phase about:
  - [ ] Images that were built
  - [ ] Containers that were started
- [ ] Build a docker image from a bunch of source files in package and pre-integration-test phases
  - [ ] Allow built containers to be started in the pre-integration phase
- [ ] Docker provider for "local docker" via tcp
- [ ] Docker provider for "remote docker" via tcp (boot2docker/vm/server)

# Further possible functionality
* Commit containers instead of cleaning them up when the integration tests have failed
* Create some kind of "wait for stuff to initialize" step: for example, Tomcat starting up.
* Add support for linux sockets for "local docker" provider
* Add support for Tutum.co
