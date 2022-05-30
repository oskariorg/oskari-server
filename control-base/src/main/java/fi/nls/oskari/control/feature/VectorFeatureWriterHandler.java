package fi.nls.oskari.control.feature;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.Feature;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.util.XmlHelper;
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

@OskariActionRoute("VectorFeatureWriter")
public class VectorFeatureWriterHandler extends AbstractFeatureHandler {
    private final static Logger LOG = LogFactory.getLogger(VectorFeatureWriterHandler.class);

    public void preProcess(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
    }

    public void handleDelete(ActionParameters params) throws ActionException {
        String layerId = params.getRequiredParam("layerId");
        OskariLayer layer = getLayer(layerId);
        if (!canEdit(layer, params.getUser())) {
            throw new ActionDeniedException("User doesn't have edit permission for layer: " + layer.getId());
        }
        String featureId = params.getRequiredParam("featureId");
        try {
            Feature feature = initFeatureByLayer(layer);
            feature.setId(featureId);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FeatureWFSTRequestBuilder.deleteFeature(baos, feature);
            String payload = baos.toString();
            String responseString = postPayload(layer.getUsername(), layer.getPassword(), payload, getURLForNamespace(layer.getName(), layer.getUrl()));

            if (responseString.indexOf("Exception") > -1) {
                throw new ActionException("Cannot delete feature");
            } else if (responseString.indexOf("<wfs:totalDeleted>1</wfs:totalDeleted>") > -1) {
                ResponseHelper.writeResponse(params, JSONHelper.createJSONObject("id", featureId));
            } else {
                throw new ActionException("Unexpected response from service: " + responseString);
            }
        } catch (XMLStreamException e) {
            throw new ActionException("Failed to create WFS-T request", e);
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        String layerId = params.getRequiredParam("layerId");
        String crs = params.getRequiredParam("crs");
        OskariLayer layer = getLayer(layerId);
        if (!canEdit(layer, params.getUser())) {
            throw new ActionDeniedException("User doesn't have edit permission for layer: " + layer.getId());
        }

        try {
            JSONObject geojson = params.getPayLoadJSON();

            Feature feature = getFeature(geojson, layerId, crs, geojson.optString("id"));
            final String wfstMessage = createWFSTMessageForInsert(feature);
            LOG.debug("Inserting feature to service at", layer.getUrl(), "with payload:\n", wfstMessage);
            final String responseString = postPayload(layer.getUsername(), layer.getPassword(), wfstMessage, getURLForNamespace(layer.getName(),layer.getUrl()));

            LOG.debug("Got response:\n", responseString);
            String idFromResponse = parseFeatureIdFromInsertResponse(responseString);
            LOG.debug("Inserted feature with id:", idFromResponse);
            if (idFromResponse == null || idFromResponse.isEmpty()) {
                LOG.warn("Problem with :", responseString);
                throw new ActionParamsException("Returned id didn't match input: " + idFromResponse);
            }
            ResponseHelper.writeResponse(params, geojson);
        } catch (JSONException e) {
            throw new ActionException("JSON processing error", e);
        } catch (FactoryException e) {
            throw new ActionException("Failed to create WFS-T request (crs)", e);
        }
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        String layerId = params.getRequiredParam("layerId");
        String crs = params.getRequiredParam("crs");
        OskariLayer layer = getLayer(layerId);
        if (!canEdit(layer, params.getUser())) {
            throw new ActionDeniedException("User doesn't have edit permission for layer: " + layer.getId());
        }

        try {
            JSONObject geojson = params.getPayLoadJSON();
            Feature feature = getFeature(geojson, layerId, crs, geojson.optString("id"));

            final String wfstMessage = createWFSTMessageForUpdate(feature);
            LOG.debug("Updating feature to service at", layer.getUrl(), "with payload:\n", wfstMessage);
            String responseString = postPayload(layer.getUsername(), layer.getPassword(), wfstMessage, getURLForNamespace(layer.getName(),layer.getUrl()));

            LOG.debug("Got response:\n", responseString);
            if (responseString.indexOf("Exception") > -1) {
                throw new ActionException("Cannot save feature: " + responseString);
            }

            ResponseHelper.writeResponse(params, geojson);
        } catch (JSONException e) {
            throw new ActionException("JSON processing error", e);
        } catch (FactoryException e) {
            throw new ActionException("Failed to create WFS-T request (crs)", e);
        }
    }

    private String createWFSTMessageForUpdate(Feature feature)
            throws ActionException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FeatureWFSTRequestBuilder.updateFeature(baos, feature);
            return baos.toString();
        } catch (XMLStreamException e) {
            throw new ActionException("Failed to create WFS-T request", e);
        }
    }
    private String createWFSTMessageForInsert(Feature feature)
            throws ActionException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FeatureWFSTRequestBuilder.insertFeature(baos, feature);
            return baos.toString();
        } catch (XMLStreamException e) {
            throw new ActionException("Failed to create WFS-T request", e);
        }
    }

    private String parseFeatureIdFromInsertResponse(String response)
            throws ActionException {

        if (response == null || response.indexOf("Exception") > -1) {
            LOG.info("Exception from WFS-T insert operation", response);
            throw new ActionParamsException("WFS-T operation failed");
        }

        if (response.indexOf("<wfs:totalInserted>1</wfs:totalInserted>") == -1) {
            throw new ActionParamsException("Didn't get the expected response from service " + response);
        }

        try {
            DocumentBuilderFactory factory = XmlHelper.newDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();

            ByteArrayInputStream input = new ByteArrayInputStream(response.getBytes("UTF-8"));
            Document doc = builder.parse(input);

            NodeList res = doc.getElementsByTagName("ogc:FeatureId");
            Element res3 = (Element) res.item(0);
            return res3.getAttribute("fid");
        } catch (ParserConfigurationException ex) {
            throw new ActionException("Parser configuration error", ex);
        } catch (IOException ex) {
            throw new ActionException("IO error", ex);
        } catch (SAXException ex) {
            throw new ActionException("SAX processing error", ex);
        }
    }
}
