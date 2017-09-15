package fi.nls.oskari.service.capabilities;

import javax.xml.stream.XMLStreamException;

import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceFactory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMS;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMTS;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wfs.GetGtWFSCapabilities;
import fi.nls.oskari.wmts.WMTSCapabilitiesParser;
import fi.nls.oskari.wmts.domain.ResourceUrl;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import fi.nls.oskari.wmts.domain.WMTSCapabilitiesLayer;

public class OskariLayerCapabilitiesHelper {

    private static final String KEY_STYLES = "styles";
    private static final String KEY_NAME = "name";

    public static void setPropertiesFromCapabilitiesWMS(OskariLayerCapabilities capabilities, OskariLayer ml)
            throws ServiceException {
        // flush cache, otherwise only db is updated but code retains the old cached version
        WebMapServiceFactory.flushCache(ml.getId());
        // parse capabilities
        WebMapService wms = WebMapServiceFactory.createFromXML(ml.getName(), capabilities.getData());
        if (wms == null) {
            throw new ServiceException("Couldn't parse capabilities for service!");
        }
        JSONObject caps = LayerJSONFormatterWMS.createCapabilitiesJSON(wms);
        ml.setCapabilities(caps);
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

    public static void setPropertiesFromCapabilitiesWMTS(OskariLayerCapabilities capabilities,
            OskariLayer ml, String crs) throws IllegalArgumentException, XMLStreamException {
        // parse capabilities
        WMTSCapabilities caps = WMTSCapabilitiesParser.parseCapabilities(capabilities.getData());
        WMTSCapabilitiesLayer layer = caps.getLayer(ml.getName());
        ResourceUrl resUrl = layer.getResourceUrlByType("tile");
        JSONObject options = ml.getOptions();
        if(resUrl != null) {
            JSONHelper.putValue(options, "requestEncoding", "REST");
            JSONHelper.putValue(options, "format", resUrl.getFormat());
            JSONHelper.putValue(options, "urlTemplate", resUrl.getTemplate());
        }

        JSONObject jscaps = LayerJSONFormatterWMTS.createCapabilitiesJSON(layer);
        ml.setCapabilities(jscaps);

        crs = crs != null ? crs : ml.getSrs_name();
        ml.setTileMatrixSetId(LayerJSONFormatterWMTS.getTileMatrixSetId(jscaps, crs));

        ml.setSupportedCRSs(LayerJSONFormatterWMTS.getCRSs(layer));
    }

    public static void setPropertiesFromCapabilitiesWFS(OskariLayer ml) {
        Map<String, Object> capa = GetGtWFSCapabilities.getGtDataStoreCapabilities(
                ml.getUrl(), ml.getVersion(), ml.getUsername(), ml.getPassword(), ml.getSrs_name());
        ml.setSupportedCRSs(GetGtWFSCapabilities.parseProjections(capa, ml.getVersion(), ml.getName()));
    }

}
