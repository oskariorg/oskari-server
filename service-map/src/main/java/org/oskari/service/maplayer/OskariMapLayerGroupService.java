package org.oskari.service.maplayer;

import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.service.OskariComponent;

import java.util.List;

/**
 * Interface for MaplayerGroup service
 */
public abstract class OskariMapLayerGroupService extends OskariComponent {
    public abstract List<MaplayerGroup> findAll();
    public abstract MaplayerGroup find(final int id);
    public abstract MaplayerGroup findByName(final String name);

    public abstract List<MaplayerGroup> findByParentId(final int groupId);

    public abstract int insert(MaplayerGroup group);
    public abstract void update(MaplayerGroup group);
    public abstract void delete(MaplayerGroup group);

    public abstract void updateOrder(MaplayerGroup group);
    public abstract void updateGroupParent(final int groupId, final int newParentId);
    public abstract void flushCache();
}
