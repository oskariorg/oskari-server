package fi.nls.oskari.utils;

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

    public static AuditLog user(String remote, String email) {
        return new AuditLog(remote, email);
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
