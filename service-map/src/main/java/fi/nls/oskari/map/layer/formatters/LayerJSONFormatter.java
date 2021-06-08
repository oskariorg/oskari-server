package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.utils.common.Sets;

import java.util.*;

import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.*;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 17.12.2013
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class LayerJSONFormatter {

    public static final String PROPERTY_AJAXURL = "oskari.ajax.url.prefix";
    public static final String KEY_ATTRIBUTE_FORCED_SRS = "forcedSRS";
    public static final String KEY_ATTRIBUTE_IGNORE_COVERAGE = "ignoreCoverage";
    public static final String KEY_LEGENDS = "legends";
    public static final String KEY_GLOBAL_LEGEND = "legendImage";
    public static final String KEY_TYPE = "type";
    public static final String KEY_DATA_PROVIDER_ID = "dataproviderId";
    protected static final String KEY_ID = "id";
    protected static final String KEY_NAME = "layerName"; // FIXME: name
    protected static final String KEY_LOCALIZED_NAME = "name"; // FIXME: title
    protected static final String KEY_SUBTITLE = "subtitle";
    protected static final String KEY_OPTIONS = "options";
    protected static final String KEY_ADMIN = "admin";
    protected static final String KEY_DATA_PROVIDER = "orgName";
    protected static final String[] STYLE_KEYS ={"name", "title", "legend"};

    // There working only plain text and html so ranked up
    public static String[] SUPPORTED_GET_FEATURE_INFO_FORMATS = new String[] {
            "text/html", "text/plain", "application/vnd.ogc.se_xml",
            "application/vnd.ogc.gml", "application/vnd.ogc.wms_xml",
            "text/xml", "application/json" };

    private static final Logger LOG = LogFactory.getLogger(LayerJSONFormatter.class);
    // map different layer types for JSON formatting
    private static Map<String, LayerJSONFormatter> typeMapping = new HashMap<String, LayerJSONFormatter>();
    static {
        typeMapping.put(OskariLayer.TYPE_WMS, new LayerJSONFormatterWMS());
        typeMapping.put(OskariLayer.TYPE_WFS, new LayerJSONFormatterWFS());
        typeMapping.put(OskariLayer.TYPE_WMTS, new LayerJSONFormatterWMTS());
        typeMapping.put(OskariLayer.TYPE_VECTOR_TILE, new LayerJSONFormatterVectorTile());
    }

    private static LayerJSONFormatter getFormatter(final String type) {
        if(type == null) {
            return null;
        }
        final LayerJSONFormatter formatter = typeMapping.get(type);
        if(formatter != null) {
            return formatter;
        }
        return null;
    }

    /**
     * Use to add custom layer JSON formatter for custom layer type
     * @param type
     * @param formatter
     */
    public static void addFormatter(final String type, final LayerJSONFormatter formatter) {
        typeMapping.put(type, formatter);
    }

    public JSONObject getJSON(final OskariLayer layer,
                                     final String lang,
                                     final boolean isSecure,
                                     final String crs) {
        LayerJSONFormatter formatter = getFormatter(layer.getType());
        // to prevent nullpointer and infinite loop
        if(formatter != null && !formatter.getClass().equals(LayerJSONFormatter.class)) {
            return formatter.getJSON(layer, lang, isSecure, crs);
        }
        return getBaseJSON(layer, lang, isSecure, crs);
    }

    public JSONObject getBaseJSON(final OskariLayer layer,
                                     final String lang,
                                     final boolean isSecure,
                                     final String crs) {
        JSONObject layerJson = new JSONObject();

        JSONHelper.putValue(layerJson, KEY_ID, layer.getId());

        //LOG.debug("Type", layer.getType());
        if(layer.isCollection()) {
            // fixing frontend type for collection layers
            if(layer.isBaseMap()) {
                JSONHelper.putValue(layerJson, KEY_TYPE, "base");
            }
            else {
                JSONHelper.putValue(layerJson, KEY_TYPE, "groupMap");
            }
        }
        else {
            JSONHelper.putValue(layerJson, KEY_TYPE, layer.getType());
            //LOG.debug("wmsName", layer.getName());
            // for easier proxy routing on ssl hosts, maps all urls with prefix and a simplified url
            // so tiles can be fetched from same host from browsers p.o.v. and the actual url
            // is proxied with a proxy for example: /proxythis/<actual wmsurl>
            JSONHelper.putValue(layerJson, "url", layer.getUrl(isSecure));
            JSONHelper.putValue(layerJson, KEY_NAME, layer.getName());
            if (useProxy(layer)) {
                JSONHelper.putValue(layerJson, "url", getProxyUrl(layer));
            }
        }

        JSONHelper.putValue(layerJson, KEY_LOCALIZED_NAME, layer.getName(lang));
        JSONHelper.putValue(layerJson, KEY_SUBTITLE, layer.getTitle(lang));
        if(layer.getGroup() != null) {
            JSONHelper.putValue(layerJson, KEY_DATA_PROVIDER, layer.getGroup().getName(lang));
            JSONHelper.putValue(layerJson, KEY_DATA_PROVIDER_ID, layer.getGroup().getId());
        }

        if(layer.getOpacity() != null && layer.getOpacity() > -1 && layer.getOpacity() <= 100) {
            JSONHelper.putValue(layerJson, "opacity", layer.getOpacity());
        }
        if(layer.getMinScale() != null && layer.getMinScale() != -1) {
            JSONHelper.putValue(layerJson, "minScale", layer.getMinScale());
        }
        if(layer.getMaxScale() != null && layer.getMaxScale() != -1) {
            JSONHelper.putValue(layerJson, "maxScale", layer.getMaxScale());
        }
        JSONObject attributes = layer.getAttributes();
        if (!attributes.optBoolean(KEY_ATTRIBUTE_IGNORE_COVERAGE, false)) {
            addLayerCoverageWKT(layerJson, layer.getGeometry(), crs);
        }

        JSONHelper.putValue(layerJson, "params", layer.getParams());
        JSONHelper.putValue(layerJson, KEY_OPTIONS, layer.getOptions());
        JSONHelper.putValue(layerJson, "attributes", attributes);

        JSONHelper.putValue(layerJson, "realtime", layer.getRealtime());
        JSONHelper.putValue(layerJson, "refreshRate", layer.getRefreshRate());

        JSONHelper.putValue(layerJson, "srs_name", layer.getSrs_name());
        JSONHelper.putValue(layerJson, "version", layer.getVersion());

        JSONHelper.putValue(layerJson, "baseLayerId", layer.getParentId());

        JSONHelper.putValue(layerJson, "created", layer.getCreated());
        JSONHelper.putValue(layerJson, "updated", layer.getUpdated());

        JSONHelper.putValue(layerJson, KEY_METADATA, getMetadataUuid(layer));
        JSONHelper.putValue(layerJson, "style", layer.getStyle());

        // setup supported projections
        Set<String> srs = getSRSs(layer.getAttributes(), layer.getCapabilities());
        if (srs != null) {
            JSONHelper.putValue(layerJson, KEY_SRS, new JSONArray(srs));
        }

        // sublayer handling
        if(layer.getSublayers() != null && !layer.getSublayers().isEmpty()) {
            JSONArray sublayers = new JSONArray();
            for(OskariLayer sub : layer.getSublayers()) {
                JSONObject subJSON = getJSON(sub, lang, isSecure, crs);
                sublayers.put(subJSON);
            }
            JSONHelper.putValue(layerJson, "subLayer", sublayers);
        }
        return layerJson;
    }

    public void removeAdminInfo(final JSONObject layer) {
        if(layer == null) {
            return;
        }
        layer.remove(KEY_ADMIN);
    }

    public void addInfoForAdmin(final JSONObject layer, final String key, final Object value) {
        if(layer == null) {
            return;
        }
        // ensure we have the admin block in place
        JSONObject additionalData = layer.optJSONObject(KEY_ADMIN);
        if(additionalData == null) {
            additionalData = new JSONObject();
            JSONHelper.putValue(layer, KEY_ADMIN, additionalData);
        }
        JSONHelper.putValue(additionalData, key, value);
    }
    protected boolean useProxy(final OskariLayer layer) {
        boolean forceProxy = false;
        if (layer.getAttributes() != null) {
            forceProxy = layer.getAttributes().optBoolean("forceProxy", false);
        }
        return ((layer.getUsername() != null) && (layer.getUsername().length() > 0)) || forceProxy;
    }

    protected boolean isBeingProxiedViaOskariServer(String url) {
        if(url == null || url.isEmpty()) {
            return false;
        }
        return url.startsWith(PropertyUtil.get(PROPERTY_AJAXURL));
    }

    public String getProxyUrl(final OskariLayer layer) {
        Map<String, String> urlParams = new HashMap<String, String>();
        urlParams.put("action_route", "GetLayerTile");
        urlParams.put(KEY_ID, Integer.toString(layer.getId()));
        return IOHelper.constructUrl(PropertyUtil.get(PROPERTY_AJAXURL), urlParams);
    }

    public JSONArray createStylesJSON(OskariLayer layer, boolean isSecure) {
        JSONArray styles = new JSONArray();
        Map<String, String> legends = JSONHelper.getObjectAsMap(layer.getOptions().optJSONObject(KEY_LEGENDS));
        JSONArray styleList = JSONHelper.getEmptyIfNull(layer.getCapabilities().optJSONArray(KEY_STYLES));
        String globalLegend = legends.getOrDefault(KEY_GLOBAL_LEGEND, "");
        if (styleList.length() == 0 && !globalLegend.isEmpty()) {
            styleList = new JSONArray();
            styleList.put(createStylesJSON("","" , globalLegend));
        }
        for(int i = 0; i < styleList.length(); i++) {
            JSONObject style = styleList.optJSONObject(i);
            String legend = style.optString(KEY_LEGEND);
            String name = style.optString(KEY_STYLE_NAME);
            String title = style.optString(KEY_STYLE_TITLE);
            if (legends.containsKey(name)) {
                legend = legends.get(name);
            } else if (!globalLegend.isEmpty()) {
                legend = globalLegend;
            }
            if (!legend.isEmpty()) {
                boolean secureUrl = legend.toLowerCase().startsWith("https://") || legend.startsWith("/");
                if ((!secureUrl && isSecure) || useProxy(layer)) {
                    legend = buildLegendUrl(layer, name);
                }
            }
            styles.put(createStylesJSON(name, title, legend));
        }
        return styles;
    }
    public static JSONObject createStylesJSON(String name, String title, String legend) {
        final JSONObject style = JSONHelper.createJSONObject(STYLE_KEYS[0], name);
        JSONHelper.putValue(style, STYLE_KEYS[1], title);
        JSONHelper.putValue(style, STYLE_KEYS[2], legend);
        return style;
    }
    protected String buildLegendUrl(final OskariLayer layer, final String styleName) {
        Map<String, String> urlParams = new HashMap<String, String>();
        urlParams.put("action_route", "GetLayerTile");
        urlParams.put("id", Integer.toString(layer.getId()));
        urlParams.put(KEY_LEGEND, "true");
        if(styleName != null){
            urlParams.put(KEY_STYLE, styleName);
        }
        return IOHelper.constructUrl(PropertyUtil.get(PROPERTY_AJAXURL), urlParams);
    }
    private String getMetadataUuid (OskariLayer layer) {
        String fixed = LayerJSONFormatter.getFixedDataUrl(layer.getMetadataId());
        if (fixed != null) {
            return fixed;
        }
        return layer.getCapabilities().optString(KEY_METADATA);
    }

    // This is solution of transition for dataUrl and for dataUrl_uuid
    public static String getFixedDataUrl(String metadataId) {
        if(metadataId == null || metadataId.isEmpty()) {
            return null;
        }
        if(!metadataId.toLowerCase().startsWith("http")) {
            // not a url -> return as is
            return metadataId;
        }
        try {
            Map<String, List<String>> params = IOHelper.parseQuerystring(metadataId);
            String idParam = params.keySet().stream()
                    .filter(key -> "uuid".equalsIgnoreCase(key) || KEY_ID.equalsIgnoreCase(key))
                    .findFirst()
                    .orElse(null);
            if (idParam == null) {
                // param not in url
                return null;
            }
            List<String> values = params.getOrDefault(idParam, Collections.emptyList());
            if (values.isEmpty()) {
                // param was present but has no value
                return null;
            }
            return values.get(0);
        } catch (Exception ignored) {
            // propably just not valid URL
            LOG.ignore("Unexpected error parsing metadataid", ignored);
        }
        LOG.debug("Couldn't parse uuid from metadata url:", metadataId);
        return null;
    }

    /**
     * Merge forced SRSs from attributes and the ones parsed from
     * GetCapabilities response into one Set of unique values
     * @param attributes of OskariLayer in question, can be null
     * @param capabilities of OskariLayer in question, can be null
     * @return null iff attributes.forcedSRS and capabilities.srs are both null
     *         otherwise a Set containing both (can be empty)
     */
    protected static Set<String> getSRSs(JSONObject attributes, JSONObject capabilities) {
        JSONArray jsonForcedSRS = attributes != null ? attributes.optJSONArray(KEY_ATTRIBUTE_FORCED_SRS): null;
        JSONArray jsonCapabilitiesSRS = capabilities != null ? capabilities.optJSONArray(KEY_SRS): null;
        if (jsonForcedSRS == null && jsonCapabilitiesSRS == null) {
            LOG.debug("No SRS information found from either attributes or capabilities");
            return null;
        }
        Set<String> srs = new HashSet<>();
        srs.addAll(JSONHelper.getArrayAsList(jsonForcedSRS));
        srs.addAll(JSONHelper.getArrayAsList(jsonCapabilitiesSRS));
        LOG.debug("SRSs from attributes and capabilities:", srs);
        return srs;
    }
    public static JSONObject getFormatsJSON(final Collection<String> formats) {
        final JSONObject formatJSON = new JSONObject();
        final JSONArray available = new JSONArray();
        JSONHelper.putValue(formatJSON, KEY_AVAILABLE, available);
        if(formats == null) {
            return formatJSON;
        }
        try {
            String value = null;
            for (String supported : SUPPORTED_GET_FEATURE_INFO_FORMATS) {
                if (formats.contains(supported)) {
                    if(value == null) {
                        // get the first one as default
                        value = supported;
                    }
                    // gather list of supported formats
                    available.put(supported);
                }
            }
            // default format
            JSONHelper.putValue(formatJSON, KEY_VALUE, value);
            return formatJSON;

        } catch (Exception e) {
            LOG.warn(e, "Couldn't parse formats for layer");
        }
        return formatJSON;
    }

    public static Set<String> getCRSsToStore(Set<String> systemCRSs,
            Set<String> capabilitiesCRSs) {
        if (systemCRSs == null || systemCRSs.isEmpty()) {
            return capabilitiesCRSs;
        }
        return Sets.intersection(systemCRSs, capabilitiesCRSs);
    }
    // value will be not added if transform failed, that's ok since client can't handle it if it's in unknown projection
    public static void addLayerCoverageWKT(final JSONObject layerJSON, final String wktWGS84, final String mapSRS) {
        if(wktWGS84 == null || wktWGS84.isEmpty() || mapSRS == null || mapSRS.isEmpty()) {
            return;
        }
        try {
            // WTK is saved as EPSG:4326 in database
            final String transformed = WKTHelper.transformLayerCoverage(wktWGS84, mapSRS);
            if(transformed == null) {
                LOG.debug("Transform failed for layer id:", layerJSON.opt("id"), "WKT was:", wktWGS84);
                return;
            }
            JSONHelper.putValue(layerJSON, KEY_LAYER_COVERAGE, transformed);
        } catch (Exception ex) {
            LOG.debug("Error transforming coverage to", mapSRS, "from", wktWGS84);
        }
    }
    protected static String coverageToWKT (ReferencedEnvelope env) {
        try {
            CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326", true);
            env = env.transform(wgs84, true);
            return WKTHelper.getBBOX(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
        } catch (Exception e) {
            LOG.debug("Error transforming coverage to WGS84 WKT");
        }
        return null;
    }
}
