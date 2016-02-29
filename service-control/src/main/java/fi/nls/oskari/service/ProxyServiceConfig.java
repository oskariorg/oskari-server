package fi.nls.oskari.service;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for a proxyable service.
 * @author SMAKINEN
 */
public class ProxyServiceConfig {
    private static final Logger log = LogFactory.getLogger(ProxyServiceConfig.class);

    public static final String DEFAULT_ENCODING = "UTF-8";

    private String url = null;
    private String username;
    private String password;
    private String encoding;
    private String[] paramNames = new String[0];
    private Map<String, String> headers = new HashMap<String, String>();

    /**
     * Checks for validity
     * @return true of url is defined
     */
    public boolean isValid() {
        return url != null;
    }

    /**
     * Returns encoding or specified or DEFAULT_ENCODING if not specified.
     * @return
     */
    public String getEncoding() {
        if(encoding == null) {
            return DEFAULT_ENCODING;
        }
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void addHeader(final String key, final String value) {
        headers.put(key, value);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public void setParamNames(String[] paramNames) {
        this.paramNames = paramNames;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Returns the configured base url
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns version of the config for given action params. This should be called first to get the correct
     * config based on the request. Returns populated config object based on parameters.
     * Default behavior returns self without any processing.
     *
     * This method should be overridden for action params based config
     * (f.ex. getting a service url based a parameter).
     *
     * @return defaults to self (think of this as a extension hook through override)
     */
    public ProxyServiceConfig getConfig(final ActionParameters params) {
        return this;
    }

    /**
     * Returns the url for the service, copies params to the base url if any are defined.
     * @return
     */
    public String getUrl(final ActionParameters params) {

        Map<String, String> urlParams = new HashMap<String, String>();
        for(String paramName : getParamNames()) {
            urlParams.put(paramName, params.getHttpParam(paramName));
        }

        final String url = IOHelper.constructUrl(getUrl(), urlParams);

        if(url.startsWith("/")) {
            // default to self if domain not specified
            return IOHelper.getMyDomain() + url;
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
