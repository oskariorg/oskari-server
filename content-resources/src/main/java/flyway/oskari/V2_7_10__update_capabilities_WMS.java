package flyway.oskari;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONObject;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.CapabilitiesUpdateResult;
import org.oskari.helpers.AppSetupHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This updates the oskari_maplayer.capabilities column in the db
 */
public class V2_7_10__update_capabilities_WMS extends V2_7_0__update_WMTS_capabilities {
    @Override
    public void migrate(Context context) throws Exception {
        Logger log = LogFactory.getLogger(V2_7_1__update_WMS_capabilities.class);
        Connection connection = context.getConnection();
        List<OskariLayer> layers = getLayers(connection, OskariLayer.TYPE_WMS);
        Set<String> systemCRS = getSystemCRS(connection);
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

    protected Set<String> getSystemCRS(Connection conn) throws SQLException {
        List<Long> appsetups = AppSetupHelper.getSetupsForUserAndDefaultType(conn);

        return appsetups.stream()
                .map(appId -> getMapfullBundle(conn, appId))
                .filter(i -> i != null)
                .map(bundle -> getSRSFromMapfullConfig(bundle.getConfigJSON()))
                .filter(i -> i != null)
                .collect(Collectors.toSet());
    }

    private Bundle getMapfullBundle(Connection conn, long appId) {
        try {
            return AppSetupHelper.getAppBundle(conn, appId, "mapfull");
        } catch (SQLException e) {
            return null;
        }
    }

    private String getSRSFromMapfullConfig(JSONObject conf) {
        if (conf == null) {
            return null;
        }
        JSONObject opts = conf.optJSONObject("mapOptions");
        if (opts == null) {
            return null;
        }
        return opts.optString("srsName", null);
    }


}
