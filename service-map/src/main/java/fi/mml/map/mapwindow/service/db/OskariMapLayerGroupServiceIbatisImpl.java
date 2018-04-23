package fi.mml.map.mapwindow.service.db;

import com.ibatis.sqlmap.client.SqlMapClient;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.service.ServiceRuntimeException;

import java.sql.SQLException;
import java.util.*;

/**
 * MaplayerGroup implementation for Ibatis
 */
@Oskari("MaplayerGroup")
public class OskariMapLayerGroupServiceIbatisImpl extends OskariMapLayerGroupService {

    private static final Cache<MaplayerGroup> ID_CACHE = CacheManager.getCache(OskariMapLayerGroupServiceIbatisImpl.class.getName());

    @Override
    protected String getNameSpace() {
        return "MaplayerGroup";
    }

    public void flushCache() {
        ID_CACHE.flush(true);
    }

    @Override
    public MaplayerGroup find(int id) {
        String cacheKey = getCacheKey(id);
        MaplayerGroup theme = ID_CACHE.get(cacheKey);
        if(theme == null) {
            theme = super.find(id);
            if(theme != null) {
                ID_CACHE.put(cacheKey, theme);
            }
        }
        return theme;
    }

    @Override
    public List<MaplayerGroup> findAll() {
        final List<MaplayerGroup> groups = super.findAll();
        ID_CACHE.setLimit(groups.size() + 10);
        ID_CACHE.flush(true);
        for(MaplayerGroup group : groups) {
            ID_CACHE.put(getCacheKey(group), group);
        }
        return groups;
    }

    /**
     * Returns the map layer groups which belong to the given parent.
     * @param groupId
     * @return child groups of the given group
     */
    public List<MaplayerGroup> findByParentId(final int groupId) {
        final List<MaplayerGroup> groups = queryForList(getNameSpace() + ".findByParentId", groupId);
        return groups;
    }

    @Override
    public MaplayerGroup findByName(String name) {
        return queryForObject(getName() + ".findByName", name);
    }

    @Override
    public void delete(int id) {
        super.delete(id);
        // update caches
        ID_CACHE.remove(getCacheKey(id));
    }

    @Override
    public void update(final MaplayerGroup group) {
        ID_CACHE.put(getCacheKey(group), group);
        super.update(group);
    }

    @Override
    public int insert(final MaplayerGroup theme) {
        final int id = super.insert(theme);
        theme.setId(id);
        ID_CACHE.put(getCacheKey(id), theme);
        return id;
    }

    public void updateOrder(MaplayerGroup group) {
        SqlMapClient client = null;
        try {
            client = getSqlMapClient();
            client.startTransaction();
            client.update(getNameSpace() + ".updateOrder", group);
            client.commitTransaction();
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to update group ordering", e);
        } finally {
            if (client != null) {
                try {
                    client.endTransaction();
                } catch (SQLException ignored) { }
            }
        }
    }

    public void updateGroupParent(final int groupId, final int newGroupId) {
        SqlMapClient client = null;
        try {
            client = getSqlMapClient();
            client.startTransaction();
            HashMap<String, Integer> insertMap = new HashMap<>();
            insertMap.put("groupId", groupId);
            insertMap.put("newGroupId", newGroupId);
            client.update(getNameSpace() + ".updateGroupParent", insertMap);
            client.commitTransaction();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update group parent", e);
        } finally {
            if (client != null) {
                try {
                    client.endTransaction();
                } catch (SQLException ignored) { }
            }
        }
    }

    private String getCacheKey(int id) {
        return Integer.toString(id);
    }

    private String getCacheKey(MaplayerGroup group) {
        return getCacheKey(group.getId());
    }

}
