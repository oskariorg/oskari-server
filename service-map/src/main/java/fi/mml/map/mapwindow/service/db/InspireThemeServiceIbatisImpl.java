package fi.mml.map.mapwindow.service.db;

import com.ibatis.sqlmap.client.SqlMapClient;
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

    // FIXME: use some caching lib for this, and clear cache on update/delete etc
    // key is theme id
    final private static Map<Integer, InspireTheme> ID_CACHE = new HashMap<Integer, InspireTheme>();
    // key is layer id
    final private static Map<Integer, List<Integer>> LINK_CACHE = new HashMap<Integer, List<Integer>>();

	@Override
	protected String getNameSpace() {
		return "InspireTheme";
	}

    public List<InspireTheme> findByMaplayerId(final int layerId) {
        final List<Integer> links = LINK_CACHE.get(layerId);
        if(links == null) {
            // FIXME: very crude way to populate cache
            findLayerMappings();
            findAll();
            return queryForList(getNameSpace() + ".findByMaplayer", layerId);
        }
        final List<InspireTheme> list = new ArrayList<InspireTheme>();
        for(Integer id : links) {
            list.add(ID_CACHE.get(id));
        }
        return list;
    }

    public List<InspireTheme> findAll() {
        final List<InspireTheme> groups = super.findAll();
        for(InspireTheme group : groups) {
            ID_CACHE.put(group.getId(), group);
        }
        return groups;
    }

    private void findLayerMappings() {
        // setup link cache
        final List<Map<String,Object>> mappings = queryForListMap(getNameSpace() + ".findByMaplayerMappings");
        LINK_CACHE.clear();
        for(Map<String,Object> result : mappings) {
            final int themeid = (Integer) result.get("themeid");
            final int maplayerid = (Integer) result.get("maplayerid");
            List<Integer> themeLayers = getLinkCache(maplayerid);
            themeLayers.add(themeid);
        }
    }
    public void delete(int id) {
        super.delete(id);
        // update caches
        ID_CACHE.remove(id);
        findLayerMappings();
    }

    public void update(final InspireTheme theme) {
        ID_CACHE.put(theme.getId(), theme);
        super.update(theme);
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
                client.insert(getNameSpace() + ".linThemeToLayer", params);
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
        List<Integer> themeLayers = LINK_CACHE.get(maplayerid);
        if(themeLayers == null) {
            themeLayers = new ArrayList<Integer>();
            LINK_CACHE.put(maplayerid, themeLayers);
        }
        return themeLayers;
    }
}
