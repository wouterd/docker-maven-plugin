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

package net.wouterdanes.docker.remoteapi.model;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class Credentials {

    public static final String DEFAULT_SERVER_NAME = "https://index.docker.io/v1/";

    @JsonProperty("username")
    private final String userName;
    @JsonProperty
    private final String password;
    @JsonProperty
    private final String email;
    @JsonProperty("serveraddress")
    private final String serverAddress;

    public Credentials(String userName, String password, String email, String serverAddress) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userName), "Username was null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(password), "Password was null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(email), "Emails was null or empty");

        this.userName = userName;
        this.password = password;
        this.email = email;
        this.serverAddress = (Strings.isNullOrEmpty(serverAddress) ? DEFAULT_SERVER_NAME  : serverAddress);
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    @Override
    public String toString() {
        return "Credentials [userName=" + userName
                + ", password=********"
                + ", email=" + email
                + ", serverAddress=" + serverAddress
                + "]";
    }

}
