package org.oskari.admin;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.util.ViewHelper;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesConstants;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilitiesHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wfs.WFSCapabilitiesService;
import fi.nls.oskari.wms.WMSCapabilitiesService;
import org.geotools.data.wfs.WFSDataStore;
import org.oskari.capabilities.CapabilitiesService;
import org.oskari.capabilities.LayerCapabilities;
import org.oskari.capabilities.ServiceConnectInfo;
import org.oskari.capabilities.ogc.api.OGCAPIFeaturesService;
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
                return WFSCapabilitiesService.getCapabilitiesResults(url, version, username, password, getSystemCRSs());
            case OskariLayer.TYPE_WMTS:
                return getCapabilitiesResultsWMTS(url, version, username, password, getSystemCRSs());
            default:
                throw new ServiceException("Couldn't determine operation based on parameters");
        }
    }

    public static ServiceCapabilitiesResult getCapabilitiesResultsWMTS (final String url, final String version,
                                                                 final String user, final String pw, final Set<String> systemCRSs)
            throws ServiceException {
        ServiceCapabilitiesResult results = new ServiceCapabilitiesResult();
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
        switch (ml.getType()) {
            case OskariLayer.TYPE_WFS:
                updateCapabilitiesWFS(ml);
                break;
            case OskariLayer.TYPE_WMS:
            case OskariLayer.TYPE_WMTS:
                CapabilitiesService.updateCapabilities(ml, getSystemCRSs());
                break;
        }
    }

    private static void updateCapabilitiesWFS(OskariLayer ml) throws ServiceException {
        if (CapabilitiesConstants.WFS3_VERSION.equals(ml.getVersion())) {
            try {
                Map<String, LayerCapabilities> caps = CapabilitiesService.getLayersFromService(ServiceConnectInfo.fromLayer(ml));
                ml.setCapabilities(CapabilitiesService.toJSON(caps.get(ml.getName()), getSystemCRSs()));
                ml.setCapabilitiesLastUpdated(new Date());
            } catch (IOException e) {
                throw new ServiceException("Error getting capabilities from " + ml.getUrl(), e);
            }
        } else {
            WFSDataStore wfs = WFSCapabilitiesService.getDataStore(ml);
            OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWFS(wfs, ml, getSystemCRSs());
        }
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
