package fi.nls.oskari.util;

import com.github.kevinsawicki.http.HttpRequest;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.commons.codec.binary.Base64;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/*
Methods using HttpRequest were moved from a class called wmshelper and are
propably pretty much duplicate implementations of existing methods in this class.
This needs to be checked
// TODO: this class propably needs refactoring
 */
public class IOHelper {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String DEFAULT_CHARSET = "UTF-8";
    private static final Logger log = LogFactory.getLogger(IOHelper.class);

    private static SSLSocketFactory TRUSTED_FACTORY;
    private static HostnameVerifier TRUSTED_VERIFIER;

    private static int CONNECTION_TIMEOUT_MS = 3000;
    private static int READ_TIMEOUT_MS = 60000;

    private static boolean trustAllCerts = false;
    private static boolean trustAllHosts = false;

    static {
        CONNECTION_TIMEOUT_MS = PropertyUtil.getOptional("oskari.connection.timeout", CONNECTION_TIMEOUT_MS);
        READ_TIMEOUT_MS = PropertyUtil.getOptional("oskari.read.timeout", READ_TIMEOUT_MS);
        trustAllCerts = "true".equals(PropertyUtil.getOptional("oskari.trustAllCerts"));
        trustAllHosts = "true".equals(PropertyUtil.getOptional("oskari.trustAllHosts"));
    }

    public static int getConnectionTimeoutMs() {
        return CONNECTION_TIMEOUT_MS;
    }
    public static int getReadTimeoutMs() {
        return READ_TIMEOUT_MS;
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
                    charset));
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
        if (userName != null && !userName.isEmpty()) {
            final String encoded = encode64(userName + ':' + password);
            log.debug(encoded, " ---- > ", encoded.replaceAll("\r", "").replaceAll("\n", ""));
            con.setRequestProperty(HEADER_AUTHORIZATION, "Basic " + encoded.replaceAll("\r", "").replaceAll("\n", ""));
        }
        return con;
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
        BufferedOutputStream proxyToWebBuf = null;
        try {
            proxyToWebBuf = new BufferedOutputStream(con.getOutputStream());
            proxyToWebBuf.write(bytes);
        } finally {
            if (proxyToWebBuf != null) {
                try {
                    proxyToWebBuf.flush();
                    proxyToWebBuf.close();
                } catch (Exception ignored) {
                }
            }
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
        return getURL(pUrl, userName, password, Collections.EMPTY_MAP);
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
                request = HttpRequest.post(url).basic(username, password)
                        .contentType(contentType).connectTimeout(30)
                        .acceptGzipEncoding().uncompress(true).trustAllCerts()
                        .trustAllHosts().send(data);
            } else {
                request = HttpRequest.post(url).contentType(contentType)
                        .connectTimeout(30).acceptGzipEncoding().uncompress(
                                true).trustAllCerts().trustAllHosts()
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
        try {
            in.close();
        } catch (Exception ignored) {
        }
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
}
