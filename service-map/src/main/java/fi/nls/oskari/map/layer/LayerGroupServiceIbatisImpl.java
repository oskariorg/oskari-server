package fi.nls.oskari.map.layer;

import fi.nls.oskari.domain.map.LayerGroup;
import fi.nls.oskari.service.db.BaseIbatisService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 16.12.2013
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
public class LayerGroupServiceIbatisImpl extends BaseIbatisService<LayerGroup> implements LayerGroupService {

    // FIXME: use some caching lib for this, and clear cache on update/delete etc
    final private static Map<Integer, LayerGroup> ID_CACHE = new HashMap<Integer, LayerGroup>();

    public LayerGroupServiceIbatisImpl() {
        // populate cache on startup
        findAll();
    }

    @Override
    protected String getNameSpace() {
        return "LayerGroup";
    }

    public LayerGroup find(int id) {
        LayerGroup group = ID_CACHE.get(id);
        if(group != null) {
            return group;
        }
        group = super.find(id);
        if(group != null) {
            ID_CACHE.put(group.getId(), group);
        }
        return group;
    }

    public List<LayerGroup> findAll() {
        final List<LayerGroup> groups = super.findAll();
        for(LayerGroup group : groups) {
            ID_CACHE.put(group.getId(), group);
        }
        return groups;
    }
}
