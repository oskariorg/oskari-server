package org.oskari.capabilities;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceFactory;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMS;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMTS;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheServiceMybatisImpl;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.wmts.WMTSCapabilitiesParser;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import fi.nls.oskari.worker.ScheduledJob;

/**
 * Updates layer capabilities
 */
@Oskari("UpdateCapabilities")
public class UpdateCapabilitiesJob extends ScheduledJob {

    private static final Logger LOG = LogFactory.getLogger(UpdateCapabilitiesJob.class);
    
    private OskariLayerService layerService;
    private CapabilitiesCacheService capabilitiesService;
    
    public UpdateCapabilitiesJob() {
        this(new OskariLayerServiceIbatisImpl(), new CapabilitiesCacheServiceMybatisImpl());
    }
    
    public UpdateCapabilitiesJob(OskariLayerService layerService, CapabilitiesCacheService capabilitiesService) {
        this.layerService = layerService;
        this.capabilitiesService = capabilitiesService;
    }
    
    @Override
    public void execute(Map<String, Object> params) {
        updateCapabilities();
    }
    
    protected void updateCapabilities() {
        for (OskariLayer layer : layerService.findAll()) {
            updateCapabilities(layer);
        }
    }
    
    protected void updateCapabilities(OskariLayer layer) {
        if (layer == null) {
            return;
        }

        switch (layer.getType()) {
        case OskariLayer.TYPE_WMS:
        case OskariLayer.TYPE_WMTS:
            JSONObject capabilitiesJSON = getCapabilitiesJSON(layer);
            if (capabilitiesJSON != null) {
                layer.setCapabilities(capabilitiesJSON);
            }
            break;
        default:
            LOG.info("Skipping layer with type: ", layer.getType());
            break;
        }
    }

    protected JSONObject getCapabilitiesJSON(OskariLayer layer) {
        try {
            OskariLayerCapabilities capabilities = capabilitiesService.getCapabilities(layer, true);
            // flush cache, otherwise only db is updated but code retains the old cached version
            WebMapServiceFactory.flushCache(layer.getId());

            switch (layer.getType()) {
            case OskariLayer.TYPE_WMS:
                return getCapabilitiesJSON_WMS(layer, capabilities);
            case OskariLayer.TYPE_WMTS:
                return getCapabilitiesJSON_WMTS(layer, capabilities);
            default:
                return null;
            }
        } catch (ServiceException e) {
            return null;
        }
    }

    private JSONObject getCapabilitiesJSON_WMS(OskariLayer layer, OskariLayerCapabilities capabilities) {
        WebMapService wms = WebMapServiceFactory.createFromXML(layer.getName(), capabilities.getData());
        if (wms == null) {
            LOG.warn("Failed to parse WMS capabilities, layer: ", layer.getName());
        }
        return LayerJSONFormatterWMS.createCapabilitiesJSON(wms);
    }

    private static JSONObject getCapabilitiesJSON_WMTS(OskariLayer layer, OskariLayerCapabilities capabilities) {
        try {
            WMTSCapabilities caps = new WMTSCapabilitiesParser().parseCapabilities(capabilities.getData());
            return LayerJSONFormatterWMTS.createCapabilitiesJSON(caps, caps.getLayer(layer.getName()));
        } catch (Exception e) {
            LOG.warn(e, "Failed to parse WMTS capabilities, layer: ", layer.getName());
            return null;
        }
    }

}
