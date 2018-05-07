package fi.nls.oskari.control.statistics.plugins.pxweb.parser;

import fi.nls.oskari.control.statistics.data.RegionValue;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import org.json.JSONArray;

import java.util.Optional;

/**
 * Allows statistical datasource to have a prefix in region ID. If prefix is defined for layer this
 * processor returns values for ONLY the regions that have the prefix and removes the prefix from the returned region ID.
 *
 * Requires datasource layer link to have a config including:
 *  {
 *      "valueProcessor": "fi.nls.oskari.control.statistics.plugins.pxweb.parser.PrefixedRegionsValueProcessor",
 *      "statsRegionPrefix": "[prefix for region ids in statistical data]"
 *  }
 */
public class PrefixedRegionsValueProcessor extends ValueProcessor {

    public Optional<RegionValue> getRegionValue(JSONArray values, String regionId, int valueIndex, DatasourceLayer layer) {
        Optional<RegionValue> value = super.getRegionValue(values, regionId, valueIndex, layer);
        if(!value.isPresent()) {
            return value;
        }
        // got value, check if regionid has prefix
        String regionIdPrefix = layer.getConfig("statsRegionPrefix");
        if(regionIdPrefix == null) {
            return value;
        }
        RegionValue v = value.get();
        if(!v.getRegion().startsWith(regionIdPrefix)) {
            // not a region in this regionset
            return Optional.empty();
        }
        // take out the prefix
        String newRegionId = v.getRegion().substring(regionIdPrefix.length());
        return Optional.of(new RegionValue(newRegionId, v.getValue()));
    }
}
