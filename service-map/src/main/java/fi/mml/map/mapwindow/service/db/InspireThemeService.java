package fi.mml.map.mapwindow.service.db;

import fi.nls.oskari.domain.map.InspireTheme;
import fi.nls.oskari.service.db.OskariComponentIbatisService;

import java.util.Collection;
import java.util.List;

/**
 * Interface for InspireTheme service
 * 
 *
 */
public abstract class InspireThemeService extends OskariComponentIbatisService<InspireTheme> {
    public abstract List<InspireTheme> findByMaplayerId(final int layerId);
    public abstract void updateLayerThemes(final long maplayerId, final Collection<InspireTheme> themes);
    public abstract List<Integer> findMaplayersByTheme(int id);
    public abstract InspireTheme findByName(final String name);
}
