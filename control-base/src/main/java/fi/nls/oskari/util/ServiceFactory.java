package fi.nls.oskari.util;


import fi.mml.map.mapwindow.service.db.InspireThemeService;
import fi.mml.map.mapwindow.service.db.InspireThemeServiceIbatisImpl;
import fi.mml.map.mapwindow.service.db.LayerClassService;
import fi.mml.map.mapwindow.service.db.LayerClassServiceIbatisImpl;
import fi.mml.map.mapwindow.service.db.MapConfigurationLayersService;
import fi.mml.map.mapwindow.service.db.MapConfigurationLayersServiceIbatisImpl;
import fi.mml.map.mapwindow.service.db.MapConfigurationService;
import fi.mml.map.mapwindow.service.db.MapConfigurationServiceIbatisImpl;
import fi.mml.map.mapwindow.service.db.MapLayerService;
import fi.mml.map.mapwindow.service.db.MapLayerServiceIbatisImpl;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.mml.portti.service.search.SearchService;
import fi.mml.portti.service.search.SearchServiceImpl;

public class ServiceFactory {
	
	private static LayerClassService layerClassService;
	private static MapLayerService mapLayerService;
	private static MapConfigurationService mapConfigurationService;
	private static MapConfigurationLayersService mapConfigurationLayersService;
	private static InspireThemeService inspireThemeService;
    private static PermissionsService permissionsService;
    private static SearchService searchService;
 
	public static LayerClassService getLayerClassService() {
		if (layerClassService == null) {
			layerClassService = new LayerClassServiceIbatisImpl();
		}
		return layerClassService;
	}
	
	public static MapLayerService getMapLayerService() {
		if (mapLayerService == null) {
			mapLayerService = new MapLayerServiceIbatisImpl();
		}
		return mapLayerService;
	}
	
	public static MapConfigurationService getMapConfigurationService() {
		if (mapConfigurationService == null) {
			mapConfigurationService = new MapConfigurationServiceIbatisImpl();
		}
		return mapConfigurationService;
	}
	
	public static MapConfigurationLayersService getMapConfigurationLayersService() {
		if (mapConfigurationLayersService == null) {
			mapConfigurationLayersService = new MapConfigurationLayersServiceIbatisImpl();
		}
		return mapConfigurationLayersService;
	}
	
	public static InspireThemeService getInspireThemeService() {
		if (inspireThemeService == null) {
			inspireThemeService = new InspireThemeServiceIbatisImpl();
		}
		return inspireThemeService;
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
    
	
}
