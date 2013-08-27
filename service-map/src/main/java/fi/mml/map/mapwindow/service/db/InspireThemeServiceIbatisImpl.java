package fi.mml.map.mapwindow.service.db;

import fi.nls.oskari.domain.map.InspireTheme;
import fi.nls.oskari.service.db.BaseIbatisService;

/**
 * InspireTheme implementation for Ibatis
 * 
 *
 */
public class InspireThemeServiceIbatisImpl extends BaseIbatisService<InspireTheme> implements InspireThemeService {

	@Override
	protected String getNameSpace() {
		return "InspireTheme";
	}
}
