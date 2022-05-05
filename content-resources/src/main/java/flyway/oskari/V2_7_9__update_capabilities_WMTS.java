package flyway.oskari;

import org.flywaydb.core.api.migration.Context;

/**
 * This updates the oskari_maplayer.capabilities column in the db
 */
public class V2_7_9__update_capabilities_WMTS extends V2_7_0__update_WMTS_capabilities {
    @Override
    public void migrate(Context context) throws Exception {
        // NO-OP since 2.7.12 updates wmts-layers
    }

}
