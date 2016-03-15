package fi.mml.portti.domain.permissions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.io.IOException;
import java.util.List;

/**
 * handles user's permissions
 *
 * Contains a list of layers that user may use.
 *
 * Similar WFSLayerPermissionsStore class can be found in transport.
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
        JedisManager.setex(KEY + session, 86400,  getAsJSON());
	}

    /**
     * Destroys one session's permissions from redis
     *
     * @param session
     */
	@JsonIgnore
	public static void destroy(String session) {
        JedisManager.del(KEY + session);
	}

    /**
     * Destroys all permissions in redis
     */
	@JsonIgnore
	public static void destroyAll() {
        JedisManager.delAll(KEY);
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
            log.error(e, "JSON Generation failed");
        } catch (JsonMappingException e) {
            log.error(e, "Mapping from Object to JSON String failed");
        } catch (IOException e) {
            log.error(e, "IO failed");
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
