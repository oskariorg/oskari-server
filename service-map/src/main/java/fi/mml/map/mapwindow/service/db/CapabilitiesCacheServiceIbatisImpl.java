package fi.mml.map.mapwindow.service.db;

import fi.nls.oskari.domain.map.CapabilitiesCache;
import fi.nls.oskari.service.db.BaseIbatisService;

public class CapabilitiesCacheServiceIbatisImpl  extends BaseIbatisService<CapabilitiesCache> implements CapabilitiesCacheService {

	@Override
	protected String getNameSpace() {
		
		return "CapabilitiesCache";
	}

}
