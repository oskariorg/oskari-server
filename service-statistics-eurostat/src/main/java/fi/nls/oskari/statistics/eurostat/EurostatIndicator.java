package fi.nls.oskari.statistics.eurostat;

import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorLayer;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;

import java.util.*;

public class EurostatIndicator implements StatisticalIndicator {

    private String id;
    private String name;
    private StatisticalIndicatorSelectors selectors;
    private List<StatisticalIndicatorLayer> layers = new ArrayList<>();
    private Map<String, String> localisedName = new HashMap<>();
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSelectors(StatisticalIndicatorSelectors selectors) {
        this.selectors = selectors;
    }
    public void addLayer(StatisticalIndicatorLayer layer) {
        layers.add(layer);
    }

    @Override
    public String getPluginId() {
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Boolean isPublic() {
        return false;
    }

    @Override
    public List<StatisticalIndicatorLayer> getLayers() {
        return layers;
    }

    @Override
    public StatisticalIndicatorSelectors getSelectors() {
        return selectors;
    }


    public Map<String, String> getLocalizedName() {

        return localisedName;
    }

    public void addLocalizedName (String lang, String name){
        localisedName.put(lang, name);
    }


    @Override
    public Map<String, String> getLocalizedSource() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getLocalizedDescription() {
        return Collections.emptyMap();
    }
}
