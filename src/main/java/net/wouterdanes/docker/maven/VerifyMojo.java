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

            Set<String> erroneousGoals = new HashSet<>();
            for (DockerPluginError error : errors) {
                erroneousGoals.add(error.getPluginGoal());
            }
            StringBuilder sb = new StringBuilder("The following plugin goals had errors: \n");
            for (String goal : erroneousGoals) {
                sb.append(" - ").append(goal).append('\n');
            }
            getLog().error(sb);

            throw new MojoFailureException("Errors occurred, stopping the build");
        }

    }
}
