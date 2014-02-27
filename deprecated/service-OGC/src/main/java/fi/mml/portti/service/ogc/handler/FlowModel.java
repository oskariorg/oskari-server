package fi.mml.portti.service.ogc.handler;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import fi.nls.oskari.domain.GuestUser;
import fi.nls.oskari.domain.User;

/**
 * 
 * FlowModel represents variables that are available in flow
 * 
 */
public class FlowModel {
	
	private String xmlData;
	
	
	
	public static final String WFS_SERVICE = "wfsService";
	public static final String WFS_SERVICES_LIST = "wfsServicesList";
	
	public static final String FLOW_PM_BBOX_PARAMETER_NAME = "flow_pm_bboxParameterName";
	public static final String FLOW_PM_OWS_ABSTRACT_EN     = "flow_pm_owsAbstractEn";
	public static final String FLOW_PM_OWS_ABSTRACT_FI     = "flow_pm_owsAbstractFi";
	public static final String FLOW_PM_OWS_ABSTRACT_SV     = "flow_pm_owsAbstractSv";
	public static final String FLOW_PM_PASSWORD            = "flow_pm_password";
	public static final String FLOW_PM_TITLE_EN            = "flow_pm_titleEn";
	public static final String FLOW_PM_TITLE_FI            = "flow_pm_titleFi";
	public static final String FLOW_PM_TITLE_SV            = "flow_pm_titleSv";
	public static final String FLOW_PM_URL                 = "flow_pm_url";
	public static final String FLOW_PM_USERNAME            = "flow_pm_username";
	public static final String FLOW_PM_WFS_LAYER_ID        = "flow_pm_wfsLayerId";
	public static final String FLOW_PM_BBOX_MIN_X          = "flow_pm_bbox_min_x";
	public static final String FLOW_PM_BBOX_MIN_Y          = "flow_pm_bbox_min_y";
	public static final String FLOW_PM_BBOX_MAX_X          = "flow_pm_bbox_max_x";
	public static final String FLOW_PM_BBOX_MAX_Y          = "flow_pm_bbox_max_y";
	public static final String FLOW_PM_MAP_WFS_QUERY_ID	   = "flow_pm_map_wfs_query_id";
	
	public static final String FLOW_PM_GRID_QNAME         	   = "flow_pm_grid_qname";
	
	
	public static final String IS_AUTHENTICATION_STORED	   = "is_authentication_stored";
	
	/* Abstract parameter type names */
	public static final String[] ABSTRACT_PARAMETER_TYPES = "gml:FeaturePropertyType".split(",");
	
	public static final String VARIABLE_USER = "user";
	
	/** ROOT Json for result */
	public static final String VARIABLE_ROOT_JSON = "root.json";
	
	/** Variables */
	private Map<String, Object> flowVariables = new HashMap<String, Object>();
	
	/**
	 * If set to true, FlowModel will throw an error every time
	 * when unknown variable is requested
	 */
	private boolean breakOnNotFoundProperty = true;
	
	/**
	 * Creates a new flow model
	 */
	public FlowModel() {
		JSONObject root = new JSONObject();
		put(VARIABLE_ROOT_JSON, root);
	}
	
	/**
	 * Returns true if there is no value with given key
	 * 
	 * @param key
	 * @return
	 */
	public boolean isEmpty(String key) {
		return (flowVariables.get(key) == null);
	}
	
	/**
	 * Checks if flow should break. Checks that value is not null and 
	 * type of object is correct
	 * 
	 * @param key
	 * @param value
	 * @param checkType is type checked
	 * @param type class of expected object
	 */
	private void checkBreak(String key, Object value, boolean checkType, Class<? extends Object> type) {
		if (breakOnNotFoundProperty && value == null) {
			throw new RuntimeException("You are trying to get variable '" + key+ "' but " +
					"that returns null value. FlowModel is set to break on this event. ");
		}
		
		
		if (checkType && value.getClass().isAssignableFrom(type.getClass())) {
			throw new RuntimeException("You are trying to get variable '" + key+ "' as '" + type.getName() + "' but " +
				"variable with that name is actually of type '" + value.getClass() + "'. FlowModel is set to break on this event. ");
		}
		
	}
	
	/**
	 * Returns object with key
	 *  
	 * @param key
	 * @return
	 */
	public Object get(String key) {
		Object value = flowVariables.get(key);
		checkBreak(key, value, false, Object.class);
		return value;
	}
	
	/**
	 * Returns object with key as String
	 *  
	 * @param key
	 * @return
	 */
	public String getAsString(String key) {
		Object value = flowVariables.get(key);
		checkBreak(key, value, true, String.class);
		return (String)value;
	}
	
	
	/**
	 * Returns object with key as JSONObject
	 *  
	 * @param key
	 * @return
	 */
	public Integer getAsInteger(String key) {
		Object value = flowVariables.get(key);
		checkBreak(key, value, true, Integer.class);
		return (Integer)value;
	}
	
	
	/**
	 * Returns themedisplay
	 * 
	 * @return
	 */
	public User getUser() {
		Object value = flowVariables.get(VARIABLE_USER);
		checkBreak(VARIABLE_USER, value, true, User.class);
		return (User) value;
	}
	
	/**
	 * Returns root JSON
	 * 
	 * @return
	 */
	public JSONObject getRootJson() {
		Object value = flowVariables.get(VARIABLE_ROOT_JSON);
		checkBreak(VARIABLE_ROOT_JSON, value, true, JSONObject.class);
		return (JSONObject) value;
	}
	
	/**
	 * Put some value to root json
	 * 
	 * @param key
	 * @param value
	 */
	public void putValueToRootJson(String key, String value) {
		JSONObject root = (JSONObject) flowVariables.get(VARIABLE_ROOT_JSON);
		try {
			root.put(key, value);
		} catch (Exception e) {
			throw new RuntimeException("Failed to put value to Root Json", e);
		}
	}
	
	/**
	 * Put map to root json
	 * 
	 * @param key
	 * @param values
	 */
	public void putValueToRootJson(String key, Map values) {
		JSONObject root = (JSONObject) flowVariables.get(VARIABLE_ROOT_JSON);
		try {
			root.put(key, values);
		} catch (Exception e) {
			throw new RuntimeException("Failed to put value to Root Json", e);
		}
	}
	
	/**
	 * Adds themedisplay
	 * 
	 * @param themeDisplay
	 */
	public void setUser(User user) {
		if (user == null) {
			user = new GuestUser();
		}
		
		flowVariables.put(VARIABLE_USER, user);
	}	
	
	/**
	 * Add variable to flow model
	 * 
	 * @param key
	 * @param value
	 */
	public void put(String key, Object value) {
		flowVariables.put(key, value);
	}

	/**
	 * Clears flowmodel from request -specific stuff
	 */
	public void clearBeforeExecution() {
		JSONObject root = new JSONObject();
		put(VARIABLE_ROOT_JSON, root);		
	}

	public  String getXmlData() {
		return xmlData;
	}

	public void setXmlData(String xmlData) {
		this.xmlData = xmlData;
	}
	
}
