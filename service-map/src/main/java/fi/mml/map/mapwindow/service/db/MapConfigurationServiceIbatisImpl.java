package fi.mml.map.mapwindow.service.db;

import fi.nls.oskari.domain.map.wms.MapConfiguration;
import fi.nls.oskari.service.db.BaseIbatisService;

@Deprecated
public class MapConfigurationServiceIbatisImpl extends BaseIbatisService<MapConfiguration> implements MapConfigurationService {

	@Override
	protected String getNameSpace() {
		return "MapConfiguration";
	}

	@Override
	public MapConfiguration findMapConfigurations(String mapPortletId) {
		return queryForObject(".MapConfigurationLayer", mapPortletId);
	}
	
	
	
}
