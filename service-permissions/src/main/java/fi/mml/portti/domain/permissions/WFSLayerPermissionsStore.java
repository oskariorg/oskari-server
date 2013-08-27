package fi.mml.portti.domain.permissions;


import java.io.IOException;
import java.util.List;
import java.util.Set;

import fi.nls.oskari.log.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import redis.clients.jedis.Jedis;

import fi.nls.oskari.log.Logger;
import fi.nls.oskari.cache.JedisManager;

/**
 * handles user's permissions
 * 
 * Contains a list of layers that user may use.
 */
public class WFSLayerPermissionsStore {

	private static final Logger log = LogFactory.getLogger(WFSLayerPermissionsStore.class);
    public static ObjectMapper mapper = new ObjectMapper();
    
	public static final String KEY = "Permission_";

	private List<Long> layerIds;

	/**
	 * Constructs object without parameters
	 */
	public WFSLayerPermissionsStore() {
	}

	/**
	 * Gets list of layer ids
	 * 
	 * @return layerIds
	 */
	public List<Long> getLayerIds() {
		return layerIds;
	}

	/**
	 * Sets layer ids
	 * 
	 * @param layerIds
	 */
	public void setLayerIds(List<Long> layerIds) {
		this.layerIds = layerIds;
	}

	/**
	 * Checks if user has permissions for a layer
	 * 
	 * @param id
	 * @return <code>true</code> if user may use the layer; <code>false</code>
	 *         otherwise.
	 */
	@JsonIgnore
	public boolean isPermission(long id) {
		return layerIds.contains(id);
	}

	/**
	 * Saves into redis
	 * 
	 * @param session
	 */
	public void save(String session) {

        if (!JedisManager.isPoolInitialized()) {
            return;
        }
        Jedis jedis = JedisManager.getInstance().getJedis();

		try {
			jedis.setex(KEY + session, 86400,  getAsJSON()); // expire in 1 day
		} finally {
			JedisManager.getInstance().returnJedis(jedis);
		}
	}

	@JsonIgnore
	public static void destroy(String session) {
        if (!JedisManager.isPoolInitialized()) {
            return;
        }

        Jedis jedis = JedisManager.getInstance().getJedis();

        try {
			jedis.del(KEY + session);
		} finally {
			JedisManager.getInstance().returnJedis(jedis);
		}
	}
	
	@JsonIgnore
	public static void destroyAll() {
        if (!JedisManager.isPoolInitialized()) {
            return;
        }

		Jedis jedis = JedisManager.getInstance().getJedis();

		try {
			Set<String> keys = jedis.keys(KEY + "*");
			if(keys.size() > 0) {
				jedis.del(keys.toArray(new String[keys.size()]));
			}
		} finally {
			JedisManager.getInstance().returnJedis(jedis);
		}
	}

	/**
	 * Transforms object to JSON String
	 * 
	 * @return JSON String
	 */
	@JsonIgnore
	public String getAsJSON() {
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonGenerationException e) {
			log.error("JSON Generation failed", e);
		} catch (JsonMappingException e) {
			log.error("Mapping from Object to JSON String failed", e);
		} catch (IOException e) {
			log.error("IO failed", e);
		}
		return null;
	}

	/**
	 * Transforms JSON String to object
	 * 
	 * @param json
	 * @return object
	 */
	@JsonIgnore
	public static WFSLayerPermissionsStore setJSON(String json)
			throws IOException {
		return mapper.readValue(json,
				WFSLayerPermissionsStore.class);
	}

	/**
	 * Gets saved permissions for certain session from redis
	 * 
	 * @param session
	 * @return permissions as JSON String
	 */
	@JsonIgnore
	public static String getCache(String session) {
		return JedisManager.get(KEY + session);
	}
}
