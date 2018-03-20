package fi.nls.oskari.wmts.domain;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMS;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wms.WMSStyle;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public class WMTSCapabilitiesLayer {

    private final String id;
    private final String title;
    private final List<WMSStyle> styles;
    private final String defaultStyle;
    private final Set<String> formats;
    private final Set<String> infoFormats;
    private final List<ResourceUrl> resourceUrls;
    private final List<TileMatrixLink> links;

    public WMTSCapabilitiesLayer(String id, String title,
            List<WMSStyle> styles, String defaultStyle,
            Set<String> formats, Set<String> infoFormats,
            List<ResourceUrl> resourceUrls,
            List<TileMatrixLink> links) {
        this.id = id;
        this.title = title;
        this.styles = styles;
        this.defaultStyle = defaultStyle;
        this.formats = formats;
        this.infoFormats = infoFormats;
        this.resourceUrls = resourceUrls;
        this.links = links;
    }

    public List<ResourceUrl> getResourceUrls() {
        return resourceUrls;
    }

    public ResourceUrl getResourceUrlByType(final String type) {
        for (ResourceUrl url : resourceUrls) {
            if (url.getType().equalsIgnoreCase(type)) {
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

    public String getId() {
        return id;
    }

    public List<WMSStyle> getStyles() {
        return styles;
    }

    public String getDefaultStyle() {
        return defaultStyle;
    }

    public Set<String> getFormats() {
        return formats;
    }

    public Set<String> getInfoFormats() {
        return infoFormats;
    }

    public List<TileMatrixLink> getLinks() {
        return links;
    }

    public List<TileMatrixLimits> getLimits(String tileMatrixSet) {
        for (TileMatrixLink link : links) {
            if (tileMatrixSet.equals(link.getTileMatrixSet().getId())) {
                return link.getLimits();
            }
        }
        return null;
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
        for(TileMatrixLink link : getLinks()) {
            JSONArray limits = new JSONArray();
            String tms = link.getTileMatrixSet().getId();
            JSONHelper.putValue(jsonMatrixLinks, tms, limits);
            // works fine for single link, but has challenges on multiple
            // tileMatrixSetId is handled in WMTSCapabilitiesParser for multiple links
            JSONHelper.putValue(obj, "tileMatrixSetId", tms);

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
