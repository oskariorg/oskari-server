package fi.nls.oskari.control.feature;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.Feature;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.referencing.FactoryException;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@OskariActionRoute("SaveFeature")
public class SaveFeatureHandler extends AbstractFeatureHandler {
    private static Logger LOG = LogFactory.getLogger(SaveFeatureHandler.class);

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        List<String> exceptions = new ArrayList<>();

        try {
            JSONArray paramFeatures = new JSONArray(params.getHttpParam("featureData"));

            Map<Integer, OskariLayer> layers = getLayers(paramFeatures);
            hasUserPermissionEditLayers(layers, params.getUser());

            for (int i = 0; i < paramFeatures.length(); i++) {
                JSONObject featureJSON = paramFeatures.getJSONObject(i);

                OskariLayer layer = getLayer(featureJSON.optString("layerId"));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                Feature feature = getFeature(featureJSON);
                FeatureWFSTRequestBuilder.updateFeature(baos, feature);

                final String wfstMessage = baos.toString();
                LOG.debug("Updating feature to service at", layer.getUrl(), "with payload", wfstMessage);

                String responseString = postPayload(layer, wfstMessage);

                if (responseString.indexOf("Exception") > -1) {
                    exceptions.add(responseString);
                }
            }

            flushLayerTilesCache(layers);

            if(exceptions.size() > 0 ) {
                throw new ActionException("Cannot save feature(s): " + exceptions.toString());
            } else {
                ResponseHelper.writeResponse(params, "Feature updated");
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

}

