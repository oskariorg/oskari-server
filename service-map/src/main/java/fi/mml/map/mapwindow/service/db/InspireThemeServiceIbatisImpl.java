package fi.mml.map.mapwindow.service.db;

import fi.nls.oskari.domain.map.InspireTheme;
import fi.nls.oskari.service.db.BaseIbatisService;

import java.util.*;

/**
 * InspireTheme implementation for Ibatis
 * 
 *
 */
public class InspireThemeServiceIbatisImpl extends BaseIbatisService<InspireTheme> implements InspireThemeService {

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
            int themeid = (Integer) result.get("themeid");
            int maplayerid = (Integer) result.get("maplayerid");
            List<Integer> themeLayers = LINK_CACHE.get(maplayerid);
            if(themeLayers == null) {
                themeLayers = new ArrayList<Integer>();
                LINK_CACHE.put(maplayerid, themeLayers);
            }
            themeLayers.add(themeid);
        }
    }

    private List<Map<String,Object>> queryForListMap(String sqlId) {
        try {
            List<Map<String,Object>> results = getSqlMapClient().queryForList(sqlId);
            return results;
        } catch (Exception e) {
        }
        return Collections.emptyList();
    }
}
