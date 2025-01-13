package org.oskari.admin;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.util.ViewHelper;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.ServiceConnectInfo;
import org.oskari.capabilities.ogc.LayerCapabilitiesOGC;
import org.oskari.capabilities.ogc.LayerCapabilitiesWMS;
import org.oskari.maplayer.admin.LayerAdminJSONHelper;
import org.oskari.maplayer.model.MapLayerStructure;
import org.oskari.maplayer.model.ServiceCapabilitiesResult;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LayerCapabilitiesHelper {

    private static final List<String> OWS_SERVICES = Arrays.asList("ows", "wms", "wmts", "wfs");

    // Hiding ugliness
    public static ServiceCapabilitiesResult getCapabilitiesResults(String url, String type, String version, String username, String password, String currentSRS) throws ServiceException {

        ServiceConnectInfo info = new ServiceConnectInfo(url, type, version);
        info.setCredentials(username, password);
        Map<String, LayerCapabilities> caps;
        try {
            caps = CapabilitiesService.getLayersFromService(info);
        } catch (IOException e) {
            throw new ServiceException ("Failed to get capabilities version: " + version + " from url: " + url, e);
        }

        Set<String> supportedSRS = getSystemCRSs();
        List<OskariLayer> layers = caps.values().stream()
                .map(layer -> toOskariLayer(layer, url, username, password, supportedSRS))
                .filter(l -> l != null)
                .collect(Collectors.toList());


        ServiceCapabilitiesResult results = new ServiceCapabilitiesResult();
        results.setTitle("N/A");
        if (!layers.isEmpty()) {
            results.setVersion(layers.get(0).getVersion());
            results.setLayers(layers.stream()
                    .map(l -> LayerAdminJSONHelper.toJSON(l))
                    .collect(Collectors.toList()));
        }

        // WMS specific structure handling
        if (OskariLayer.TYPE_WMS.equals(type)) {
            Collection<LayerCapabilitiesWMS> list = caps.values().stream().map(l -> {
                        if (!(l instanceof LayerCapabilitiesWMS)) {
                            return null;
                        }
                        return (LayerCapabilitiesWMS) l;
                    })
                    .filter(l -> l != null)
                    .collect(Collectors.toList());
            results.setStructure(parseStructureJson(list, new ArrayList<>()));
        }

        results.setCurrentSrs(currentSRS);
        results.setExistingLayers(getExistingLayers(url, type));

        return results;
    }

    private static OskariLayer toOskariLayer(LayerCapabilities layer, String url, String user, String pw, Set<String> systemCRSs) {
        final OskariLayer ml = new OskariLayer();
        ml.setType(layer.getType());
        ml.setUrl(url);
        ml.setPassword(pw);
        ml.setUsername(user);
        ml.setName(layer.getName());
        if (layer instanceof LayerCapabilitiesOGC) {
            LayerCapabilitiesOGC ogcCaps = (LayerCapabilitiesOGC) layer;
            ml.setVersion(ogcCaps.getVersion());
        }
        if (layer instanceof LayerCapabilitiesWMS) {
            LayerCapabilitiesWMS wmsCaps = (LayerCapabilitiesWMS) layer;
            // Check what this comment means from previous impl: "THIS IS ON PURPOSE: min -> max, max -> min"
            ml.setMaxScale(wmsCaps.getMaxScale());
            ml.setMinScale(wmsCaps.getMinScale());
        }
        // default style for wms + wmts mostly
        ml.setStyle(layer.getDefaultStyle());

        String title = layer.getTitle();
        if (title == null || title.isEmpty()) {
            title = layer.getName();
        }
        for (String lang : PropertyUtil.getSupportedLanguages()) {
            ml.setName(lang, title);
        }
        ml.setCapabilities(CapabilitiesService.toJSON(layer, systemCRSs));
        ml.setCapabilitiesLastUpdated(new Date());
        return ml;
    }

    private static List<MapLayerStructure> parseStructureJson(Collection<LayerCapabilitiesWMS> caps, List<String> seen) {
        List<MapLayerStructure> layers = caps.stream()
                .map(l -> {
                    if (!(l instanceof LayerCapabilitiesWMS)) {
                        return null;
                    }

                    if (seen.contains(l.getName())) {
                        return null;
                    }

                    MapLayerStructure cap = new MapLayerStructure();
                    cap.setName(l.getName());
                    seen.add(l.getName());
                    List<LayerCapabilitiesWMS> sublayers = l.getLayers();
                    cap.setStructure(parseStructureJson(sublayers, seen));
                    return cap;
                })
                .filter(l -> l != null)
                .collect(Collectors.toList());
        return layers;
    }

    private static Map<String, List<Integer>> getExistingLayers(String url, String type) {
        String existingUrl = removeOWSServiceFromUrl(url);
        return getOskariLayerService().findNamesAndIdsByUrl(existingUrl, type);
    }

    private static String removeOWSServiceFromUrl(String url) {
        for (String ows : OWS_SERVICES) {
            if (url.toLowerCase().endsWith(ows)) {
                return url.substring(0, url.length() - ows.length());
            }
        }
        return url;
    }

    public static void updateCapabilities(OskariLayer ml) throws ServiceException {
        CapabilitiesService.updateCapabilities(ml, getSystemCRSs());
    }

    public static Set<String> getSystemCRSs() throws ServiceException {
        return ViewHelper.getSystemCRSs(getViewService());
    }

    private static ViewService getViewService() {
        return OskariComponentManager.getComponentOfType(ViewService.class);
    }

    private static OskariLayerService getOskariLayerService() {
        return OskariComponentManager.getComponentOfType(OskariLayerService.class);
    }
}
