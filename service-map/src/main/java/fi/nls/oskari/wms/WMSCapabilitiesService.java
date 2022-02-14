package fi.nls.oskari.wms;

import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilitiesHelper;

import fi.nls.oskari.util.PropertyUtil;
import org.geotools.ows.wms.Layer;
import org.geotools.ows.wms.WMSCapabilities;
import org.geotools.ows.wms.xml.WMSSchema;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.handlers.DocumentHandler;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.ServiceConnectInfo;
import org.oskari.capabilities.ogc.LayerCapabilitiesWMS;
import org.oskari.maplayer.admin.LayerAdminJSONHelper;
import org.oskari.maplayer.model.MapLayerStructure;
import org.oskari.maplayer.model.ServiceCapabilitiesResultWMS;
import org.oskari.service.util.ServiceFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WMSCapabilitiesService {
    private static final Logger log = LogFactory.getLogger(WMSCapabilitiesService.class);


    public ServiceCapabilitiesResultWMS getCapabilitiesResults (final String url, final String version, final String user, final String pwd,
                                                                final Set<String> systemCRSs) throws ServiceException {
        try {
            ServiceConnectInfo info = new ServiceConnectInfo(url, OskariLayer.TYPE_WMS, version);
            Map<String, LayerCapabilities> caps = CapabilitiesService.getLayersFromService(info);
            return parseCapabilitiesResults(caps, url, user, pwd, systemCRSs);
        } catch (Exception ex) {
            throw new ServiceException("Couldn't read/get wms capabilities response from url: " + url, ex);
        }
    }

    protected static ServiceCapabilitiesResultWMS parseCapabilitiesResults(Map<String, LayerCapabilities> caps, String url,
                                                                           String user, String pwd, Set<String> systemCRSs) {

        List<OskariLayer> layers = caps.values().stream()
                .map(layer -> layerToOskariLayer(layer, url, user, pwd, systemCRSs))
                .filter(l -> l != null)
                .collect(Collectors.toList());


        ServiceCapabilitiesResultWMS results = new ServiceCapabilitiesResultWMS();
        results.setTitle("N/A");
        if (!layers.isEmpty()) {
            results.setVersion(layers.get(0).getVersion());
            results.setLayers(layers.stream()
                    .map(l -> LayerAdminJSONHelper.toJSON(l))
                    .collect(Collectors.toList()));
        }
        // capabilitiesResult.setLayersWithErrors((List<String>) capabilities.get(CapabilitiesConstants.KEY_ERROR_LAYERS));
        Collection<LayerCapabilitiesWMS> list = caps.values().stream().map(l -> {
                    if (!(l instanceof LayerCapabilitiesWMS)) {
                        return null;
                    }
                    return (LayerCapabilitiesWMS)l;
                })
                .filter(l -> l != null)
                .collect(Collectors.toList());
        results.setStructure(parseStructureJson(list));

        return results;
    }
    private static List<MapLayerStructure> parseStructureJson (Collection<LayerCapabilitiesWMS> caps) {
        //List<MapLayerStructure> layers = new ArrayList<>();
        List<MapLayerStructure> layers = caps.stream()
                .map(l -> {
                    if (!(l instanceof LayerCapabilitiesWMS)) {
                        return null;
                    }
                    MapLayerStructure cap = new MapLayerStructure();
                    cap.setName(l.getName());
                    List<LayerCapabilitiesWMS> sublayers = l.getLayers();
                    cap.setStructure(parseStructureJson(sublayers));
                    return cap;
                })
                .filter(l -> l != null)
                .collect(Collectors.toList());
        return layers;
    }


    public static OskariLayer layerToOskariLayer(LayerCapabilities caps, String url, String user, String pw,
                                                 Set<String> systemCRSs) {
        if (!(caps instanceof LayerCapabilitiesWMS)) {
            return null;
        }
        final OskariLayer oskariLayer = new OskariLayer();
        oskariLayer.setType(OskariLayer.TYPE_WMS);
        oskariLayer.setUrl(url);
        oskariLayer.setName(caps.getName());
        oskariLayer.setUsername(user);
        oskariLayer.setPassword(pw);

        LayerCapabilitiesWMS wmsCaps = (LayerCapabilitiesWMS) caps;
        oskariLayer.setVersion(wmsCaps.getVersion());
        // Check what this comment means from previous impl: "THIS IS ON PURPOSE: min -> max, max -> min"
        oskariLayer.setMaxScale(wmsCaps.getMaxScale());
        oskariLayer.setMinScale(wmsCaps.getMinScale());

        // setup UI names for all supported languages
        final String[] languages = PropertyUtil.getSupportedLanguages();
        String title = wmsCaps.getTitle();
        for (String lang : languages) {
            oskariLayer.setName(lang, title);
        }
        oskariLayer.setCapabilities(CapabilitiesService.toJSON(caps, systemCRSs));
        oskariLayer.setStyle(wmsCaps.getDefaultStyle());
        return oskariLayer;
    }

    @Deprecated
    protected static ServiceCapabilitiesResultWMS parseCapabilitiesResults(String xml, String url, String version,
            String user, String pwd, Set<String> systemCRSs) {
        WMSCapabilities caps = createCapabilities(xml);

        List<OskariLayer> layers = getActualLayers(caps)
                .map(layer -> layerToOskariLayer(caps, layer, url, version, user, pwd, systemCRSs))
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

    @Deprecated
    protected static Stream<Layer> getActualLayers(WMSCapabilities caps) {
        return caps.getLayerList()
                .stream()
                .filter(WMSCapabilitiesService::isActualLayer);
    }

    @Deprecated
    private static boolean isActualLayer (Layer layer) {
        String layerName = layer.getName();
        return layerName != null && !layerName.isEmpty();
    }

    @Deprecated
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

    @Deprecated
    public static OskariLayer layerToOskariLayer(WMSCapabilities caps, Layer capabilitiesLayer, String url, String version, String user, String pw,
                                                 Set<String> systemCRSs) {
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

        try {
            OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMS(caps,capabilitiesLayer, oskariLayer, systemCRSs);
        } catch (Exception ex) {
            log.warn ("Couldn't parse capabilities for WMS layer:", layerName, "message:", ex.getMessage());
        }
        OskariLayerCapabilitiesHelper.setDefaultStyleFromCapabilitiesJSON(oskariLayer);
        return oskariLayer;
    }
    @Deprecated
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
