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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.wouterdanes.docker.remoteapi.model.ContainerLink;

/**
 * This class is responsible for holding the start configuration of a docker container<br> See <a
 * href="http://docs.docker.io/reference/api/docker_remote_api_v1.10/#21-containers">
 * http://docs.docker.io/reference/api/docker_remote_api_v1.10/#start-a-container</a>
 */
public class ContainerStartConfiguration {

    public static final int DEFAULT_STARTUP_TIMEOUT = 5 * 60;

    private String image;
    private String id;
    private List<ContainerLink> links;
    private Map<String, String> env;
    
    /**
     * Regular expression to look for that indicates the container has started up
     */
    private String waitForStartup;

    /**
     * The maximum time to wait for this container to start (seconds), default is 30 sec.
     */
    private int startupTimeout;

    /**
     * Hostname to give to this container
     */
    private String hostname;

    /**
     * Supply an optional mac address for the container
     */
    private String macAddress;

    /**
     * Set the image name or id to use and returns the object so you can chain from/with statements.
     *
     * @param image the image name or id
     * @return this object
     */
    public ContainerStartConfiguration fromImage(String image) {
        this.image = image;
        return this;
    }

    public ContainerStartConfiguration withId(String id) {
        this.id = id;
        return this;
    }

    public ContainerStartConfiguration withLinks(ContainerLink... links) {
        if (this.links == null) {
            this.links = new ArrayList<>(links.length);
        }
        Collections.addAll(this.links, links);
        return this;
    }

    public ContainerStartConfiguration withLink(ContainerLink link) {
        return withLinks(link);
    }

    public ContainerStartConfiguration waitForStartup(String pattern) {
        this.waitForStartup = pattern;
        return this;
    }

    public ContainerStartConfiguration withStartupTimeout(int timeout) {
        this.startupTimeout = timeout;
        return this;
    }

    public ContainerStartConfiguration withEnv(Map<String, String> env) {
    	this.env = env;
    	return this;
    }

    public ContainerStartConfiguration withHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public ContainerStartConfiguration withMacAddress(String macAddress) {
        this.macAddress = macAddress;
        return this;
    }
    
    public String getImage() {
        return image;
    }

    public String getId() {
        return id != null ? id : image;
    }

    public List<ContainerLink> getLinks() {
        return links != null ? Collections.unmodifiableList(links) : Collections.<ContainerLink>emptyList();
    }

    public Map<String, String> getEnv() {
    	return env != null ? Collections.unmodifiableMap(env) : Collections.<String, String>emptyMap();
    }

    public String getHostname() {
        return hostname;
    }

    public String getMacAddress() {
        return macAddress;
    }
    
    public String getWaitForStartup() {
        return waitForStartup;
    }

    public int getStartupTimeout() {
        return startupTimeout != 0 ? startupTimeout : DEFAULT_STARTUP_TIMEOUT;
    }
}
