/*
    Copyright 2014 Wouter Danes, Lachlan Coote

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


import net.wouterdanes.docker.provider.model.PushableImage;
import net.wouterdanes.docker.remoteapi.exception.DockerException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.util.Iterator;

/**
 * This class is responsible for pushing docking images in the deploy phase of the maven build. The goal
 * is called "push-images"
 */
@Mojo(defaultPhase = LifecyclePhase.DEPLOY, name = "push-images", threadSafe = true,
        instantiationStrategy = InstantiationStrategy.PER_LOOKUP)
public class PushImageMojo extends AbstractDockerMojo {

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        ensureThatAllPushableImagesHaveAName();
        for (PushableImage image : getImagesToPush()) {
            getLog().info(String.format("Pushing image '%s' with tag '%s'",
                    image.getImageId(), image.getNameAndTag().orElse("<Unspecified>")));
            try {
                getDockerProvider().pushImage(image.getNameAndTag().get());
            } catch (DockerException e) {
                String message = String.format("Cannot push image '%s' with tag '%s'",
                        image.getImageId(), image.getNameAndTag().orElse("<Unspecified>"));
                handleDockerException(message, e);
            }
        }

        for (String imageID : imagesToDeleteAfterPush) {
            try {
                getDockerProvider().removeImage(imageID);
            } catch (DockerException e) {
                getLog().error("Failed to remove image", e);
            }
        }
    }

    private void ensureThatAllPushableImagesHaveAName() throws MojoFailureException {
        Iterator<PushableImage> imagesWithoutNameAndTag = getImagesToPush().parallelStream()
                .filter(image -> !image.getNameAndTag().isPresent()).iterator();

        if (imagesWithoutNameAndTag.hasNext()) {
            imagesWithoutNameAndTag.forEachRemaining(image -> {
                String message = String.format("Image '%s' needs to be pushed but doesn't have a name.", image.getImageId());
                getLog().error(message);
            });
            throw new MojoFailureException("There are images that need to be pushed without a name.");
        }
    }
}