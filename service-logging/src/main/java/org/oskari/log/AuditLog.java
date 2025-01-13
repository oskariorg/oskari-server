package org.oskari.log;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

// AuditLog.user("127.0.0.1", "me@my.com")
//  .withParam("id", 123)
//  .deleted(AuditLog.ResourceType.MYPLACES)
public class AuditLog {

    public enum ResourceType {
        GENERIC,
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
        STATISTICAL_DATA,
        VECTOR_STYLE
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
    private final String user;
    private String message;
    private Map<String, Object> params;

    private enum Ident {
        UNSET,
        EMAIL,
        NICK
    }
    private static Ident userIdentityType = Ident.UNSET;

    private AuditLog(String remote, String user) {
        ip = remote;
        this.user = user;
    }

    public static AuditLog guest(String remote) {
        return user(remote, "");
    }

    public static AuditLog user(String remote, User user) {
        return user(remote, getUserIdentity(user));
    }

    public static AuditLog user(String remote, String user) {
        return new AuditLog(remote, user);
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
    public void wasDenied(ResourceType type) {
        wasDenied(type.name());
    }
    public void wasDenied(String msg) {
        send(Level.WARN, getJSON(msg, Op.UNAUTHORIZED));
    }

    public void errored(ResourceType type) {
        errored(type.name());
    }

    public void errored(String msg) {
        send(Level.WARN, getJSON(msg, Op.ERROR));
    }

    public void usedInvalidParams(ResourceType type) {
        usedInvalidParams(type.name());
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

    private static String getUserIdentity(User user) {
        // guest always null
        if (user.isGuest()) {
            return null;
        }
        // setup for future calls
        if (Ident.UNSET == userIdentityType) {
            if (PropertyUtil.getOptional("audit.user.email", false)) {
                userIdentityType = Ident.EMAIL;
            } else {
                userIdentityType = Ident.NICK;
            }
        }
        // email or username
        if(Ident.EMAIL == userIdentityType) {
            return user.getEmail();
        }
        return user.getScreenname();
    }

    private String getJSON(String resource, Op operation) {
        JSONObject json = JSONHelper.createJSONObject("ip", ip);
        if (user != null && !user.isEmpty()) {
            JSONHelper.putValue(json, "user", user);
        }
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
