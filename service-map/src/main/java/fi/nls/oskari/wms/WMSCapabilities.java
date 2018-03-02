package fi.nls.oskari.wms;

import fi.mml.map.mapwindow.service.wms.WebMapService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 17.2.2014
 * Time: 12:48
 * To change this template use File | Settings | File Templates.
 */
public class WMSCapabilities implements WebMapService {

    private String capabilitiesURL = null;
    private String version = "1.3.0";
    private boolean queryable = false;
    private String[] keywords = new String[0];
    private String[] formats = new String[0];
    private List<WMSStyle> styles = new ArrayList<WMSStyle>();
    private List<String> time = new ArrayList<>();
    private String[] CRSs = new String[0];
    private String geom = null;

    public void setQueryable(boolean val) {
        this.queryable = val;
    }

    @Override
    public boolean isQueryable() {
        return queryable;
    }

    public void setKeywords(String[] keywords) {
        if(keywords != null) {
            this.keywords = keywords;
        }
    }

    @Override
    public String[] getKeywords() {
        return keywords;
    }

    public void setVersion(String ver) {
        version = ver;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void addStyle(WMSStyle style) {
        if(style != null) {
            styles.add(style);
        }
    }

    @Override
    public Map<String, String> getSupportedStyles() {
        Map<String, String> map = new HashMap<String, String>();
        for(WMSStyle s : styles) {
            map.put(s.getName(), s.getTitle());
        }
        return map;
    }

    @Override
    public Map<String, String> getSupportedLegends() {
        Map<String, String> map = new HashMap<String, String>();
        for(WMSStyle s : styles) {
            map.put(s.getName(), s.getLegend());
        }
        return map;
    }

    @Override
    /**
     * Defined here only to meet interface definition. Never supposed to be called
     */
    public String getLegendForStyle(String key) {
        return null;
    }
    @Override
    public String getCapabilitiesUrl() {
        return capabilitiesURL;
    }

    public void setCapabilitiesUrl(String url) {
        capabilitiesURL = url;
    }

    public void setFormats(List<String> val) {
        if(val != null) {
            formats = val.toArray(formats);
        }
    }
    public void setFormats(String[] val) {
        if(val != null) {
            formats = val;
        }
    }
    @Override
    public String[] getFormats() {
        return formats;
    }

    @Override
    public List<String> getTime() {
        return time;
    }

    @Override
    public String[] getCRSs() {
        return CRSs;
    }
    
    @Override
    public String getGeom() {
        return geom;
    }

}
