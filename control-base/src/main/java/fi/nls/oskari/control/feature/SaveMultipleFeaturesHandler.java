package fi.nls.oskari.control.feature;
import fi.nls.oskari.annotation.OskariActionRoute;

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

@OskariActionRoute("SaveMultipleFeatures")
public class SaveMultipleFeaturesHandler extends AbstractFeatureHandler {
    private static Logger LOG = LogFactory.getLogger(SaveMultipleFeaturesHandler.class);

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

        boolean hasException = false;

        try {
            JSONArray featuresJSONArray = new JSONArray(params.getHttpParam("featureData"));

            for(int i=0; i < featuresJSONArray.length(); i++) {
                JSONObject jsonObject = featuresJSONArray.getJSONObject(i);

                OskariLayer layer = getLayer(jsonObject.optString("layerId"));

                if (!canEdit(layer, params.getUser())) {
                    LOG.warn("User doesn't have edit permission for layer: " + layer.getId());
                    continue;
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                List<Feature> features = new ArrayList<>();
                Feature feature = getFeature(jsonObject);
                features.add(feature);
                FeatureWFSTRequestBuilder.updateFeatures(baos, features);

                final String wfstMessage = baos.toString();
                LOG.debug("Updating feature to service at", layer.getUrl(), "with payload", wfstMessage);

                String responseString = postPayload(layer, wfstMessage);
                flushLayerTilesCache(layer.getId());

                if (responseString.indexOf("Exception") > -1) {
                    hasException = true;
                }
            }
            if(hasException) {
                LOG.error("Cannot save features");
                throw new ActionException("Cannot save features");
            } else {
                ResponseHelper.writeResponse(params, "Features updated");
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

