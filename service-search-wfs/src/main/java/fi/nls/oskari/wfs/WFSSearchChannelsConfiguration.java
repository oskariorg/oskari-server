package fi.nls.oskari.wfs;

import fi.nls.oskari.search.channel.WFSChannelHandler;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import fi.nls.oskari.util.JSONHelper;

public class WFSSearchChannelsConfiguration {

	protected final static String PARAM_ID = "id";
    protected final static String PARAM_WFS_LAYER_ID = "wfsId";
    protected final static String PARAM_LOCALE = "locale";
    protected final static String PARAM_CONFIG = "config";
    protected final static String PARAM_PARAMS_FOR_SEARCH = "params_for_search";
    protected final static String PARAM_IS_DEFAULT = "is_default";
	
	
	private int id = -1;
	private int WFSLayerId;
	private JSONObject locale;
	private JSONObject config;
	private JSONArray paramsForSearch;
	private Boolean isDefault;
	private String layerName;
	private String url;
	private String srs;
	private String version;
	private String username;
	private String password;
	
	
	public JSONObject getAsJSONObject() {
		final JSONObject root = new JSONObject();
		JSONHelper.putValue(root, PARAM_ID, this.getId());
		JSONHelper.putValue(root, PARAM_WFS_LAYER_ID, this.getWFSLayerId());
		JSONHelper.putValue(root, PARAM_LOCALE, this.getLocale());
		JSONHelper.putValue(root, PARAM_CONFIG, this.getConfig());
		JSONHelper.putValue(root, PARAM_PARAMS_FOR_SEARCH, this.getParamsForSearch());
		JSONHelper.putValue(root, PARAM_IS_DEFAULT, this.getIsDefault());
		return root;
	}
	
	public Boolean requiresAuth(){
		return username!=null && !username.isEmpty() && password!=null && !password.isEmpty();
	}

	public String getName(String language) {
		JSONObject langJSON = locale.optJSONObject(language);
		if(langJSON == null) {
			langJSON = locale.optJSONObject(PropertyUtil.getDefaultLanguage());
		}
		if(langJSON == null) {
			return null;
		}
		return langJSON.optString("name");
	}

	public String getHandler() {
		String handler = config.optString("handler");
		if(handler != null) {
			return handler;
		}
		return WFSChannelHandler.ID;
	}

	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public int getWFSLayerId() {
		return WFSLayerId;
	}


	public void setWFSLayerId(int wFSLayerId) {
		WFSLayerId = wFSLayerId;
	}


	public JSONObject getLocale() {
		if(locale == null) {
			locale = new JSONObject();
		}
		return locale;
	}


	public void setLocale(JSONObject locale) {
		this.locale = locale;
	}


	public JSONObject getConfig() {
		if(config == null) {
			config = new JSONObject();
		}
		return config;
	}


	public void setConfig(JSONObject config) {
		this.config = config;
	}


	public JSONArray getParamsForSearch() {
		if(paramsForSearch == null) {
			paramsForSearch = new JSONArray();
		}
		return paramsForSearch;
	}


	public void setParamsForSearch(JSONArray paramsForSearch) {
		this.paramsForSearch = paramsForSearch;
	}


	public Boolean getIsDefault() {
		return isDefault;
	}


	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}


	public String getLayerName() {
		return layerName;
	}


	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public String getSrs() {
		return srs;
	}


	public void setSrs(String srs) {
		this.srs = srs;
	}


	public String getVersion() {
		return version;
	}


	public void setVersion(String version) {
		this.version = version;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}
}
