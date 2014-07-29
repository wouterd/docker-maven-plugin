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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class VerifyMojoTest {

    private VerifyMojo mojo;
    private Map<String, Object> fakePluginContext;

    @Before
    public void setUp() throws Exception {

        mojo = new VerifyMojo();
        fakePluginContext = new HashMap<>();
        mojo.setPluginContext(fakePluginContext);

    }

    @Test(expected = MojoFailureException.class)
    public void testThatBuildFailsAndLogsAnErrorWhenErrorsHaveHappened() throws Exception {

        DockerPluginError error = new DockerPluginError("some-goal", "Something went wrong", null);
        fakePluginContext.put("errors", Collections.singletonList(error));
        Log fakeLog = Mockito.mock(Log.class);
        mojo.setLog(fakeLog);

        mojo.execute();

        Mockito.verify(fakeLog, Mockito.atLeastOnce()).error(Matchers.anyString());
    }

    @Test
    public void testThatBuildDoesNotFailWhenNoErrorsHaveHappened() throws Exception {

        mojo.execute();

    }
}