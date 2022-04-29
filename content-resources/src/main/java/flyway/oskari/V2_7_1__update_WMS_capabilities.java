package flyway.oskari;

import org.flywaydb.core.api.migration.Context;

/**
 * A layer specific JSON is now saved to db as capabilities.
 * This updates the oskari_maplayer.capabilities column in the db
 */
public class V2_7_1__update_WMS_capabilities extends V2_7_0__update_WMTS_capabilities {

    @Override
    public void migrate(Context context) throws Exception {
        // NO-OP since 2.7.10 updates wms-layers
    }
}
