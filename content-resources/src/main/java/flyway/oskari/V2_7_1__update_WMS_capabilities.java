package flyway.oskari;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.util.ViewHelper;
import fi.nls.oskari.service.OskariComponentManager;
import org.flywaydb.core.api.migration.Context;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.CapabilitiesUpdateResult;

import java.sql.*;
import java.util.*;

/**
 * A layer specific JSON is now saved to db to get matrix sets etc directly for openlayers.
 * This updates the oskari_maplayer.capabilities column in the db
 */
public class V2_7_1__update_WMS_capabilities extends V2_7_0__update_WMTS_capabilities {

    @Override
    public void migrate(Context context) throws Exception {
        Logger log = LogFactory.getLogger(V2_7_1__update_WMS_capabilities.class);
        Connection connection = context.getConnection();
        List<OskariLayer> layers = getLayers(connection, OskariLayer.TYPE_WMS);
        Set<String> systemCRS = ViewHelper.getSystemCRSs(OskariComponentManager.getComponentOfType(ViewService.class));
        List<CapabilitiesUpdateResult> results = CapabilitiesService.updateCapabilities(layers, systemCRS);

        Map<String, OskariLayer> layersById = new HashMap<>(layers.size());
        layers.forEach(l -> {
            layersById.put(Integer.toString(l.getId()), l);
        });
        for (CapabilitiesUpdateResult res : results) {
            if (res.getErrorMessage() != null) {
                log.warn( "Capabilities update error for layer:", res.getLayerId(), " - Error:", res.getErrorMessage());
                continue;
            }
            layers.stream()
                    .filter(l -> res.getLayerId().equals(Integer.toString(l.getId())))
                    .forEach(l -> {
                        try {
                            updateCapabilities(connection, l.getId(), l.getCapabilities());
                        } catch (SQLException e) {
                            log.warn(e, "Error updating db");
                        }
                    });
        }
    }
}
