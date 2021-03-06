import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpClientBugs {

    private static CloseableHttpClient client = HttpClients.createDefault();

    public void forgotToResetGetInTryWithResources() throws URISyntaxException {
        HttpGet httpGet = new HttpGet(new URI("http://www.example.com"));
        try
        {
            try (CloseableHttpResponse response = client.execute(httpGet);)
            {
                System.out.println("response: " + response);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally {
            httpGet.releaseConnection();
        }
    }

    public void forgotToResetGetInTry() throws URISyntaxException {
        HttpGet httpGet = new HttpGet(new URI("http://www.example.com"));
        CloseableHttpResponse response = null;
        try
        {
            response = client.execute(httpGet);
            System.out.println("response: " + response);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        httpGet.releaseConnection();
        if (response != null) {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void forgotToResetGetInTryWithResourcesWithFinally() throws URISyntaxException {
        HttpGet httpGet = new HttpGet(new URI("http://www.example.com"));
        try
        {
            try (CloseableHttpResponse response = client.execute(httpGet);)
            {
                System.out.println("response: " + response);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally {
            httpGet.releaseConnection();
            System.out.println("Something");
        }
    }

    public void forgotToResetGetInTryWithFinally() throws URISyntaxException {
        HttpGet httpGet = new HttpGet(new URI("http://www.example.com"));
        CloseableHttpResponse response = null;
        try
        {
            response = client.execute(httpGet);
            System.out.println("response: " + response);
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        httpGet.releaseConnection();
        
        try {
            System.out.println(response); // decoy try/finally
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }
    
    public void tooComplexToFix() throws URISyntaxException, IOException {
        HttpGet httpGet = new HttpGet(new URI("http://www.example.com"));
        CloseableHttpResponse response = client.execute(httpGet);
        System.out.println("response: " + response);
    }
}
