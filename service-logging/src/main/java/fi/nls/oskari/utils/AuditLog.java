package fi.nls.oskari.utils;

public class AuditLog extends Log4JLogger {

    private static final AuditLog instance = new AuditLog();

    private AuditLog() {
        super("AUDIT");
    }

    public static AuditLog get() {
        return instance;
    }

    public String addUserInfo(String remote, String email) {
        return null;
    }
}
