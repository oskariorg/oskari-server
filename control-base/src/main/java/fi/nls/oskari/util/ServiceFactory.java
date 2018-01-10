package fi.nls.oskari.util;


import fi.mml.map.mapwindow.service.db.*;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupServiceIbatisImpl;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.mml.portti.service.search.SearchService;
import fi.mml.portti.service.search.SearchServiceImpl;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.DataProviderServiceIbatisImpl;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheServiceMybatisImpl;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;

public class ServiceFactory {
	
	private static DataProviderService dataProviderService;
	private static OskariLayerService mapLayerService;
	private static OskariMapLayerGroupService oskariMapLayerGroupService;
    private static MaplayerProjectionService maplayerProjectionService;
    private static PermissionsService permissionsService;
    private static SearchService searchService;
    private static CapabilitiesCacheService capabilitiesCacheService;
    private static WFSLayerConfigurationService wfsLayerService;
 
	public static DataProviderService getDataProviderService() {
		if (dataProviderService == null) {
            dataProviderService = new DataProviderServiceIbatisImpl();
		}
		return dataProviderService;
	}
	
	public static OskariLayerService getMapLayerService() {
		if (mapLayerService == null) {
			mapLayerService = new OskariLayerServiceIbatisImpl();
		}
		return mapLayerService;
	}

	public static OskariMapLayerGroupService getOskariMapLayerGroupService() {
		if (oskariMapLayerGroupService == null) {
			oskariMapLayerGroupService = new OskariMapLayerGroupServiceIbatisImpl();
		}
		return oskariMapLayerGroupService;
	}

    public static MaplayerProjectionService getMaplayerProjectionService() {
        if (maplayerProjectionService == null) {
            maplayerProjectionService = new MaplayerProjectionServiceIbatisImpl();
        }
        return maplayerProjectionService;
    }
    public static PermissionsService getPermissionsService() {
        if (permissionsService == null) {
            permissionsService = new PermissionsServiceIbatisImpl();
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
