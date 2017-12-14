package org.oskari.wfst;

public class InsertedFeature {

    private final String fid;
    private final String handle;

    public InsertedFeature(String fid, String handle) {
        this.fid = fid;
        this.handle = handle;
    }

    public String getFid() {
        return fid;
    }

    public String getHandle() {
        return handle;
    }

}
