package fi.mml.map.mapwindow.service.db;

import java.util.List;

import fi.nls.oskari.domain.map.wms.MapConfigurationLayer;
import fi.nls.oskari.service.db.BaseIbatisService;

public class MapConfigurationLayersServiceIbatisImpl extends BaseIbatisService<MapConfigurationLayer> implements MapConfigurationLayersService{

	@Override
	protected String getNameSpace() {
		return "MapConfigurationLayer";
	}
	
	@Override
	public List<MapConfigurationLayer> findSelectedLayers(int map_conf_id) {
		return queryForList(getNameSpace() + ".findSelectedLayers", map_conf_id);
	}
	
}
