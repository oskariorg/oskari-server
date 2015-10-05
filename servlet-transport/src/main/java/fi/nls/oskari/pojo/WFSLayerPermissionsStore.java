package fi.nls.oskari.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wfs.extension.AnalysisFilter;
import fi.nls.oskari.wfs.extension.MyPlacesFilter;
import fi.nls.oskari.wfs.extension.UserLayerFilter;

import java.io.IOException;
import java.util.List;

/**
 * handles user's permissions
 *
 * Contains a list of layers that user may use.
 *
 * Similar WFSLayerPermissionsStore class can be found in oskari-permissions.
 */
public class WFSLayerPermissionsStore {
	private static final Logger log = LogFactory.getLogger(WFSLayerPermissionsStore.class);
	private static final ObjectMapper mapper = new ObjectMapper();

	public static final String KEY = "Permission_";

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
			return mapper.writeValueAsString(this); // thread-safe
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

	/**
     * Return base wfs id, if analysis_ layer
     * @param id
     * @return id
     */
    @JsonIgnore
    private String getBaseLayerId(String id) {
        // TODO: should check analysis & myplaces rights for the type's layer id (not for WFS layer id)
        if (id.startsWith(AnalysisFilter.ANALYSIS_PREFIX)) {
            id = PropertyUtil.get(AnalysisFilter.ANALYSIS_BASE_LAYER_ID);
        } else if (id.startsWith(MyPlacesFilter.MY_PLACES_PREFIX)) {
            id = PropertyUtil.get(MyPlacesFilter.MY_PLACES_BASE_LAYER_ID);
        } else if (id.startsWith(UserLayerFilter.USERLAYER_PREFIX)) {
            id = PropertyUtil.get(UserLayerFilter.USERLAYER_BASE_LAYER_ID);
        }
        return id;
    }

}
