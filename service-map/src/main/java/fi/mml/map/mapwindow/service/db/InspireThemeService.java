package fi.mml.map.mapwindow.service.db;

import fi.nls.oskari.domain.map.InspireTheme;
import fi.nls.oskari.service.db.BaseService;

import java.util.List;

/**
 * Interface for InspireTheme service
 * 
 *
 */
public interface InspireThemeService extends BaseService<InspireTheme> {
    public List<InspireTheme> findByMaplayerId(final int layerId);
}
