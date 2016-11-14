package fi.nls.oskari.control.statistics.plugins;

import java.util.*;

/**
 * Each indicator has:
 * - Plugin id and id (id is unambiguous only within one plugin)
 * - A localized description shown to user.
 * - An ordered set of different granularity layers such as "Kunta", or "Maakunta".
 * - A set of selectors with a localized name and type and a list of allowed values, and their localizations.
 *   These could be for example: "Gender": "Male", "Female", "Other", "All", or "Year": "2010", "2011", ....
 */
public abstract class AbstractStatisticalIndicator implements StatisticalIndicator {
    private String id;

    private List<StatisticalIndicatorLayer> layers = new ArrayList<>();
    private Map<String, String> localisedName = new HashMap<>();
    private StatisticalIndicatorSelectors selectors;

    public String getPluginId() {
        return null;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }

    /**
     * User created indicators can be private so that they are only shown to the user who created them.
     */
    public Boolean isPublic() {
        return true;
    }
    public void addLayer(StatisticalIndicatorLayer layer) {
        layers.add(layer);
    }
    public List<StatisticalIndicatorLayer> getLayers() {
        return layers;
    }
    public StatisticalIndicatorLayer getLayer(long id) {
        for (StatisticalIndicatorLayer layer : getLayers()) {
            if (layer.getOskariLayerId() == id) {
                return layer;
            }
        }
        return null;
    }
    /*
     * Please note that while it would be convenient to just pass untyped JSON here,
     * it would make developing future plugins more error prone.
     */
    public Map<String, String> getLocalizedName() {
        return localisedName;
    }
    public void addLocalizedName (String lang, String name){
        localisedName.put(lang, name);
    }

    public void setSelectors(StatisticalIndicatorSelectors selectors) {
        this.selectors = selectors;
    }
    public StatisticalIndicatorSelectors getSelectors() {
        return selectors;
    }

    public Map<String, String> getLocalizedSource() {
        return Collections.emptyMap();
    }
    public Map<String, String> getLocalizedDescription() {
        return Collections.emptyMap();
    }
}
