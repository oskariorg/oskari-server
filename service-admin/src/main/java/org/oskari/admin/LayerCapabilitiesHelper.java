package org.oskari.admin;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.util.ViewHelper;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wms.WMSCapabilitiesService;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.ServiceConnectInfo;
import org.oskari.capabilities.ogc.LayerCapabilitiesWFS;
import org.oskari.maplayer.admin.LayerAdminJSONHelper;
import org.oskari.maplayer.model.ServiceCapabilitiesResult;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LayerCapabilitiesHelper {

    private static final List<String> OWS_SERVICES = Arrays.asList("ows", "wms", "wmts", "wfs");
    private static WMSCapabilitiesService wmsCapabilities = new WMSCapabilitiesService();

    // Hiding ugliness
    public static ServiceCapabilitiesResult getCapabilitiesResults(String url, String type, String version, String username, String password, String currentSRS) throws ServiceException {
        ServiceCapabilitiesResult results = getCapabilitiesResultForType(url, type, version, username, password, currentSRS);
        results.setCurrentSrs(currentSRS);
        results.setExistingLayers(getExistingLayers(url, type));
        return results;
    }
    private static ServiceCapabilitiesResult getCapabilitiesResultForType(String url, String type, String version, String username, String password, String currentSRS) throws ServiceException {
        switch (type) {
            case OskariLayer.TYPE_WMS:
                return wmsCapabilities.getCapabilitiesResults(url, version, username, password, getSystemCRSs());
            case OskariLayer.TYPE_WFS:
                return getCapabilitiesResultsWFS(url, version, username, password, getSystemCRSs());
            case OskariLayer.TYPE_WMTS:
                return getCapabilitiesResultsWMTS(url, version, username, password, getSystemCRSs());
            default:
                throw new ServiceException("Couldn't determine operation based on parameters");
        }
    }
    public static ServiceCapabilitiesResult getCapabilitiesResultsWFS (final String url, final String version,
                                                                        final String user, final String pw, final Set<String> systemCRSs)
            throws ServiceException {
        try {
            ServiceConnectInfo info = new ServiceConnectInfo(url, OskariLayer.TYPE_WFS, version);
            info.setCredentials(user, pw);
            Map<String, LayerCapabilities> caps = CapabilitiesService.getLayersFromService(info);


            List<OskariLayer> layers = caps.values().stream()
                    .map(layer -> toOskariLayer((LayerCapabilitiesWFS) layer, url, user, pw, systemCRSs))
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

            return results;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException ("Failed to get capabilities version: " + version + " from url: " + url, e);
        }

    }

    private static OskariLayer toOskariLayer(LayerCapabilitiesWFS layer, String url, String user, String pw, Set<String> systemCRSs) {
        final OskariLayer ml = new OskariLayer();
        ml.setType(OskariLayer.TYPE_WFS);
        ml.setUrl(url);
        ml.setMaxScale(1d);
        ml.setMinScale(1500000d);
        ml.setName(layer.getName());
        ml.setVersion(layer.getVersion());
        ml.setPassword(pw);
        ml.setUsername(user);
        String title = layer.getTitle();
        if (title == null || title.isEmpty()) {
            title = layer.getName();
        }
        for (String lang : PropertyUtil.getSupportedLanguages()) {
            ml.setName(lang, title);
        }
        ml.setCapabilities(CapabilitiesService.toJSON(layer, systemCRSs));
        return ml;
    }

    public static ServiceCapabilitiesResult getCapabilitiesResultsWMTS (final String url, final String version,
                                                                 final String user, final String pw, final Set<String> systemCRSs)
            throws ServiceException {
        ServiceCapabilitiesResult results = new ServiceCapabilitiesResult();
        results.setTitle("N/A");
        results.setVersion(version);

        ServiceConnectInfo info = new ServiceConnectInfo(url, OskariLayer.TYPE_WMTS, version);
        info.setCredentials(user, pw);
        try {
            Map<String, LayerCapabilities> capabilitiesMap = CapabilitiesService.getLayersFromService(info);

            List<OskariLayer> layers = new ArrayList<>();
            final String[] languages = PropertyUtil.getSupportedLanguages();
            for (LayerCapabilities cap : capabilitiesMap.values()) {
                OskariLayer layer = new OskariLayer();
                layer.setType(OskariLayer.TYPE_WMTS);
                layer.setName(cap.getName());
                layer.setUrl(url);
                layer.setVersion(version);
                layer.setUsername(user);
                layer.setPassword(pw);

                for (String lang : languages) {
                    layer.setName(lang, cap.getTitle());
                }
                layer.setStyle(cap.getDefaultStyle());

                layer.setCapabilities(CapabilitiesService.toJSON(cap, systemCRSs));
                layers.add(layer);
            }
            results.setLayers(layers.stream()
                    .map(l -> LayerAdminJSONHelper.toJSON(l))
                    .collect(Collectors.toList()));

        } catch (IOException e) {
            throw new ServiceException("Error loading capabilities", e);
        } catch (ServiceException e) {
            // TODO: error handling?
            throw e;
        }

        return results;
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

    private static Set<String> getSystemCRSs() throws ServiceException {
        return ViewHelper.getSystemCRSs(getViewService());
    }

    private static ViewService getViewService() {
        return OskariComponentManager.getComponentOfType(ViewService.class);
    }

    private static OskariLayerService getOskariLayerService() {
        return OskariComponentManager.getComponentOfType(OskariLayerService.class);
    }
}
