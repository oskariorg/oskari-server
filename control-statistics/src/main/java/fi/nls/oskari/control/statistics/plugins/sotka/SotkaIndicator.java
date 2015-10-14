package fi.nls.oskari.control.statistics.plugins.sotka;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorLayer;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;

public class SotkaIndicator implements StatisticalIndicator {
    private String id;
    private Map<String, String> localizedName;

    public SotkaIndicator(String id, Map<String, String> localizedName) {
        this.id = id;
        this.localizedName = localizedName;
    }
    public SotkaIndicator(JSONObject jsonObject) {
        // TODO: Implement
    }
    @Override
    public String getId() {
        return this.id;
    }
    @Override
    public Map<String, String> getLocalizedName() {
        return this.localizedName;
    }
    
    @Override
    public List<StatisticalIndicatorLayer> getLayers() {
        // TODO: Implement
        return null;
    }
    @Override
    public StatisticalIndicatorSelectors getSelectors() {
        // TODO: Implement
        return null;
    }
}
