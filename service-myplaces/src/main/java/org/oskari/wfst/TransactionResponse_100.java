package org.oskari.wfst;

public class TransactionResponse_100 {

    public enum Status {
        SUCCESS, FAILED, PARTIAL
    }

    private final Status status;
    private final String locator;
    private final String message;
    private final String[] insertedIds;

    public TransactionResponse_100(Status status, String locator,
            String message, String[] insertedIds) {
        this.status = status;
        this.locator = locator;
        this.message = message;
        this.insertedIds = insertedIds;
    }

    public Status getStatus() {
        return status;
    }

    public String getLocator() {
        return locator;
    }

    public String getMessage() {
        return message;
    }

    public String[] getInsertedIds() {
        return insertedIds;
    }

}
