package fi.nls.oskari.control;

import fi.nls.oskari.domain.GuestUser;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.RequestHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A wrapper class for request/response that is populated before giving to ActionHandlers.
 * ActionHandlers use this to process a request.
 */
public class ActionParameters {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private User user;
    private Locale locale;
    private Map<String, Object> additionalParams = new HashMap<String, Object>();

    /**
     * The original request
     * @return
     */
    public HttpServletRequest getRequest() {
        return request;
    }
    /**
     * The original request
     */
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
    /**
     * The original response
     * @return
     */
    public HttpServletResponse getResponse() {
        return response;
    }
    /**
     * The original response
     */
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }
    /**
     * The user requesting an operation.
     * Defaults to GuestUser
     * @return
     */
    public User getUser() {
        if(user == null) {
            user = GuestUser.getInstance();
        }
        return user;
    }

    /**
     * The user requesting an operation.
     * @param user
     */
    public void setUser(User user) {
        this.user = user;
    }
    public Locale getLocale() {
        return locale;
    }
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * This can be used to provide handlers with platform specific parameters without
     * poluting base-packages with such dependencies.
     * For example ThemeDisplay for Liferay could be passed here.
     * Note that the component populating the params must fill this and
     * the key needs to be known by both the populator and the handler using it.
     * @param key
     * @param obj
     */
    public void putAdditionalParam(final String key, final Object obj) {
        additionalParams.put(key, obj);
    }
    /**
     * @see #putAdditionalParam
     */
    public Object getAdditionalParam(final String key) {
        return additionalParams.get(key);
    }

    /**
     * Returns a cleaned up (think XSS) value for the requested parameter
     * @param key parameter name
     * @return cleaned up value for the parameter as int
     * @throws ActionParamsException if parameter is not found, is empty or can't be parsed as int
     */
    public int getRequiredParamInt(final String key) throws ActionParamsException {
        final String errMsg = "Required parameter '" + key + "' missing!";
        final String val = getRequiredParam(key, errMsg);

        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            throw new ActionParamsException(errMsg);
        }
    }

    /**
     * Returns a cleaned up (think XSS) value for the requested parameter
     * @param key parameter name
     * @return cleaned up value for the parameter as long
     * @throws ActionParamsException if parameter is not found, is empty or can't be parsed as long
     */
    public long getRequiredParamLong(final String key) throws ActionParamsException {
        final String errMsg = "Required parameter '" + key + "' missing!";
        final String val = getRequiredParam(key, errMsg);

        try {
            return Long.parseLong(val);
        } catch (Exception e) {
            throw new ActionParamsException(errMsg);
        }
    }

    /**
     * Returns a cleaned up (think XSS) value for the requested parameter
     * @param key parameter name
     * @return cleaned up value for the parameter as double
     * @throws ActionParamsException if parameter is not found, is empty or can't be parsed as double
     */
    public double getRequiredParamDouble(final String key) throws ActionParamsException {
        final String errMsg = "Required parameter '" + key + "' missing!";
        final String val = getRequiredParam(key, errMsg);

        try {
            return Double.parseDouble(val);
        } catch (Exception e) {
            throw new ActionParamsException(errMsg);
        }
    }


    /**
     * Returns a cleaned up (think XSS) value for the requested parameter
     * @param key parameter name
     * @return cleaned up value for the parameter
     * @throws ActionParamsException if parameter is not found or is empty
     */
    public String getRequiredParam(final String key) throws ActionParamsException {
        return getRequiredParam(key, "Required parameter '" + key + "' missing!");
    }
    /**
     * Returns a cleaned up (think XSS) value for the requested parameter
     * @param key parameter name
     * @param msg message for exception to be thrown if parameter is not present
     * @return cleaned up value for the parameter
     * @throws ActionParamsException if parameter is not found or is empty
     */
    public String getRequiredParam(final String key, final String msg) throws ActionParamsException {
        if(msg == null) {
            return getRequiredParam(key);
        }
        final String value = RequestHelper.cleanString(getRequest().getParameter(key));
        if(value == null || value.isEmpty()) {
            throw new ActionParamsException(msg);
        }
        return value;
    }

    /**
     * Returns a cleaned up (think XSS) value for the requested parameter
     * @param key parameter name
     * @return cleaned up value for the parameter or null if not found
     */
    public String getHttpParam(final String key) {
        return RequestHelper.cleanString(getRequest().getParameter(key));
    }
    /**
     * Returns a cleaned up (think XSS) value for the requested parameter
     * @param key parameter name
     * @param defaultValue value to be returned if parameter is not present in the request
     * @return cleaned up value for the parameter or given defaultValue if not found
     */
    public String getHttpParam(final String key, final String defaultValue) {
        return RequestHelper.getString(getRequest().getParameter(key), defaultValue);
    }
    /**
     * Returns a parameter as integer or default value if not present/can't be parsed
     * @param key parameter name for an integer parameter
     * @param defaultValue value to be returned if parameter is not present in the request or can't be parsed
     * @return cleaned up value for the parameter or given defaultValue if not found
     */
    public int getHttpParam(final String key, final int defaultValue) {
        return ConversionHelper.getInt(getHttpParam(key), defaultValue);
    }
    /**
     * Returns a parameter as long or default value if not present/can't be parsed
     * @param key parameter name for an long parameter
     * @param defaultValue value to be returned if parameter is not present in the request or can't be parsed
     * @return cleaned up value for the parameter or given defaultValue if not found
     */
    public long getHttpParam(final String key, final long defaultValue) {
        return ConversionHelper.getLong(getHttpParam(key), defaultValue);
    }
    /**
     * Returns a parameter as boolean or default value if not present/can't be parsed
     * @param key parameter name for a boolean parameter
     * @param defaultValue value to be returned if parameter is not present in the request or can't be parsed
     * @return cleaned up value for the parameter or given defaultValue if not found
     */
    public boolean getHttpParam(final String key, final boolean defaultValue) {
        return ConversionHelper.getBoolean(getHttpParam(key), defaultValue);
    }
    /**
     * Returns value of a header field matching given key
     * @param key header name
     * @return
     */
    public String getHttpHeader(final String key) {
        return getRequest().getHeader(key);
    }

    /**
     * Returns clients ip from header "x-forwarded-for" or requests remote address.
     * Defaults to "0.0.0.0" on exception.
     * @return
     */
    public String getClientIp() {
        final HttpServletRequest hsr = getRequest();
        final String ip = hsr.getHeader("x-forwarded-for");
        try {
            return ip != null ? ip : hsr.getRemoteAddr();
        } catch (Exception ignored) { }
        return "0.0.0.0";
    }

    /**
     * Returns the cookie matching the given name.
     * @param name cookie name
     * @return
     */
    public Cookie getCookie(final String name) {
        final Cookie[] cookies = request.getCookies();
        if(cookies != null && name != null) {
            for (int i=0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(name)) return cookies[i];
            }
        }
        return null;
    }

    /**
     * Calling this will throw an exception if user isn't logged in
     * @throws ActionDeniedException if user is guest
     */
    public void requireLoggedInUser() throws ActionDeniedException {
        if(getUser().isGuest()) {
            throw new ActionDeniedException("Session expired");
        }
    }

    /**
     * Calling this will throw an exception if user isn't an admin
     * @throws ActionDeniedException if user is not admin
     */
    public void requireAdminUser() throws ActionDeniedException {
        requireLoggedInUser();
        if(!getUser().isAdmin()) {
            throw new ActionDeniedException("Admin only");
        }
    }

    /**
     * Returns an api key for the request
     * @return
     */
    public String getAPIkey() {
        // TODO: use something better than session id
        return getRequest().getSession().getId();
    }

    /**
     * Get play load JSON
     * @return
     */
    public JSONObject getPayLoadJSON() throws ActionParamsException {
        HttpServletRequest req = this.getRequest();
        try (InputStream in = req.getInputStream()) {
            final byte[] json = IOHelper.readBytes(in);
            final String jsonString = new String(json, StandardCharsets.UTF_8);
            return new JSONObject(jsonString);
        } catch (Exception exception) {
            throw new ActionParamsException("Cannot get payload JSON");
        }
    }
}
