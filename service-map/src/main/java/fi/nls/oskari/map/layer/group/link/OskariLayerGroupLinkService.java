package fi.nls.oskari.map.layer.group.link;

import java.util.List;

public interface OskariLayerGroupLinkService {

    public List<OskariLayerGroupLink> findAll();

    public List<OskariLayerGroupLink> findByLayerId(int layerId);
    public List<OskariLayerGroupLink> findByGroupId(int groupId);

    public void insert(OskariLayerGroupLink link);
    public void insertAll(List<OskariLayerGroupLink> links);

    public void deleteLink(int layerId, int groupId);
    public void deleteLinksByLayerId(int layerId);

    public void replace(OskariLayerGroupLink old, OskariLayerGroupLink link);
    public boolean hasLinks(int groupId);

}
