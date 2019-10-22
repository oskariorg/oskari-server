package fi.nls.oskari.map.layer;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.service.OskariComponent;

import java.util.List;
import java.util.Map;

public abstract class OskariLayerService extends OskariComponent {

    public abstract OskariLayer find(int id);
    public abstract List<OskariLayer> findByIdList(final List<Integer> idList);
    public abstract List<OskariLayer> findAll();
    public abstract List<OskariLayer> findByUrlAndName(final String url, final String name);
    public abstract List<OskariLayer> findByMetadataId(String uuid);
    public abstract List<OskariLayer> findAllWithPositiveUpdateRateSec();
    public abstract Map<String, List<Integer>> findNamesAndIdsByUrl(final String url, final String type);
    public abstract int insert(final OskariLayer layer);
    public abstract void update(final OskariLayer layer);
    public abstract void delete(final int layerId);

}
