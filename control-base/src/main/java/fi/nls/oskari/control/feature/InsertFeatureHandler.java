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
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

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
        // TODO: rewrite to use wfs-t related code under myplaces OR atleast using an xml lib
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
                insertGeometries(wfsMetadata.getGMLGeometryProperty(), requestData, jsonPayload.getJSONObject("geometries"), srsName);
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

    protected void insertGeometries(String geometryProperty, StringBuilder requestData, JSONObject geometries, String srsName) throws ActionParamsException, JSONException {
        String geometryType = geometries.getString("type");
        if(!isAllowedGeomType(geometryType)) {
            throw new ActionParamsException("Invalid geometry type: " + geometryProperty);
        }
        JSONArray data = geometries.getJSONArray("data");
        requestData.append("<" + geometryProperty + ">");
        requestData.append(getGeometry(geometryType, data, srsName));
        requestData.append("</" + geometryProperty + ">");
    }
}

