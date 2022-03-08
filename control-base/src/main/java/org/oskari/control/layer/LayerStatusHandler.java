package org.oskari.control.layer;

import fi.nls.oskari.control.*;
import org.oskari.control.layer.status.LayerStatusService;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;

@OskariActionRoute("LayerStatus")
public class LayerStatusHandler extends RestActionHandler {

    private LayerStatusService getService() {
        return OskariComponentManager.getComponentOfType(LayerStatusService.class);
    }

    public void handleGet(ActionParameters params) throws ActionDeniedException {
        params.requireAdminUser();
        String layerId = params.getHttpParam("id");
        if (layerId == null) {
            writeListing(params);
        } else {
            LayerStatusService service = getService();
            ResponseHelper.writeResponse(params, service.getDetails(layerId));
        }
    }

    private void writeListing(ActionParameters params) {
        LayerStatusService service = getService();
        final JSONObject response = new JSONObject();
        service.getStatuses().forEach(status -> {
            try {
                JSONObject value = status.asJSON();
                value.remove("id");
                response.put(status.getId(), value);
            } catch (JSONException ignored) {}
        });
        ResponseHelper.writeResponse(params, response);
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionParamsException {
        JSONObject payload = params.getPayLoadJSON();
        LayerStatusService service = getService();
        service.saveStatus(payload);
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        params.requireAdminUser();
        String layerId = params.getRequiredParam("id");
        String dataId = params.getHttpParam("dataId");
        if (dataId == null) {
            getService().removeLayerStatus(layerId);
        } else {
            getService().removeLayerRawData(layerId, dataId);
        }
        ResponseHelper.writeResponse(params, "OK");
    }
}