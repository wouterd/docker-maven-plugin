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

package net.wouterdanes.docker.provider.model;

import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;

/**
 * This class is responsible for holding the configuration of a single docker image to be built by the
 * {@link net.wouterdanes.docker.maven.BuildImageMojo}
 */
public class ImageBuildConfiguration {

    @Parameter(required = true)
    private File dockerFile;

    @Parameter(required = true)
    private String id;

    @Parameter
    private String nameAndTag;

    @Parameter(defaultValue = "false")
    private boolean keep;

    @Parameter(defaultValue = "false")
    private boolean push;

    @Parameter
    private String registry;

	@Parameter
	private List<Artifact> artifacts;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getNameAndTag() {
        return nameAndTag;
    }

    public void setNameAndTag(final String nameAndTag) {
        this.nameAndTag = nameAndTag;
    }

    public boolean isKeep() {
        return keep;
    }

    public void setKeep(final boolean keep) {
        this.keep = keep;
    }

    public boolean isPush() {
        return push;
    }

    public void setPush(boolean push) {
        this.push = push;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

	public List<Artifact> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<Artifact> artifacts) {
		this.artifacts = artifacts;
	}

	public File getDockerFile() {
		return dockerFile;
	}

	public void setDockerFile(File dockerFile) {
		this.dockerFile = dockerFile;
	}
}
