# version 2.2
- Changed the default port for the http interface to the IANA port for docker: 2375. (thanks Erno Tukia!)
- Fixed pulling images from a private repository. (thanks Erno Tukia!)

# version 2.1.1
- Bug fix: Image names can now contain hyphens and dots and get properly pushed. (thanks Erno Tukia!)

# version 2.1
- Fixed a bug in `start-container` goal that crashed the plugin if a container had `waitForStartup` set, but failed to
    start
- `push-images` goal no longer allows you to try to push an image without a name.
- DEV: the build now starts a docker registry in docker so you can integration test against a registry too.
- A container now waits with starting up before linked containers have finished starting up.
- Environment variables can now be specified for a starting container (thanks Scott Stevelinck!)

# version 2.0
- The plugin now targets v1.12 of the Docker remote API, which shipped with version 1.0 of Docker. So from this version
 on, you will need at least version 1.0 of the docker daemon.
- The plugin now enables you to scan the stderr & stdout of a container for a certain phrase that indicates successful
    initialization of the container. This phrase can be any regular expression and it is globally matched.
- The plugin now allows you to link containers that you start.

# version 1.6
- The plugin now forces at least Maven 3.1.1, because else the plugin will not function well
- The plugin now checks for duplicate image IDs, failing the build if the same image ID is used twice
- The plugin now checks for duplicate container IDs, failing the build if the same container ID is used twice
- Fixed Push & Tag for private repositories that got broken in 1.5
- Upgraded maven plugin/api dependencies to the version that comes with Maven 3.1.1
