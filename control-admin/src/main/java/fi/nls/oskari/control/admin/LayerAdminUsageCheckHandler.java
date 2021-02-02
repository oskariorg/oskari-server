package fi.nls.oskari.control.admin;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

import java.util.*;

@OskariActionRoute("LayerAdminUsageCheck")
public class LayerAdminUsageCheckHandler extends RestActionHandler {
    private static final Logger LOG = LogFactory.getLogger(LayerAdminUsageCheckHandler.class);

    private static final String PARAM_LAYER_ID = "id";
    private OskariLayerService mapLayerService;

    @Override
    public void init() {
        try {
            mapLayerService = OskariComponentManager.getComponentOfType(OskariLayerService.class);
        } catch (Exception e) {
            throw new ServiceRuntimeException("Exception occured while initializing map layer service", e);
        }
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        params.requireAdminUser();
        final int layerId = params.getRequiredParamInt(PARAM_LAYER_ID);
        LOG.info("Checking layer usage in other layers for layerId: ", layerId);
        Map<String, Set<Integer>> layerUsages = new HashMap<>();
        layerUsages.put("timeseries", getTimeseriesLayerIds(layerId));
        ResponseHelper.writeResponse(params, new JSONObject(layerUsages));
    }

    private Set<Integer> getTimeseriesLayerIds(int layerId) throws ActionException {
        Set<Integer> timeseriesLayerIds = new HashSet<>();
        Set<OskariLayer> layers = LayerAdminHelper.getTimeseriesReferencedLayers(layerId, mapLayerService.findAll());
        for (OskariLayer layer : layers) {
            timeseriesLayerIds.add(layer.getId());
        }
        return timeseriesLayerIds;
    }
}
