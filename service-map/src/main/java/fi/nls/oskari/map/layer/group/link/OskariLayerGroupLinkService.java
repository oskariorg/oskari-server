package fi.nls.oskari.map.layer.group.link;

import fi.nls.oskari.service.OskariComponent;

import java.util.List;

public abstract class OskariLayerGroupLinkService extends OskariComponent {

    public abstract List<OskariLayerGroupLink> findAll();

    public abstract List<OskariLayerGroupLink> findByLayerId(int layerId);
    public abstract List<OskariLayerGroupLink> findByGroupId(int groupId);

    public abstract void insert(OskariLayerGroupLink link);
    public abstract void insertAll(List<OskariLayerGroupLink> links);

    public abstract void deleteLink(int layerId, int groupId);
    public abstract void deleteLinksByLayerId(int layerId);
    public abstract void deleteLinksByGroupId(int groupId);

    public abstract void replace(OskariLayerGroupLink old, OskariLayerGroupLink link);
    public abstract boolean hasLinks(int groupId);

}
