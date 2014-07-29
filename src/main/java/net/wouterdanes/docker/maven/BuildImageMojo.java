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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import net.wouterdanes.docker.provider.model.ImageBuildConfiguration;
import net.wouterdanes.docker.remoteapi.exception.DockerException;

/**
 * This class is responsible for building docker images specified in the POM file. It runs by default during the
 * package phase of a maven project.
 */
@Mojo(name = "build-images", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true,
        instantiationStrategy = InstantiationStrategy.PER_LOOKUP)
public class BuildImageMojo extends AbstractDockerMojo {

    @Parameter(required = true)
    private List<ImageBuildConfiguration> images;

    public void setImages(final List<ImageBuildConfiguration> images) {
        this.images = images;
    }

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        if (images == null || images.isEmpty()) {
            getLog().warn("No images to build specified.");
            return;
        }

        validateAllImages();

        for (ImageBuildConfiguration image : images) {
            try {
                logImageConfig(image);
                String imageId = getDockerProvider().buildImage(image);
                getLog().info(String.format("Image '%s' has Id '%s'", image.getId(), imageId));
                registerBuiltImage(imageId, image);
            } catch (DockerException e) {
                String errorMessage = String.format("Cannot build image '%s'", image.getId());
                getLog().error(errorMessage, e);
                Optional<String> apiResponse = e.getApiResponse();
                if (apiResponse.isPresent()) {
                    getLog().info(String.format("Api response:\n%s", apiResponse.get()));
                }
                registerPluginError(new DockerPluginError("build-images", errorMessage, e));
            }
        }
    }

    private void logImageConfig(final ImageBuildConfiguration image) {
        StringBuilder builder = new StringBuilder(String.format("Building image '%s'", image.getId()));
        if (image.getNameAndTag() != null) {
            builder.append(String.format(", with name and tag '%s'", image.getNameAndTag()));
        }
        builder.append("..");
        getLog().info(builder.toString());
    }

    private void validateAllImages() throws MojoExecutionException {
        Set<String> ids = new HashSet<>(images.size());
        for (ImageBuildConfiguration image : images) {
            if (ids.contains(image.getId())) {
                throw new MojoExecutionException(String.format("Image ID '%s' used twice, Image IDs must be unique!",
                        image.getId()));
            }
            ids.add(image.getId());
            if (!image.isValid()) {
                throw new MojoExecutionException(String.format("Image '%s' not valid, did you specify a Dockerfile?",
                        image.getId()));
            }
        }
    }

}
