package fi.nls.oskari.service.capabilities;

import java.sql.Timestamp;

/**
 * Simple wrapper class for values returning from CapabilitiesMapper#insert()
 */
public class OskariLayerCapabilitiesInsertInfo {

    protected final long id;
    protected final Timestamp created;
    protected final Timestamp updated;

    protected OskariLayerCapabilitiesInsertInfo(final long id, final Timestamp created, final Timestamp updated) {
        this.id = id;
        this.created = created;
        this.updated = updated;
    }

}
