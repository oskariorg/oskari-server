package org.oskari.service.util;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupServiceIbatisImpl;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.portti.service.search.SearchService;
import fi.mml.portti.service.search.SearchServiceImpl;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.DataProviderServiceMybatisImpl;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkService;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkServiceMybatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheServiceMybatisImpl;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.PermissionServiceMybatisImpl;

public class ServiceFactory {

    private static DataProviderService dataProviderService;
    private static OskariLayerService mapLayerService;
    private static ViewService viewService;
    private static OskariMapLayerGroupService oskariMapLayerGroupService;
    private static OskariLayerGroupLinkService layerGroupLinkService;
    private static PermissionService permissionsService;
    private static SearchService searchService;
    private static CapabilitiesCacheService capabilitiesCacheService;
    private static WFSLayerConfigurationService wfsLayerService;

    public static DataProviderService getDataProviderService() {
        if (dataProviderService == null) {
            dataProviderService = new DataProviderServiceMybatisImpl();
        }
        return dataProviderService;
    }

    public static OskariLayerService getMapLayerService() {
        if (mapLayerService == null) {
            mapLayerService = new OskariLayerServiceMybatisImpl();
        }
        return mapLayerService;
    }

    public static ViewService getViewService() {
        if (viewService == null) {
            viewService = new AppSetupServiceMybatisImpl();
        }
        return viewService;
    }

    public static OskariMapLayerGroupService getOskariMapLayerGroupService() {
        if (oskariMapLayerGroupService == null) {
            oskariMapLayerGroupService = new OskariMapLayerGroupServiceIbatisImpl();
        }
        return oskariMapLayerGroupService;
    }

    public static OskariLayerGroupLinkService getOskariLayerGroupLinkService() {
        if (layerGroupLinkService == null) {
            layerGroupLinkService = new OskariLayerGroupLinkServiceMybatisImpl();
        }
        return layerGroupLinkService;
    }

    public static PermissionService getPermissionsService() {
        if (permissionsService == null) {
            permissionsService = new PermissionServiceMybatisImpl();
        }
        return permissionsService;
    }

    public static SearchService getSearchService() {
        if (searchService == null) {
            searchService = new SearchServiceImpl();
        }
        return searchService;
    }

    public static CapabilitiesCacheService getCapabilitiesCacheService() {
        if (capabilitiesCacheService == null) {
            capabilitiesCacheService = new CapabilitiesCacheServiceMybatisImpl();
        }
        return capabilitiesCacheService;
    }

    public static WFSLayerConfigurationService getWfsLayerService() {
        if (wfsLayerService == null) {
            wfsLayerService = new WFSLayerConfigurationServiceIbatisImpl();
        }
        return wfsLayerService;
    }
}
