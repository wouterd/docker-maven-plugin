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

package net.wouterdanes.docker.remoteapi;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.BaseEncoding;

import net.wouterdanes.docker.remoteapi.model.Credentials;

public class CredentialsTest {

    private static final String USERNAME = "jim";
    private static final String PASSWORD = "123456";
    private static final String EMAIL = "jim_rulz87@hotmail.com";
    private static final String SERVERNAME = "http://myspace.com/jim.the.impaler";

    private BaseService miscService;

    @Before
    public void setUp() {
        miscService = new BaseService("don't care", "doesn't matter") {};
    }

    @Test
    public void testAuthHeaderWithServerName() {
        Credentials credsWithServer = new Credentials(USERNAME, PASSWORD, EMAIL, SERVERNAME);
        miscService.setCredentials(credsWithServer);

        String encodedResult = miscService.getRegistryAuthHeaderValue();
        DecodedAuthHeader decodedResult = decodeAuthHeader(encodedResult);

        assertEquals(USERNAME, decodedResult.getUserName());
        assertEquals(PASSWORD, decodedResult.getPassword());
        assertEquals(EMAIL, decodedResult.getEmail());
        assertEquals(SERVERNAME, decodedResult.getServerAddr());
    }

    @Test
    public void testAuthHeaderWithoutServerName() {
        Credentials credsWithoutServer = new Credentials(USERNAME, PASSWORD, EMAIL, null);
        miscService.setCredentials(credsWithoutServer);

        String encodedResult = miscService.getRegistryAuthHeaderValue();
        DecodedAuthHeader decodedResult = decodeAuthHeader(encodedResult);

        assertEquals(USERNAME, decodedResult.getUserName());
        assertEquals(PASSWORD, decodedResult.getPassword());
        assertEquals(EMAIL, decodedResult.getEmail());
        assertEquals(Credentials.DEFAULT_SERVER_NAME, decodedResult.getServerAddr());
    }

    @Test
    public void testAuthHeaderWithoutCredentials() {
        String encodedResult = miscService.getRegistryAuthHeaderValue();
        assertEquals("null", encodedResult);
    }

    protected DecodedAuthHeader decodeAuthHeader(String encodedValue) {
        try {
            byte[] jsonBytes = BaseEncoding.base64().decode(encodedValue);
            JsonNode node = new ObjectMapper().readTree(jsonBytes);
            return new DecodedAuthHeader(node);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static class DecodedAuthHeader {

        private final JsonNode node;

        public DecodedAuthHeader(JsonNode node) {
            this.node = node;
        }

        public String getUserName() {
            return getTextField("username");
        }

        public String getPassword() {
            return getTextField("password");
        }

        public String getEmail() {
            return getTextField("email");
        }

        public String getServerAddr() {
            return getTextField("serveraddress");
        }

        private String getTextField(String fieldName) {
            return node.get(fieldName).getTextValue();
        }

    }

}
