package fi.nls.oskari.service;

public class ServiceRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;
    private String messageKey = "unknown";
    private String level = "error";

	public ServiceRuntimeException(final String message, final Throwable e) {
        super(message, e);
    }

    public ServiceRuntimeException(final String message, final Throwable e, final String key, final String level) {
        super(message, e);
        this.messageKey = key;
        this.level = level;
    }
    public ServiceRuntimeException(final String message, final Throwable e, final String key) {
        super(message, e);
        this.messageKey = key;
    }
    public ServiceRuntimeException(final String message) {
        super(message);
    }

    public ServiceRuntimeException(final String message, final String key) {
        super(message);
        this.messageKey = key;
    }

    public ServiceRuntimeException(final String message, final String key, final String level) {
        super(message);
        this.messageKey = key;
        this.level = level;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getLevel() {
        return level;
    }
}