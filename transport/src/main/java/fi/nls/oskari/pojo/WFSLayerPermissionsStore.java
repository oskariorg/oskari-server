package fi.nls.oskari.pojo;

import java.io.IOException;
import java.util.List;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;

import fi.nls.oskari.log.Logger;
import fi.nls.oskari.transport.TransportService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;

/**
 * handles user's permissions
 * 
 * Contains a list of layers that user may use.
 *
 * Similar WFSLayerPermissionsStore class can be found in oskari-permissions.
 */
public class WFSLayerPermissionsStore {


	private static final Logger log = LogFactory.getLogger(WFSLayerPermissionsStore.class);

	public static final String KEY = "Permission_";
	public static final String ANALYSIS_BASELAYER_ID = "analysis.baselayer.id";
	public static final String ANALYSIS_PREFIX = "analysis_";

	private List<String> layerIds;

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
	public List<String> getLayerIds() {
		return layerIds;
	}

	/**
	 * Sets layer ids
	 * 
	 * @param layerIds
	 */
	public void setLayerIds(List<String> layerIds) {
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
	public boolean isPermission(String id) {
		 // Fix, if prefix layers
        return layerIds.contains(getBaseLayerId(id));
	}

	/**
	 * Transforms object to JSON String
	 * 
	 * @return JSON String
	 */
	@JsonIgnore
	public String getAsJSON() {
		try {
			return TransportService.mapper.writeValueAsString(this); // thread-safe
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
		return TransportService.mapper.readValue(json,
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
	  /**
     * Return base wfs id, if analysis_ layer
     * @param sid
     * @return  id
     */
    private String getBaseLayerId(String sid) {

        String id = sid;
        if (sid.indexOf(ANALYSIS_PREFIX) > -1) {
            id = PropertyUtil.get(ANALYSIS_BASELAYER_ID);
        }
        return id;
    }
}
