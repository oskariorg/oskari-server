package fi.nls.oskari.transport;

public class TransportJobException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private String messageKey = "unknown";
    private String level = "error";


    public TransportJobException(final String message, final Throwable e) {
        super(message, e);
    }

    public TransportJobException(final String message, final Throwable e, final String key) {
        super(message, e);
        this.messageKey = key;
    }


    public TransportJobException(final String message, final Throwable e, final String key,  final String level) {
        super(message, e);
        this.messageKey = key;
        this.level = level;
    }

    public TransportJobException(final String message) {
        super(message);
    }

    public TransportJobException(final String message, final String key) {
        super(message);
        this.messageKey = key;
    }

    public TransportJobException(final String message, final String key,  final String level) {
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
