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

package net.wouterdanes.docker.provider;

import net.wouterdanes.docker.provider.model.*;
import net.wouterdanes.docker.remoteapi.BaseService;
import net.wouterdanes.docker.remoteapi.ContainersService;
import net.wouterdanes.docker.remoteapi.ImagesService;
import net.wouterdanes.docker.remoteapi.MiscService;
import net.wouterdanes.docker.remoteapi.exception.ImageNotFoundException;
import net.wouterdanes.docker.remoteapi.exception.MavenArtifactNotFoundException;
import net.wouterdanes.docker.remoteapi.model.ContainerCreateRequest;
import net.wouterdanes.docker.remoteapi.model.ContainerInspectionResult;
import net.wouterdanes.docker.remoteapi.model.ContainerStartRequest;
import net.wouterdanes.docker.remoteapi.model.Credentials;
import net.wouterdanes.docker.remoteapi.util.DockerHostFromEnvironmentSupplier;
import net.wouterdanes.docker.remoteapi.util.DockerHostFromPropertySupplier;
import net.wouterdanes.docker.remoteapi.util.DockerPortFromEnvironmentSupplier;
import net.wouterdanes.docker.remoteapi.util.DockerPortFromPropertySupplier;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import java.io.*;
import java.util.*;

public abstract class RemoteApiBasedDockerProvider implements DockerProvider {

    private final String host;
    private final int port;

    private final ContainersService containersService;
    private final ImagesService imagesService;
    private final MiscService miscService;

    private final Set<BaseService> services;

    private RepositorySystem repositorySystem;
    private RepositorySystemSession repositorySystemSession;
    private List<RemoteRepository> remoteRepositories;
    private Log log;

    private static final int DEFAULT_DOCKER_PORT = 2375;
    private static final String DEFAULT_DOCKER_HOST = "127.0.0.1";
    public static final String DOCKER_HOST_SYSTEM_ENV = "DOCKER_HOST";
    public static final String DOCKER_HOST_PROPERTY = "docker.host";
    public static final String DOCKER_PORT_PROPERTY = "docker.port";

    public static final String TCP_PROTOCOL = "tcp";

    public RemoteApiBasedDockerProvider() {
        this(getDockerHostFromEnvironment(), getDockerPortFromEnvironment());
    }

    @Override
    public void setCredentials(Credentials credentials) {
        for (BaseService service : services) {
            service.setCredentials(credentials);
        }
    }

    @Override
    public void stopContainer(final String containerId) {
        getContainersService().stopContainer(containerId);
    }

    @Override
    public void deleteContainer(final String containerId) {
        getContainersService().deleteContainer(containerId);
    }

    @Override
    public String buildImage(final ImageBuildConfiguration image) {
        byte[] bytes = getTgzArchiveForFiles(image);
        return miscService.buildImage(bytes, Optional.ofNullable(image.getNameAndTag()), Optional.ofNullable(image.getBuildArguments()));
    }

    @Override
    public String commitContainer(final ContainerCommitConfiguration configuration) {
        return miscService.commitContainer(
                configuration.getId(),
                Optional.ofNullable(configuration.getRepo()),
                Optional.ofNullable(configuration.getTag()),
                Optional.ofNullable(configuration.getComment()),
                Optional.ofNullable(configuration.getAuthor())
        );
    }

    @Override
    public void removeImage(final String imageId) {
        getImagesService().deleteImage(imageId);
    }

    @Override
    public void pushImage(final String nameAndTag) {
        getImagesService().pushImage(nameAndTag);
    }

    @Override
    public void tagImage(final String imageId, final String nameAndTag) {
        getImagesService().tagImage(imageId, nameAndTag);
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public String getLogs(final String containerId) {
        return containersService.getLogs(containerId);
    }

    @Override
    public void setLogger(final Log logger) {
        this.log = logger;
    }

    @Override
    public void setRepositorySystem(RepositorySystem repositorySystem) {
        this.repositorySystem = repositorySystem;
    }

    @Override
    public void setRepositorySystemSession(RepositorySystemSession repositorySystemSession) {
        this.repositorySystemSession = repositorySystemSession;
    }

    @Override
    public void setRemoteRepositories(List<RemoteRepository> remoteRepositories) {
        this.remoteRepositories = remoteRepositories;
    }

    protected RemoteApiBasedDockerProvider(final String host, final int port) {
        this.host = host;
        this.port = port;
        String dockerApiRoot = String.format("%s:%s", host, port);
        containersService = new ContainersService(dockerApiRoot);
        imagesService = new ImagesService(dockerApiRoot);
        miscService = new MiscService(dockerApiRoot);
        services = new HashSet<>();
        register(containersService, imagesService, miscService);
    }

    protected ContainerInspectionResult startContainer(ContainerStartConfiguration configuration,
                                                       ContainerStartRequest startRequest) {
        String imageId = configuration.getImage();
        ContainerCreateRequest createRequest = new ContainerCreateRequest()
                .fromImage(imageId)
                .withEnv(configuration.getEnv())
                .withHostname(configuration.getHostname())
                .withMacAddress(configuration.getMacAddress());

        String containerId;
        try {
            containerId = containersService.createContainer(createRequest);
        } catch (ImageNotFoundException e) {
            log.info(String.format("Pulling image %s...", imageId));
            imagesService.pullImage(imageId);
            containerId = containersService.createContainer(createRequest);
        }

        containersService.startContainer(containerId, startRequest);

        return containersService.inspectContainer(containerId);
    }

    protected ContainersService getContainersService() {
        return containersService;
    }

    protected ImagesService getImagesService() {
        return imagesService;
    }

    protected void register(BaseService... servicesToBeRegistered) {
        Collections.addAll(services, servicesToBeRegistered);
    }

    protected String getHost() {
        return host;
    }

    protected int getPort() {
        return port;
    }

    private byte[] getTgzArchiveForFiles(final ImageBuildConfiguration image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (
            TarArchiveOutputStream tar = new TarArchiveOutputStream(baos)
        ) {
            tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            addToTar(tar, image.getDockerFile(), "Dockerfile");

            if (image.getArtifacts() != null) {
                for (Artifact artifact : image.getArtifacts()) {
                    File file = artifact.getFile();
                    String pathInTar = artifact.getDest().orElse(file.getName());
                    addToTar(tar, file, pathInTar);
                }
            }

            if (image.getMavenArtifacts() != null) {
                for (MavenArtifact mavenArtifact : image.getMavenArtifacts()) {
                    ArtifactRequest request = new ArtifactRequest();
                    request.setArtifact(new DefaultArtifact(mavenArtifact.getDependency()));
                    request.setRepositories(remoteRepositories);

                    log.debug("Resolving artifact " + mavenArtifact.getDependency() + " from " + remoteRepositories);

                    ArtifactResult result;

                    try {
                        result = repositorySystem.resolveArtifact(repositorySystemSession, request);
                    } catch (ArtifactResolutionException e) {
                        throw new MavenArtifactNotFoundException(e.getMessage(), e);
                    }

                    log.debug("Resolved artifact " + mavenArtifact.getDependency() + " to " + result.getArtifact().getFile() + " from " + result.getRepository());

                    String pathInTar = mavenArtifact.getDest().orElse(result.getArtifact().getFile().getName());
                    addToTar(tar, result.getArtifact().getFile(), pathInTar);
                }
            }

            tar.flush();
            baos.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create output archive", e);
        }
        return baos.toByteArray();
    }

    private static void addToTar(ArchiveOutputStream tar, File file, String fileNameAndPath) throws IOException {
        if (!file.exists() || !file.canRead()) {
            throw new FileNotFoundException(String.format("Cannot read file %s. Are you sure it exists?",
                    file.getAbsolutePath()));
        }
        if (file.isDirectory()) {
            for (File fileInDirectory : file.listFiles()) {
                if (!fileNameAndPath.endsWith("/")) {
                    fileNameAndPath = fileNameAndPath + "/";
                }
                addToTar(tar, fileInDirectory, fileNameAndPath + fileInDirectory.getName());
            }
        } else {
            ArchiveEntry entry = tar.createArchiveEntry(file, fileNameAndPath);
            tar.putArchiveEntry(entry);
            try (FileInputStream fis = new FileInputStream(file)) {
                IOUtils.copy(fis, tar);
            }
            tar.closeArchiveEntry();
        }
    }

    private static Integer getDockerPortFromEnvironment() {
        return DockerPortFromPropertySupplier.INSTANCE.get()
                .orElse(DockerPortFromEnvironmentSupplier.INSTANCE.get()
                        .orElse(DEFAULT_DOCKER_PORT)
                );
    }

    private static String getDockerHostFromEnvironment() {
        return DockerHostFromPropertySupplier.INSTANCE.get()
                .orElse(DockerHostFromEnvironmentSupplier.INSTANCE.get()
                        .orElse(DEFAULT_DOCKER_HOST)
                );
    }
}
