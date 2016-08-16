package fi.nls.oskari.spatineo.dao;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;

public class HttpRequester {
    
    private static final Logger log = LogFactory.getLogger(SpatineoMonitoringDao.class);

    public String GET(String url) {
        URL obj = getURL(url);
        Proxy proxy = getProxy();
        HttpURLConnection con = getConnection(obj, proxy, "GET");

        int responseCode = getResponseCode(con);
        
        log.debug("Sending 'GET' request to URL: " + url);
        log.debug("Response Code: " + responseCode);

        // Handle result
        if (responseCode == 200) {
              return getResponse(con);
        }
        return null;
    }

    public String POST(String url, Map<String, String> headers, Map<String, String> params) {
        URL obj = getURL(url);
        Proxy proxy = getProxy();
        HttpURLConnection con = getConnection(obj, proxy, "POST");

        // Headers
        for (Map.Entry<String, String> h : headers.entrySet()) {
            con.setRequestProperty(h.getKey(), h.getValue());
        }

        // Parameters
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, String> p : params.entrySet()) {
            str.append(p.getKey()).append("=").append(p.getValue());
        }
        
        write(con, str.toString());
        int responseCode = getResponseCode(con);

        System.out.println("Sending 'POST' request to URL: " + url);
        System.out.println("Post parameters : " + str.toString());
        System.out.println("Response Code : " + responseCode);

        String response = getResponse(con);
        return response;
    }

    /**
     * Write to connection
     * 
     * @param con Connection object. @see getConnection
     * @param str Contents to write
     */
    private void write(HttpURLConnection con, String str) {
        // Send POST request
        con.setDoOutput(true);
        DataOutputStream wr;
        try {
            wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(str);
            wr.flush();
            wr.close();
        } catch (IOException ex) {
            log.error("Error writing POST parameters!", ex);
        }
    }

    /**
     * Get URL
     * 
     * @param url URL as String.
     * @return URL object
     */
    private URL getURL(String url) {
        URL obj = null;
        try {
            obj = new URL(url);
        } catch (MalformedURLException ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
            log.error("Malformed URL when trying to connect to Spatineo Monitoring API.");
        }
        return obj;
    }
    
    /**
     * Get proxy
     * 
     * @return Proxy object or null, depending on environment variables.
     */
    private Proxy getProxy() {
        // Proxy (if configured)
        String host = null;
        int port;
        Proxy proxy = null;
        if (null != System.getProperty("http.proxyHost") && null != System.getProperty("http.proxyPort")) {
            host = System.getProperty("http.proxyHost");
            port = Integer.parseInt(System.getProperty("http.proxyPort"));
            log.debug("Setting proxy to host=" + host + ", port=" + System.getProperty("http.proxyPort"));
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        } else {
            log.debug("No proxy set");
        }
        return proxy;
    }

    /**
     * Get connection
     * 
     * @param url       URL object
     * @param proxy     Proxy object, can be null.
     * @param method    "GET" or "POST" usually.
     * @return          HttpURLConnection object
     */
    private HttpURLConnection getConnection(URL url, Proxy proxy, String method) {
        HttpURLConnection con = null;
        int responseCode = 0;
        try {
            if (proxy != null) {
                con = (HttpURLConnection) url.openConnection(proxy);
            } else {
                con = (HttpURLConnection) url.openConnection();
            }
            con.setRequestMethod(method);
        } catch (IOException ex) {
            ex.printStackTrace();
            log.error("Connection failed to Spatineo Monitoring API!");
            log.error(ex.getMessage());
        }
        return con;
    }
    
    /**
     * Get response from server
     * 
     * @param con
     * @return Response as String object
     */
    public String getResponse(HttpURLConnection con) {
        StringBuilder response = null;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (IOException ex) {
            log.error("Error while reading Spatineo server response!", ex);
        }
        return response.toString();
    }
    
    /**
     * Get response code
     * 
     * @param con
     * @return 
     */
    public int getResponseCode(HttpURLConnection con) {
        int responseCode = 0;
        try {
            responseCode = con.getResponseCode();
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
        return responseCode;
    }
    
}
