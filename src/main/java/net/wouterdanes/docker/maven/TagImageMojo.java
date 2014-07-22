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

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.common.base.Optional;

import net.wouterdanes.docker.provider.model.BuiltImageInfo;
import net.wouterdanes.docker.provider.model.ImageTagConfiguration;
import net.wouterdanes.docker.remoteapi.exception.DockerException;

/**
 * This class is responsible for tagging docking images in the deploy phase of the maven build. The goal
 * is called "tag-images"
 */
@Mojo(defaultPhase = LifecyclePhase.DEPLOY, name = "tag-images", threadSafe = true,
		instantiationStrategy = InstantiationStrategy.PER_LOOKUP)
public class TagImageMojo extends AbstractDockerMojo {

    @Parameter(required = true)
    private List<ImageTagConfiguration> images;

    public void setImages(final List<ImageTagConfiguration> images) {
        this.images = images;
    }

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        for (ImageTagConfiguration config : images) {
            applyTagsToImage(config);
        }
    }

    private void applyTagsToImage(ImageTagConfiguration config) {
        String startId = config.getId();
        String imageId = startId;
        boolean push = config.isPush();
        Optional<String> registry = Optional.fromNullable(config.getRegistry());

        Optional<BuiltImageInfo> builtInfo = getBuiltImageForStartId(imageId);
        if (builtInfo.isPresent()) {
            imageId = builtInfo.get().getImageId();
            registry = registry.or(builtInfo.get().getRegistry());
        }

        List<String> tags = config.getTags();
        for (String nameAndTag : tags) {
            applyTagToImage(imageId, nameAndTag, registry, push);
        }
    }

    private void applyTagToImage(String imageId, String nameAndTag, Optional<String> registry, boolean push) {
        try {
            getLog().info(String.format("Tagging image '%s' with tag '%s'..", imageId, nameAndTag));
            getDockerProvider().tagImage(imageId, nameAndTag);
        } catch (DockerException e) {
            getLog().error("Failed to tag image", e);
            return;
        }

        if (push) {
            try {
                getLog().info(String.format("Pushing image %s to registry %s..",
                        imageId, registry.or("<Unspecified>")));
                getDockerProvider().pushImage(nameAndTag, registry);
            } catch (DockerException e) {
                getLog().error("Failed to push image", e);
            }
        }
    }

}