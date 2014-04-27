import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class NginxIT {

    @Test
    public void testName() throws Exception {
        String baseUrl = System.getProperty("cache.base.url");
        HttpGet get = new HttpGet(baseUrl);
        CloseableHttpClient httpClient = HttpClients.createDefault();

        try (CloseableHttpResponse response = httpClient.execute(get)) {
            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
        }
    }
}
