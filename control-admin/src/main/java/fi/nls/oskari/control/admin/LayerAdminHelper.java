package fi.nls.oskari.control.admin;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.domain.map.OskariLayer;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LayerAdminHelper {

    protected static Set<OskariLayer> getTimeseriesReferencedLayers(int layerId, List<OskariLayer> layers) throws ActionException {
        return layers.stream()
                .filter(it -> isReferencedByTimeseriesMetadata(layerId, it))
                .collect(Collectors.toSet());
    }

    protected static boolean isReferencedByTimeseriesMetadata(int layerId, OskariLayer layer) {
        JSONObject options = layer.getOptions();
        if (options == null) {
            return false;
        }

        JSONObject timeseriesOptions = options.optJSONObject("timeseries");
        if (timeseriesOptions == null) {
            return false;
        }

        JSONObject timeseriesMetadata = timeseriesOptions.optJSONObject("metadata");
        if (timeseriesMetadata == null) {
            return false;
        }

        int metadataLayerId = timeseriesMetadata.optInt("layer", -1);
        return metadataLayerId == layerId;
    }

}
