package fi.nls.oskari.control.admin;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LayerAdminHelper {
    private static final Logger LOG = LogFactory.getLogger(LayerAdminHelper.class);

    protected static Set<OskariLayer> getTimeseriesReferencedLayers(int layerId, List<OskariLayer> layers) throws ActionException {
        Set<OskariLayer> timeseriesLayers = new HashSet<>();
        for (OskariLayer layer : layers) {
            JSONObject options = layer.getOptions();
            try {
                if (options != null && options.has("timeseries")) {
                    JSONObject timeseriesOptions = options.getJSONObject("timeseries");
                    JSONObject timeseriesMetadata = timeseriesOptions.optJSONObject("metadata");
                    if (timeseriesMetadata == null) {
                        continue;
                    }
                    Integer metadataLayerId = timeseriesMetadata.getInt("layer");
                    if (metadataLayerId == layerId) {
                        LOG.debug("Found timeseries referenced layer, layerId: " + layerId);
                        timeseriesLayers.add(layer);
                    }
                }
            } catch (JSONException e) {
                throw new ActionException("Cannot parse layer metadata options for layer: " +
                        layer.getName() + ", id: " + layer.getId());
            }
        }
        return timeseriesLayers;
    }

}
