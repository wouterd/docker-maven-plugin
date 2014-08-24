# version 1.7
- The plugin now enables you to scan the stderr & stdout of a container for a certain phrase that indicates succesful
    initialization of the container. This phrase can be any regular expression and it is globally matched.
- The plugin now allows you to link containers that you start.

# version 1.6
- The plugin now forces at least Maven 3.1.1, because else the plugin will not function well
- The plugin now checks for duplicate image IDs, failing the build if the same image ID is used twice
- The plugin now checks for duplicate container IDs, failing the build if the same container ID is used twice
- Fixed Push & Tag for private repositories that got broken in 1.5
- Upgraded maven plugin/api dependencies to the version that comes with Maven 3.1.1
