package fi.nls.oskari.control.statistics.plugins.pxweb.parser;

import fi.nls.oskari.control.statistics.data.RegionValue;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TKValueProcessor extends ValueProcessor {

    private Map<String, String> regionsetType = new HashMap<>();

    public TKValueProcessor() {
        String propertiesPrefix = "stats.tk.";
        PropertyUtil.getPropertyNamesStartingWith(propertiesPrefix).stream()
                .forEach(propName -> {
                    String regionType = propName.substring(propertiesPrefix.length());
                    regionsetType.put(regionType.toLowerCase(), PropertyUtil.get(propName));
                });
    }
    public Optional<RegionValue> getRegionValue(JSONArray values, String regionId, int valueIndex, DatasourceLayer layer) {
        Optional<RegionValue> value = super.getRegionValue(values, regionId, valueIndex, layer);
        if(!value.isPresent()) {
            return value;
        }
        // got value, check if regionid has prefix
        String type = layer.getConfig().optString("regionType");
        if(type == null) {
            return value;
        }
        String regionIdPrefix = regionsetType.get(type.toLowerCase());
        if(regionIdPrefix == null) {
            return value;
        }
        RegionValue v = value.get();
        if(!v.getRegion().startsWith(regionIdPrefix)) {
            // not a regino in this regionset
            return Optional.empty();
        }
        // take out the prefix
        String newRegionId = v.getRegion().substring(regionIdPrefix.length());
        return Optional.of(new RegionValue(newRegionId, v.getValue()));
    }
}
