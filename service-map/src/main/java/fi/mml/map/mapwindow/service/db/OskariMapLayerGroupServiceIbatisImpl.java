package fi.mml.map.mapwindow.service.db;

import com.ibatis.sqlmap.client.SqlMapClient;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;

import java.sql.SQLException;
import java.util.*;

/**
 * MaplayerGroup implementation for Ibatis
 * 
 *
 */
@Oskari("MaplayerGroup")
public class OskariMapLayerGroupServiceIbatisImpl extends OskariMapLayerGroupService {

    private Logger log = LogFactory.getLogger(OskariMapLayerGroupServiceIbatisImpl.class);

    // key is theme id
    private static final Cache<MaplayerGroup> ID_CACHE = CacheManager.getCache(OskariMapLayerGroupServiceIbatisImpl.class.getName());
    // key is layer id
    private static final Cache<List<Integer>> LINK_CACHE = CacheManager.getCache(OskariMapLayerGroupServiceIbatisImpl.class.getName() + "Links");

	@Override
	protected String getNameSpace() {
		return "MaplayerGroup";
	}

    public List<MaplayerGroup> findByMaplayerId(final int layerId) {

        final List<Integer> links = LINK_CACHE.get(Integer.toString(layerId));
        if(links == null) {
            // very crude way to populate cache, but load all if it's empty. Most applications provide layer listing anyways
            if(LINK_CACHE.getSize() == 0) {
                findLayerMappings();
                findAll();
            }
            final List<MaplayerGroup> themes = queryForList(getNameSpace() + ".findByMaplayer", layerId);
            // populate link cache
            final List<Integer> newLinks = getLinkCache(layerId);
            for(MaplayerGroup theme : themes) {
                newLinks.add(theme.getId());
            }
            return themes;
        }
        final List<MaplayerGroup> list = new ArrayList<>();
        for(Integer id : links) {
            MaplayerGroup theme = find(id);
            if(theme != null) {
                list.add(theme);
            }
            else {
                log.warn("Layer with id", layerId, "links to a non-existing theme (id:", id,
                        "). Rows referencing theme should be removed from oskari_maplayer_themes DB table.");
            }
        }
        return list;
    }

    /**
     * Returns first theme (searched in arbitratry order) that has any part
     * of the theme name in any language(!) matching the given name-parameter.
     * Use carefully and preferrably with long parameter name.
     * FIXME: Quick and dirty
     * @param name
     * @return matching theme or null if no match
     */
    public MaplayerGroup findByName(final String name) {
        final List<MaplayerGroup> themes = findAll();
        for(MaplayerGroup theme : themes) {
            if(theme.getLocale().toString().indexOf(name) > -1) {
                return theme;
            }
        }
        return null;
    }

    @Override
    public MaplayerGroup find(int id) {
        MaplayerGroup theme = ID_CACHE.get(Integer.toString(id));
        if(theme == null) {
            theme = super.find(id);
            if(theme != null) {
                ID_CACHE.put("" + theme.getId(), theme);
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
            ID_CACHE.put(Integer.toString(group.getId()), group);
        }
        return groups;
    }

    private void findLayerMappings() {
        // setup link cache
        final List<Map<String,Object>> mappings = queryForListMap(getNameSpace() + ".findByMaplayerMappings");
        LINK_CACHE.setLimit(mappings.size() + 10);
        LINK_CACHE.flush(true);
        for(Map<String,Object> result : mappings) {
            if(result.get("themeid") == null) {
                // this will make the keys case insensitive (needed for hsqldb compatibility...)
                final Map<String, Object> caseInsensitiveData = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                caseInsensitiveData.putAll(result);
                result = caseInsensitiveData;
            }
            final int themeid = (Integer) result.get("themeid");
            final int maplayerid = (Integer) result.get("maplayerid");
            List<Integer> themeLayers = getLinkCache(maplayerid);
            themeLayers.add(themeid);
        }
    }

    @Override
    public void delete(int id) {
        super.delete(id);
        // update caches
        ID_CACHE.remove(Integer.toString(id));
        findLayerMappings();
    }

    public List<Integer> findMaplayersByGroup(int id) {
        return queryForList(getNameSpace() + ".findMaplayersByGroup", id);
    }

    @Override
    public void update(final MaplayerGroup theme) {
        ID_CACHE.put(Integer.toString(theme.getId()), theme);
        super.update(theme);
    }

    @Override
    public int insert(final MaplayerGroup theme) {
        final int id = super.insert(theme);
        theme.setId(id);
        ID_CACHE.put(Integer.toString(theme.getId()), theme);
        return id;
    }

    public synchronized void updateLayerThemes(final long maplayerId, final Collection<MaplayerGroup> themes) {
        // expects themes to be in database already - doesn't insert new ones
        SqlMapClient client = null;
        try {
            client = getSqlMapClient();
            client.startTransaction();
            // remove old links
            client.delete(getNameSpace() + ".removeThemeLinks", maplayerId);
            final List<Integer> themeLayers = getLinkCache((int)maplayerId);
            themeLayers.clear();
            // link new set of themes and update cache
            final Map<String, Object> params = new HashMap<>(2);
            params.put("layerId", maplayerId);
            if(themes != null) {
                // sublayers dont have themes
                for(MaplayerGroup theme : themes) {
                    params.put("themeId", theme.getId());
                    client.insert(getNameSpace() + ".linkThemeToLayer", params);
                    themeLayers.add(theme.getId());
                }
            }
            client.commitTransaction();
        } catch (Exception e) {
            throw new ServiceRuntimeException("Failed to set links", e);
        } finally {
            if (client != null) {
                try {
                    client.endTransaction();
                } catch (SQLException ignored) { }
            }
        }
    }

    private List<Map<String,Object>> queryForListMap(String sqlId) {
        try {
            List<Map<String,Object>> results = getSqlMapClient().queryForList(sqlId);
            return results;
        } catch (Exception e) {
            log.error(e, "Error getting list for sqlId:", sqlId);
        }
        return Collections.emptyList();
    }

    private List<Integer> getLinkCache(int maplayerid) {
        List<Integer> themeLayers = LINK_CACHE.get(Integer.toString(maplayerid));
        if(themeLayers == null) {
            themeLayers = new ArrayList<>();
            LINK_CACHE.put("" + maplayerid, themeLayers);
        }
        return themeLayers;
    }
}
