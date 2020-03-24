package fi.nls.oskari.control.feature;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.Feature;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.util.XmlHelper;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.referencing.FactoryException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OskariActionRoute("InsertFeature")
public class InsertFeatureHandler extends AbstractFeatureHandler {
    private final static Logger LOG = LogFactory.getLogger(InsertFeatureHandler.class);

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

        try {
            JSONArray paramFeatures = new JSONArray(params.getHttpParam("featureData"));
            JSONArray updatedFeatureIds = new JSONArray();

            Map<Integer, OskariLayer> layers = getLayers(paramFeatures);
            hasUserPermissionEditLayers(layers, params.getUser());

            for (int i = 0; i < paramFeatures.length(); i++) {
                JSONObject featureJSON = paramFeatures.getJSONObject(i);
                OskariLayer layer = getLayer(featureJSON.optString("layerId"));
                final String wfstMessage = createWFSTMessage(featureJSON);
                LOG.debug("Inserting feature to service at", layer.getUrl(), "with payload", wfstMessage);
                final String responseString = postPayload(layer.getUsername(), layer.getPassword(), wfstMessage, getURLForNamespace(layer.getName(),layer.getUrl()));
                updatedFeatureIds.put(parseFeatureIdFromResponse(responseString));
            }

            flushLayerTilesCache(layers);

            ResponseHelper.writeResponse(params, JSONHelper.createJSONObject("fids", updatedFeatureIds));
        } catch (JSONException e) {
            LOG.error(e, "JSON processing error");
            throw new ActionException("JSON processing error", e);
        }
    }

    private String createWFSTMessage(JSONObject jsonPayload)
            throws ActionException {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Feature feature = getFeature(jsonPayload);
            FeatureWFSTRequestBuilder.insertFeature(baos, feature);
            return baos.toString();
        } catch (XMLStreamException e) {
            LOG.error(e, "Failed to create WFS-T request");
            throw new ActionException("Failed to create WFS-T request", e);
        } catch (JSONException e) {
            LOG.error(e, "JSON processing error");
            throw new ActionException("JSON processing error", e);
        } catch (FactoryException e) {
            LOG.error(e, "Failed to create WFS-T request (crs)");
            throw new ActionException("Failed to create WFS-T request (crs)", e);
        }
    }

    private String parseFeatureIdFromResponse(String response)
            throws ActionException {

        if (response == null || response.indexOf("Exception") > -1) {
            LOG.error("Exception from WFS-T insert operation", response);
            throw new ActionException("WFS-T operation failed");
        }

        if (response.indexOf("<wfs:totalInserted>1</wfs:totalInserted>") == -1) {
            throw new ActionException("Didn't get the expected response from service " + response);
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
}
