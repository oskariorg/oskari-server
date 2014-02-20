package fi.nls.oskari.wms;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMS;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.ows.StyleImpl;
import org.geotools.data.ows.Service;
import org.geotools.data.wms.xml.MetadataURL;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URL;
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
    private final static LayerJSONFormatterWMS FORMATTER = new LayerJSONFormatterWMS();

    /**
     * Get all WMS layers data in JSON  ( layer tree groups->groups/layers->groups/layers...
     * @param rurl WMS service url
     * @return
     * @throws ServiceException
     */
    public static JSONObject getWMSCapabilities(final String rurl) throws ServiceException {
        try {

            URL url = new URL(getUrl(rurl));
            WebMapServer wms = new WebMapServer(url, IOHelper.getReadTimeoutMs());
            WMSCapabilities caps = wms.getCapabilities();
            // caps to json
            return parseLayer(caps.getLayer(), rurl, caps);
        } catch (Exception ex) {
            throw new ServiceException("Couldn't read/get wms capabilities response from url.", ex);
        }
    }

    /**
     * Parse layer (group- or wmslayer)
     * @param layer geotools layer
     * @param rurl WMS service url
     * @param caps WMS capabilities
     * @throws ServiceException
     */
    public static JSONObject parseLayer(Layer layer, String rurl, WMSCapabilities caps) throws ServiceException {
        if(layer == null) {
            return null;
        }
        try {
            if (layer.getLayerChildren().size() > 0) {
                // Add group of layers
                final JSONObject groupNode = new JSONObject();
                // note that this group layer is NOT the same as Oskari groupMap but a
                // presentational group "root" node which will be handled in admin bundle
                JSONHelper.putValue(groupNode, "type", GROUP_LAYER_TYPE);
                JSONHelper.putValue(groupNode, "title", layer.getTitle());
                JSONHelper.putValue(groupNode, "version", caps.getVersion());

                JSONArray groups = new JSONArray();
                JSONArray layers = new JSONArray();
                groupNode.put(KEY_GROUPS, groups);
                groupNode.put(KEY_LAYERS, layers);
                if(layer.getName() != null && !layer.getName().isEmpty()) {
                    // add self to layers if we have a wmsName so
                    // the group node layers are selectable as well on frontend
                    final JSONObject self = layerToOskariLayerJson(layer, rurl, caps);
                    layers.put(self);
                }
                // Loop children
                for (Iterator ii = layer.getLayerChildren().iterator(); ii.hasNext(); ) {
                    Layer sublayer = (Layer) ii.next();
                    if (sublayer != null) {
                        final JSONObject child = parseLayer(sublayer, rurl, caps);
                        final String type = child.optString("type");
                        if(GROUP_LAYER_TYPE.equals(type)) {
                            groups.put(child);
                        }
                        else if(OskariLayer.TYPE_WMS.equals(type)) {
                            layers.put(child);
                        }
                    }
                }
                return groupNode;
            } else {
                // Parse layer to JSON
                return layerToOskariLayerJson(layer, rurl, caps);
            }
        } catch (Exception ex) {
            throw new ServiceException("Couldn't parse wms capabilities layer", ex);
        }
    }

    /**
     * WMS layer data to json
     * @param capabilitiesLayer  geotools layer
     * @param rurl Wms service url
     * @param caps wms capabilities
     * @return
     * @throws ServiceException
     */
    public static JSONObject layerToOskariLayerJson(Layer capabilitiesLayer, String rurl, WMSCapabilities caps) throws ServiceException {

        final OskariLayer oskariLayer = new OskariLayer();
        oskariLayer.setType(OskariLayer.TYPE_WMS);
        oskariLayer.setUrl(rurl);
        oskariLayer.setMaxScale(capabilitiesLayer.getScaleDenominatorMax());
        oskariLayer.setMinScale(capabilitiesLayer.getScaleDenominatorMin());
        oskariLayer.setName(capabilitiesLayer.getName());

        // setup UI names for all supported languages
        final String[] languages = PropertyUtil.getSupportedLanguages();
        for(String lang : languages) {
            oskariLayer.setName(lang, capabilitiesLayer.getTitle());
        }

// JSON formatter will parse uuid from url
/*
OnlineResource xlink:type="simple" xlink:href="http://www.paikkatietohakemisto.fi/geonetwork/srv/en/main.home?uuid=a22ec97f-d418-4957-9b9d-e8b4d2ec3eac"/>
<inspire_common:MetadataUrl xsi:type="inspire_common:resourceLocatorType"><inspire_common:URL>http://www.paikkatietohakemisto.fi/geonetwork/srv/fi/iso19139.xml?uuid=a22ec97f-d418-4957-9b9d-e8b4d2ec3eac</inspire_common:URL>
*/

        oskariLayer.setMetadataId(getMetaDataUrl(caps.getService()));
        final List<MetadataURL> meta = capabilitiesLayer.getMetadataURL();
        if (meta != null) {
            if (meta.size() > 0)
            {
                oskariLayer.setMetadataId(meta.get(0).getUrl().toString());
            }
        }

        try {

            // Populating capabilities
            final fi.nls.oskari.wms.WMSCapabilities capabilities = new fi.nls.oskari.wms.WMSCapabilities();
            // gfi
            capabilities.setQueryable(capabilitiesLayer.isQueryable());
            // Keywords
            capabilities.setKeywords(capabilitiesLayer.getKeywords());
            capabilities.setVersion(caps.getVersion());

            // List<String> caps.getRequest().getGetFeatureInfo().getFormats()
            if(caps.getRequest() != null && caps.getRequest().getGetFeatureInfo() != null) {
                capabilities.setFormats(caps.getRequest().getGetFeatureInfo().getFormats());
            }

            // Styles
            final List<StyleImpl> styles = capabilitiesLayer.getStyles();
            if (styles != null) {
                for (Iterator ii = styles.iterator(); ii.hasNext(); ) {
                    StyleImpl style = (StyleImpl) ii.next();
                    WMSStyle ostyle = new WMSStyle();
                    ostyle.setTitle(style.getTitle().toString());
                    if(style.getLegendURLs().size() > 0) ostyle.setLegend((String) style.getLegendURLs().get(0));
                    ostyle.setName(style.getName());
                    capabilities.addStyle(ostyle);
                }
            }

            JSONObject json = FORMATTER.getJSON(oskariLayer, PropertyUtil.getDefaultLanguage(), false, capabilities);
            // add/modify admin specific fields
            OskariLayerWorker.modifyCommonFieldsForEditing(json, oskariLayer);
            // for admin ui only
            JSONHelper.putValue(json, "title", capabilitiesLayer.getTitle());

            // NOTE! Important to remove id since this is at template
            json.remove("id");
            // ---------------
            return json;
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
     * Get service metadata url
     * @param service  geotools WMS capabilities service
     * @return
     */
    static private String getMetaDataUrl(Service service) {
       if ( service.getOnlineResource() != null) return service.getOnlineResource().toString();
       return null;
    }
}
