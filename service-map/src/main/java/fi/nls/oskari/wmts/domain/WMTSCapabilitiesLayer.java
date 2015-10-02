package fi.nls.oskari.wmts.domain;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMS;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wms.WMSStyle;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 17.2.2014
 * Time: 12:48
 * To change this template use File | Settings | File Templates.
 */
public class WMTSCapabilitiesLayer {

    private String title;
    private String id;
    private List<WMSStyle> styles = new ArrayList<WMSStyle>();
    private String defaultStyle;
    private Set<String> formats = new HashSet<String>();
    private Set<String> infoFormats = new HashSet<String>();
    private List<ResourceUrl> resourceUrls = new ArrayList<>();

    private Map<String, Set<TileMatrixLimits>> links = new HashMap<String, Set<TileMatrixLimits>>();

    private Set<String> keywords = new HashSet<String>();

    public List<ResourceUrl> getResourceUrls() {
        return resourceUrls;
    }

    public void addResourceUrl(ResourceUrl url) {
        getResourceUrls().add(url);
    }
    public ResourceUrl getResourceUrlByType(final String type) {
        for(ResourceUrl url : resourceUrls) {
            if(url.getType().equalsIgnoreCase(type)) {
                return url;
            }
        }
        return null;
    }


    public boolean isQueryable() {
        return infoFormats.size() > 0;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addStyle(WMSStyle style) {
        getStyles().add(style);
    }

    public List<WMSStyle> getStyles() {
        return styles;
    }

    public void setStyles(List<WMSStyle> styles) {
        this.styles = styles;
    }

    public String getDefaultStyle() {
        return defaultStyle;
    }

    public void setDefaultStyle(String defaultStyle) {
        this.defaultStyle = defaultStyle;
    }

    public void addFormat(String format) {
        getFormats().add(format);
    }
    public Set<String> getFormats() {
        return formats;
    }

    public void setFormats(Set<String> formats) {
        this.formats = formats;
    }

    public void addInfoFormat(String format) {
        getInfoFormats().add(format);
    }
    public Set<String> getInfoFormats() {
        return infoFormats;
    }

    public void setInfoFormats(Set<String> infoFormats) {
        this.infoFormats = infoFormats;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }

    public Map<String, Set<TileMatrixLimits>> getLinks() {
        return links;
    }
    public Set<TileMatrixLimits> getLimits(final String tileMatrixSet) {
        return links.get(tileMatrixSet);
    }

    public void setLinks(Map<String, Set<TileMatrixLimits>> links) {
        this.links = links;
    }

    public JSONObject getAsJSON() {
        final JSONObject obj = new JSONObject();
        JSONHelper.putValue(obj, "type", OskariLayer.TYPE_WMTS);
        JSONHelper.putValue(obj, "isQueryable", getInfoFormats().size() > 0);
        JSONHelper.putValue(obj, "name", getTitle());

        // TODO: setup min/maxscale based on tilematrix?

        JSONHelper.putValue(obj, "layerName", getId());
        JSONHelper.putValue(obj, "title", getTitle());
        JSONHelper.putValue(obj, "style", getDefaultStyle());

        final JSONArray styles = new JSONArray();
        JSONHelper.putValue(obj, "styles", styles);
        try {
            for(WMSStyle style : getStyles()) {
                styles.put(style.toJSON());
            }
        } catch (Exception ignored) {}

        // using layer json formatter to get the same JSON here
        JSONHelper.putValue(obj, "formats", LayerJSONFormatterWMS.getFormatsJSON(getInfoFormats()));

        final JSONObject jsonMatrixLinks = new JSONObject();
        JSONHelper.putValue(obj, "TileMatrixSetLinks", jsonMatrixLinks);
        for(String matrixSet : getLinks().keySet()) {
            JSONArray limits = new JSONArray();
            JSONHelper.putValue(jsonMatrixLinks, matrixSet, limits);
            // works fine for single link, but has challenges on multiple
            // tileMatrixSetId is handled in WMTSCapabilitiesParser for multiple links
            JSONHelper.putValue(obj, "tileMatrixSetId", matrixSet);

            // TODO: setup limits
            // limits are not used for now so skipping
            /*
            for(TileMatrixLimits l : getLimits(matrixSet)) {

            }
            */

        }

        return obj;
    }
}
