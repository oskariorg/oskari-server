package fi.nls.oskari.utils;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

// AuditLog.user("127.0.0.1", "me@my.com")
//  .withParam("id", 123)
//  .deleted(AuditLog.ResourceType.MYPLACES)
public class AuditLog {

    public enum ResourceType {
        MAPLAYER,
        MAPLAYER_GROUP,
        MAPLAYER_PERMISSION,
        DATAPROVIDER,
        MYPLACES,
        MYPLACES_LAYER,
        USERLAYER,
        ANALYSIS,
        SYSTEM_VIEW,
        USER_VIEW,
        EMBEDDED_VIEW,
        TERMS_OF_USE,
        SEARCH,
        USER,
        STATISTICAL_DATA
    }

    enum Op {
        ERROR,
        INVALID_PARAMS,
        UNAUTHORIZED,
        ADDED,
        UPDATED,
        DELETED
    }

    enum Level {
        INFO,
        WARN
    }

    private static final Logger LOGGER = LogFactory.getLogger("AUDIT");
    private final String ip;
    private final String email;
    private String message;
    private Map<String, Object> params;

    private AuditLog(String remote, String email) {
        ip = remote;
        this.email = email;
    }

    public static AuditLog guest(String remote) {
        return user(remote, "");
    }

    public static AuditLog user(String remote, User user) {
        String email = null;
        if (!user.isGuest()) {
            email = user.getEmail();
        }
        return user(remote, email);
    }

    public static AuditLog user(String remote, String email) {
        return new AuditLog(remote, email);
    }

    public AuditLog withParams(Map<String, String[]> params) {
        if (params == null) {
            return this;
        }
        params.entrySet().stream()
                .forEach(p -> withParam(p.getKey(), p.getValue()));
        return this;
    }

    public AuditLog withParam(String key, Object value) {
        if (params == null) {
            params = new LinkedHashMap();
        }
        params.put(key, value);
        return this;
    }

    public AuditLog withMsg(String msg) {
        message = msg;
        return this;
    }

    /*
     * Actual logging. Probably write out JSON so it can be parsed easily
     */
    public void wasDenied(String msg) {
        send(Level.WARN, getJSON(msg, Op.UNAUTHORIZED));
    }

    public void errored(ResourceType type) {
        errored(type.name());
    }

    public void errored(String msg) {
        send(Level.WARN, getJSON(msg, Op.ERROR));
    }

    public void usedInvalidParams(String msg) {
        send(Level.INFO, getJSON(msg, Op.INVALID_PARAMS));
    }

    public void added(ResourceType type) {
        added(type.name());
    }

    public void added(String msg) {
        send(Level.INFO, getJSON(msg, Op.ADDED));
    }

    public void updated(ResourceType type) {
        updated(type.name());
    }

    public void updated(String msg) {
        send(Level.INFO, getJSON(msg, Op.UPDATED));
    }

    public void deleted(ResourceType type) {
        deleted(type.name());
    }

    public void deleted(String msg) {
        send(Level.INFO, getJSON(msg, Op.DELETED));
    }

    private String getJSON(String resource, Op operation) {
        JSONObject json = JSONHelper.createJSONObject("ip", ip);
        JSONHelper.putValue(json, "email", email);
        JSONHelper.putValue(json, "resource", resource);
        JSONHelper.putValue(json, "op", operation.name());
        if (params != null) {
            JSONHelper.putValue(json, "params", params);
        }
        if (message != null) {
            JSONHelper.putValue(json, "msg", message);
        }
        return json.toString();
    }

    private void send(Level lvl, String json) {
        switch (lvl) {
            case INFO:
                LOGGER.info(json);
                break;
            case WARN:
                LOGGER.warn(json);
                break;
            default:
                LOGGER.debug(json);
        }
    }
}
