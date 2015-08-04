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

package net.wouterdanes.docker.remoteapi.exception;

import java.util.Optional;

public class DockerException extends RuntimeException {

    private Optional<String> apiResponse = Optional.empty();

    public DockerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DockerException(final Throwable e) {
        super("Docker internal error occurred", e);
    }

    public DockerException(final String message) {
        super(message);
    }

    public DockerException(final String message, final String apiResponse) {
        super(message);
        this.apiResponse = Optional.of(apiResponse);
    }

    public Optional<String> getApiResponse() {
        return apiResponse;
    }
}
