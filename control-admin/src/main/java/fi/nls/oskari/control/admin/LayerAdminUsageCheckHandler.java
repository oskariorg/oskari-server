package fi.nls.oskari.control.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

@OskariActionRoute("LayerAdminUsageCheck")
public class LayerAdminUsageCheckHandler extends RestActionHandler {
    private static final Logger LOG = LogFactory.getLogger(LayerAdminUsageCheckHandler.class);

    private static final String PARAM_LAYER_ID = "id";
    private OskariLayerService mapLayerService;
    private ObjectMapper mapper = new ObjectMapper();

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
        final int layerId = params.getRequiredParamInt(PARAM_LAYER_ID);
        LOG.info("Checking layer usage in other layers for layerId: ", layerId);
        Map<String, Set<Integer>> layerUsages = new HashMap<>();
        Set<Integer> timeseriesLayerIds = new HashSet<>();
        for (OskariLayer layer : mapLayerService.findAll()) {
            JSONObject options = layer.getOptions();
            try {
                if (options != null && options.has("timeseries")) {
                    JSONObject timeseriesOptions = options.getJSONObject("timeseries");
                    Integer metadataLayerId = timeseriesOptions.getJSONObject("metadata").getInt("layer");
                    if (metadataLayerId == layerId) {
                        timeseriesLayerIds.add(layer.getId());
                    }
                }
            } catch (JSONException e) {
                throw new ActionException("Cannot parse layer metadata options for layer: " +
                        layer.getName() + ", id: " + layer.getId());
            }

        }
        layerUsages.put("timeseries", timeseriesLayerIds);
        writeResponse(params, layerUsages);
    }

    private void writeResponse(ActionParameters params, Object output) {
        params.getResponse().setCharacterEncoding("UTF-8");
        params.getResponse().setContentType("application/json;charset=UTF-8");
        try {
            ResponseHelper.writeResponse(params, mapper.writeValueAsString(output));
        } catch (JsonProcessingException e) {
            ResponseHelper.writeError(params, "Couldn't serialize JSON");
        }
    }

}
