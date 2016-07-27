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

package net.wouterdanes.docker.maven;

import net.wouterdanes.docker.provider.DockerProvider;
import net.wouterdanes.docker.provider.DockerProviderSupplier;
import net.wouterdanes.docker.provider.model.BuiltImageInfo;
import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;
import net.wouterdanes.docker.provider.model.PushableImage;
import net.wouterdanes.docker.remoteapi.exception.DockerException;
import net.wouterdanes.docker.remoteapi.model.ContainerInspectionResult;
import net.wouterdanes.docker.remoteapi.model.Credentials;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.stripToNull;

/**
 * Base class for all Mojos with shared functionality
 */
public abstract class AbstractDockerMojo extends AbstractMojo {

    private static final String STARTED_CONTAINERS_KEY = "startedContainers";
    private static final String BUILT_IMAGES_KEY = "builtImages";
    private static final String PUSHABLE_IMAGES_KEY = "pushableImages";
    private static final String ERRORS_KEY = "errors";

    @Component
    private RepositorySystem repositorySystem;

    @Parameter(property = "docker.keepContainers")
    protected boolean keepContainers;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repositorySystemSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}")
    private List<RemoteRepository> remoteRepositories;

    @Parameter(defaultValue = "remote", property = "docker.provider", required = true)
    private String providerName;

    @Parameter(defaultValue = "false", property = "docker.skip", required = false)
    private boolean skip;

    @Parameter(defaultValue = "", property = "docker.userName", required = false)
    private String userName;

    @Parameter(defaultValue = "", property = "docker.email", required = false)
    private String email;

    @Parameter(defaultValue = "", property = "docker.password", required = false)
    private String password;

    @Parameter(property = "serverId")
    private String serverId;

    /**
     * Maven settings from ~/.m2/settings.xml
     */
    @Component
    private Settings settings;

    /**
     * Write docker logs output to this directory, using a filename of ${container-name}.log (from the
     * plugin configuration's container name field).
     */
    @Parameter(defaultValue = "", property = "docker.logs", required = false)
    private String logs;

    public void setProviderName(final String providerName) {
        this.providerName = providerName;
    }

    public void setSkip(final boolean skip) {
        this.skip = skip;
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Execution skipped");
            return;
        }

        getLog().info("Using docker provider: " + providerName);
        doExecute();
    }

    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

    protected void registerStartedContainer(String containerId, ContainerInspectionResult container) {
        StartedContainerInfo info = new StartedContainerInfo(containerId, container);
        Map<String, StartedContainerInfo> startedContainers = obtainMapFromPluginContext(STARTED_CONTAINERS_KEY);
        startedContainers.put(containerId, info);
    }

    protected Collection<StartedContainerInfo> getStartedContainers() {
        Map<String, StartedContainerInfo> map = obtainMapFromPluginContext(STARTED_CONTAINERS_KEY);
        return map.values();
    }

    protected Optional<StartedContainerInfo> getInfoForContainerStartId(String startId) {
        Map<String, StartedContainerInfo> map = obtainMapFromPluginContext(STARTED_CONTAINERS_KEY);
        return ofNullable(map.get(startId));
    }

    protected void registerBuiltImage(String imageId, ImageBuildConfiguration imageConfig)
            throws MojoFailureException, MojoExecutionException
    {
        BuiltImageInfo info = new BuiltImageInfo(imageId, imageConfig);

        Map<String, BuiltImageInfo> builtImages = obtainMapFromPluginContext(BUILT_IMAGES_KEY);
        builtImages.put(info.getStartId(), info);

        if (imageConfig.isPush()) {
            enqueueForPushing(imageId, imageConfig);
        }
    }

    protected Collection<BuiltImageInfo> getBuiltImages() {
        Map<String, BuiltImageInfo> builtImagesMap = obtainMapFromPluginContext(BUILT_IMAGES_KEY);
        return Collections.unmodifiableCollection(builtImagesMap.values());
    }

    protected void cleanUpStartedContainers()
            throws MojoExecutionException
    {
        Optional<Path> logsDir = Optional.ofNullable(stripToNull(logs)).map(this::getLogDirectory);
        if (logsDir.isPresent()) {
            getLog().info("Writing docker container logs to directory: " + logsDir.get());
        } else {
            getLog().info("NOT writing docker container logs.");
        }

        List<String> stoppedContainerIds = new ArrayList<>();
        for (Iterator<StartedContainerInfo> it = getStartedContainers().iterator(); it.hasNext(); ) {
            StartedContainerInfo startedContainerInfo = it.next();
            String containerId = startedContainerInfo.getContainerInfo().getId();
            String containerName = startedContainerInfo.getContainerId();
            getLog().info(String.format("Stopping container '%s'..", containerId));
            try {
                if (logsDir.isPresent()) {
                    Path logFile = logsDir.get().resolve(containerName + ".log");
                    getLog().info("Writing logs to: " + logFile);
                    String logs = getDockerProvider().getLogs(containerId);

                    try {
                        Files.copy(new ByteArrayInputStream(logs.getBytes()), logFile, REPLACE_EXISTING);
                    } catch (IOException e) {
                        if (getLog().isDebugEnabled()) {
                            getLog().error("Failed to write docker logs to: " + logFile, e);
                        } else {
                            getLog().error("Failed to write docker logs to: " + logFile);
                        }
                    }
                }

                getDockerProvider().stopContainer(containerId);
                it.remove();
                stoppedContainerIds.add(containerId);
            } catch (DockerException e) {
                getLog().error("Failed to stop container (this means it also won't be deleted)", e);
            }
        }

        if ( !keepContainers )
        {
            for (String containerId : stoppedContainerIds) {
                getLog().info(String.format("Deleting container '%s'..", containerId));
                try {
                    getDockerProvider().deleteContainer(containerId);
                } catch (DockerException e) {
                    getLog().error("Failed to delete container", e);
                }
            }
        }
    }

    private Path getLogDirectory(String logs) {
        try {
            return Files.createDirectories(Paths.get(logs));
        } catch (IOException e) {
            getLog().error("Cannot create log directory", e);
            return null;
        }
    }

    protected DockerProvider getDockerProvider()
            throws MojoExecutionException
    {
        DockerProvider provider = new DockerProviderSupplier(providerName).get();
        provider.setCredentials(getCredentials());
        provider.setLogger(getLog());
        provider.setRepositorySystem(repositorySystem);
        provider.setRepositorySystemSession(repositorySystemSession);
        provider.setRemoteRepositories(remoteRepositories);
        return provider;
    }

    protected Credentials getCredentials() throws MojoExecutionException {
        // priority to credentials from plugin configuration over the ones from settings
        return Stream.of(getCredentialsFromParameters(), getCredentialsFromSettings())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElse(null);
    }

    private Optional<Credentials> getCredentialsFromParameters()
            throws MojoExecutionException
    {
        if (isBlank(userName)) {
            getLog().debug("No user name provided. Will try <servers> from settings.xml");
            return empty();
        }
        getLog().debug("Using credentials from plugin config: " + userName);

        return of(new Credentials(userName, decryptPassword(password), email, null));
    }

    private Optional<Credentials> getCredentialsFromSettings() throws MojoExecutionException {
        if(settings == null) {
            getLog().debug("No settings.xml");
            return empty();
        }
        Server server = settings.getServer(serverId);
        if(server == null) {
            getLog().debug("Cannot find server " + serverId + " in Maven settings");
            return empty();
        }
        getLog().debug("Using credentials from Maven settings: " + server.getUsername());

        return of(new Credentials(server.getUsername(), decryptPassword(server.getPassword()), getEmail(server), null));
    }

    private String decryptPassword( String password )
            throws MojoExecutionException
    {
        try {
            SecDispatcher secDispatcher = new DefaultSecDispatcher() {{
                _cipher = new DefaultPlexusCipher();
            }};

            return secDispatcher.decrypt(password);

        } catch (SecDispatcherException e) {
            throw new MojoExecutionException("Unable to decrypt password", e);
        } catch (PlexusCipherException e) {
            throw new MojoExecutionException("Unable to initialize password decryption", e);
        }
    }

    /**
     * <pre>
     * <servers>
     *   <server>
     *     <id>docker-private-registry</id>
     *     [...]
     *     <configuration>
     *       <email>foo@bar.com</email>
     *     </configuration>
     *   </server>
     * </servers>
     * </pre>
     */
    private String getEmail(Server server) {
        Xpp3Dom configuration = (Xpp3Dom) server.getConfiguration();
        if (configuration != null) {
            Xpp3Dom emailNode = configuration.getChild("email");
            if (emailNode != null) {
                return emailNode.getValue();
            }
        }
        return null;
    }

    protected Optional<BuiltImageInfo> getBuiltImageForStartId(final String imageId) {
        Map<String, BuiltImageInfo> builtImages = obtainMapFromPluginContext(BUILT_IMAGES_KEY);
        return ofNullable(builtImages.get(imageId));
    }

    protected void registerPluginError(DockerPluginError error) {
        List<DockerPluginError> errors = obtainListFromPluginContext(ERRORS_KEY);
        errors.add(error);
    }

    protected List<DockerPluginError> getPluginErrors() {
        List<DockerPluginError> list = obtainListFromPluginContext(ERRORS_KEY);
        return Collections.unmodifiableList(list);
    }

    protected void enqueueForPushing(final String imageId, final ImageBuildConfiguration imageConfig)
            throws MojoFailureException, MojoExecutionException
    {
        enqueueForPushing(imageId, ofNullable(imageConfig.getNameAndTag()), ofNullable(imageConfig.getRegistry()));
    }

    protected void enqueueForPushing(final String imageId, final Optional<String> nameAndTag, final Optional<String> registry)
            throws MojoFailureException, MojoExecutionException
    {
        if (!registry.isPresent()) {
            enqueueForPushing(imageId, nameAndTag);
            return;
        }

        enqueueForPushingToRegistry(imageId, nameAndTag, registry.get());
    }

    protected void enqueueForPushingToRegistry(final String imageId, final Optional<String> nameAndTag, final String registry)
            throws MojoFailureException, MojoExecutionException
    {
        requireNonNull(nameAndTag.orElse(null), "When pushing to an explicit registry, name-and-tag must be set.");

        // build extended tag by prepending registry to name and tag
        String newNameAndTag = registry + "/" + nameAndTag.get();

        // apply extended tag
        attachTag(imageId, newNameAndTag);

        // now enqueue for pushing
        enqueueForPushing(imageId, of(newNameAndTag));
    }

    protected void enqueueForPushing(final String imageId, final Optional<String> nameAndTag) {
        getLog().info(String.format("Enqueuing image '%s' to be pushed with tag '%s'..", imageId, nameAndTag.orElse("<none>")));

        List<PushableImage> images = obtainListFromPluginContext(PUSHABLE_IMAGES_KEY);
        PushableImage newImage = new PushableImage(imageId, nameAndTag);
        if (!images.contains(newImage)) {
            images.add(newImage);
        }
    }

    protected List<PushableImage> getImagesToPush() {
        List<PushableImage> list = obtainListFromPluginContext(PUSHABLE_IMAGES_KEY);
        return Collections.unmodifiableList(list);
    }

    protected void attachTag(String imageId, String nameAndTag)
            throws MojoFailureException, MojoExecutionException
    {
        try {
            getLog().info(String.format("Tagging image '%s' with tag '%s'..", imageId, nameAndTag));
            getDockerProvider().tagImage(imageId, nameAndTag);
        } catch (DockerException e) {
            String message = String.format("Failed to add tag '%s' to image '%s'", nameAndTag, imageId);
            handleDockerException(message, e);
        }
    }

    /**
     * Common method for re-throwing a {@link DockerException} as a {@link MojoFailureException} with a more specific
     * error message. Extract into a common, template method in this base class to allow pre "verify" Mojos to handle
     * errors differently.
     *
     * @param message The message for the exception
     * @param e       The Docker Exception
     * @throws org.apache.maven.plugin.MojoFailureException to indicate Plugin failure
     */
    protected void handleDockerException(final String message, DockerException e) throws MojoFailureException {
        Optional<String> apiResponse = e.getApiResponse();
        String exceptionMessage = apiResponse.isPresent() ?
                (message + "\nApi response:\n" + apiResponse.get()) :
                message;
        throw new MojoFailureException(exceptionMessage, e);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> obtainListFromPluginContext(String name) {
        Object obj = getPluginContext().get(name);
        if (obj == null) {
            ArrayList<T> list = new ArrayList<>();
            getPluginContext().put(name, list);
            return list;
        } else {
            return (List<T>) obj;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Map<String, T> obtainMapFromPluginContext(String name) {
        Object obj = getPluginContext().get(name);
        if (obj == null) {
            Map<String, T> map = new HashMap<>();
            getPluginContext().put(name, map);
            return map;
        } else {
            return (Map<String, T>) obj;
        }
    }
}
