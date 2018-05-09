package fi.nls.oskari.control.statistics.plugins.pxweb.parser;

import fi.nls.oskari.control.statistics.data.IndicatorValueFloat;
import fi.nls.oskari.control.statistics.data.RegionValue;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import org.json.JSONArray;

import java.util.Optional;

/**
 * Returns the region ID/value pair if one is found.
 */
public class ValueProcessor {
    /**
     * These parameters might make no sense for the functionality in the long run. For simplicity we could give the
     * region ID and the value as parameter or for more complex handling we could pass the whole resultset.
     * TODO: Consider this API subject to breaking changes in the future
     * @param values The array of all the values returned by the PX-web API
     * @param regionId Id of the region that we are currently processing
     * @param valueIndex index for array in values-array for the region
     * @param layer
     * @return
     */
    public Optional<RegionValue> getRegionValue(JSONArray values, String regionId, int valueIndex, DatasourceLayer layer) {
        Double val = values.optDouble(valueIndex);
        if (val.isNaN()) {
            return Optional.empty();
        }

        return Optional.of(new RegionValue(regionId, new IndicatorValueFloat(val)));
    }
}
