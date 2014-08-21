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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import net.wouterdanes.docker.provider.DockerProvider;
import net.wouterdanes.docker.provider.DockerProviderSupplier;
import net.wouterdanes.docker.provider.model.BuiltImageInfo;
import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;
import net.wouterdanes.docker.provider.model.PushableImage;
import net.wouterdanes.docker.remoteapi.exception.DockerException;
import net.wouterdanes.docker.remoteapi.model.ContainerInspectionResult;
import net.wouterdanes.docker.remoteapi.model.Credentials;

/**
 * Base class for all Mojos with shared functionality
 */
public abstract class AbstractDockerMojo extends AbstractMojo {

    private static final String STARTED_CONTAINERS_KEY = "startedContainers";
    private static final String BUILT_IMAGES_KEY = "builtImages";
    private static final String PUSHABLE_IMAGES_KEY = "pushableImages";
    private static final String ERRORS_KEY = "errors";

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
        List<StartedContainerInfo> startedContainers = obtainListFromPluginContext(STARTED_CONTAINERS_KEY);
        startedContainers.add(info);
    }

    protected List<StartedContainerInfo> getStartedContainers() {
        return obtainListFromPluginContext(STARTED_CONTAINERS_KEY);
    }

    protected void registerBuiltImage(String imageId, ImageBuildConfiguration imageConfig) throws MojoFailureException {
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

    protected DockerProvider getDockerProvider() {
        DockerProvider provider = new DockerProviderSupplier(providerName).get();
        provider.setCredentials(getCredentials());
        return provider;
    }

    protected Credentials getCredentials() {
        if (Strings.isNullOrEmpty(userName)) {
            getLog().info("No user name provided");
            return null;
        }

        getLog().info("Using credentials: " + userName);
        return new Credentials(userName, password, email, null);
    }

    protected Optional<BuiltImageInfo> getBuiltImageForStartId(final String imageId) {
        Map<String, BuiltImageInfo> builtImages = obtainMapFromPluginContext(BUILT_IMAGES_KEY);
        return Optional.fromNullable(builtImages.get(imageId));
    }

    protected void registerPluginError(DockerPluginError error) {
        List<DockerPluginError> errors = obtainListFromPluginContext(ERRORS_KEY);
        errors.add(error);
    }

    protected List<DockerPluginError> getPluginErrors() {
        List<DockerPluginError> list = obtainListFromPluginContext(ERRORS_KEY);
        return Collections.unmodifiableList(list);
    }

    protected void enqueueForPushing(final String imageId, final ImageBuildConfiguration imageConfig) throws MojoFailureException {
       enqueueForPushing(imageId,
               Optional.fromNullable(imageConfig.getNameAndTag()),
               Optional.fromNullable(imageConfig.getRegistry()));
    }

    protected void enqueueForPushing(final String imageId, final Optional<String> nameAndTag, final Optional<String> registry) throws MojoFailureException {
        if (!registry.isPresent()) {
           enqueueForPushing(imageId, nameAndTag);
           return;
        }

        enqueueForPushingToRegistry(imageId, nameAndTag, registry.get());
    }

    protected void enqueueForPushingToRegistry(final String imageId, final Optional<String> nameAndTag, final String registry) throws MojoFailureException {
        Preconditions.checkArgument(nameAndTag.isPresent(), "When pushing to an explicit registry, name-and-tag must be set.");

        // build extended tag by prepending registry to name and tag
        String newNameAndTag = registry + "/" + nameAndTag.get();

        // apply extended tag
        attachTag(imageId, newNameAndTag);

        // now enqueue for pushing
        enqueueForPushing(imageId, Optional.fromNullable(newNameAndTag));
    }

    protected void enqueueForPushing(final String imageId, final Optional<String> nameAndTag) {
        getLog().info(String.format("Enqueuing image '%s' to be pushed with tag '%s'..", imageId, nameAndTag.or("<none>")));

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

    protected void attachTag(String imageId, String nameAndTag) throws MojoFailureException {
        try {
            getLog().info(String.format("Tagging image '%s' with tag '%s'..", imageId, nameAndTag));
            getDockerProvider().tagImage(imageId, nameAndTag);
        } catch (DockerException e) {
            String message = String.format("Failed to add tag '%s' to image '%s'", imageId, nameAndTag);
            handleDockerException(message, e);
        }
    }

    /**
     * Common method for re-throwing a {@link DockerException} as a {@link MojoFailureException}
     * with a more specific error message. Extract into a common, template method in this base class
     * to allow pre "verify" Mojos to handle errors differently.
     */
    protected void handleDockerException(String message, DockerException e) throws MojoFailureException {
        Optional<String> apiResponse = e.getApiResponse();
        if (apiResponse.isPresent()) {
            message += "\nApi response:\n%s" + apiResponse.get();
        }
        throw new MojoFailureException(message, e);
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
