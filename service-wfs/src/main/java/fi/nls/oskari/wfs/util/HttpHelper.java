package fi.nls.oskari.wfs.util;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wfs.WFSExceptionHelper;

import java.io.BufferedInputStream;
import java.io.Reader;

/**
 * Implements HTTP request and response methods
 */
public class HttpHelper {

    private static final Logger log = LogFactory.getLogger(HttpHelper.class);

    private static final int CONNECTION_TIMEOUT_MS_DEFAULT = 3000;
    private static final int READ_TIMEOUT_MS_DEFAULT = 60000;
    private static final boolean LOG_RESPONSES_DEFAULT = false;

    private static final int CONNECTION_TIMEOUT_MS;
    private static final int READ_TIMEOUT_MS;
    private static final boolean LOG_RESPONSES;

    static {
        CONNECTION_TIMEOUT_MS = PropertyUtil.getOptional("oskari.connection.timeout", CONNECTION_TIMEOUT_MS_DEFAULT);
        READ_TIMEOUT_MS = PropertyUtil.getOptional("oskari.read.timeout", READ_TIMEOUT_MS_DEFAULT);
        LOG_RESPONSES = PropertyUtil.getOptional("transport.response.debug", LOG_RESPONSES_DEFAULT);
    }

    /**
     * Basic HTTP GET method
     * 
     * @param url
     * @return response body
     */
    public static String getRequest(String url, String cookies) {
        HttpRequest request;
        String response = null;
        try {
            request = HttpRequest.get(url)
                    .acceptGzipEncoding().uncompress(true)
                    .trustAllCerts()
                    .trustAllHosts();
            if(cookies != null) {
                request.getConnection().setRequestProperty("Cookie", cookies);
            }
            if(request.ok() || request.code() == 304) {
                response = request.body();
                if (LOG_RESPONSES) {
                    log.debug(response);
                }
            } else {
                handleHTTPError("GET", url, request.code(), false);
            }
        } catch (HttpRequestException e) {
            handleHTTPRequestFail(url, e, false);
        } catch (Exception e) {
            handleHTTPRequestFail(url, e, false);
        }
        return response;
    }
    /**
     * Basic HTTP GET method
     *
     * @param url
     * @key header params key
     * @return header param value
     */
    public static String getHeaderValue(String url, String cookies, String key) {
        HttpRequest request;
        String headerValue = null;
        try {
            request = HttpRequest.get(url)
                    .acceptGzipEncoding().uncompress(true)
                    .trustAllCerts()
                    .trustAllHosts();
            if(cookies != null) {
                request.getConnection().setRequestProperty("Cookie", cookies);
            }
            if(request.ok() || request.code() == 304) {
                if(key != null) {
                    headerValue = request.header(key);
                }
            } else {
                handleHTTPError("GET", url, request.code(), false);
            }
        } catch (HttpRequestException e) {
            handleHTTPRequestFail(url, e, false);
        } catch (Exception e) {
            handleHTTPRequestFail(url, e, false);
        }
        return headerValue;
    }

    /**
     * HTTP GET method with optional basic authentication and contentType definition
     * 
     * @param url
     * @param contentType
     * @param username
     * @param password
     * @return response body
     */
    public static BufferedInputStream getRequestStream(String url, String contentType, String username, String password) {
        HttpRequest request = getRequest(url, contentType, username, password);
        if(request != null) {
            return request.buffer();
        }
        return null;
    }

    /**
     * HTTP GET method with optional basic authentication and contentType definition
     * 
     * @param url
     * @param contentType
     * @param username
     * @param password
     * @return response body
     */
    public static Reader getRequestReader(String url, String contentType, String username, String password) {
        HttpRequest request = getRequest(url, contentType, username, password);
        if(request != null) {
            return request.bufferedReader();
        }
        return null;
    }

    /**
     * HTTP GET method with optional basic authentication and contentType definition
     * 
     * @param url
     * @param contentType
     * @param username
     * @param password
     * @return response body
     */
    public static HttpRequest getRequest(String url, String contentType, String username, String password) {
        HttpRequest request;
        try {

            HttpRequest.keepAlive(false);
            if(username != null && !username.equals("") && !username.equals("null")) {
                request = HttpRequest.get(url)
                        .basic(username, password)
                        .accept(contentType)
                        .connectTimeout(CONNECTION_TIMEOUT_MS)
                        .readTimeout(READ_TIMEOUT_MS)
                        .acceptGzipEncoding().uncompress(true)
                        .trustAllCerts()
                        .trustAllHosts();
            } else {
                request = HttpRequest.get(url)
                        .contentType(contentType)
                        .connectTimeout(CONNECTION_TIMEOUT_MS)
                        .readTimeout(READ_TIMEOUT_MS)
                        .acceptGzipEncoding().uncompress(true)
                        .trustAllCerts()
                        .trustAllHosts();
            }
            if(request.ok() || request.code() == 304)
                return request;
            else {
                handleHTTPError("GET", url, request.code(), false);
            }

        } catch (HttpRequestException e) {
            handleHTTPRequestFail(url, e, false);
        } catch (Exception e) {
            handleHTTPRequestFail(url, e, false);
        }
        return null;
    }
    /**
     * HTTP POST method with optional basic authentication and contentType definition
     *
     * @param url
     * @param contentType
     * @param username
     * @param password
     * @return response body
     */
    public static Reader postRequestReader(String url, String contentType, String data, String username,
            String password) {
        return postRequestReader(url, contentType, data, username, password, false);
    }

    /**
     * HTTP POST method with optional basic authentication and contentType definition
     * 
     * @param url
     * @param contentType
     * @param username
     * @param password
     * @param throwException
     * @return response body
     */
    public static Reader postRequestReader(String url, String contentType, String data, String username,
            String password, boolean throwException) {
        HttpRequest request;
        Reader response = null;
        try {

            HttpRequest.keepAlive(false);
            if(username != null && !username.equals("") && !username.equals("null")) {
                request = HttpRequest.post(url)
                        .basic(username, password)
                        .contentType(contentType)
                        .connectTimeout(CONNECTION_TIMEOUT_MS)
                        .readTimeout(READ_TIMEOUT_MS)
                        .acceptGzipEncoding().uncompress(true)
                        .trustAllCerts()
                        .trustAllHosts()
                        .send(data);
            } else {
                request = HttpRequest.post(url)
                        .contentType(contentType)
                        .connectTimeout(CONNECTION_TIMEOUT_MS)
                        .readTimeout(READ_TIMEOUT_MS)
                        .acceptGzipEncoding().uncompress(true)
                        .trustAllCerts()
                        .trustAllHosts()
                        .send(data);
            }
            if(request.ok() || request.code() == 304) {
                // default charset is UTF-8
                log.debug("request charset:", request.charset());
                response = request.bufferedReader();
                if (LOG_RESPONSES) {
                    response = new DebugLoggingReader(response);
                }
            } else {
                handleHTTPError("POST", url, request.code(), throwException);
            }

        } catch (HttpRequestException e) {
            handleHTTPRequestFail(url, e, throwException);
        } catch (Exception e) {
            handleHTTPRequestFail(url, e, throwException);
        }
        return response;
    }

    /**
     * Handles HTTP error logging for HTTP request methods
     * 
     * @param type
     * @param url
     * @param code
     * @param throwException
     */
    private static void handleHTTPError(String type, String url, int code, boolean throwException) {
        log.warn("HTTP " + type + " request error (" + code + ") when requesting: " + url);
        if (throwException) {
            throw new ServiceRuntimeException("HTTP " + type + " request error (" + Integer.toString(code) + ") when requesting: " + url,
                    WFSExceptionHelper.ERROR_COMMON_PROCESS_REQUEST_FAILURE);
        }
    }

    /**
     * Handles Exceptions logging for HTTP request methods
     *
     * @param url
     * @param e
     * @param throwException
     */
    private static void handleHTTPRequestFail(String url, Exception e, boolean throwException) {
        log.warn(e, "HTTP request failed when requesting: " + url);
        if (throwException) {
            throw new ServiceRuntimeException("HTTP request failed when requesting: " + url,
                    e, WFSExceptionHelper.ERROR_COMMON_PROCESS_REQUEST_FAILURE);
        }
    }

}
