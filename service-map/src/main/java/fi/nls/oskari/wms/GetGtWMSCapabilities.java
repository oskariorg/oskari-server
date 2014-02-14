package fi.nls.oskari.wms;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.service.ServiceException;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.ows.StyleImpl;
import org.geotools.data.ows.Service;
import org.geotools.data.wms.xml.MetadataURL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *  Methods for parsing WMS capabilities data
 */
public class GetGtWMSCapabilities {

    private static final Logger log = LogFactory.getLogger(GetGtWMSCapabilities.class);

    private final static String KEY_GROUPS = "groups";
    private final static String KEY_LAYERS = "layers";

    private final static String GROUP_LAYER_TYPE = "grouplayer";
    private final static String WMSLAYER_TYPE = "wmslayer";
    private final static String NAME_TEMP = "[nametemp]";
    private final static String LOCALES_TEMPLATE = "{\"fi\": \""+NAME_TEMP+"\", \"sv\": \""+NAME_TEMP+"\",\"en\": \""+NAME_TEMP+"\" }";

    /**
     * Get all WMS layers data in JSON  ( layer tree groups->groups/layers->groups/layers...
     * @param rurl WMS service url
     * @return
     * @throws ServiceException
     */
    public static JSONObject getWMSCapabilities(final String rurl) throws ServiceException {
        try {

            URL url = new URL(getUrl(rurl));
            WebMapServer wms = new WebMapServer(url);
            WMSCapabilities caps = wms.getCapabilities();

            // caps to json
            return parseLayer(caps.getLayer(), rurl, getMetaDataUrl(caps.getService()));
        } catch (Exception ex) {
            throw new ServiceException("Couldn't read/get wms capabilities response from url.", ex);
        }
    }

    /**
     * Parse layer (group- or wmslayer)
     * @param layer geotools layer
     * @param rurl WMS service url
     * @param metaUrl WMS service metadata url
     * @throws ServiceException
     */
    public static JSONObject parseLayer(Layer layer, String rurl, String metaUrl) throws ServiceException {
        if(layer == null) {
            return null;
        }
        try {
            if (layer.getLayerChildren().size() > 0) {
                // Add group layer
                WMSCapabilityLayer glayer = new WMSCapabilityLayer(GROUP_LAYER_TYPE, rurl, layer.getTitle(), metaUrl);
                JSONObject groupLayer = glayer.toJSON();
                JSONArray groups = new JSONArray();
                JSONArray layers = new JSONArray();
                groupLayer.put(KEY_GROUPS, groups);
                groupLayer.put(KEY_LAYERS, layers);
                // Loop children
                for (Iterator ii = layer.getLayerChildren().iterator(); ii.hasNext(); ) {
                    Layer sublayer = (Layer) ii.next();
                    if (sublayer != null) {
                        final JSONObject child = parseLayer(sublayer, rurl, metaUrl);
                        final String type = child.optString("type");
                        if(GROUP_LAYER_TYPE.equals(type)) {
                            groups.put(child);
                        }
                        else if(WMSLAYER_TYPE.equals(type)) {
                            layers.put(child);
                        }
                    }
                }
                return groupLayer;
            } else {
                // Parse layer to JSON
                return layerToOskariLayerJson(layer, rurl, metaUrl);
            }
        } catch (Exception ex) {
            throw new ServiceException("Couldn't parse wms capabilities layer", ex);
        }
    }

    /**
     *  WMS layer data to json
     * @param layer  geotools layer
     * @param rurl Wms service url
     * @param metaUrl wms service metadata url
     * @return
     * @throws ServiceException
     */
    public static JSONObject layerToOskariLayerJson(Layer layer, String rurl, String metaUrl) throws ServiceException {

        WMSCapabilityLayer nlayer = new WMSCapabilityLayer(WMSLAYER_TYPE, rurl, layer.getTitle(), metaUrl);
        try {
            // Get set capabilities data
            nlayer.setIsQueryable(layer.isQueryable());
            if (!isNaN(layer.getScaleDenominatorMax())) nlayer.setMaxScale(layer.getScaleDenominatorMax());
            if (!isNaN(layer.getScaleDenominatorMin())) nlayer.setMinScale(layer.getScaleDenominatorMin());
            nlayer.setWmsName(layer.getName());
            //Metadata
            List<MetadataURL> meta = layer.getMetadataURL();
            if (meta != null) {
                if (meta.size() > 0)
                {
                    nlayer.setDataUrl_uuid((String) meta.get(0).getUrl().toString());
                }
            }
            // Locale
            String loca = LOCALES_TEMPLATE.replace(NAME_TEMP,layer.getTitle());
            nlayer.setName(loca);
            // Keywords
            nlayer.setKeywords(layer.getKeywords());
            // Styles
            List<StyleImpl> styles = layer.getStyles();
            if (styles != null) {
                List<WMSStyle> ostyles = new ArrayList();
                for (Iterator ii = styles.iterator(); ii.hasNext(); ) {
                    StyleImpl style = (StyleImpl) ii.next();
                    WMSStyle ostyle = new WMSStyle();
                    ostyle.setTitle(style.getTitle().toString());
                    if(style.getLegendURLs().size() > 0) ostyle.setLegend((String) style.getLegendURLs().get(0));
                    ostyle.setName(style.getName());
                    ostyles.add(ostyle);
                }
                nlayer.setStyles(ostyles);
            }
            return nlayer.toJSON();
        } catch (Exception ex) {
            log.warn("Couldn't parse wmslayer to json", ex);
            return new JSONObject();
        }
    }

    /**
     * Finalise WMS service url for GetCapabilities request
     * @param urlin
     * @return
     */
    private static String getUrl(String urlin) {

        if (urlin.isEmpty())
            return "";
        String url = urlin;
        // check params
        if (url.indexOf("?") == -1) {
            url = url + "?";
            if (url.toLowerCase().indexOf("service=") == -1)
                url = url + "service=WMS";
            if (url.toLowerCase().indexOf("getcapabilities") == -1)
                url = url + "&request=GetCapabilities";
        } else {
            if (url.toLowerCase().indexOf("service=") == -1)
                url = url + "&service=WMS";
            if (url.toLowerCase().indexOf("getcapabilities") == -1)
                url = url + "&request=GetCapabilities";

        }

        return url;
    }

    /**
     * Numeric value test
     * @param v
     * @return
     */
    static public boolean isNaN(double v) {
        return (v != v);
    }

    /**
     * Get service metadata url
     * @param service  geotools WMS capabilities service
     * @return
     */
    static private String getMetaDataUrl(Service service) {
       if ( service.getOnlineResource() != null) return service.getOnlineResource().toString();
       return null;
    }
}
