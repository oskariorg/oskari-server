package fi.nls.oskari.map.layer;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.OskariComponent;

import java.util.List;

public abstract class OskariLayerService extends OskariComponent {

    public abstract OskariLayer find(int id);
    public abstract OskariLayer find(final String idStr);
    public abstract List<OskariLayer> find(final List<String> idList);
    public abstract List<OskariLayer> findByIdList(final List<Integer> idList);
    public abstract List<OskariLayer> findAll();
    public abstract List<OskariLayer> findByUrlAndName(final String url, final String name);
    public abstract List<OskariLayer> findByMetadataId(String uuid);
    public abstract List<OskariLayer> findAllWithPositiveUpdateRateSec();
    public abstract List<OskariLayer> findAllByGroupId(final int groupId);
    public abstract int insert(final OskariLayer layer);
    public abstract void update(final OskariLayer layer);
    public abstract void delete(final int layerId);
    public abstract void updateOrder(final int layerId, final int groupId, final int orderNumber);
    public abstract void updateGroup(final int layerId, final int oldGroupId, final int newGroupId);
}
