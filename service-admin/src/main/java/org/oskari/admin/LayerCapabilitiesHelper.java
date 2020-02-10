package org.oskari.admin;

import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.util.ViewHelper;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesConstants;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilitiesHelper;
import fi.nls.oskari.wfs.WFSCapabilitiesService;
import fi.nls.oskari.wms.WMSCapabilitiesService;
import fi.nls.oskari.wmts.WMTSCapabilitiesService;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import org.geotools.data.wfs.WFSDataStore;
import org.oskari.maplayer.admin.LayerAdminJSONHelper;
import org.oskari.maplayer.model.ServiceCapabilitiesResult;
import org.oskari.maplayer.model.ServiceCapabilitiesResultWMS;
import org.oskari.maplayer.model.ServiceCapabilitiesResultWMTS;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LayerCapabilitiesHelper {

    private static final List<String> OWS_SERVICES = Arrays.asList("ows", "wms", "wmts", "wfs");
    private static WMTSCapabilitiesService wmtsCapabilities = new WMTSCapabilitiesService();
    private static WMSCapabilitiesService wmsCapabilities = new WMSCapabilitiesService();

    // Hiding ugliness
    public static ServiceCapabilitiesResult getCapabilitiesResults(String url, String type, String version, String username, String password, String currentSRS) throws ServiceException {
        switch (type) {
            case OskariLayer.TYPE_WMS:
                return getWMSCapabilities(url, version, username, password, currentSRS);
            case OskariLayer.TYPE_WFS:
                return getWFSCapabilities(url, version, username, password, currentSRS);
            case OskariLayer.TYPE_WMTS:
                return getWMTSCapabilities(url, version, username, password, currentSRS);
            default:
                throw new ServiceException("Couldn't determine operation based on parameters");
        }
    }

    private static ServiceCapabilitiesResult getWMSCapabilities(String url, String version, String username, String password, String currentSRS) throws ServiceException {
        ServiceCapabilitiesResultWMS capabilitiesResult = wmsCapabilities.getCapabilitiesResults(url, version, username, password, getSystemCRSs());
        capabilitiesResult.setCurrentSrs(currentSRS);
        String existingUrl = removeOWSServiceFromUrl(url);
        capabilitiesResult.setExistingLayers(getOskariLayerService().findNamesAndIdsByUrl(existingUrl, OskariLayer.TYPE_WMS));
        return capabilitiesResult;
    }

    private static ServiceCapabilitiesResult getWFSCapabilities(String url, String version, String username, String password, String currentSRS) throws ServiceException {
        ServiceCapabilitiesResult capabilitiesResult = new ServiceCapabilitiesResult();
        Map<String, Object> capabilities = WFSCapabilitiesService.getCapabilitiesResults(url, version, username, password, getSystemCRSs());
        setCommonFields(url, OskariLayer.TYPE_WFS, capabilitiesResult, capabilities, currentSRS);
        return capabilitiesResult;
    }

    private static ServiceCapabilitiesResult getWMTSCapabilities(String url, String version, String username, String password, String currentSRS) throws ServiceException {
        ServiceCapabilitiesResultWMTS capabilitiesResult = new ServiceCapabilitiesResultWMTS();
        Map<String, Object> capabilities = wmtsCapabilities.getCapabilitiesResults(url, version, username, password, currentSRS, getSystemCRSs());
        setCommonFields(url, OskariLayer.TYPE_WMTS, capabilitiesResult, capabilities, currentSRS);
        capabilitiesResult.setMatrixSets(capabilities.get(CapabilitiesConstants.KEY_WMTS_MATRIXSET));
        return capabilitiesResult;
    }

    private static void setCommonFields(String url, String type, ServiceCapabilitiesResult capabilitiesResult, Map<String, Object> capabilities, String currentSRS) {
        capabilitiesResult.setTitle((String) capabilities.getOrDefault(CapabilitiesConstants.KEY_TITLE, ""));
        capabilitiesResult.setVersion((String) capabilities.get(CapabilitiesConstants.KEY_VERSION));
        capabilitiesResult.setCurrentSrs(currentSRS);

        List<OskariLayer> layers = (List<OskariLayer>) capabilities.get(CapabilitiesConstants.KEY_LAYERS);
        capabilitiesResult.setLayers(layers.stream().map(l -> LayerAdminJSONHelper.toJSON(l)).collect(Collectors.toList()));

        String existingUrl = removeOWSServiceFromUrl(url);
        capabilitiesResult.setExistingLayers(getOskariLayerService().findNamesAndIdsByUrl(existingUrl, type));
        capabilitiesResult.setLayersWithErrors((List<String>) capabilities.get(CapabilitiesConstants.KEY_ERROR_LAYERS));
    }


    private static String removeOWSServiceFromUrl(String url) {
        for (String ows : OWS_SERVICES) {
            if (url.toLowerCase().endsWith(ows)) {
                return url.substring(0, url.length() - ows.length());
            }
        }
        return url;
    }

    // not used currently but this seems The place for this
    public static void updateCapabilities(OskariLayer ml) throws ServiceException {
        switch (ml.getType()) {
            case OskariLayer.TYPE_WFS:
                WFSDataStore wfs = WFSCapabilitiesService.getDataStore(ml);
                OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWFS(wfs, ml, getSystemCRSs());
                break;
            case OskariLayer.TYPE_WMS:
                WebMapService wms = wmsCapabilities.updateCapabilities(ml);
                OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMS(wms, ml, getSystemCRSs());
                break;
            case OskariLayer.TYPE_WMTS:
                WMTSCapabilities wmts = wmtsCapabilities.updateCapabilities(ml);
                OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMTS(wmts, ml, getSystemCRSs());
                break;
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
