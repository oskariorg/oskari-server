package fi.nls.oskari.wms;

import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.map.analysis.domain.AnalysisMethodParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Layer class for WMS GetCapabilities layers
 */
public class WMSCapabilityLayer {


    private final String DEFAULT_FORMATS = "{ \"value\": \"null\", \"available\": [\"text/html\", \"text/plain\", \"application/vnd.ogc.gml\"] }";
    private final String DEFAULT_VERSION = "1.3.0";
    private final Double DEFAULT_MINSCALE = 2000000.0;
    private final Double DEFAULT_MAXSCALE = 1.0;
    private final int DEFAULT_OPACITY = 100;

    private String type;
    private String title;
    private String dataUrl_uuid;
    private String wmsName;
    private List<WMSStyle> styles;
    private String geom;
    private int baseLayerId;
    private String orgName;
    private String legendImage;
    private String formats;
    private String keywords[];

    private String version;
    private Boolean isQueryable;
    private int id;
    private Double minScale;
    private Double maxScale;
    private String style;
    private String updated;
    private String created;
    private String wmsUrl;
    private String admin;
    private String name;
    private String permissions;

    private JSONObject subtitle;
    private int opacity;
    private String inspire;

    /**
     * Constructor for default values and common values
     * @param type  layertype wmslayer or grouplayer
     * @param url   WMS service URL
     * @param title WMS layer title
     * @param metaUrl WMS Service metadata url
     */
    public WMSCapabilityLayer(String type, String url, String title, String metaUrl) {
        this.type = type;
        this.title = title;
        this.wmsUrl = url;
        this.dataUrl_uuid = metaUrl;
        this.formats = DEFAULT_FORMATS;
        this.version = DEFAULT_VERSION;
        isQueryable = false;
        this.minScale = DEFAULT_MINSCALE;
        this.maxScale = DEFAULT_MAXSCALE;
        this.opacity = DEFAULT_OPACITY;
        this.baseLayerId = -1;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDataUrl_uuid() {
        return dataUrl_uuid;
    }

    public void setDataUrl_uuid(String dataUrl_uuid) {
        this.dataUrl_uuid = dataUrl_uuid;
    }

    public String getWmsName() {
        return wmsName;
    }

    public void setWmsName(String wmsName) {
        this.wmsName = wmsName;
    }

    public List<WMSStyle> getStyles() {
        return styles;
    }

    public void setStyles(List<WMSStyle> styles) {
        this.styles = styles;
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }

    public int getBaseLayerId() {
        return baseLayerId;
    }

    public void setBaseLayerId(int baseLayerId) {
        this.baseLayerId = baseLayerId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getLegendImage() {
        return legendImage;
    }

    public void setLegendImage(String legendImage) {
        this.legendImage = legendImage;
    }

    public String getFormats() {
        return formats;
    }

    public void setFormats(String formats) {
        this.formats = formats;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getIsQueryable() {
        return isQueryable;
    }

    public void setIsQueryable(Boolean queryable) {
        this.isQueryable = queryable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getMinScale() {
        return minScale;
    }

    public void setMinScale(Double minScale) {
        this.minScale = minScale;
    }

    public Double getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(Double maxScale) {
        this.maxScale = maxScale;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getWmsUrl() {
        return wmsUrl;
    }

    public void setWmsUrl(String wmsUrl) {
        this.wmsUrl = wmsUrl;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public JSONObject getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(JSONObject subtitle) {
        this.subtitle = subtitle;
    }

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public String getInspire() {
        return inspire;
    }

    public void setInspire(String inspire) {
        this.inspire = inspire;
    }

    /**
     * Layer data to JSON
     * @return
     * @throws JSONException
     */

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", this.getId());
        json.put("type", this.getType());
        json.put("title", this.getTitle());
        json.put("name", this.getName());
        json.put("subtitle", this.getSubtitle());
        json.put("orgName", this.getOrgName());
        json.put("inspire", this.getInspire());

        json.put("opacity", this.getOpacity());
        json.put("minScale", this.getMinScale());
        json.put("maxScale", this.getMaxScale());
        json.put("dataUrl_uuid", this.getDataUrl_uuid());
        json.put("wmsName", this.getWmsName());
        JSONArray styles = new JSONArray();
        if (this.getStyles() != null) {
            for (WMSStyle style : this.getStyles()) {
                styles.put(style.toJSON());
            }
        }
        json.put("styles", styles);
        if (this.getKeywords() != null) {
            JSONArray keywords = new JSONArray();
            for (String s : this.getKeywords()) {
                keywords.put(s);
            }
        }
        json.put("keywords", keywords);
        json.put("geom", this.getGeom());
        json.put("baseLayerId", this.getBaseLayerId());
        json.put("legendImage", this.getLegendImage());
        json.put("formats", this.getFormats());
        json.put("version", this.getVersion());
        json.put("isQueryable", this.getIsQueryable());
        json.put("wmsUrl", this.getWmsUrl());
        json.put("style", this.getStyle());
        json.put("updated", this.getUpdated());
        json.put("created", this.getCreated());
        json.put("permissions", this.getPermissions());
        json.put("admin", this.getAdmin());


        return json;
    }


}

