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


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import net.wouterdanes.docker.provider.model.PushableImage;
import net.wouterdanes.docker.remoteapi.exception.DockerException;

/**
 * This class is responsible for pushing docking images in the deploy phase of the maven build. The goal
 * is called "push-images"
 */
@Mojo(defaultPhase = LifecyclePhase.DEPLOY, name = "push-images", threadSafe = true,
		instantiationStrategy = InstantiationStrategy.PER_LOOKUP)
public class PushImageMojo extends AbstractDockerMojo {

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        for (PushableImage image : getImagesToPush()) {
            getLog().info(String.format("Pushing image %s to registry %s..",
                    image.getImageId(), image.getRegistry().or("<Default>")));
            try {
                getDockerProvider().pushImage(image.getImageId(), image.getRegistry());
            } catch (DockerException e) {
                String message = String.format("Cannot push image '%s' to registry '%s'",
                        image.getImageId(), image.getRegistry().or("<Default>"));
                throw new MojoFailureException(message, e);
            }
        }
    }

}