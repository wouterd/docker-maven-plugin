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