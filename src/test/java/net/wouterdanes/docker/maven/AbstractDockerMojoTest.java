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
import org.apache.maven.plugin.logging.Log;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractDockerMojoTest {

    @Test
    public void testThatMojoIsNotExecutedWhenSkipIsSet() throws Exception {
        AbstractDockerMojo mojo = new AbstractDockerMojo() {
            @Override
            protected void doExecute() throws MojoExecutionException, MojoFailureException {
                Assert.fail("doExecute should not be called");
            }
        };

        mojo.setSkip(true);
        mojo.execute();
    }

    @Test
    public void testThatMojoIsExecutedWhenSkipIsNotSet() throws Exception {
        AbstractDockerMojo mojo = Mockito.mock(AbstractDockerMojo.class);
        when(mojo.getLog()).thenReturn(mock(Log.class));

        mojo.execute();

        Mockito.verify(mojo, atLeastOnce()).doExecute();
    }
}