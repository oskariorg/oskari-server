package fi.nls.oskari.printout.output.layer;

import java.net.ProxySelector;

import org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.impl.nio.conn.PoolingClientAsyncConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.params.CoreConnectionPNames;

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
    HttpAsyncClient httpclient;

    public AsyncLayerProcessor() throws IOReactorException {

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
                .getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory
                .getSocketFactory()));

        IOReactorConfig config = new IOReactorConfig();
        config.setIoThreadCount(4);
        config.setSoReuseAddress(true);

        ConnectingIOReactor ioreactor = new DefaultConnectingIOReactor();
        PoolingClientAsyncConnectionManager cm = new PoolingClientAsyncConnectionManager(
                ioreactor);
        cm.setMaxTotal(10000);
        cm.setDefaultMaxPerRoute(4);

        DefaultHttpAsyncClient asyncHttpclient = new DefaultHttpAsyncClient(cm);

        httpclient = asyncHttpclient;

        ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
                schemeRegistry, ProxySelector.getDefault());
        asyncHttpclient.setRoutePlanner(routePlanner);

        httpclient
                .getParams()
                .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 16000)
                .setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 8000)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE,
                        8 * 1024)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true);
    }

    public void execute(HttpUriRequest arg0, FutureCallback<HttpResponse> arg1) {
        httpclient.execute(arg0, arg1);
    }

    public void shutdown() throws InterruptedException {
        httpclient.shutdown();
        httpclient = null;
    }

    public void start() {
        httpclient.start();
    }
}
