package org.oskari.maplayer.model;

import java.util.Map;

/**
 * Used as input data model for layers in the admin UI.
 */
public class MapLayerAdminInput extends MapLayer {
    private Map<Long, Status> vectorStyleStatus;

    public Map<Long, Status> getVectorStyleStatus() {
        return vectorStyleStatus;
    }
    public void setVectorStyleStatus(Map<Long, Status> vectorStyleStatus) {
        this.vectorStyleStatus = vectorStyleStatus;
    }
    public enum Status { NEW, UPDATED, DELETED, NOOP };
}