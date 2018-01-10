package fi.nls.oskari.service.capabilities;

import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceFactory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMS;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMTS;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wfs.GetGtWFSCapabilities;
import fi.nls.oskari.wmts.domain.ResourceUrl;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import fi.nls.oskari.wmts.domain.WMTSCapabilitiesLayer;

import java.util.Date;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class OskariLayerCapabilitiesHelper {

    private static final Logger LOG = LogFactory.getLogger(OskariLayerCapabilitiesHelper.class);

    private static final String KEY_STYLES = "styles";
    private static final String KEY_NAME = "name";

    /**
     * Tries to parse WMS GetCapabilities response
     * @return the parsed WebMapService, null if something went wrong
     */
    public static WebMapService parseWMSCapabilities(String xml, OskariLayer ml) {
        // flush cache, otherwise only db is updated but code retains the old cached version
        WebMapServiceFactory.flushCache(ml.getId());
        return WebMapServiceFactory.createFromXML(ml.getName(), xml);
    }

    public static void setPropertiesFromCapabilitiesWMS(WebMapService wms, OskariLayer ml) {
        JSONObject caps = LayerJSONFormatterWMS.createCapabilitiesJSON(wms);
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
        ml.setSupportedCRSs(LayerJSONFormatterWMS.getCRSs(wms));
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

    public static void setPropertiesFromCapabilitiesWMTS(WMTSCapabilities caps,
            OskariLayer ml, String crs) {
        int id = ml.getId();
        String name = ml.getName();

        WMTSCapabilitiesLayer layer = caps.getLayer(name);
        if (layer == null) {
            /*
             * TODO: Push a notification to a 'Admin notification service', disable layer?
             */
            LOG.warn("Can not find Layer from GetCapabilities"
                    + " layer id", id, "name", name);
            throw new IllegalArgumentException();
        }

        ResourceUrl resUrl = layer.getResourceUrlByType("tile");
        if (resUrl == null) {
            /*
             * TODO: Push a notification to a 'Admin notification service', disable layer?
             */
            LOG.warn("Can not find ResourceUrl of type 'tile' from GetCapabilities"
                    + " layer id", id, "name", name);
            throw new IllegalArgumentException();
        }

        JSONObject options = ml.getOptions();
        JSONHelper.putValue(options, "requestEncoding", "REST");
        JSONHelper.putValue(options, "format", resUrl.getFormat());
        JSONHelper.putValue(options, "urlTemplate", resUrl.getTemplate());

        JSONObject jscaps = LayerJSONFormatterWMTS.createCapabilitiesJSON(layer);
        ml.setCapabilities(jscaps);
        ml.setCapabilitiesLastUpdated(new Date());

        crs = crs != null ? crs : ml.getSrs_name();
        ml.setTileMatrixSetId(LayerJSONFormatterWMTS.getTileMatrixSetId(jscaps, crs));

        ml.setSupportedCRSs(LayerJSONFormatterWMTS.getCRSs(layer));
    }

    public static void setPropertiesFromCapabilitiesWFS(OskariLayer ml) {
        Map<String, Object> capa = GetGtWFSCapabilities.getGtDataStoreCapabilities(
                ml.getUrl(), ml.getVersion(), ml.getUsername(), ml.getPassword(), ml.getSrs_name());
        ml.setSupportedCRSs(GetGtWFSCapabilities.parseProjections(capa, ml.getVersion(), ml.getName()));
        ml.setCapabilitiesLastUpdated(new Date());
    }

}
