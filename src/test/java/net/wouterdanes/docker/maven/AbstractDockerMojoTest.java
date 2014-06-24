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