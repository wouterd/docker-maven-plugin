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

import net.wouterdanes.docker.provider.model.ContainerStartConfiguration;
import net.wouterdanes.docker.remoteapi.exception.DockerException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import javax.inject.Inject;
import java.util.List;

/**
 * This class is responsible for starting docking containers in the pre-integration phase of the maven build. The goal
 * is called "start-containers"
 */
@Mojo(defaultPhase = LifecyclePhase.INTEGRATION_TEST, name = "run", threadSafe = true, requiresDirectInvocation = true)
public class RunContainersMojo extends StartContainerMojo {

    private static final int SLEEP = 500;

    @Inject
    public RunContainersMojo(List<ContainerStartConfiguration> containers) {
        super(containers);
    }

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanUpStartedContainers));

        super.doExecute();

        getLog().info("Press Ctrl-C to stop the container...");
        waitForUserInteraction();
    }

    private void waitForUserInteraction() {
        while (true) {
            try {
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                throw new DockerException("Aborting container wait.", e);
            }
        }
    }

    @Override
    protected String getMojoGoalName() {
        return "run";
    }
}