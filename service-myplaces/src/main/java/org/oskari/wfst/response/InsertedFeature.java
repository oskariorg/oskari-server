package org.oskari.wfst.response;

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
