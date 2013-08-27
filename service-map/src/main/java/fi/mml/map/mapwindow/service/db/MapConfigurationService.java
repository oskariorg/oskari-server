package fi.mml.map.mapwindow.service.db;

import fi.nls.oskari.domain.map.wms.MapConfiguration;
import fi.nls.oskari.service.db.BaseService;

public interface MapConfigurationService extends BaseService<MapConfiguration>{

	public MapConfiguration findMapConfigurations(String mapPortletId);

}
