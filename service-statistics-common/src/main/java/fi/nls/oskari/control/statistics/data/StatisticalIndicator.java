package fi.nls.oskari.control.statistics.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.nls.oskari.util.PropertyUtil;

import java.util.*;

/**
 * Each indicator has:
 * - Plugin id and id (id is unambiguous only within one plugin)
 * - A localized description shown to user.
 * - An ordered set of different granularity layers such as "Kunta", or "Maakunta".
 * - A set of selectors with a localized name and type and a list of allowed values, and their localizations.
 *   These could be for example: "Gender": "Male", "Female", "Other", "All", or "Year": "2010", "2011", ....
 */
public class StatisticalIndicator {
    private String id;
    @JsonProperty("public")
    private boolean isPublic = true;

    private List<StatisticalIndicatorLayer> layers = new ArrayList<>();
    private Map<String, String> name = new HashMap<>();
    private Map<String, String> source = new HashMap<>();
    private Map<String, String> desc = new HashMap<>();
    private StatisticalIndicatorDataModel dataModel;

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
        return isPublic;
    }
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
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
    public String getName(String lang) {
        return getLocalizedValue(getName(), lang);
    }
    public String getSource(String lang) {
        return getLocalizedValue(getSource(), lang);
    }
    public String getDescription(String lang) {
        return getLocalizedValue(getDescription(), lang);
    }

    private String getLocalizedValue(Map<String, String> map, String lang) {
        if(map == null) {
            // nothing to use
            return null;
        }
        if(lang == null) {
            lang = PropertyUtil.getDefaultLanguage();
        }
        String value = map.get(lang);
        if(value == null) {
            // try with default language
            return map.get(PropertyUtil.getDefaultLanguage());
        }
        return value;
    }
    /*
     * Please note that while it would be convenient to just pass untyped JSON here,
     * it would make developing future plugins more error prone.
     */
    public void setName(Map<String, String> localized) {
        name = localized;
    }
    public Map<String, String> getName() {
        return name;
    }

    public void addName(String lang, String name){
        this.name.put(lang, name);
    }

    public void setDataModel(StatisticalIndicatorDataModel dataModel) {
        this.dataModel = dataModel;
    }
    public StatisticalIndicatorDataModel getDataModel() {
        if(dataModel == null) {
            dataModel = new StatisticalIndicatorDataModel();
        }
        return dataModel;
    }

    public void addSource(String lang, String name){
        this.source.put(lang, name);
    }
    public void setSource(Map<String, String> localized) {
        source = localized;
    }
    public Map<String, String> getSource() {
        return source;
    }
    @JsonProperty("desc")
    public Map<String, String> getDescription() {
        return desc;
    }
    public void addDescription(String lang, String name){
        this.desc.put(lang, name);
    }
    public void setDescription(Map<String, String> localized) {
        desc = localized;
    }
}
