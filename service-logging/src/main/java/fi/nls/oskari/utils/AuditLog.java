package fi.nls.oskari.utils;

import fi.nls.oskari.domain.User;

import java.util.LinkedHashMap;
import java.util.Map;

// AuditLog.user("127.0.0.1", "me@my.com")
//  .withParam("id", 123)
//  .deleted("My places")
public class AuditLog {

    private static final Log4JLogger LOGGER = new Log4JLogger("AUDIT");
    private final String ip;
    private final String email;
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

    /*
     * Actual logging. Probably write out JSON so it can be parsed easily
     */
    public void wasDenied(String msg) {
        LOGGER.warn(email, ip, "unauthorized", msg, params);
    }

    public void errored(String msg) {
        LOGGER.warn(email, ip, "unsuccessful", msg, params);
    }

    public void usedInvalidParams(String msg) {
        LOGGER.warn(email, ip, "invalid params", msg, params);
    }

    public void added(String msg) {
        LOGGER.info(email, ip, "added", msg, params);
    }

    public void updated(String msg) {
        LOGGER.info(email, ip, "updated", msg, params);
    }

    public void deleted(String msg) {
        LOGGER.info(email, ip, "deleted", msg, params);
    }
}
