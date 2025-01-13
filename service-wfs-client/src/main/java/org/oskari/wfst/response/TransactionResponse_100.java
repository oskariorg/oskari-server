package org.oskari.wfst.response;

import java.util.List;

public class TransactionResponse_100 {

    public enum Status {
        SUCCESS, FAILED, PARTIAL
    }

    private final List<InsertedFeature> insertedFeatures;
    private final Status status;
    private final String locator;
    private final String message;

    public TransactionResponse_100(List<InsertedFeature> insertedFeatures,
            Status status, String locator, String message) {
        this.insertedFeatures = insertedFeatures;
        this.status = status;
        this.locator = locator;
        this.message = message;
    }

    public List<InsertedFeature> getInsertedFeatures() {
        return insertedFeatures;
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

}
