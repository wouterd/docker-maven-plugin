package net.wouterdanes.docker.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * This {@link org.apache.maven.plugin.Mojo} checks for any errors in the Docker Maven Plugin during the previous build
 * phases and fails the build if any errors have happened.
 */
@Mojo(name = "verify", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true,
        instantiationStrategy = InstantiationStrategy.PER_LOOKUP)
public class VerifyMojo extends AbstractDockerMojo {
    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {

        if (!getPluginErrors().isEmpty()) {
            throw new MojoFailureException("Errors occurred, stopping the build");
        }

    }
}
