package fi.nls.oskari.wms;

import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceFactoryHelper;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatter;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilitiesHelper;

import fi.nls.oskari.util.PropertyUtil;
import org.geotools.ows.wms.Layer;
import org.geotools.data.ows.Service;
import org.geotools.ows.wms.WMSCapabilities;
import org.geotools.ows.wms.xml.MetadataURL;
import org.geotools.ows.wms.xml.WMSSchema;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.handlers.DocumentHandler;
import org.oskari.maplayer.admin.LayerAdminJSONHelper;
import org.oskari.maplayer.model.MapLayerStructure;
import org.oskari.maplayer.model.ServiceCapabilitiesResultWMS;
import org.oskari.service.util.ServiceFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public ServiceCapabilitiesResultWMS getCapabilitiesResults (final String url, final String version, final String user, final String pwd,
                                                                final Set<String> systemCRSs) throws ServiceException {
        try {
            OskariLayerCapabilities capabilities = capabilitiesService.getCapabilities(url, OskariLayer.TYPE_WMS, version, user, pwd);
            // if capabilities is more than 5 minutes old, get it from the service directly
            if (capabilities.isOlderThan(TimeUnit.MINUTES.toMillis(5))) {
                capabilities = capabilitiesService.getCapabilitiesFromService(url, OskariLayer.TYPE_WMS, version, user, pwd);
            }
            final String xml = capabilities.getData();
            ServiceCapabilitiesResultWMS results = parseCapabilitiesResults(xml, url, version, user, pwd, systemCRSs);
            if (capabilities.getId() == null) {
                capabilitiesService.save(capabilities);
            }
            return results;
        } catch (Exception ex) {
            throw new ServiceException("Couldn't read/get wms capabilities response from url: " + url, ex);
        }
    }

    protected static ServiceCapabilitiesResultWMS parseCapabilitiesResults(String xml, String url, String version,
            String user, String pwd, Set<String> systemCRSs) {
        WMSCapabilities caps = createCapabilities(xml);
        final String metadataUrl = getMetaDataUrl(caps.getService());

        List<OskariLayer> layers = getActualLayers(caps)
                .map(layer -> layerToOskariLayer(layer, url, version, user, pwd, metadataUrl, xml, systemCRSs))
                .collect(Collectors.toList());
        Layer capabilitiesLayer = caps.getLayer();

        ServiceCapabilitiesResultWMS results = new ServiceCapabilitiesResultWMS();
        results.setTitle(capabilitiesLayer.getTitle());
        results.setVersion(caps.getVersion());
        results.setLayers(layers.stream()
                .map(l -> LayerAdminJSONHelper.toJSON(l))
                .collect(Collectors.toList()));

        // capabilitiesResult.setLayersWithErrors((List<String>) capabilities.get(CapabilitiesConstants.KEY_ERROR_LAYERS));
        results.setStructure(parseStructureJson(capabilitiesLayer));

        return results;
    }

    protected static Stream<Layer> getActualLayers(WMSCapabilities caps) {
        return caps.getLayerList()
                .stream()
                .filter(WMSCapabilitiesService::isActualLayer);
    }

    private static boolean isActualLayer (Layer layer) {
        String layerName = layer.getName();
        return layerName != null && !layerName.isEmpty();
    }

    private static List<MapLayerStructure> parseStructureJson (Layer layer) {
        List<MapLayerStructure> layers = new ArrayList<>();
        List<Layer> sublayers = layer.getLayerChildren();
        boolean isLayer = isActualLayer(layer);
        if (sublayers == null || sublayers.isEmpty()) {
            if (!isLayer) {
                // no sublayers AND not an actual layer
                return layers;
            }
            // no sublayers: just return this layer
            MapLayerStructure cap = new MapLayerStructure();
            cap.setName(layer.getName());
            layers.add(cap);
            return layers;
        }
        // has sublayers
        if (!isLayer) {
            // just sublayers: skip the acu
            for (Layer sublayer : sublayers ) {
                layers.addAll(parseStructureJson(sublayer));
            }
            return layers;
        }
        // group layer
        MapLayerStructure cap = new MapLayerStructure();
        cap.setName(layer.getName());
        List<MapLayerStructure> sublayerOutput = new ArrayList<>();
        for (Layer sublayer : sublayers ) {
            sublayerOutput.addAll(parseStructureJson(sublayer));
        }
        cap.setStructure(sublayerOutput);
        layers.add(cap);
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
        oskariLayer.setMetadataId(LayerJSONFormatter.getFixedDataUrl(oskariLayer.getMetadataId()));

        try {
            // TODO: could we use (to get rid of capabilitiesXML):
            //WebMapService wmsImpl = WebMapServiceFactory.buildWebMapService(oskariLayer);
            WebMapService wmsImpl = WebMapServiceFactoryHelper.createFromXML(layerName, capabilitiesXML);
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
