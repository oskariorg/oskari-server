package flyway.oskari;

import org.flywaydb.core.api.migration.Context;

/**
 * This updates the oskari_maplayer.capabilities column in the db
 */
public class V2_7_9__update_capabilities_WMTS extends V2_7_0__update_WMTS_capabilities {
    @Override
    public void migrate(Context context) throws Exception {
        // There's a newer migration that will populate WMTS-capabilities (2.7.0)
        // no need to do it twice
    }

}
