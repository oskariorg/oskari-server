package fi.nls.oskari.domain.map.stats;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StatsVisualization {

    private Map<String, String> name = new HashMap<String, String>();

    private long id;
    private String nameJSON;
    private String visualization;
    private String classes;
    private String colors;
    private String layername;
    private String filterproperty;
    private String geometryproperty;
    private String externalid;

    public boolean isValid() {
        if(getLayername() == null) {
            return false;
        }

        if(getFilterproperty() == null) {
            return false;
        }
        
        final String[] vclasses = getClassGroups(); 
        final String[] vcolors = getGroupColors();
        if(vclasses.length == 0 || vclasses.length != vcolors.length) {
            return false;
        }
            
        return true;
    }

    public String[] getClassGroups() {
        return splitGroups(getClasses());
    }

    public String[] getGroupColors() {
        return splitGroups(getColors());
    }

    private String[] splitGroups(final String grouped) {
        if (grouped != null && !grouped.isEmpty()) {
            return grouped.split("\\|");
        }
        return new String[0];
    }

    public String getName(final String language) {
        final String value = name.get(language);
        if (value == null) {
            return "undefined";
        }
        return value;
    }

    public void setNameJSON(String json) {
        this.nameJSON = json;
        final JSONObject obj = JSONHelper.createJSONObject(json);
        final Iterator<String> i = obj.keys();
        while (i.hasNext()) {
            String key = i.next();
            try {
                name.put(key, obj.getString(key));
            } catch (JSONException ignored) {
            }
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getVisualization() {
        return visualization;
    }

    public void setVisualization(String visualization) {
        this.visualization = visualization;
    }

    public String getClasses() {
        return classes;
    }

    public void setClasses(String classes) {
        this.classes = classes;
    }

    public String getColors() {
        return colors;
    }

    public void setColors(String colors) {
        this.colors = colors;
    }

    public String getLayername() {
        return layername;
    }

    public void setLayername(String layername) {
        this.layername = layername;
    }

    public String getFilterproperty() {
        return filterproperty;
    }

    public void setFilterproperty(String filterproperty) {
        this.filterproperty = filterproperty;
    }

    public String getGeometryproperty() {
        return geometryproperty;
    }

    public void setGeometryproperty(String geometryproperty) {
        this.geometryproperty = geometryproperty;
    }

    public String getExternalid() {
        return externalid;
    }

    public void setExternalid(String externalid) {
        this.externalid = externalid;
    }

}
