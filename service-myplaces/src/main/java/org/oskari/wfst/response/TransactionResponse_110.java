package org.oskari.wfst.response;

import java.util.List;

public class TransactionResponse_110 {

    private final int totalInserted;
    private final int totalUpdated;
    private final int totalDeleted;
    private final List<InsertedFeature> insertedFeatures;

    public TransactionResponse_110(int totalInserted, int totalUpdated,
            int totalDeleted, List<InsertedFeature> insertedFeatures) {
        this.totalInserted = totalInserted;
        this.totalUpdated = totalUpdated;
        this.totalDeleted = totalDeleted;
        this.insertedFeatures = insertedFeatures;
    }

    public int getTotalInserted() {
        return totalInserted;
    }

    public int getTotalUpdated() {
        return totalUpdated;
    }

    public int getTotalDeleted() {
        return totalDeleted;
    }

    public List<InsertedFeature> getInsertedFeatures() {
        return insertedFeatures;
    }

}
