package fi.nls.oskari.control.statistics.plugins.pxweb.parser;

import fi.nls.oskari.control.statistics.data.IndicatorValueFloat;
import fi.nls.oskari.control.statistics.data.RegionValue;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import org.json.JSONArray;

import java.util.Optional;

public class ValueProcessor {
    public Optional<RegionValue> getRegionValue(JSONArray values, String regionId, int valueIndex, DatasourceLayer layer) {
        // check isRegionForLayer
        Double val = values.optDouble(valueIndex);
        if (val.isNaN()) {
            return Optional.empty();
        }

        return Optional.of(new RegionValue(regionId, new IndicatorValueFloat(val)));
    }
}
