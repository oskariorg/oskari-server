package fi.nls.oskari.search.util;

import java.util.Map;
import java.util.Set;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class VillageSearchUtil {
	
	private static final Logger log = LogFactory.getLogger(VillageSearchUtil.class);
	
	public static final String VILLAGES_URL_PROPERTY = "search.villages.url";
	
	
	
	public static Map getVillages() {
		Map<String, String> villagesMap = SearchUtil.getVillages();
		return villagesMap;
	}
	
	
	public static void printVillages(){
		Map villagesMap = getVillages();
		Set<String> keys = villagesMap.keySet();
		
		for(String key: keys){
			log.debug("key: " + key + " -- Value: " + (String)villagesMap.get(key));
		}
		
		
	}

}
