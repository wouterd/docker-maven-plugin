package net.wouterdanes.docker;

import org.junit.Assert;
import org.junit.Test;

import net.wouterdanes.docker.provider.DockerProvider;
import net.wouterdanes.docker.provider.DockerProviderSupplier;
import net.wouterdanes.docker.provider.model.ContainerStartConfiguration;
import net.wouterdanes.docker.remoteapi.model.ContainerInspectionResult;

public class VerifyHostnameSetIT {

    @Test
    public void testThatHostnameIsSet() throws Exception {

        DockerProvider dockerProvider = new DockerProviderSupplier("remote").get();

        ContainerStartConfiguration startConfiguration = new ContainerStartConfiguration()
                .fromImage("busybox")
                .withHostname("hoaxname");


        ContainerInspectionResult result = dockerProvider.startContainer(startConfiguration);

        dockerProvider.stopContainer(result.getId());
        dockerProvider.deleteContainer(result.getId());

        Assert.assertEquals("hoaxname", result.getConfig().getHostname());

    }
}
