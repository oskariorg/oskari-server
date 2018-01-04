package fi.mml.map.mapwindow.service.db;

import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.service.db.OskariComponentIbatisService;

import java.util.Collection;
import java.util.List;

/**
 * Interface for MaplayerGroup service
 * 
 *
 */
public abstract class OskariMapLayerGroupService extends OskariComponentIbatisService<MaplayerGroup> {
    public abstract List<MaplayerGroup> findByMaplayerId(final int layerId);
    public abstract void updateLayerThemes(final long maplayerId, final Collection<MaplayerGroup> themes);
    public abstract List<Integer> findMaplayersByGroup(int id);
    public abstract MaplayerGroup findByName(final String name);
}
