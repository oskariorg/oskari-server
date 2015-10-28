package flyway.oskari;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by SMAKINEN on 25.8.2015.
 */
public class V1_32_1__populate_capabilities_cache implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_32_1__populate_capabilities_cache.class);

    private static final OskariLayerService LAYER_SERVICE = new OskariLayerServiceIbatisImpl();
    private static final CapabilitiesCacheService CAPABILITIES_SERVICE = OskariComponentManager.getComponentOfType(CapabilitiesCacheService.class);

    public void migrate(Connection connection) {
        List<OskariLayer> layers = LAYER_SERVICE.findAll();

        LOG.info("Start populating capabilities for layers - count:", layers.size());
        Set<String> keys = new HashSet<>();
        int progress = 0;
        for(OskariLayer layer : layers) {
            try {
                final String layerKey = (layer.getSimplifiedUrl(true) + "----" + layer.getType()).toLowerCase();
                if(keys.contains(layerKey)) {
                    progress++;
                    continue;
                }
                keys.add(layerKey);
                CAPABILITIES_SERVICE.getCapabilities(layer);
                progress++;
                LOG.info("Capabilities populated:", progress, "/", layers.size());
            } catch (ServiceException e) {
                LOG.error(e, "Error getting capabilities for layer", layer);
            }
        }
    }
}
