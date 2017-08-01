package fi.nls.oskari.control.layer;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceFactory;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMS;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMTS;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.util.ServiceFactory;
import fi.nls.oskari.wmts.WMTSCapabilitiesParser;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;

/**
 * Updates layer capabilities
 */
@OskariActionRoute("UpdateLayerCapabilities")
public class UpdateLayerCapabilitiesHandler extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(UpdateLayerCapabilitiesHandler.class);
    private static final String PARAM_ID = "id";

    private final OskariLayerService layerService;
    private final CapabilitiesCacheService capabilitiesService;

    public UpdateLayerCapabilitiesHandler() {
        this(ServiceFactory.getMapLayerService(), 
                ServiceFactory.getCapabilitiesCacheService());
    }

    public UpdateLayerCapabilitiesHandler(OskariLayerService layerService, 
            CapabilitiesCacheService capabilitiesService) {
        this.layerService = layerService;
        this.capabilitiesService = capabilitiesService;
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        try {
            String id = params.getHttpParam(PARAM_ID);
            if (id == null || id.length() == 0) {
                updateCapabilities(layerService.findAll());
            } else {
                if (id.indexOf(',') >= 0) {
                    updateCapabilities(layerService.find(Arrays.asList(id.split(","))));
                } else {
                    updateCapabilities(layerService.find(id));
                }
            }
        } catch (ServiceException e) {
            LOG.info(e);
            throw new ActionException(e.getMessage(), e);
        }
    }

    private void updateCapabilities(final List<OskariLayer> layers) throws ServiceException {
        if (layers != null) {
            for (OskariLayer layer : layers) {
                updateCapabilities(layer);
            }
        }
    }

    private void updateCapabilities(OskariLayer layer) throws ServiceException {
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
        }
    }

    private JSONObject getCapabilitiesJSON(OskariLayer layer) throws ServiceException {
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
    }

    private JSONObject getCapabilitiesJSON_WMS(OskariLayer layer, OskariLayerCapabilities capabilities) throws ServiceException {
        WebMapService wms = WebMapServiceFactory.createFromXML(layer.getName(), capabilities.getData());
        if (wms == null) {
            throw new ServiceException("Couldn't parse capabilities for service!");
        }
        return LayerJSONFormatterWMS.createCapabilitiesJSON(wms);
    }

    private JSONObject getCapabilitiesJSON_WMTS(OskariLayer layer, OskariLayerCapabilities capabilities) throws ServiceException {
        try {
            WMTSCapabilities caps = new WMTSCapabilitiesParser().parseCapabilities(capabilities.getData());
            return LayerJSONFormatterWMTS.createCapabilitiesJSON(caps, caps.getLayer(layer.getName()));
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

}
