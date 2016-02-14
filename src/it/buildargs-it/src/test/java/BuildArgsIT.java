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

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class BuildArgsIT {

    @Test
    public void buildArgsContainerRunsCorrectly() throws Exception {
        String baseUrl = System.getProperty("app.base.url");
        HttpGet get = new HttpGet(baseUrl);
        CloseableHttpClient httpClient = HttpClients.createDefault();

        try (CloseableHttpResponse response = httpClient.execute(get)) {
            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
        }
    }
}
