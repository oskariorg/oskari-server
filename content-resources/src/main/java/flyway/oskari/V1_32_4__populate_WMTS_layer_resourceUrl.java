package flyway.oskari;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wmts.WMTSCapabilitiesParser;
import fi.nls.oskari.wmts.domain.ResourceUrl;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import fi.nls.oskari.wmts.domain.WMTSCapabilitiesLayer;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.util.List;

/**
 * Generates resource URL information for WMTS layers
 */
public class V1_32_4__populate_WMTS_layer_resourceUrl implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_32_4__populate_WMTS_layer_resourceUrl.class);

    private static final OskariLayerService LAYER_SERVICE = new OskariLayerServiceIbatisImpl();
    private static final CapabilitiesCacheService CAPABILITIES_SERVICE = OskariComponentManager.getComponentOfType(CapabilitiesCacheService.class);
    private static final WMTSCapabilitiesParser PARSER = new WMTSCapabilitiesParser();

    public void migrate(Connection connection) {
        List<OskariLayer> layers = LAYER_SERVICE.findAll();

        LOG.info("Start generating resource URLS for WMTS layers - count:", layers.size());
        int progress = 0;
        for(OskariLayer layer : layers) {

            if(!OskariLayer.TYPE_WMTS.equalsIgnoreCase(layer.getType())) {
                // only process wmts-layers
                continue;
            }
            try {
                // update
                OskariLayerCapabilities caps = CAPABILITIES_SERVICE.getCapabilities(layer);
                WMTSCapabilities parsed = PARSER.parseCapabilities(caps.getData());
                WMTSCapabilitiesLayer capsLayer = parsed.getLayer(layer.getName());
                ResourceUrl resUrl = capsLayer.getResourceUrlByType("tile");
                if(resUrl != null) {
                    JSONHelper.putValue(layer.getOptions(), "requestEncoding", "REST");
                    JSONHelper.putValue(layer.getOptions(), "format", resUrl.getFormat());
                    JSONHelper.putValue(layer.getOptions(), "urlTemplate", resUrl.getTemplate());
                }
                LAYER_SERVICE.update(layer);
                progress++;
                LOG.info("Capabilities populated:", progress, "/", layers.size());
            } catch (Exception e) {
                LOG.error(e, "Error getting capabilities for layer", layer);
            }
        }
    }
}
