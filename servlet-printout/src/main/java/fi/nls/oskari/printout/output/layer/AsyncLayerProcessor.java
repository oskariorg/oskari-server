package fi.nls.oskari.printout.output.layer;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

/**
 * This class is used to process map tile requests.
 *
 * Async http client is used to hopefully serve client with improved response
 * times.
 *
 * Note : performance has not been optimized or measured atm.
 *
 */
public class AsyncLayerProcessor {
    protected static Log log = LogFactory.getLog(AsyncLayerProcessor.class);
    CloseableHttpAsyncClient httpclient;

    public AsyncLayerProcessor() {

        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(3000).setConnectTimeout(3000).build();
        httpclient = HttpAsyncClients.custom()
                .setDefaultRequestConfig(requestConfig).build();
    }

    public void execute(HttpGet arg0, FutureCallback<HttpResponse> arg1) {
        httpclient.execute(arg0, arg1);
    }

    public void shutdown() throws InterruptedException, IOException {
        httpclient.close();
        httpclient = null;
    }

    public void start() {
        httpclient.start();
    }
}