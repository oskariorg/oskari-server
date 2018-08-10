package fi.nls.oskari.control.feature;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;

@OskariActionRoute("DeleteFeature")
public class DeleteFeatureHandler extends AbstractFeatureHandler {

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

        JSONObject jsonObject = params.getHttpParamAsJSON("featureData");
        OskariLayer layer = getLayer(jsonObject.optString("layerId"));

        if (!canEdit(layer, params.getUser())) {
            throw new ActionDeniedException("User doesn't have edit permission for layer: " + layer.getId());
        }

        String payload = createPayload(layer.getName(), getFeatureId(jsonObject));
        String responseString = postPayload(layer, payload);
        flushLayerTilesCache(layer.getId());

        if (responseString.indexOf("Exception") > -1) {
            // FIXME: throwing an exception would be better. Just need to sync the change with frontend
            ResponseHelper.writeResponse(params, "Exception");
        } else if (responseString.indexOf("<wfs:totalDeleted>1</wfs:totalDeleted>") > -1) {
            // FIXME: empty response? why?
            ResponseHelper.writeResponse(params, "");
        } else {
            throw new ActionParamsException("Unexpected response from service: " + responseString);
        }
    }

    private String getFeatureId(JSONObject json) throws ActionParamsException {
        try {
            return json.getString("featureId");
        } catch (JSONException e) {
            throw new ActionParamsException("Error getting featureId from payload");
        }
    }

    protected String createPayload(String layerName, String featureId) {
        // TODO: rewrite
        StringBuilder requestData = new StringBuilder(
                "<wfs:Transaction service='WFS' version='1.1.0'" +
                        " xmlns:ogc='http://www.opengis.net/ogc'" +
                        " xmlns:wfs='http://www.opengis.net/wfs'>" +
                        "<wfs:Delete typeName='" + layerName + "'>" +
                            "<ogc:Filter><ogc:FeatureId fid='" + featureId + "'/></ogc:Filter>" +
                        "</wfs:Delete></wfs:Transaction>");
        return requestData.toString();

    }
}

