package fi.nls.oskari.wms;

import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceFactory;
import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMS;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.data.ows.*;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.xml.MetadataURL;
import org.geotools.data.wms.xml.WMSSchema;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.XMLSAXHandler;
import org.geotools.xml.handlers.DocumentHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Methods for parsing WMS capabilities data
 */
public class GetGtWMSCapabilities {

    private static final Logger log = LogFactory.getLogger(GetGtWMSCapabilities.class);
    private final static String KEY_GROUPS = "groups";
    private final static String KEY_LAYERS = "layers";
    private static final String KEY_LAYERS_WITH_REMARKS = "layersWithRemarks";

    private final static String GROUP_LAYER_TYPE = "grouplayer";
    private final static LayerJSONFormatterWMS FORMATTER = new LayerJSONFormatterWMS();

    private GetGtWMSCapabilities() {

    }

    // based on https://github.com/geotools/geotools/blob/master/modules/extension/wms/src/test/java/org/geotools/data/wms/test/WMS1_0_0_OnlineTest.java#L253-L276
    public static WMSCapabilities createCapabilities(String xml) {
        if(xml == null || xml.isEmpty()) {
            return null;
        }
        final Map hints = new HashMap();
        hints.put(DocumentHandler.DEFAULT_NAMESPACE_HINT_KEY, WMSSchema.getInstance());
        hints.put(DocumentFactory.VALIDATION_HINT, false);
        try(InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            final Object object = DocumentFactory.getInstance(stream, hints, Level.WARNING);
            if(object instanceof WMSCapabilities) {
                return (WMSCapabilities) object;
            }
        } catch (Exception ex) {
            log.error(ex, "Error reading WMS capabilities");
        }

        return null;
    }
    /**
     * Get all WMS layers data in JSON  ( layer tree groups->groups/layers->groups/layers...
     *
     * @param rurl WMS service url
     * @return
     * @throws ServiceException
     */
    public static JSONObject getWMSCapabilities(final CapabilitiesCacheService service,
            final String rurl, final String user, final String pwd,
            final String version, final String currentCrs) throws ServiceException {
        try {
            /*check url validity*/
            new URL(rurl);
            OskariLayerCapabilities capabilities = service.getCapabilities(rurl, OskariLayer.TYPE_WMS, user, pwd, version);
            String capabilitiesXML = capabilities.getData();
            if(capabilitiesXML == null || capabilitiesXML.trim().isEmpty()) {
                // retry from service - might get empty xml from db
                capabilities = service.getCapabilities(rurl, OskariLayer.TYPE_WMS, user, pwd, version, true);
                capabilitiesXML = capabilities.getData();
            }
            WMSCapabilities caps = createCapabilities(capabilitiesXML);
            // caps to json
            return parseLayer(caps.getLayer(), rurl, caps, capabilitiesXML, currentCrs, false);
        } catch (Exception ex) {
            throw new ServiceException("Couldn't read/get wms capabilities response from url.", ex);
        }
    }

    /**
     * Convenience method to filter a layer with given name from the capabilities
     * @param caps      capabilities
     * @param layerName layer to find
     * @return
     */
    public static Layer findLayer(final WMSCapabilities caps, final String layerName) {
        if(caps == null || layerName == null) {
            return null;
        }

        return findLayer(caps.getLayer(), layerName);
    }

    /**
     * Convenience method to filter a layer with given name from the capabilities
     * @param root      root layer for current iteration
     * @param layerName layer to find
     * @return null if not found or the layer object if found
     */
    private static Layer findLayer(final Layer root, final String layerName) {

        if(root == null) {
            return null;
        }
        if(layerName.equalsIgnoreCase(root.getName())) {
            return root;
        }

        for (Iterator ii = root.getLayerChildren().iterator(); ii.hasNext(); ) {
            Layer found = findLayer((Layer) ii.next(), layerName);
            if(found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Parse layer (group- or wmslayer)
     *
     * @param layer geotools layer
     * @param rurl  WMS service url
     * @param caps  WMS capabilities
     * @param capabilitiesXML The original capabilites XML
     *
     * @throws ServiceException
     */
    public static JSONObject parseLayer(Layer layer, String rurl, WMSCapabilities caps, String capabilitiesXML, String currentCrs, boolean recursiveCall)
            throws ServiceException {
        if (layer == null) {
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
                if (layer.getName() != null && !layer.getName().isEmpty()) {
                    // add self to layers if we have a wmsName so
                    // the group node layers are selectable as well on frontend
                    final JSONObject self = layerToOskariLayerJson(layer, rurl, caps, capabilitiesXML, currentCrs);
                    JSONHelper.putValue(groupNode, "self", self);
                }
                // Loop children
                for (Iterator ii = layer.getLayerChildren().iterator(); ii.hasNext(); ) {
                    Layer sublayer = (Layer) ii.next();
                    if (sublayer != null) {
                        final JSONObject child = parseLayer(sublayer, rurl, caps, capabilitiesXML, currentCrs, true);
                        final String type = child.optString("type");
                        if (GROUP_LAYER_TYPE.equals(type)) {
                            groups.put(child);
                        } else if (OskariLayer.TYPE_WMS.equals(type)) {
                            layers.put(child);
                            // Simple remark check
                            if(child.has("title")  && JSONHelper.getStringFromJSON(child,"title","").indexOf("*") > -1){
                                groupNode.put(KEY_LAYERS_WITH_REMARKS, "true");
                            }
                        }
                    }
                }
                return groupNode;
            } else {
                // Parse layer to JSON
                //handle a single layer or a sublayer (=recursive call)
                if (recursiveCall) {
                    return layerToOskariLayerJson(layer, rurl, caps, capabilitiesXML, currentCrs);
                } else {
                    //handle the case where there actually is just one layer
                    final JSONObject node = new JSONObject();
                    JSONArray layers = new JSONArray();
                    node.put(KEY_LAYERS, layers);
                    layers.put(layerToOskariLayerJson(layer, rurl, caps, capabilitiesXML, currentCrs));
                    return node;
                }
            }
        } catch (Exception ex) {
            throw new ServiceException("Couldn't parse wms capabilities layer", ex);
        }
    }

    /**
     * WMS layer data to json
     *
     * @param capabilitiesLayer geotools layer
     * @param rurl              Wms service url
     * @param caps              wms capabilities
     * @return
     * @throws ServiceException
     */
    public static JSONObject layerToOskariLayerJson(Layer capabilitiesLayer, String rurl, WMSCapabilities caps, String capabilitiesXML,
                                                    String currentCrs)
            throws ServiceException {

        final OskariLayer oskariLayer = new OskariLayer();
        oskariLayer.setType(OskariLayer.TYPE_WMS);
        oskariLayer.setUrl(rurl);
        // THIS IS ON PURPOSE: min -> max, max -> min
        oskariLayer.setMaxScale(capabilitiesLayer.getScaleDenominatorMin());
        oskariLayer.setMinScale(capabilitiesLayer.getScaleDenominatorMax());
        oskariLayer.setName(capabilitiesLayer.getName());
        oskariLayer.setVersion(caps.getVersion());

        // setup UI names for all supported languages
        final String[] languages = PropertyUtil.getSupportedLanguages();
        for (String lang : languages) {
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
            if (meta.size() > 0 && meta.get(0).getUrl() != null) {
                oskariLayer.setMetadataId(meta.get(0).getUrl().toString());
            }
        }

        try {
            // setup capabilities json for layer, styles etc
            JSONObject oskariLayerCapabilities = getLayerCapabilitiesAsJson(capabilitiesLayer, caps);
            //using an implementation of our own instead...
            WebMapService wmsImpl = WebMapServiceFactory.createFromXML(oskariLayer.getName(), capabilitiesXML);
            Map<String, String> supportedStyles = wmsImpl.getSupportedStyles();
            Iterator it = supportedStyles.entrySet().iterator();
            JSONArray styles = new JSONArray();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry)it.next();
                String name = entry.getKey();
                String title = entry.getValue();
                String styleLegend = wmsImpl.getLegendForStyle(name);
                JSONObject styleJSON = FORMATTER.createStylesJSON(name, title, styleLegend);
                styles.put(styleJSON);
            }
            oskariLayerCapabilities.put("styles", styles);
            oskariLayer.setCapabilities(oskariLayerCapabilities);

            JSONObject json = FORMATTER.getJSON(oskariLayer, PropertyUtil.getDefaultLanguage(), false, currentCrs);

            // add/modify admin specific fields
            OskariLayerWorker.modifyCommonFieldsForEditing(json, oskariLayer);

            // Add *, if current map epsg is not supported in the service capabilities for this layer
            String remark = LayerJSONFormatterWMS.getCRSs(wmsImpl).contains(currentCrs) ? "" : " *";

            // for admin ui only
            JSONHelper.putValue(json, "title", capabilitiesLayer.getTitle() + remark);


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
     * Returns formats for GFI request if available. Empty list if parsing fails.
     * @param caps
     * @return list of formats
     */
    public static List<String> getInfoFormats(final WMSCapabilities caps) {
        if(caps == null || caps.getRequest() == null || caps.getRequest().getGetFeatureInfo() == null) {
            return Collections.emptyList();
        }
        return caps.getRequest().getGetFeatureInfo().getFormats();
    }

    /**
     * Layer capabilities to be saved for OskariLayer.setCapabilities()
     * @param layer
     * @return
     */
    public static JSONObject getLayerCapabilitiesAsJson(Layer layer, WMSCapabilities capabilities) {
        JSONObject caps = new JSONObject();
        if(layer == null) {
            return caps;
        }
        final JSONArray styles = getStylesFromCapabilities(layer);

        JSONHelper.putValue(caps, "styles", styles);
        JSONHelper.putValue(caps, "isQueryable", layer.isQueryable());
        JSONHelper.putValue(caps, "formats", FORMATTER.getFormatsJSON(getInfoFormats(capabilities)));
        return caps;
    }

    public static JSONArray getStylesFromCapabilities(Layer layer) {
        final JSONArray styles = new JSONArray();
        if(layer.getStyles() == null) {
            return styles;
        }

        for(StyleImpl style : layer.getStyles()) {
            String styleLegend = "";
            if(style.getLegendURLs() != null && !style.getLegendURLs().isEmpty()) {
                styleLegend = (String)style.getLegendURLs().get(0);
            }
            String title = style.getName();
            if(style.getTitle() != null) {
                title = style.getTitle().toString();
            }
            JSONObject styleJSON = FORMATTER.createStylesJSON(style.getName(), title, styleLegend);
            styles.put(styleJSON);
        }
        return styles;
    }

    /**
     * Get service metadata url
     *
     * @param service geotools WMS capabilities service
     * @return
     */
    static private String getMetaDataUrl(Service service) {
        if (service.getOnlineResource() != null) {
            return service.getOnlineResource().toString();
        }
        return null;
    }

}
