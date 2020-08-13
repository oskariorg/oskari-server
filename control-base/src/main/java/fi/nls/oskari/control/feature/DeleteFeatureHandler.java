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
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.referencing.FactoryException;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@OskariActionRoute("DeleteFeature")
public class DeleteFeatureHandler extends AbstractFeatureHandler {
    private static Logger LOG = LogFactory.getLogger(DeleteFeatureHandler.class);

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

        JSONObject jsonObject = params.getHttpParamAsJSON("featureData");
        OskariLayer layer = getLayer(jsonObject.optString("layerId"));

        if (!canEdit(layer, params.getUser())) {
            throw new ActionDeniedException("User doesn't have edit permission for layer: " + layer.getId());
        }

        try {
            String payload = createPayload(jsonObject);
            String responseString = postPayload(layer.getUsername(), layer.getPassword(), payload, getURLForNamespace(layer.getName(), layer.getUrl()));
            flushLayerTilesCache(layer.getId());

            if (responseString.indexOf("Exception") > -1) {
                throw new ActionException("Cannot delete feature");
            } else if (responseString.indexOf("<wfs:totalDeleted>1</wfs:totalDeleted>") > -1) {
                ResponseHelper.writeResponse(params, "Feature deleted");
            } else {
                throw new ActionException("Unexpected response from service: " + responseString);
            }
        } catch (JSONException e) {
            LOG.error(e, "JSON processing error");
            throw new ActionException("JSON processing error", e);
        } catch (XMLStreamException e) {
            LOG.error(e, "Failed to create WFS-T request");
            throw new ActionException("Failed to create WFS-T request", e);
        } catch (FactoryException e) {
            LOG.error(e, "Failed to create WFS-T request (crs)");
            throw new ActionException("Failed to create WFS-T request (crs)", e);
        }
    }

    protected String createPayload(JSONObject jsonObject) throws ActionParamsException, JSONException,
            XMLStreamException, FactoryException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Feature feature = getFeature(jsonObject);
        FeatureWFSTRequestBuilder.deleteFeature(baos, feature);

        return baos.toString();
    }
}

