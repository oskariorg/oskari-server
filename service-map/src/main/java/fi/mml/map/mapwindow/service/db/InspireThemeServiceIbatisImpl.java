package fi.mml.map.mapwindow.service.db;

import com.ibatis.sqlmap.client.SqlMapClient;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.domain.map.InspireTheme;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.db.BaseIbatisService;

import java.sql.SQLException;
import java.util.*;

/**
 * InspireTheme implementation for Ibatis
 * 
 *
 */
public class InspireThemeServiceIbatisImpl extends BaseIbatisService<InspireTheme> implements InspireThemeService {

    private Logger log = LogFactory.getLogger(InspireThemeServiceIbatisImpl.class);

    // key is theme id
    final private static Cache<InspireTheme> ID_CACHE = CacheManager.getCache("InspireTheme");
    // key is layer id
    final private static Cache<List<Integer>> LINK_CACHE = CacheManager.getCache("InspireThemeLinks");

	@Override
	protected String getNameSpace() {
		return "InspireTheme";
	}

    public List<InspireTheme> findByMaplayerId(final int layerId) {

        final List<Integer> links = LINK_CACHE.get("" + layerId);
        if(links == null) {
            // FIXME: very crude way to populate cache
            findLayerMappings();
            findAll();
            return queryForList(getNameSpace() + ".findByMaplayer", layerId);
        }
        final List<InspireTheme> list = new ArrayList<InspireTheme>();
        for(Integer id : links) {
            list.add(find(id));
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
    public InspireTheme findByName(final String name) {
        final List<InspireTheme> themes = findAll();
        for(InspireTheme theme : themes) {
            if(theme.getLocale().toString().indexOf(name) > -1) {
                return theme;
            }
        }
        return null;
    }

    @Override
    public InspireTheme find(int id) {
        InspireTheme theme = ID_CACHE.get("" + id);
        if(theme == null) {
            theme = super.find(id);
            ID_CACHE.put("" + theme.getId(), theme);
        }
        return theme;
    }

    public List<InspireTheme> findAll() {
        final List<InspireTheme> groups = super.findAll();
        ID_CACHE.flush(true);
        for(InspireTheme group : groups) {
            ID_CACHE.put("" + group.getId(), group);
        }
        return groups;
    }

    private void findLayerMappings() {
        // setup link cache
        final List<Map<String,Object>> mappings = queryForListMap(getNameSpace() + ".findByMaplayerMappings");
        LINK_CACHE.flush(true);
        for(Map<String,Object> result : mappings) {
            if(result.get("themeid") == null) {
                // this will make the keys case insensitive (needed for hsqldb compatibility...)
                final Map<String, Object> caseInsensitiveData = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
                caseInsensitiveData.putAll(result);
                result = caseInsensitiveData;
            }
            final int themeid = (Integer) result.get("themeid");
            final int maplayerid = (Integer) result.get("maplayerid");
            List<Integer> themeLayers = getLinkCache(maplayerid);
            themeLayers.add(themeid);
        }
    }
    public void delete(int id) {
        super.delete(id);
        // update caches
        ID_CACHE.remove("" + id);
        findLayerMappings();
    }

    public List<Integer> findMaplayersByTheme(int id) {
        return queryForList(getNameSpace() + ".findMaplayersByTheme", id);
    }

    public void update(final InspireTheme theme) {
        ID_CACHE.put("" + theme.getId(), theme);
        super.update(theme);
    }
    public int insert(final InspireTheme theme) {
        final int id = super.insert(theme);
        theme.setId(id);
        ID_CACHE.put("" + theme.getId(), theme);
        return id;
    }

    public synchronized void updateLayerThemes(final long maplayerId, final Collection<InspireTheme> themes) {
        // expects themes to be in database already - doesn't insert new ones
        SqlMapClient client = null;
        try {
            client = getSqlMapClient();
            client.startTransaction();
            // remove old links
            log.debug("Removing");
            client.delete(getNameSpace() + ".removeThemeLinks", maplayerId);
            log.debug("Removed");
            final List<Integer> themeLayers = getLinkCache((int)maplayerId);
            themeLayers.clear();
            // link new set of themes and update cache
            final Map<String, Object> params = new HashMap<String, Object>(2);
            params.put("layerId", maplayerId);
            log.debug("Inserting");
            for(InspireTheme theme : themes) {
                params.put("themeId", theme.getId());
                client.insert(getNameSpace() + ".linkThemeToLayer", params);
                themeLayers.add(theme.getId());
            }
            log.debug("Linked");
            client.commitTransaction();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set links", e);
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
        List<Integer> themeLayers = LINK_CACHE.get("" + maplayerid);
        if(themeLayers == null) {
            themeLayers = new ArrayList<Integer>();
            LINK_CACHE.put("" + maplayerid, themeLayers);
        }
        return themeLayers;
    }
}
