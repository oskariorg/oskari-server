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
    public abstract void updateLayerGroups(final long maplayerId, final Collection<MaplayerGroup> groups);
    public abstract List<Integer> findMaplayersByGroup(int id);
    public abstract MaplayerGroup findByName(final String name);
    public abstract List<MaplayerGroup> findByParentId(final int groupId);
    public abstract void updateOrder(MaplayerGroup group);
    public abstract void updateGroupParent(final int groupId, final int newParentId);
	public abstract void flushCache();
}
