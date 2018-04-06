package fi.nls.oskari.service.capabilities;

import fi.mml.map.mapwindow.service.wms.LayerNotFoundInCapabilitiesException;
import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceFactory;
import fi.mml.map.mapwindow.service.wms.WebMapServiceParseException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatter;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMS;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMTS;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wfs.GetGtWFSCapabilities;
import fi.nls.oskari.wmts.domain.ResourceUrl;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import fi.nls.oskari.wmts.domain.WMTSCapabilitiesLayer;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class OskariLayerCapabilitiesHelper {

    private static final Logger LOG = LogFactory.getLogger(OskariLayerCapabilitiesHelper.class);

    private static final String KEY_STYLES = "styles";
    private static final String KEY_NAME = "name";

    /**
     * Tries to parse WMS GetCapabilities response
     * @return the parsed WebMapService
     * @throws WebMapServiceParseException if something goes wrong
     * @throws LayerNotFoundInCapabilitiesException if layer can't be found in capabilities
     */
    public static WebMapService parseWMSCapabilities(String xml, OskariLayer ml)
            throws WebMapServiceParseException, LayerNotFoundInCapabilitiesException {
        // flush cache, otherwise only db is updated but code retains the old cached version
        WebMapServiceFactory.flushCache(ml.getId());
        return WebMapServiceFactory.createFromXML(ml.getName(), xml);
    }

    /**
     * @deprecated use {@link #setPropertiesFromCapabilitiesWMS(WebMapService, OskariLayer, Set)}
     */
    @Deprecated
    public static void setPropertiesFromCapabilitiesWMS(WebMapService wms, OskariLayer ml) {
        setPropertiesFromCapabilitiesWMS(wms, ml, null);
    }

    public static void setPropertiesFromCapabilitiesWMS(WebMapService wms,
            OskariLayer ml, Set<String> systemCRSs) {
        JSONObject caps = LayerJSONFormatterWMS.createCapabilitiesJSON(wms, systemCRSs);
        ml.setCapabilities(caps);
        ml.setCapabilitiesLastUpdated(new Date());
        //TODO: similiar parsing for WMS GetCapabilities for admin layerselector  and this
        // Parsing is processed twice:
        // 1st with geotools parsing for admin layerselector (styles are not parsered correct in all cases)
        // 2nd in this class
        // Fix default style, if no legendimage setup
        String style = getDefaultStyle(ml, caps);
        if (style != null) {
            ml.setStyle(style);
        }
    }

    private static String getDefaultStyle(OskariLayer ml, final JSONObject caps) {
        String style = null;
        if (ml.getId() == -1 && ml.getLegendImage() == null && caps.has(KEY_STYLES)) {
            // Take 1st style name for default - geotools parsing is not always correct
            JSONArray styles = JSONHelper.getJSONArray(caps, KEY_STYLES);
            JSONObject jstyle = JSONHelper.getJSONObject(styles, 0);
            if (jstyle != null) {
                style = JSONHelper.getStringFromJSON(jstyle, KEY_NAME, null);
                return style;
            }
        }
        return style;
    }

    /**
     * @deprecated use {@link #setPropertiesFromCapabilitiesWMTS(WMTSCapabilities, OskariLayer, String, Set)}
     */
    @Deprecated
    public static void setPropertiesFromCapabilitiesWMTS(WMTSCapabilities caps,
            OskariLayer ml, String crs) {
        setPropertiesFromCapabilitiesWMTS(caps, ml, crs, null);
    }

    public static void setPropertiesFromCapabilitiesWMTS(WMTSCapabilities caps,
            OskariLayer ml, String crs, Set<String> systemCRSs) {
        int id = ml.getId();
        String name = ml.getName();

        WMTSCapabilitiesLayer layer = caps.getLayer(name);
        if (layer == null) {
            String err = "Can not find Layer from GetCapabilities"
                    + " layer id:" + id + " name: " + name;
            LOG.warn(err);
            throw new IllegalArgumentException(err);
        }

        ResourceUrl resUrl = layer.getResourceUrlByType("tile");
        JSONObject options = ml.getOptions();
        if (resUrl != null) {
            JSONHelper.putValue(options, "requestEncoding", "REST");
            JSONHelper.putValue(options, "format", resUrl.getFormat());
            JSONHelper.putValue(options, "urlTemplate", resUrl.getTemplate());
        } else {
            LOG.debug("Layer", id, name, "does not report to support WMTS using RESTful");
            options.remove("requestEncoding");
            options.remove("format");
            options.remove("urlTemplate");
        }

        JSONObject jscaps = LayerJSONFormatterWMTS.createCapabilitiesJSON(layer, systemCRSs);
        ml.setCapabilities(jscaps);
        ml.setCapabilitiesLastUpdated(new Date());
    }

    public static void setPropertiesFromCapabilitiesWFS(OskariLayer ml,
            Set<String> systemCRSs) {
        Map<String, Object> capa = GetGtWFSCapabilities.getGtDataStoreCapabilities(
                ml.getUrl(), ml.getVersion(),
                ml.getUsername(), ml.getPassword(), ml.getSrs_name());
        Set<String> capabilitiesCRSs = GetGtWFSCapabilities.parseProjections(capa, ml.getName());
        Set<String> crss = LayerJSONFormatter.getCRSsToStore(systemCRSs, capabilitiesCRSs);

        JSONObject capabilities = new JSONObject();
        JSONHelper.put(capabilities, LayerJSONFormatter.KEY_SRS, new JSONArray(crss));
        ml.setCapabilities(capabilities);
        ml.setCapabilitiesLastUpdated(new Date());
    }
}
