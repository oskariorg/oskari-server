package fi.mml.map.mapwindow.service.wms;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

public abstract class AbstractWebMapService implements WebMapService {

    protected static final QName XLINK_HREF = new QName("http://www.w3.org/1999/xlink", "href");
    protected static final String LEGEND_HASHMAP_KEY_SEPARATOR = "_";

    protected String getCapabilitiesUrl;
    protected boolean queryable;
    protected String[] formats;
    protected String[] keywords;
    protected String[] CRSs;
    protected String geom;
    protected List<String> time;
    protected Map<String, String> styles;
    protected Map<String, String> legends;

    protected AbstractWebMapService(String getCapabilitiesUrl) {
        this.getCapabilitiesUrl = getCapabilitiesUrl;
    }

    public String getCapabilitiesUrl() {
        return getCapabilitiesUrl;
    }

    public Map<String, String> getSupportedStyles() {
        return styles;
    }

    public String[] getFormats() {
        return formats;
    }

    public Map<String, String> getSupportedLegends() {
        return legends;
    }

    public boolean isQueryable() {
        return queryable;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public String[] getCRSs() {
        return CRSs;
    }

	public String getGeom() {
		return geom;
	}

	public List<String> getTime() {
        return time;
    }

    public String getLegendForStyle(String key) {
        Map<String, String> supportedStyles = this.getSupportedStyles();
        return legends.get(key + LEGEND_HASHMAP_KEY_SEPARATOR + supportedStyles.get(key));
    }
}
