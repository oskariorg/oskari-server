package fi.nls.oskari.view.modifier;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import fi.nls.oskari.domain.User;

/**
 * A wrapper class for the request information needed by ViewModifiers.
 * GetAppSetupHandler populates this so ViewModifiers can alter the startupsequence
 * and/or configuration based on the request.
 */
public class ModifierParams {

    private String referer;
    private String baseAjaxUrl;
    private String ajaxRouteParamName;
    private String paramValue;
    private JSONObject config;
    private JSONArray startupSequence;
    private Locale locale;
    private String clientIP;
    private String viewType;
    private User user;
    private long viewId;
    private boolean locationModified = false;
    private boolean oldPublishedMap = false;
    private boolean modifyURLs = false;

    /**
     * Boolean switch if urls should be provided as configured or modified to match proxy forwarding rules.
     * @return true if URLs should be modified
     */
    public boolean isModifyURLs() {
        return modifyURLs;
    }

    /**
     * Boolean switch if urls should be provided as configured or modified to match proxy forwarding rules.
     * @param modifyURLs true to modify URLs, false to keep URLs as is
     */
    public void setModifyURLs(boolean modifyURLs) {
        this.modifyURLs = modifyURLs;
    }

    /**
     * Referer information from the request
     * @return
     */
    public String getReferer() {
        return referer;
    }
    public void setReferer(String referer) {
        this.referer = referer;
    }

    /**
     * AjaxUrl to use for this request (could be locale specific)
     * @return
     */
    public String getBaseAjaxUrl() {
        return baseAjaxUrl;
    }
    public void setBaseAjaxUrl(String baseAjaxUrl) {
        this.baseAjaxUrl = baseAjaxUrl;
    }

    /**
     * Value of the parameter to process for ViewModifiers of type ParamHandler
     * @return
     */
    public String getParamValue() {
        return paramValue;
    }
    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    /**
     * Configuration JSONObject for the view to be modified
     * @return
     */
    public JSONObject getConfig() {
        return config;
    }
    public void setConfig(JSONObject config) {
        this.config = config;
    }
    public Locale getLocale() {
        return locale;
    }
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    /**
     * IP of the client from the request
     * @return
     */
    public String getClientIP() {
        return clientIP;
    }
    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }
    /**
     * Type of the view we are modifying
     * @see fi.nls.oskari.domain.map.view.ViewTypes
     */
    public String getViewType() {
        return viewType;
    }
    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    /**
     * The user from the request
     * @return
     */
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Id for the view we are modifying
     * @return
     */
    public long getViewId() {
        return viewId;
    }
    public void setViewId(long viewId) {
        this.viewId = viewId;
    }
    /**
     * StartupSequence (bundle load order) as a JSONArray for the view to be modified
     * @return
     */
    public JSONArray getStartupSequence() {
        return startupSequence;
    }
    public void setStartupSequence(JSONArray startupSequence) {
        this.startupSequence = startupSequence;
    }

    /**
     * Information if any modifier has modified the map location.
     * Usably f.ex. if we don't want to provide geolocation support
     * when user has specified a location
     * @return
     */
    public boolean isLocationModified() {
        return locationModified;
    }
    public void setLocationModified(boolean locationModified) {
        this.locationModified = locationModified;
    }

    /**
     * True if this is a migrated map.
     * @return
     */
    public boolean isOldPublishedMap() {
        return oldPublishedMap;
    }
    public void setOldPublishedMap(boolean oldPublishedMap) {
        this.oldPublishedMap = oldPublishedMap;
    }
    public String getAjaxRouteParamName() {
        return ajaxRouteParamName;
    }
    public void setAjaxRouteParamName(String ajaxRouteParamName) {
        this.ajaxRouteParamName = ajaxRouteParamName;
    }
}
