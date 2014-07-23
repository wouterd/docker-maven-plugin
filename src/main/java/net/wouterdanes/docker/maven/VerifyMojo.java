package net.wouterdanes.docker.maven;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        List<DockerPluginError> errors = getPluginErrors();
        if (!errors.isEmpty()) {

            Set<String> erronousGoals = new HashSet<>();
            for (DockerPluginError error : errors) {
                erronousGoals.add(error.getGoal());
            }
            StringBuilder sb = new StringBuilder("The following goals had errors: \n");
            for (String goal : erronousGoals) {
                sb.append(" - ").append(goal).append('\n');
            }
            getLog().error(sb);

            throw new MojoFailureException("Errors occurred, stopping the build");
        }

    }
}
