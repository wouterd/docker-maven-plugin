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

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Supplier;

/**
 * This class creates a docker provider based on the passed name when the get() method is called. Used in for example
 * {@link com.google.common.base.Optional#or(com.google.common.base.Supplier)}
 */
public class DockerProviderSupplier implements Supplier<DockerProvider> {

    private final String providerName;
    private static volatile Map<String, Class<? extends DockerProvider>> providers = new HashMap<>();

    static {
        providers.put("remote", RemoteDockerProvider.class);
        providers.put("local", LocalDockerProvider.class);
    }

    public DockerProviderSupplier(final String providerName) {
        this.providerName = providerName;
    }

    public static void registerProvider(String name, Class<? extends DockerProvider> providerClass) {
        assert providerClass != null;
        providers.put(name, providerClass);
    }

    public static void removeProvider(String name) {
        assert name != null;
        providers.remove(name);
    }

    @Override
    public DockerProvider get() {
        if (providers.containsKey(providerName)) {
            Class<? extends DockerProvider> providerClass = providers.get(providerName);
            try {
                return providerClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("Can't instantiate provider", e);
            }
        }
        throw new IllegalStateException(String.format("No provider known by name '%s'", providerName));
    }
}
