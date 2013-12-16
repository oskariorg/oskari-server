package fi.mml.map.mapwindow.service.db;

import java.util.List;

import fi.nls.oskari.domain.map.wms.MapConfigurationLayer;
import fi.nls.oskari.service.db.BaseService;

@Deprecated
public interface MapConfigurationLayersService  extends BaseService<MapConfigurationLayer>{
	
	public List<MapConfigurationLayer> findSelectedLayers(int map_conf_id); 
	
}
