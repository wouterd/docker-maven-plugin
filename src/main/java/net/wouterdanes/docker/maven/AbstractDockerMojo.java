package net.wouterdanes.docker.maven;

import java.util.ArrayList;
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

    private static final String STARTED_CONTAINERS_KEY = "startedContainers";
    private static final String BUILT_IMAGES_KEY = "builtImages";

    @Parameter(defaultValue = "remote", property = "docker.provider", required = true)
    private String providerName;

    public void setProviderName(final String providerName) {
        this.providerName = providerName;
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        doExecute();
    }

    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

    protected void registerStartedContainer(String id) {
        List<String> startedContainers = obtainListFromPluginContext(STARTED_CONTAINERS_KEY);
        startedContainers.add(id);
    }

    protected List<String> getStartedContainers() {
        return obtainListFromPluginContext(STARTED_CONTAINERS_KEY);
    }

    protected void registerBuiltImage(String startId, String imageId) {
        List<BuiltImageInfo> builtImages = obtainListFromPluginContext(BUILT_IMAGES_KEY);
        builtImages.add(new BuiltImageInfo(startId, imageId));
    }

    protected List<BuiltImageInfo> getBuiltImages() {
        return obtainListFromPluginContext(BUILT_IMAGES_KEY);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> obtainListFromPluginContext(String name) {
        Object obj = getPluginContext().get(name);
        if (obj == null) {
            ArrayList<T> list = new ArrayList<>();
            getPluginContext().put(name, list);
            return list;
        } else {
            return (List<T>) obj;
        }
    }

    protected DockerProvider getDockerProvider() {
        return new DockerProviderSupplier(providerName).get();
    }
}
