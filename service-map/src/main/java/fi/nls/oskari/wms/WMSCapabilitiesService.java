package fi.nls.oskari.wms;

import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceFactory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilitiesHelper;

import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.Service;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.xml.MetadataURL;
import org.geotools.data.wms.xml.WMSSchema;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.handlers.DocumentHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.service.util.ServiceFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.*;

public class WMSCapabilitiesService {
    private static final Logger log = LogFactory.getLogger(WMSCapabilitiesService.class);
    private CapabilitiesCacheService capabilitiesService = ServiceFactory.getCapabilitiesCacheService();

    public WebMapService updateCapabilities (OskariLayer ml) throws ServiceException {
        String data =  CapabilitiesCacheService.getFromService(ml);
        WebMapService wms;
        try {
            wms = OskariLayerCapabilitiesHelper.parseWMSCapabilities(data, ml);
        } catch (Exception e) {
            throw new ServiceException("Failed to parse WMS capabilities xml", e);
        }
        //update after parsing to cache valid xml
        capabilitiesService.save(ml, data);

        return wms;
    }
    public Map<String, Object> getCapabilitiesResults (final String url, final String version, final String user, final String pwd,
                                                              final Set<String> systemCRSs) throws ServiceException {
        try {
            final Map<String, Object> result = new HashMap<>();
            final OskariLayerCapabilities capabilities = capabilitiesService.getCapabilities(url, OskariLayer.TYPE_WMS, version, user, pwd);
            final String xml = capabilities.getData();
            WMSCapabilities caps = createCapabilities(xml);
            final String metadataUrl = getMetaDataUrl(caps.getService());

            List <OskariLayer> layers = caps.getLayerList()
                    .stream()
                    .filter(WMSCapabilitiesService::isActualLayer)
                    .map(layer -> layerToOskariLayer(layer, url, version, user, pwd, metadataUrl, xml, systemCRSs))
                    .collect(Collectors.toList());
            Layer capabilitiesLayer = caps.getLayer();
            result.put(KEY_TITLE, capabilitiesLayer.getTitle());
            result.put(KEY_VERSION, caps.getVersion());
            result.put(KEY_LAYERS, layers);
            result.put(KEY_WMS_STRUCTURE, parseStructureJson(capabilitiesLayer));

            if (capabilities.getId() == null) {
                capabilitiesService.save(capabilities);
            }
            return result;
        } catch (Exception ex) {
            throw new ServiceException("Couldn't read/get wms capabilities response from url: " + url, ex);
        }

    }
    private static boolean isActualLayer (Layer layer) {
        String layerName = layer.getName();
        return layerName != null && !layerName.isEmpty();
    }

    private static JSONArray parseStructureJson (Layer layer) {
        JSONArray layers = new JSONArray();
        List<Layer> sublayers = layer.getLayerChildren();
        boolean isLayer = isActualLayer(layer);
        if (sublayers != null && !sublayers.isEmpty()) {
            if (isLayer) { // group layer
                JSONObject groupLayer = new JSONObject();
                JSONHelper.putValue(groupLayer, KEY_LAYER_NAME, layer.getName());
                JSONArray childs = new JSONArray();
                for (Layer sublayer : sublayers ) {
                    JSONHelper.putAll(childs, parseStructureJson(sublayer));
                }
                JSONHelper.putValue(groupLayer, KEY_WMS_STRUCTURE, childs);
                layers.put(groupLayer);
            } else {
                for (Layer sublayer : sublayers ) {
                    JSONHelper.putAll(layers, parseStructureJson(sublayer));
                }
            }
        } else if(isLayer) {
            layers.put(layer.getName());
        }
        return layers;
    }
    public static OskariLayer layerToOskariLayer(Layer capabilitiesLayer, String url, String version, String user, String pw,
                                                 String metadataUrl, String capabilitiesXML, Set<String> systemCRSs) {

        final OskariLayer oskariLayer = new OskariLayer();
        final String layerName = capabilitiesLayer.getName();
        oskariLayer.setType(OskariLayer.TYPE_WMS);
        oskariLayer.setUrl(url);
        // THIS IS ON PURPOSE: min -> max, max -> min
        oskariLayer.setMaxScale(capabilitiesLayer.getScaleDenominatorMin());
        oskariLayer.setMinScale(capabilitiesLayer.getScaleDenominatorMax());
        oskariLayer.setName(layerName);
        oskariLayer.setVersion(version);
        oskariLayer.setUsername(user);
        oskariLayer.setPassword(pw);

        // setup UI names for all supported languages
        final String[] languages = PropertyUtil.getSupportedLanguages();
        String title = capabilitiesLayer.getTitle();
        for (String lang : languages) {
            oskariLayer.setName(lang, title);
        }

        // JSON formatter will parse uuid from url
        /*
        OnlineResource xlink:type="simple" xlink:href="http://www.paikkatietohakemisto.fi/geonetwork/srv/en/main.home?uuid=a22ec97f-d418-4957-9b9d-e8b4d2ec3eac"/>
        <inspire_common:MetadataUrl xsi:type="inspire_common:resourceLocatorType"><inspire_common:URL>http://www.paikkatietohakemisto.fi/geonetwork/srv/fi/iso19139.xml?uuid=a22ec97f-d418-4957-9b9d-e8b4d2ec3eac</inspire_common:URL>
        */
        oskariLayer.setMetadataId(metadataUrl);
        final List<MetadataURL> meta = capabilitiesLayer.getMetadataURL();
        if (meta != null) {
            if (meta.size() > 0 && meta.get(0).getUrl() != null) {
                oskariLayer.setMetadataId(meta.get(0).getUrl().toString());
            }
        }

        try {
            // TODO: could we use (to get rid of capabilitiesXML):
            //WebMapService wmsImpl = WebMapServiceFactory.buildWebMapService(oskariLayer);
            WebMapService wmsImpl = WebMapServiceFactory.createFromXML(layerName, capabilitiesXML);
            OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMS(wmsImpl, oskariLayer, systemCRSs);
        } catch (Exception ex) {
            log.warn ("Couldn't parse capabilities for WMS layer:", layerName, "message:", ex.getMessage());
        }
        return oskariLayer;
    }
    static private String getMetaDataUrl(Service service) {
        if (service.getOnlineResource() != null) {
            return service.getOnlineResource().toString();
        }
        return null;
    }
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
}
