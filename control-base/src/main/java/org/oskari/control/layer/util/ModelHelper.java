package org.oskari.control.layer.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.control.ActionCommonException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatter;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.control.layer.model.LayerExtendedOutput;
import org.oskari.control.layer.model.LayerOutput;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fi.nls.oskari.domain.map.OskariLayer.PROPERTY_AJAXURL;
import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.*;
import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.KEY_TIMES;

/**
 * Note! Work-in-progress - a bunch of methods have been copy-pasted from LayerJSONFormatter
 */
public class ModelHelper {
    private static final Logger LOG = LogFactory.getLogger(ModelHelper.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    public static LayerOutput getLayerForListing(OskariLayer layer, String lang) {
        return fillModel(new LayerOutput(), layer, lang, null);
    }

    public static LayerOutput getLayerDetails(OskariLayer layer, String lang, String crs) {
        // cast for convenience so it's easier to separate between base info and extended in the IDE
        LayerExtendedOutput extended = fillModel(new LayerExtendedOutput(), layer, lang, crs);
        // extended metadata
        extended.desc = layer.getTitle(lang);
        extended.gfiContent = layer.getGfiContent();
        JSONObject attributes = layer.getAttributes();
        if (!attributes.optBoolean(LayerJSONFormatter.KEY_ATTRIBUTE_IGNORE_COVERAGE, false)) {
            extended.coverage = getCoverageWKT(layer.getGeometry(), crs);
        }
        extended.attributes = getAsMap(attributes);
        extended._dataproviderName = layer.getGroup().getName(lang);

        JSONObject capabilities = layer.getCapabilities();
        if (attributes.has(KEY_ISQUERYABLE)) {
            // attributes can be used to force GFI for layer even if capabilities allow it or enable it not
            extended.isQueryable = attributes.optBoolean(KEY_ISQUERYABLE);
        } else {
            extended.isQueryable = capabilities.optBoolean(KEY_ISQUERYABLE);
        }

        // copy time from capabilities to attributes
        // time data is merged into attributes  (times:{start:,end:,interval:}  or times: []
        // this is used by the timeseries functionality for construction the UI
        if (capabilities.has(KEY_TIMES) && isTimeseriesLayer(layer)) {
            extended.attributes.put(KEY_TIMES, getAsList(capabilities.optJSONArray(KEY_TIMES)));
        }
        return cleanupModel(extended);
    }

    public static String getString(LayerOutput... output) throws ActionException {
        try {
            return MAPPER.writeValueAsString(output);
        } catch (Exception e) {
            throw new ActionCommonException("Error writing response", e);
        }
    }

    // shovel in the basic stuff
    private static <T extends LayerOutput> T fillModel(T output, OskariLayer layer, String lang, String crs) {
        output.id = layer.getId();
        output.layer = layer.getName();
        output.version = layer.getVersion();
        if (output.version == null) {
            output.version = layer.getCapabilities().optString(KEY_VERSION);
        }
        if (useProxy(layer)) {
            output.url = getProxyUrl(layer);
        } else {
            output.url = layer.getUrl();
        }
        output.type = layer.getType();
        if (layer.isCollection()) {
            // fixing frontend type for collection layers
            output.type = layer.isBaseMap() ? "base" : "groupMap";
        }
        output.name = layer.getName(lang);
        output.dataprovider = layer.getDataproviderId();
        output.opacity = layer.getOpacity();
        output.minScale = layer.getMinScale();
        output.maxScale = layer.getMaxScale();
        // TODO: move refreshRate & realtime to options?
        output.refreshRate = layer.getRefreshRate();
        output.realtime = layer.getRealtime();
        output.baseLayerId = layer.getParentId();
        output.options = getAsMap(layer.getOptions());
        output.params = getAsMap(layer.getParams());
        output.metadataId = LayerJSONFormatter.getMetadataUuid(layer);
        output.srs = LayerJSONFormatter.getSRSs(layer.getAttributes(), layer.getCapabilities());
        output.created = layer.getCreated();
        output.updated = layer.getUpdated();

        output.sublayers = layer.getSublayers().stream()
                .map(sublayer -> getLayerDetails(sublayer, lang, crs))
                .collect(Collectors.toList());
        return output;
    }

    private static Map<String, Object> getAsMap(JSONObject obj) {
        if (obj == null) {
            // since JSONHelper returns an immutable map for null -> we might add to the map later so handle it here instead
            return new HashMap<>();
        }
        return JSONHelper.getObjectAsMap(obj);
    }

    private static List<Object> getAsList(JSONArray obj) {
        return JSONHelper.getArrayAsList(obj);
    }

    /**
     * Sets default/empty values to null so they don't get written on the JSON output
     * @param output
     * @return
     */
    private static LayerOutput cleanupModel(LayerOutput output) {

        if (output.opacity == 100) {
            output.opacity = null;
        }
        if (output.minScale == -1) {
            output.minScale = null;
        }
        if (output.maxScale == -1) {
            output.maxScale = null;
        }
        if (output.refreshRate == 0) {
            output.refreshRate = null;
        }
        if (output.baseLayerId == -1) {
            output.baseLayerId = null;
        }
        if (!output.realtime) {
            output.realtime = null;
        }

        output.sublayers = output.sublayers.stream()
                .map(sublayer -> cleanupModel(sublayer))
                .collect(Collectors.toList());

        // remove style if there is only one style available?
        if (!(output instanceof LayerExtendedOutput)) {
            // anything beyond this point is extended so we can return early
            return output;
        }

        // cast for convenience so it's easier to separate between base info and extended in the IDE
        LayerExtendedOutput extended = (LayerExtendedOutput) output;

        if (!extended.isQueryable) {
            extended.isQueryable = null;
        }
        return extended;
    }

    protected static boolean useProxy(final OskariLayer layer) {
        boolean forceProxy = false;
        if (layer.getAttributes() != null) {
            forceProxy = layer.getAttributes().optBoolean("forceProxy", false);
        }
        return ((layer.getUsername() != null) && (layer.getUsername().length() > 0)) || forceProxy;
    }

    public static String getProxyUrl(final OskariLayer layer) {
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("action_route", "GetLayerTile");
        urlParams.put(LayerJSONFormatter.KEY_ID, Integer.toString(layer.getId()));
        return IOHelper.constructUrl(PropertyUtil.get(PROPERTY_AJAXURL), urlParams);
    }

    // value will be not added if transform failed, that's ok since client can't handle it if it's in unknown projection
    private static String getCoverageWKT(final String wktWGS84, final String mapSRS) {
        if (wktWGS84 == null || wktWGS84.isEmpty() || mapSRS == null || mapSRS.isEmpty()) {
            return null;
        }
        try {
            // WTK is saved as EPSG:4326 in database
            return WKTHelper.transformLayerCoverage(wktWGS84, mapSRS);
        } catch (Exception ex) {
            LOG.debug("Error transforming coverage to", mapSRS, "from", wktWGS84);
        }
        return null;
    }

    private static boolean isTimeseriesLayer(final OskariLayer layer) {
        JSONObject options = layer.getOptions();
        if(options != null && options.has("timeseries")) {
            JSONObject timeseriesOptions = options.optJSONObject("timeseries");
            if(timeseriesOptions != null && timeseriesOptions.has("ui")) {
                String ui = timeseriesOptions.optString("ui");
                if(ui != null && ui.equals("none")) {
                    return false;
                }
            }
        }
        return true;
    }
}
