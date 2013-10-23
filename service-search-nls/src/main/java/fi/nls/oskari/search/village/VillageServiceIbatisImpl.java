package fi.nls.oskari.search.village;

import fi.nls.oskari.service.db.BaseIbatisService;

public class VillageServiceIbatisImpl extends BaseIbatisService<Village> implements VillageService {
	
	@Override
	protected String getNameSpace() {
		return "Village";
	}
}
