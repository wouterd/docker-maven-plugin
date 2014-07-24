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

public class ImageNotFoundException extends DockerException {

    public ImageNotFoundException(String imageName) {
        super(makeMessage(imageName));
    }

    public ImageNotFoundException(String imageName, Throwable cause) {
        super(makeMessage(imageName), cause);
    }

    private static String makeMessage(String imageName) {
        return String.format("Image '%s' not found.", imageName);
    }

}
