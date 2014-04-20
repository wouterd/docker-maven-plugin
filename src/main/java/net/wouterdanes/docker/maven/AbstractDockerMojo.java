package net.wouterdanes.docker.maven;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import net.wouterdanes.docker.provider.DockerProvider;

/**
 * Base class for all Mojos with shared functionality
 */
public abstract class AbstractDockerMojo extends AbstractMojo {

    @Parameter(defaultValue = "remote", property = "docker.provider", required = true)
    private String providerName;

    public void setProviderName(final String providerName) {
        this.providerName = providerName;
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        doExecute();
    }

    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException ;

    @SuppressWarnings("unchecked")
    protected void registerStartedContainer(String id) {
        Object obj = getPluginContext().get("startedContainers");
        List<String> startedContainers;
        if (obj == null) {
            startedContainers = new ArrayList<>();
            getPluginContext().put("startedContainers", startedContainers);
        } else {
            startedContainers = (List<String>) obj;
        }
        startedContainers.add(id);
    }

    @SuppressWarnings("unchecked")
    protected List<String> getStartedContainers() {
        Object obj = getPluginContext().get("startedContainers");
        if (obj == null) {
            return Collections.emptyList();
        }
        return (List<String>) obj;
    }

    protected DockerProvider getDockerProvider() {
        return new DockerProviderSupplier(providerName).get();
    }
}
