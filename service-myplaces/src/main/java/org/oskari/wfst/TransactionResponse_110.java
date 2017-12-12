package org.oskari.wfst;

public class TransactionResponse_110 {

    private final int totalInserted;
    private final int totalUpdated;
    private final int totalDeleted;
    private final String[] insertedIds;

    public TransactionResponse_110(int totalInserted, int totalUpdated,
            int totalDeleted, String[] insertedIds) {
        this.totalInserted = totalInserted;
        this.totalUpdated = totalUpdated;
        this.totalDeleted = totalDeleted;
        this.insertedIds = insertedIds;
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

    public String[] getInsertedIds() {
        return insertedIds;
    }

}
