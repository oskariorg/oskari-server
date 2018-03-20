package fi.nls.oskari.util;

import com.github.kevinsawicki.http.HttpRequest;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.commons.codec.binary.Base64;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/*
Methods using HttpRequest were moved from a class called wmshelper and are
propably pretty much duplicate implementations of existing methods in this class.
This needs to be checked
// TODO: this class propably needs refactoring
 */
public class IOHelper {

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENTTYPE = "Content-Type";
    public static final String HEADER_USERAGENT = "User-Agent";
    public static final String HEADER_REFERER = "Referer";
    public static final String HEADER_ACCEPT = "Accept";

    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String DEFAULT_CHARSET = CHARSET_UTF8;
    public static final String CONTENTTYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_XML = "application/xml";
    private static final Logger log = LogFactory.getLogger(IOHelper.class);

    private static SSLSocketFactory TRUSTED_FACTORY;
    private static HostnameVerifier TRUSTED_VERIFIER;

    private static int CONNECTION_TIMEOUT_MS = 3000;
    private static int READ_TIMEOUT_MS = 60000;
    private static String MY_DOMAIN = "http://localhost:2373";

    private static boolean trustAllCerts = false;
    private static boolean trustAllHosts = false;

    static {
        CONNECTION_TIMEOUT_MS = PropertyUtil.getOptional("oskari.connection.timeout", CONNECTION_TIMEOUT_MS);
        READ_TIMEOUT_MS = PropertyUtil.getOptional("oskari.read.timeout", READ_TIMEOUT_MS);
        trustAllCerts = "true".equals(PropertyUtil.getOptional("oskari.trustAllCerts"));
        trustAllHosts = "true".equals(PropertyUtil.getOptional("oskari.trustAllHosts"));
        MY_DOMAIN = PropertyUtil.get("oskari.domain", MY_DOMAIN);
    }

    public static int getConnectionTimeoutMs() {
        return CONNECTION_TIMEOUT_MS;
    }
    public static int getReadTimeoutMs() {
        return READ_TIMEOUT_MS;
    }
    public static String getMyDomain() {
        return MY_DOMAIN;
    }
    /**
     * Reads the given input stream and converts its contents to a string using #DEFAULT_CHARSET
     * @param is
     * @return
     * @throws IOException
     */
    public static String readString(InputStream is) throws IOException {
        return readString(is, DEFAULT_CHARSET);
    }

    /**
     * Reads the given input stream and converts its contents to a string using #DEFAULT_CHARSET
     * @param conn
     * @return
     * @throws IOException
     */
    public static String readString(HttpURLConnection conn) throws IOException {
        return readString(conn, DEFAULT_CHARSET);
    }
    /**
     * Reads the given input stream and converts its contents to a string using given charset
     * @param conn connection used to get inputstream and detect gzip encoding
     * @param charset
     * @return
     * @throws IOException
     */
    public static String readString(HttpURLConnection conn, final String charset) throws IOException {
        if("gzip".equals(conn.getContentEncoding())) {
            return readString(new GZIPInputStream(conn.getInputStream()), charset);
        }
        return readString(conn.getInputStream(), charset);
    }

    /**
     * Reads the given input stream and converts its contents to a string using given charset
     * @param is
     * @param charset
     * @return
     * @throws IOException
     */
    public static String readString(InputStream is, final String charset)
            throws IOException {
        /*
         * To convert the InputStream to String we use the Reader.read(char[]
         * buffer) method. We iterate until the Reader return -1 which means
         * there's no more data to read. We use the StringWriter class to
         * produce the string.
         */

        if (is == null) {
            return "";
        }

        final Writer writer = new StringWriter();
        final char[] buffer = new char[1024];
        try {
            final Reader reader = new BufferedReader(new InputStreamReader(is,
                    charset == null ? DEFAULT_CHARSET : charset ));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        return writer.toString();
    }

    /**
     * Reads the given input stream and returns its contents as a byte array.
     * @param conn used to get inputstream and detect possible gzip encoding
     * @return
     * @throws IOException
     */
    public static byte[] readBytes(HttpURLConnection conn) throws IOException {
        if("gzip".equals(conn.getContentEncoding())) {
            return readBytes(new GZIPInputStream(conn.getInputStream()));
        }
        return readBytes(conn.getInputStream());
    }
    /**
     * Reads the given input stream and returns its contents as a byte array.
     * @param is
     * @return
     * @throws IOException
     */
    public static byte[] readBytes(InputStream is) throws IOException {
        if (is == null) {
            return new byte[0];
        }
        final ByteArrayOutputStream ous = new ByteArrayOutputStream();
        final byte[] buffer = new byte[4096];
        try {
            int read = 0;
            while ((read = is.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        } finally {
            is.close();
        }
        return ous.toByteArray();
    }

    /**
     * Copies data from InputStream to OutputStream
     * Does not close either of the streams
     * Does nothing if either InputStream or OutputStream is null
     * 
     * @param in
     * @param out
     * @throws IOException
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        if (in == null || out == null) {
            return;
        }

        final byte[] buffer = new byte[4096];
        int read = 0;
        while ((read = in.read(buffer, 0, 4096)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * Returns a connection based on properties:
     * - [propertiesPrefix]url=[url to call for this service] (required)
     * - [propertiesPrefix]user=[username for basic auth] (optional)
     * - [propertiesPrefix]pass=[password for basic auth] (optional)
     * - [propertiesPrefix]header.[header name]=[header value] (optional)
     */
    public static HttpURLConnection getConnectionFromProps(final String propertiesPrefix)
            throws IOException {
        final String url = PropertyUtil.getNecessary(propertiesPrefix + "url");
        return getConnectionFromProps(url, propertiesPrefix);
    }
    /**
     * Returns a connection based on properties:
     * - [propertiesPrefix]user=[username for basic auth] (optional)
     * - [propertiesPrefix]pass=[password for basic auth] (optional)
     * - [propertiesPrefix]header.[header name]=[header value] (optional)
     */
    public static HttpURLConnection getConnectionFromProps(final String url, final String propertiesPrefix)
            throws IOException {
        final String username = PropertyUtil.getOptional(propertiesPrefix + "user");
        final String password = PropertyUtil.getOptional(propertiesPrefix + "pass");
        final HttpURLConnection conn = getConnection(url, username, password);
        final String headerPropPrefix = propertiesPrefix + "header.";
        final List<String> headerPropNames = PropertyUtil.getPropertyNamesStartingWith(headerPropPrefix);
        for (String propName : headerPropNames) {
            final String header = propName.substring(headerPropPrefix.length());
            final String value = PropertyUtil.get(propName);
            writeHeader(conn, header, value);
        }
        return conn;
    }

    /**
     * Opens a HttpURLConnection to given url
     * @param pUrl
     * @return
     * @throws IOException
     */
    public static HttpURLConnection getConnection(final String pUrl)
            throws IOException {
        log.debug("Opening connection to", pUrl);
        final URL url = new URL(pUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(CONNECTION_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        if(trustAllCerts) trustAllCerts(conn);
        if(trustAllHosts) trustAllHosts(conn);
        return conn;
    }

    /**
     * If logger is set to debug this method reads the input stream, prints the response as text
     * and wraps it to another input stream for further consumption.
     * If debug is not enabled, returns the inputstream as is
     * @param in
     * @return
     * @throws IOException
     */
    public static InputStream debugResponse(final InputStream in) throws IOException {
        if(!log.isDebugEnabled()) {
            // return the input stream as is if we are not in debug mode
            return in;
        }
        byte[] response = readBytes(in);
        log.debug("Got response from inputstream:\n", new String(response));

        final ByteArrayInputStream debug = new ByteArrayInputStream(response);
        return debug;
    }

    public static String getCharset(final HttpURLConnection con) {
        return getCharset(con, null);
    }

    public static String getCharset(final HttpURLConnection con, final String defaultCharset) {
        final String contentType = con.getContentType();
        final String[] values = contentType.split(";");

        for (String value : values) {
            value = value.trim();

            if (value.toLowerCase().startsWith("charset=")) {
                return value.substring("charset=".length());
            }
        }
        return defaultCharset;
    }
    /**
     * Opens a HttpURLConnection to given url and sets up basic authentication with given user/pass.
     * @param pUrl
     * @param userName
     * @param password
     * @return
     * @throws IOException
     */
    public static HttpURLConnection getConnection(final String pUrl,
                                                  final String userName, final String password)
            throws IOException {
        final HttpURLConnection con = getConnection(pUrl);
        setupBasicAuth(con, userName,password);
        return con;
    }

    /**
     * Sets the authorization header for connection.
     * @param con
     * @param userName
     * @param password
     */
    public static void setupBasicAuth(final HttpURLConnection con,final String userName, final String password) {
        if (userName != null && !userName.isEmpty()) {
            final String encoded = encode64(userName + ':' + password);
            con.setRequestProperty(HEADER_AUTHORIZATION, "Basic " + encoded.replaceAll("\r", "").replaceAll("\n", ""));
        }
    }

    /**
     * Writes data to given connection as a payload.
     * @see #writeToConnection(java.net.HttpURLConnection, byte[])
     * @param con
     * @param postData payload
     * @throws IOException
     */
    public static void writeToConnection(final HttpURLConnection con,
                                         final String postData) throws IOException {
        if(postData == null) {
            log.info("Nothing to write to connection:", con.getURL());
            return;
        }
        writeToConnection(con, postData.getBytes());
    }

    /**
     * Writes data to given connection. If method has not been set as POST, sets it and calls connect before writing the payload.
     * @param con
     * @param bytes
     * @throws IOException
     */
    public static void writeToConnection(final HttpURLConnection con,
                                         final byte[] bytes) throws IOException {
        if(bytes == null || bytes.length == 0) {
            log.info("Nothing to write to connection:", con.getURL());
            return;
        }
        if(log.isDebugEnabled()) {
            log.debug("Writing to connection:", con.getURL(), "\npayload:", new String(bytes));
        }
        if(!"POST".equals(con.getRequestMethod())) {
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.connect();
        }
        try (OutputStream out = con.getOutputStream()) {
            out.write(bytes);
        }
    }

    /**
     * Calls given URL and returns the response as String
     * @param pUrl
     * @return
     * @throws IOException
     */
    public static String getURL(final String pUrl) throws IOException {
        return getURL(pUrl, Collections.EMPTY_MAP);
    }

    /**
     * Calls given URL and returns the response interpreted with given charset as String
     * @param pUrl
     * @param charset
     * @return
     * @throws IOException
     */
    public static String getURL(final String pUrl, final String charset) throws IOException {
        return getURL(pUrl, Collections.EMPTY_MAP, charset);
    }

    /**
     * Calls given URL with given http headers and returns the response as String.
     * @param pUrl
     * @param headers
     * @return
     * @throws IOException
     */
    public static String getURL(final String pUrl,
                                final Map<String, String> headers) throws IOException {
        return getURL(pUrl, headers, DEFAULT_CHARSET);
    }

    /**
     * Calls given URL with given http headers and returns the response interpreted with given charset as String
     * @param pUrl
     * @param headers
     * @param charset
     * @return
     * @throws IOException
     */
    public static String getURL(final String pUrl,
                                final Map<String, String> headers, final String charset) throws IOException {
        final HttpURLConnection con = getConnection(pUrl);
        return getURL(con, headers, charset);
    }

    /**
     * Writes the given http headers to the connection and returns the response interpreted with given charset as String
     * @param con
     * @param headers
     * @param charset
     * @return
     * @throws IOException
     */
    public static String getURL(final HttpURLConnection con,
                                final Map<String, String> headers, final String charset) throws IOException {
        try {
            writeHeaders(con, headers);
            return IOHelper.readString(con.getInputStream(), charset);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    /**
     * Calls given URL and returns the response as String
     * @param pUrl
     * @param userName
     * @param password
     * @return
     * @throws IOException
     */
    public static String getURL(final String pUrl, final String userName, final String password ) throws IOException {
        if (userName != null && userName.length() > 0 && password != null && password.length() > 0) {
            return getURL(pUrl, userName, password, Collections.EMPTY_MAP);
        }
        else {
            return getURL(pUrl, Collections.EMPTY_MAP);
        }
    }
    /**
     * Calls given URL with given http headers and returns the response as String.
     * @param pUrl
     * @param userName
     * @param password
     * @param headers
     * @return
     * @throws IOException
     */
    public static String getURL(final String pUrl, final String userName, final String password,
                                final Map<String, String> headers) throws IOException {
        return getURL(pUrl, userName, password, headers, DEFAULT_CHARSET);
    }
    /**
     * Calls given URL with given http headers and returns the response interpreted with given charset as String
     * @param pUrl
     * @param userName
     * @param password
     * @param headers
     * @param charset
     * @return
     * @throws IOException
     */
    public static String getURL(final String pUrl,final String userName, final String password,
                                final Map<String, String> headers, final String charset) throws IOException {
        final HttpURLConnection con = getConnection(pUrl, userName, password);
        final int responseCode = con.getResponseCode();
        // Unauthorized
        if (responseCode == 401) {
            throw new IOException("Unauthorized");
        }
        return getURL(con, headers, charset);
    }

    /**
     * Writes the given http headers to the connection.
     * @param con
     * @param headers
     * @throws IOException
     */
    public static void writeHeaders(final HttpURLConnection con,
                                    final Map<String, String> headers) throws IOException {
        if (headers != null) {
            for (String key : headers.keySet()) {
                writeHeader(con, key, headers.get(key));
            }
        }
    }
    /**
     * Writes the given http header to the connection.
     * @param con       connection to write to
     * @param header    header name
     * @param value     header value
     * @throws IOException
     */
    public static void writeHeader(final HttpURLConnection con,
                                    final String header, final String value) throws IOException {
        if (header != null && value != null) {
            con.setRequestProperty(header, value);
        }
    }

    /**
     * Writes Content-type header to the connection.
     * @param con       connection to write to
     * @param value     content type
     * @throws IOException
     */
    public static void setContentType(final HttpURLConnection con,
                                   final String value) throws IOException {
        if (value != null) {
            con.setRequestProperty(HEADER_CONTENTTYPE, value);
        }
    }

    /**
     * Encodes the given String with base64
     * @param in
     * @return
     */
    public static String encode64(String in) {
        if (in != null && !in.isEmpty()) {
            byte[] decoded = Base64.encodeBase64(in.getBytes());
            String out = new String(decoded);
            return out;
        }
        return in;
    }

    /**
     * Decodes the given base64 encoded String
     * @param in
     * @return
     */
    public static String decode64(String in) {
        if (in != null && !in.isEmpty()) {
            if (Base64.isArrayByteBase64(in.getBytes())) {
                byte[] decoded = Base64.decodeBase64(in.getBytes());
                String out = new String(decoded);
                return out;
            }
        }
        return in;
    }

    /**
     * HTTP request method with optional basic authentication and contentType
     * definition
     *
     * @param url
     * @param data          data to post (if empty, GET method otherwise POST)
     * @param username
     * @param password
     * @param host          host name for header params (optional)
     * @param authorization (optional)
     * @param contentType
     * @return response body
     */
    public static String httpRequestAction(String url, String data, String username,
                                           String password, String host, String authorization,
                                           String contentType) {
        String response = null;

        if (!data.isEmpty()) {
            response = postRequest(url, contentType, data, username, password,
                    host, authorization);

        } else
            response = getRequest(url, contentType, username, password, host,
                    authorization);

        return response;
    }

    /**
     * HTTP GET method with optional basic authentication and contentType
     * definition
     *
     * @param url
     * @param contentType
     * @param username
     * @param password
     * @return response body
     */
    public static String getRequest(String url, String contentType,
                                    String username, String password, String host, String authorization) {
        HttpRequest request = null;
        try {

            HttpRequest.keepAlive(false);
            if (username != null && !username.isEmpty()) {
                request = HttpRequest.get(url).basic(username, password)
                        .accept(contentType).connectTimeout(30)
                        .acceptGzipEncoding().uncompress(true).trustAllCerts()
                        .trustAllHosts();
            } else {
                request = HttpRequest.get(url).contentType(contentType)
                        .connectTimeout(30).acceptGzipEncoding().uncompress(
                                true).trustAllCerts().trustAllHosts();
            }
            if (host != null && !host.isEmpty()) {
                request.header("Host", host);
            }

            if (authorization != null && !authorization.isEmpty()) {
                request.authorization(authorization);
            }
            if (request.ok() || request.code() == 304)
                return request.body();
            else {
                handleHTTPError("GET", url, request.code());
            }

        } catch (HttpRequest.HttpRequestException e) {
            handleHTTPRequestFail(url, e);
        } catch (Exception e) {
            handleHTTPRequestFail(url, e);
        }
        return null;
    }

    public static HttpURLConnection postForm(String url, Map<String, String> keyValuePairs)
            throws IOException {
        String requestBody = getParams(keyValuePairs);
        return post(url, CONTENTTYPE_FORM_URLENCODED,
                requestBody.getBytes(StandardCharsets.UTF_8));
    }

    public static HttpURLConnection post(String url, String contentType,
            byte[] body) throws IOException {
        return send(getConnection(url), "POST", contentType, body);
    }

    public static HttpURLConnection post(String url, String contentType,
            ByteArrayOutputStream baos) throws IOException {
        return send(getConnection(url), "POST", contentType, baos);
    }

    public static HttpURLConnection post(HttpURLConnection conn, String contentType,
            byte[] body) throws IOException {
        return send(conn, "POST", contentType, body);
    }

    public static HttpURLConnection post(HttpURLConnection conn, String contentType,
            ByteArrayOutputStream baos) throws IOException {
        return send(conn, "POST", contentType, baos);
    }

    public static HttpURLConnection put(String url, String contentType, byte[] body)
            throws IOException {
        return put(getConnection(url), contentType, body);
    }

    public static HttpURLConnection put(HttpURLConnection conn, String contentType, byte[] body)
            throws IOException {
        return send(conn, "PUT", contentType, body);
    }

    private static HttpURLConnection send(HttpURLConnection conn, String method,
            String contentType, byte[] body) throws IOException {
        conn.setRequestMethod(method);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        setContentType(conn, contentType);
        conn.setRequestProperty("Content-Length", Integer.toString(body.length));
        try (OutputStream out = conn.getOutputStream()) {
            out.write(body);
        }
        return conn;
    }

    private static HttpURLConnection send(HttpURLConnection conn, String method,
            String contentType, ByteArrayOutputStream baos) throws IOException {
        conn.setRequestMethod(method);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        setContentType(conn, contentType);
        conn.setRequestProperty("Content-Length", Integer.toString(baos.size()));
        try (OutputStream out = conn.getOutputStream()) {
            baos.writeTo(out);
        }
        return conn;
    }

    public static String postRequest(String url) {
        return postRequest(url, "", "", "", null, null, null);
    }

    /**
     * HTTP POST method with optional basic authentication and contentType
     * definition
     *
     * @param url
     * @param contentType
     * @param username
     * @param password
     * @return response body
     */
    public static String postRequest(String url, String contentType,
                                     String data, String username, String password, String host,
                                     String authorization) {
        HttpRequest request = null;
        String response = null;
        try {

            HttpRequest.keepAlive(false);
            if (username != null && !username.isEmpty()) {
                request = HttpRequest.post(url)
                        .basic(username, password)
                        .contentType(contentType)
                        .connectTimeout(getConnectionTimeoutMs())
                        .acceptGzipEncoding()
                        .uncompress(true)
                        .trustAllCerts()
                        .trustAllHosts()
                        .send(data);
            } else {
                request = HttpRequest.post(url)
                        .contentType(contentType)
                        .connectTimeout(getConnectionTimeoutMs())
                        .acceptGzipEncoding()
                        .uncompress(true)
                        .trustAllCerts()
                        .trustAllHosts()
                        .send(data);
            }
            if (host != null && !host.isEmpty()) {
                request.header("Host", host);
            }

            if (authorization != null && !authorization.isEmpty()) {
                request.authorization(authorization);
            }
            if (request.ok() || request.code() == 304)
                response = request.body();
            else {
                handleHTTPError("POST", url, request.code());
            }

        } catch (HttpRequest.HttpRequestException e) {
            handleHTTPRequestFail(url, e);
        } catch (Exception e) {
            handleHTTPRequestFail(url, e);
        }
        return response;
    }

    /**
     * In many case we are not interested in exceptions generated on stream close.
     * This method closes the given Closeable param and ignores any exception that might be thrown.
     *
     * @param in
     */
    public static void close(final Closeable in) {
        if(in == null) {
            return;
        }
        try {
            in.close();
        } catch (Exception ignored) { }
    }

    /**
     * Handles HTTP error logging for HTTP request methods
     *
     * @param type
     * @param url
     * @param code
     */
    private static void handleHTTPError(String type, String url, int code) {
        log.warn("HTTP " + type + " request error (" + code
                + ") when requesting: " + url);
    }

    /**
     * Handles Exceptions logging for HTTP request methods
     *
     * @param url
     * @param e
     */
    private static void handleHTTPRequestFail(String url, Exception e) {
        log.warn("HTTP request failed when requesting: " + url, e);
    }


    public static void trustAllCerts(final HttpURLConnection connection) throws IOException {
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(getTrustedFactory());
        }
    }
    public static void trustAllHosts(final HttpURLConnection connection) {
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setHostnameVerifier(getTrustedVerifier());
        }
    }
    private static SSLSocketFactory getTrustedFactory() throws IOException {
        if (TRUSTED_FACTORY == null) {
            final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    // Intentionally left blank
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    // Intentionally left blank
                }
            } };
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, trustAllCerts, new SecureRandom());
                TRUSTED_FACTORY = context.getSocketFactory();
            } catch (Exception e) {
                IOException ioException = new IOException(
                        "Security exception configuring SSL context");
                ioException.initCause(e);
                throw ioException;
            }
        }

        return TRUSTED_FACTORY;
    }

    private static HostnameVerifier getTrustedVerifier() {
        if (TRUSTED_VERIFIER == null)
            TRUSTED_VERIFIER = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
        return TRUSTED_VERIFIER;
    }

    /**
     * Adds parameters to given base URL. URLEncodes parameter values.
     * Note that
     * @param url
     * @param params
     * @return constructed url including additional parameters
     */
    public static String constructUrl(final String url, Map<String, String> params) {
        if(params == null) {
            return url;
        }
        if(params.isEmpty()) {
            return url;
        }
        final StringBuilder urlBuilder = new StringBuilder(url);

        if(!url.contains("?")) {
            urlBuilder.append("?");
        }
        else {
            final char lastChar = urlBuilder.charAt(urlBuilder.length()-1);
            if((lastChar != '&' && lastChar != '?')) {
                urlBuilder.append("&");
            }
        }
        final String queryString = getParams(params);
        if(queryString.isEmpty()) {
            // drop last character ('?' or '&')
            return urlBuilder.substring(0, urlBuilder.length()-1);
        }
        return urlBuilder.append(queryString).toString();
    }

    public static String fixPath(String url) {
        String[] parts = url.split("://");
        if(parts.length < 2) {
            return url;
        }

        return parts[0] + "://" + parts[1].replaceAll("//", "/");
    }

    /**
     * Convenience method for just adding one param to an URL.
     * Using constructUrl(String, Map<String, String>) is more efficent with multiple params.
     * @param url
     * @param key
     * @param value
     * @return
     */
    public static String addUrlParam(final String url, String key, String value) {
        final Map<String, String> params = new HashMap<>(1);
        params.put(key, value);
        return constructUrl(url, params);
    }

    public static String getParams(Map<String, String> kvps) {
        if (kvps == null || kvps.isEmpty()) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : kvps.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            if (key == null || key.isEmpty() || value == null || value.isEmpty()) {
                continue;
            }
            final String keyEnc = urlEncode(key);
            final String valueEnc = urlEncode(value);
            if (!first) {
                sb.append('&');
            }
            sb.append(keyEnc).append('=').append(valueEnc);
            first = false;
        }
        return sb.toString();
    }

    /**
     * Same as {@link #getParams(Map) getParams} but this allows
     * the map and the generated query string to have multiple instances
     * with the same key e.g. ?foo=bar&foo=baz
     */
    public static String getParamsMultiValue(Map<String, String[]> kvps) {
        if (kvps == null || kvps.isEmpty()) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String[]> entry : kvps.entrySet()) {
            final String key = entry.getKey();
            final String[] values = entry.getValue();
            if (key == null || key.isEmpty() || values == null || values.length == 0) {
                continue;
            }
            String keyEnc = urlEncode(key);
            for (String value : values) {
                if (value == null || value.isEmpty()) {
                    continue;
                }
                String valueEnc = urlEncode(value);
                if (!first) {
                    sb.append('&');
                }
                sb.append(keyEnc).append('=').append(valueEnc);
                first = false;
            }
        }
        return sb.toString();
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, CHARSET_UTF8);
        } catch (UnsupportedEncodingException ignore) {
            // Ignore the exception, 'UTF-8' is supported
        }
        // return something, this code is unreachable
        return s;
    }

    public static InputStream getInputStream(HttpURLConnection conn) {
        try {
            return conn.getInputStream();
        } catch (IOException e) {
            return conn.getErrorStream();
        }
    }

}
