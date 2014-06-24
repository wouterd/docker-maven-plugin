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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import net.wouterdanes.docker.provider.DockerProvider;
import net.wouterdanes.docker.provider.DockerProviderSupplier;
import net.wouterdanes.docker.provider.model.BuiltImageInfo;

/**
 * Base class for all Mojos with shared functionality
 */
public abstract class AbstractDockerMojo extends AbstractMojo {

    private static final String STARTED_CONTAINERS_KEY = "startedContainers";
    private static final String BUILT_IMAGES_KEY = "builtImages";

    @Parameter(defaultValue = "remote", property = "docker.provider", required = true)
    private String providerName;

    @Parameter(defaultValue = "false", property = "docker.skip", required = false)
    private boolean skip;

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

    protected void registerStartedContainer(String id) {
        List<String> startedContainers = obtainListFromPluginContext(STARTED_CONTAINERS_KEY);
        startedContainers.add(id);
    }

    protected List<String> getStartedContainers() {
        return obtainListFromPluginContext(STARTED_CONTAINERS_KEY);
    }

    protected void registerBuiltImage(String startId, String imageId, boolean keep) {
        Map<String, BuiltImageInfo> builtImages = obtainMapFromPluginContext(BUILT_IMAGES_KEY);
        builtImages.put(startId, new BuiltImageInfo(startId, imageId, keep));
    }

    protected Collection<BuiltImageInfo> getBuiltImages() {
        Map<String, BuiltImageInfo> builtImagesMap = obtainMapFromPluginContext(BUILT_IMAGES_KEY);
        return builtImagesMap.values();
    }

    protected DockerProvider getDockerProvider() {
        return new DockerProviderSupplier(providerName).get();
    }

    protected Optional<BuiltImageInfo> getBuiltImageForStartId(final String imageId) {
        Map<String, BuiltImageInfo> builtImages = obtainMapFromPluginContext(BUILT_IMAGES_KEY);
        return Optional.fromNullable(builtImages.get(imageId));
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
