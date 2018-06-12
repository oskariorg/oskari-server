package fi.nls.oskari.control.feature;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.db.DBHandler;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@OskariActionRoute("SaveFeature")
public class SaveFeatureHandler extends AbstractFeatureHandler {
    private static Logger log = LogFactory.getLogger(DBHandler.class);

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        JSONObject jsonObject = params.getHttpParamAsJSON("featureData");
        OskariLayer layer = getLayer(jsonObject.optString("layerId"));

        if (!canEdit(layer, params.getUser())) {
            throw new ActionDeniedException("User doesn't have edit permission for layer: " + layer.getId());
        }
        try {
            String srsName = JSONHelper.getStringFromJSON(jsonObject, "srsName", "http://www.opengis.net/gml/srs/epsg.xml#3067");
            WFSLayerConfiguration lc = getWFSConfiguration(layer.getId());

            // TODO: rewrite to use wfs-t related code under myplaces OR atleast using an xml lib
            StringBuilder requestData = new StringBuilder(
                    "<wfs:Transaction service='WFS' version='1.1.0' " +
                            "xmlns:ogc='http://www.opengis.net/ogc' " +
                            "xmlns:wfs='http://www.opengis.net/wfs'>" +
                            "<wfs:Update typeName='" + layer.getName() + "'>");
            JSONArray jsonArray = jsonObject.getJSONArray("featureFields");
            for (int i = 0; i < jsonArray.length(); i++) {
                requestData.append("<wfs:Property><wfs:Name>" + jsonArray.getJSONObject(i).getString("key") +
                        "</wfs:Name><wfs:Value>" + jsonArray.getJSONObject(i).getString("value") +
                        "</wfs:Value></wfs:Property>");
            }

            if (jsonObject.has("geometries")) {
                insertGeometries(lc.getGMLGeometryProperty(), requestData, jsonObject.getJSONObject("geometries"), srsName);
            }
            requestData.append("<ogc:Filter><ogc:FeatureId fid='" + jsonObject.getString("featureId") + "'/></ogc:Filter></wfs:Update></wfs:Transaction>");

            String responseString = postPayload(layer, requestData.toString());
            flushLayerTilesCache(layer.getId());

            if (responseString.indexOf("Exception") > -1) {
                ResponseHelper.writeResponse(params, "Exception");
            } else if (responseString.indexOf("<wfs:totalUpdated>1</wfs:totalUpdated>") > -1) {
                ResponseHelper.writeResponse(params, "");
            }
        } catch (JSONException ex) {
            log.error(ex, "JSON processing error");
            throw new ActionException("JSON processing error", ex);
        }
    }

    protected void insertGeometries(String geometryProperty, StringBuilder requestData, JSONObject geometries, String srsName) throws ActionParamsException, JSONException {
        String geometryType = geometries.getString("type");
        if(!isAllowedGeomType(geometryType)) {
            throw new ActionParamsException("Invalid geometry type: " + geometryProperty);
        }
        JSONArray data = geometries.getJSONArray("data");
        requestData.append("<wfs:Property><wfs:Name>");
        requestData.append(geometryProperty);
        requestData.append("</wfs:Name><wfs:Value>");
        requestData.append(getGeometry(geometryType, data, srsName));
        requestData.append("</wfs:Value></wfs:Property>");
    }
}

