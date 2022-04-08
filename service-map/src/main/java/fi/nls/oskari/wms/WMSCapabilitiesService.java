package fi.nls.oskari.wms;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.ServiceConnectInfo;
import org.oskari.capabilities.ogc.LayerCapabilitiesWMS;
import org.oskari.maplayer.admin.LayerAdminJSONHelper;
import org.oskari.maplayer.model.MapLayerStructure;
import org.oskari.maplayer.model.ServiceCapabilitiesResultWMS;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WMSCapabilitiesService {

    public ServiceCapabilitiesResultWMS getCapabilitiesResults(final String url, final String version, final String user, final String pwd,
                                                               final Set<String> systemCRSs) throws ServiceException {
        try {
            ServiceConnectInfo info = new ServiceConnectInfo(url, OskariLayer.TYPE_WMS, version);
            info.setCredentials(user, pwd);
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
        Collection<LayerCapabilitiesWMS> list = caps.values().stream().map(l -> {
                    if (!(l instanceof LayerCapabilitiesWMS)) {
                        return null;
                    }
                    return (LayerCapabilitiesWMS) l;
                })
                .filter(l -> l != null)
                .collect(Collectors.toList());
        results.setStructure(parseStructureJson(list));

        return results;
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

    private static List<MapLayerStructure> parseStructureJson(Collection<LayerCapabilitiesWMS> caps) {
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

}
