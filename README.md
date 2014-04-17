docker-maven-plugin
===================

A maven plugin to manage docker containers and images for integration tests.

# Architecture principles
* The plugin needs to work in CI server environments, so it needs to make sure there are no port collisions and multiple builds can run on the same server in parallel.
* Multiple "docker providers" need to be supported and pluggable

# Musts for 1.0
* Build a docker image from a bunch of source files in package and pre-integration-test phases
* Start a container in the pre-integration-test phase based on an image:
  * Known on the docker host by a name
  * Just built by the plugin itself
* Supply information to the project during the integration-test phase about:
  * Images that were built
  * Containers that were started
* Shut down containers in the post-integration-test phase that were started in the pre-integration-test phase
* Docker provider for "local docker" via tcp
* Docker provider for "remote docker" via tcp (boot2docker/vm/server)

# Further possible functionality
* Commit containers instead of cleaning them up when the integration tests have failed
* Create some kind of "wait for stuff to initialize" step: for example, Tomcat starting up.
