package net.wouterdanes.docker.maven;

import java.util.List;

import com.google.common.base.Optional;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

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
        validateAllImages();

        for (ImageBuildConfiguration image : images) {
            try {
                getLog().info(String.format("Building image '%s'..", image.getId()));
                String imageId = getDockerProvider().buildImage(image);
                getLog().info(String.format("Image '%s' has Id '%s'", image.getId(), imageId));
                registerBuiltImage(image.getId(), imageId);
            } catch (DockerException e) {
                getLog().error(String.format("Cannot build image '%s'", image.getId()), e);
                Optional<String> apiResponse = e.getApiResponse();
                if (apiResponse.isPresent()) {
                    getLog().info(String.format("Api response:\n%s", apiResponse.get()));
                }
            }
        }
    }

    private void validateAllImages() throws MojoExecutionException {
        for (ImageBuildConfiguration image : images) {
            if (!image.isValid()) {
                throw new MojoExecutionException(String.format("Image '%s' not valid, did you specify a Dockerfile?",
                        image.getId()));
            }
        }
    }
}
