package fi.nls.oskari.control.feature;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.db.DBHandler;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

@OskariActionRoute("SaveFeature")
public class SaveFeatureHandler extends AbstractFeatureHandler {
    public final static String KEY = "WFSImage_";
    private static Logger log = LogFactory.getLogger(DBHandler.class);
    private String geometryProperty;

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

            geometryProperty = lc.getGMLGeometryProperty();

            StringBuilder requestData = new StringBuilder("<wfs:Transaction service='WFS' version='1.1.0' xmlns:ogc='http://www.opengis.net/ogc' xmlns:wfs='http://www.opengis.net/wfs'><wfs:Update typeName='" + lay.getName() + "'>");
            JSONArray jsonArray = jsonObject.getJSONArray("featureFields");
            for (int i = 0; i < jsonArray.length(); i++) {
                requestData.append("<wfs:Property><wfs:Name>" + jsonArray.getJSONObject(i).getString("key") + "</wfs:Name><wfs:Value>" + jsonArray.getJSONObject(i).getString("value") + "</wfs:Value></wfs:Property>");
            }

            if (jsonObject.has("geometries")) {
                FillGeometries(requestData, jsonObject.getJSONObject("geometries"), srsName);
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
        } catch (ClientProtocolException ex) {
            log.error(ex, "Geoserver connection error");
            throw new ActionException("Geoserver connection error", ex);
        } catch (IOException ex) {
            log.error(ex, "IO error");
            throw new ActionException("IO error", ex);
        }
    }

    private void FillGeometries(StringBuilder requestData, JSONObject geometries, String srsName) throws JSONException {
        String geometryType = geometries.getString("type");
        if (geometryType.equals("multipoint")) {
            FillMultiPointGeometries(requestData, geometries, srsName);
        } else if (geometryType.equals("multilinestring")) {
            FillLineStringGeometries(requestData, geometries, srsName);
        } else if (geometryType.equals("multipolygon")) {
            FillPolygonGeometries(requestData, geometries, srsName);
        }
    }

    private void FillMultiPointGeometries(StringBuilder requestData, JSONObject geometries, String srsName) throws JSONException {
        JSONArray data = geometries.getJSONArray("data");
        requestData.append("<wfs:Property><wfs:Name>" + geometryProperty
                + "</wfs:Name><wfs:Value><gml:MultiPoint xmlns:gml='http://www.opengis.net/gml' srsName='" + srsName + "'>");
        for (int i = 0; i < data.length(); i++) {
            requestData.append("<gml:pointMember><gml:Point><gml:coordinates decimal=\".\" cs=\",\" ts=\" \">"
                    + data.getJSONObject(i).getString("x") + "," + data.getJSONObject(i).getString("y")
                    + "</gml:coordinates></gml:Point></gml:pointMember>");
        }
        requestData.append("</gml:MultiPoint></wfs:Value></wfs:Property>");
    }

    private void FillLineStringGeometries(StringBuilder requestData, JSONObject geometries, String srsName) throws JSONException {
        JSONArray data = geometries.getJSONArray("data");
        requestData.append("<wfs:Property><wfs:Name>" + geometryProperty
                + "</wfs:Name><wfs:Value><gml:MultiLineString xmlns:gml='http://www.opengis.net/gml' srsName='" + srsName + "'>");
        for (int i = 0; i < data.length(); i++) {
            requestData
                    .append("<gml:lineStringMember><gml:LineString><gml:coordinates decimal=\".\" cs=\",\" ts=\" \">");
            JSONArray arr = data.getJSONArray(i);
            for (int j = 0; j < arr.length(); j++) {
                requestData.append(arr.getJSONObject(j).getString("x") + "," + arr.getJSONObject(j).getString("y"));
                if (j < (arr.length() - 1)) {
                    requestData.append(" ");
                }
            }
            requestData.append("</gml:coordinates></gml:LineString></gml:lineStringMember>");
        }
        requestData.append("</gml:MultiLineString></wfs:Value></wfs:Property>");
    }

    private void FillPolygonGeometries(StringBuilder requestData, JSONObject geometries, String srsName) throws JSONException {
        JSONArray data = geometries.getJSONArray("data");
        requestData.append("<wfs:Property><wfs:Name>" + geometryProperty
                + "</wfs:Name><wfs:Value><gml:MultiPolygon xmlns:gml='http://www.opengis.net/gml' srsName='" + srsName + "'>");
        for (int i = 0; i < data.length(); i++) {
            requestData.append("<gml:polygonMember><gml:Polygon>");
            JSONArray arr = data.getJSONArray(i);
            for (int j = 0; j < arr.length(); j++) {
                if (j > 0) {
                    requestData.append("<gml:interior><gml:LinearRing><gml:posList>");
                } else {
                    requestData.append("<gml:exterior><gml:LinearRing><gml:posList>");
                }

                JSONArray arr2 = arr.getJSONArray(j);
                for (int k = 0; k < arr2.length(); k++) {
                    requestData
                            .append(arr2.getJSONObject(k).getString("x") + " " + arr2.getJSONObject(k).getString("y"));
                    if (k < (arr2.length() - 1)) {
                        requestData.append(" ");
                    }
                }

                if (j > 0) {
                    requestData.append("</gml:posList></gml:LinearRing></gml:interior>");
                } else {
                    requestData.append("</gml:posList></gml:LinearRing></gml:exterior>");
                }
            }
            requestData.append("</gml:Polygon></gml:polygonMember>");
        }
        requestData.append("</gml:MultiPolygon></wfs:Value></wfs:Property>");
    }

}

