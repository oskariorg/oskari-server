package org.oskari.service.wfs.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Map;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;

public class OskariWFSHttpUtil {

    private static final Logger LOG = LogFactory.getLogger(OskariWFSHttpUtil.class);
    private static final int MAX_REDIRECTS = 5;

    public static HttpURLConnection getConnection(String endPoint,
            String user, String pass, Map<String, String> queryParams) throws IOException {
        return getConnection(endPoint, user, pass, queryParams, Collections.emptyMap());
    }

    public static HttpURLConnection getConnection(String endPoint,
            String user, String pass, Map<String, String> query, Map<String, String> headers) throws IOException {
        String request = IOHelper.constructUrl(endPoint, query);
        HttpURLConnection conn = IOHelper.getConnection(request, user, pass);
        headers.forEach((k, v) -> conn.setRequestProperty(k, v));
        return followRedirect(conn, user, pass, query, headers, MAX_REDIRECTS);
    }

    public static HttpURLConnection followRedirect(HttpURLConnection conn,
            String user, String pass, Map<String, String> queryParams,
            Map<String, String> headers, int redirectLatch) throws IOException {
        final int sc = conn.getResponseCode();
        if (sc == HttpURLConnection.HTTP_MOVED_PERM
                || sc == HttpURLConnection.HTTP_MOVED_TEMP
                || sc == HttpURLConnection.HTTP_SEE_OTHER) {
            if (--redirectLatch == 0) {
                throw new IOException("Too many redirects!");
            }
            String location = conn.getHeaderField("Location");
            int i = location.indexOf('?');
            i = i < 0 ? location.length() : i;
            String newEndPoint = location.substring(0, i);
            LOG.info("Following redirect to", newEndPoint);
            String newRequest = IOHelper.constructUrl(newEndPoint, queryParams);
            HttpURLConnection newConnection = IOHelper.getConnection(newRequest, user, pass);
            headers.forEach((k, v) -> newConnection.setRequestProperty(k, v));
            return followRedirect(newConnection, user, pass, queryParams, headers, redirectLatch);
        } else {
            return conn;
        }
    }

}
