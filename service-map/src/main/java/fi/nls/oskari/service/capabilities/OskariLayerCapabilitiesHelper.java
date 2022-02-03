package fi.nls.oskari.service.capabilities;

import fi.mml.map.mapwindow.service.wms.LayerNotFoundInCapabilitiesException;
import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceFactory;
import fi.mml.map.mapwindow.service.wms.WebMapServiceParseException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWFS;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMS;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.internal.WFSGetCapabilities;
import org.geotools.ows.wms.Layer;
import org.geotools.ows.wms.WMSCapabilities;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.service.wfs3.WFS3Service;

import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.KEY_STYLES;
public class OskariLayerCapabilitiesHelper {

    private static final Logger LOG = LogFactory.getLogger(OskariLayerCapabilitiesHelper.class);
    private static final String KEY_NAME = "name";

    /**
     * Tries to parse WMS GetCapabilities response
     * @return the parsed WebMapService
     * @throws WebMapServiceParseException if something goes wrong
     * @throws LayerNotFoundInCapabilitiesException if layer can't be found in capabilities
     */
    @Deprecated
    public static WebMapService parseWMSCapabilities(String xml, OskariLayer ml)
            throws WebMapServiceParseException, LayerNotFoundInCapabilitiesException {
        // flush cache, otherwise only db is updated but code retains the old cached version
        WebMapServiceFactory.flushCache(ml.getId());
        return WebMapServiceFactory.createFromXML(ml.getName(), xml);
    }

    @Deprecated
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
    public static void setPropertiesFromCapabilitiesWMS(WMSCapabilities capa, Layer capabilitiesLayer,
                                                        OskariLayer ml, Set<String> systemCRSs) {
        JSONObject caps = LayerJSONFormatterWMS.createCapabilitiesJSON(capa, capabilitiesLayer, systemCRSs);
        ml.setCapabilities(caps);
        ml.setCapabilitiesLastUpdated(new Date());
    }

    public static void setDefaultStyleFromCapabilitiesJSON(OskariLayer ml) {
        JSONArray styles = ml.getCapabilities().optJSONArray(KEY_STYLES);
        String style = "";
        if (styles != null && styles.length() > 0) {
            style = JSONHelper.optString(JSONHelper.getJSONObject(styles, 0), "name");
        }
        ml.setStyle(style);
    }

    @Deprecated
    private static String getDefaultStyle(OskariLayer ml, final JSONObject caps) {
        String style = null;
        if (ml.getId() == -1 && ml.getLegendImage() == null && caps.has(KEY_STYLES)) {
            // Take 1st style name for default - geotools parsing is not always correct
            JSONArray styles = JSONHelper.getJSONArray(caps, KEY_STYLES);
            if (styles.length() == 0) {
                return null;
            }
            JSONObject jstyle = JSONHelper.getJSONObject(styles, 0);
            if (jstyle != null) {
                style = JSONHelper.getStringFromJSON(jstyle, KEY_NAME, null);
                return style;
            }
        }
        return style;
    }

    public static void setPropertiesFromCapabilitiesWFS(WFSDataStore data, OskariLayer ml,
                                                        Set<String> systemCRSs) throws ServiceException {
        try {
            SimpleFeatureSource source = data.getFeatureSource(ml.getName());
            WFSGetCapabilities capa = data.getWfsClient().getCapabilities();
            setPropertiesFromCapabilitiesWFS(capa, source, ml, systemCRSs);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't find layer: " + ml.getName());
        }
    }

    public static void setPropertiesFromCapabilitiesWFS(WFSGetCapabilities capa, SimpleFeatureSource source, OskariLayer ml,
                                                        Set<String> systemCRSs) throws ServiceException {
        ml.setCapabilities(LayerJSONFormatterWFS.createCapabilitiesJSON(capa, source, systemCRSs));
        ml.setCapabilitiesLastUpdated(new Date());
    }

    public static void setPropertiesFromCapabilitiesOAPIF(WFS3Service service, OskariLayer ml,
                                                        Set<String> systemCRSs) {
        ml.setCapabilities(LayerJSONFormatterWFS.createCapabilitiesJSON(service, ml.getName(), systemCRSs));
        ml.setCapabilitiesLastUpdated(new Date());
    }

}
