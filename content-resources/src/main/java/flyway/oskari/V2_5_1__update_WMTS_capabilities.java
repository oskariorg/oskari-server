package flyway.oskari;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * A layer specific JSON is now saved to db to get matrix sets etc directly for openlayers.
 * This updates the oskari_maplayer.capabilities column in the db
 */
public class V2_5_1__update_WMTS_capabilities extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        // There's a newer migration that will populate WMTS-capabilities (2.7.0)
        // no need to do it twice
    }
}
