package fi.nls.oskari.control.feature;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.util.XmlHelper;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@OskariActionRoute("InsertFeature")
public class InsertFeatureHandler extends AbstractFeatureHandler {
    private final static Logger LOG = LogFactory.getLogger(InsertFeatureHandler.class);

    @Override
    public void handleAction(ActionParameters params)
            throws ActionException {

        JSONObject jsonPayload = params.getHttpParamAsJSON("featureData");
        OskariLayer layer = getLayer(jsonPayload.optString("layerId"));

        if (!canEdit(layer, params.getUser())) {
            throw new ActionDeniedException("User doesn't have edit permission for layer: " + layer.getId());
        }

        final WFSLayerConfiguration wfsMetadata = getWFSConfiguration(layer.getId());
        final String wfstMessage = createWFSTMessage(jsonPayload, layer, wfsMetadata);
        LOG.debug("Inserting feature to service at", layer.getUrl(), "with payload", wfstMessage);
        final String responseString = postPayload(layer, wfstMessage);
        final String updatedFeatureId = parseFeatureIdFromResponse(responseString);

        flushLayerTilesCache(layer.getId());
        ResponseHelper.writeResponse(params, JSONHelper.createJSONObject("fid", updatedFeatureId));
    }

    private String createWFSTMessage(JSONObject jsonPayload, OskariLayer layer, WFSLayerConfiguration wfsMetadata)
            throws ActionException {
        final String srsName = JSONHelper.getStringFromJSON(jsonPayload, "srsName", "http://www.opengis.net/gml/srs/epsg.xml#3067");
        final StringBuilder requestData = new StringBuilder("<wfs:Transaction service='WFS' version='1.1.0' xmlns:ogc='http://www.opengis.net/ogc' xmlns:wfs='http://www.opengis.net/wfs'><wfs:Insert><" + layer.getName() + " xmlns:" + wfsMetadata.getFeatureNamespace() + "='" + wfsMetadata.getFeatureNamespaceURI() + "'>");
        try {
            final JSONArray jsonArray = jsonPayload.getJSONArray("featureFields");
            for (int i = 0; i < jsonArray.length(); i++) {
                String key = jsonArray.getJSONObject(i).getString("key");
                String value = jsonArray.getJSONObject(i).getString("value");
                if (value.isEmpty() == false) {
                    requestData.append("<" + key + ">" + value + "</" + key + ">");
                }
            }

            if (jsonPayload.has("geometries")) {
                FillGeometries(wfsMetadata.getGMLGeometryProperty(), requestData, jsonPayload.getJSONObject("geometries"), srsName);
            }
        } catch (JSONException ex) {
            throw new ActionParamsException("Couldn't create WFS-T message from params", jsonPayload.toString(), ex);
        }
        requestData.append("</" + layer.getName() + "></wfs:Insert></wfs:Transaction>");
        return requestData.toString();
    }

    private String parseFeatureIdFromResponse(String response)
            throws ActionException {

        if (response == null || response.indexOf("Exception") > -1) {
            LOG.error("Exception from WFS-T insert operation", response);
            throw new ActionParamsException("WFS-T operation failed");
        }

        if (response.indexOf("<wfs:totalInserted>1</wfs:totalInserted>") == -1) {
            throw new ActionParamsException("Didn't get the expected response from service", response);
        }

        try {
            DocumentBuilderFactory factory = XmlHelper.newDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();

            ByteArrayInputStream input = new ByteArrayInputStream(response.getBytes("UTF-8"));
            Document doc = builder.parse(input);

            NodeList res = doc.getElementsByTagName("ogc:FeatureId");
            Element res3 = (Element) res.item(0);
            return res3.getAttribute("fid");
        } catch (ClientProtocolException ex) {
            LOG.error(ex, "Geoserver connection error");
            throw new ActionException("Geoserver connection error", ex);
        } catch (ParserConfigurationException ex) {
            LOG.error(ex, "Parser configuration error");
            throw new ActionException("Parser configuration error", ex);
        } catch (IOException ex) {
            LOG.error(ex, "IO error");
            throw new ActionException("IO error", ex);
        } catch (SAXException ex) {
            LOG.error(ex, "SAX processing error");
            throw new ActionException("SAX processing error", ex);
        }
    }

    private void FillGeometries(String geometryProperty, StringBuilder requestData, JSONObject geometries,
                                String srsName)
            throws JSONException {
        String geometryType = geometries.getString("type");
        if (geometryType.equals("multipoint")) {
            FillMultiPointGeometries(geometryProperty, requestData, geometries, srsName);
        } else if (geometryType.equals("multilinestring")) {
            FillLineStringGeometries(geometryProperty, requestData, geometries, srsName);
        } else if (geometryType.equals("multipolygon")) {
            FillPolygonGeometries(geometryProperty, requestData, geometries, srsName);
        }
    }

    private void FillMultiPointGeometries(String geometryProperty, StringBuilder requestData, JSONObject geometries,
                                          String srsName)
            throws JSONException {
        JSONArray data = geometries.getJSONArray("data");
        requestData.append("<" + geometryProperty
                + "><gml:MultiPoint xmlns:gml='http://www.opengis.net/gml' srsName='" + srsName + "'>");

        for (int i = 0; i < data.length(); i++) {
            requestData.append("<gml:pointMember><gml:Point><gml:coordinates decimal=\".\" cs=\",\" ts=\" \">"
                    + data.getJSONObject(i).getString("x") + "," + data.getJSONObject(i).getString("y")
                    + "</gml:coordinates></gml:Point></gml:pointMember>");
        }
        requestData.append("</gml:MultiPoint></" + geometryProperty + ">");
    }

    private void FillLineStringGeometries(String geometryProperty, StringBuilder requestData, JSONObject geometries,
                                          String srsName)
            throws JSONException {
        JSONArray data = geometries.getJSONArray("data");
        requestData.append("<" + geometryProperty
                + "><gml:MultiLineString xmlns:gml='http://www.opengis.net/gml' srsName='" + srsName + "'>");

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
        requestData.append("</gml:MultiLineString></" + geometryProperty + ">");
    }

    private void FillPolygonGeometries(String geometryProperty, StringBuilder requestData, JSONObject geometries,
                                       String srsName)
            throws JSONException {
        JSONArray data = geometries.getJSONArray("data");
        requestData.append("<" + geometryProperty
                + "><gml:MultiPolygon xmlns:gml='http://www.opengis.net/gml' srsName='" + srsName + "'>");

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
        requestData.append("</gml:MultiPolygon></" + geometryProperty + ">");
    }

}

