package fi.nls.oskari.wfs;

import org.json.JSONArray;
import org.json.JSONObject;

import fi.nls.oskari.util.JSONHelper;

public class WFSSearchChannelsConfiguration {

	protected final static String PARAM_ID = "id";
    protected final static String PARAM_WFS_LAYER_ID = "wfsId";
    protected final static String PARAM_TOPIC = "topic";
    protected final static String PARAM_DESC = "desc";
    protected final static String PARAM_PARAMS_FOR_SEARCH = "params_for_search";
    protected final static String PARAM_IS_DEFAULT = "is_default";
    protected final static String PARAM_IS_ADDRESS = "is_address";
	
	
	private int id = -1;
	private int WFSLayerId;
	private JSONObject topic;
	private JSONObject desc;
	private JSONArray paramsForSearch;
	private Boolean isDefault;
	private Boolean isAddress;
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
		JSONHelper.putValue(root, PARAM_TOPIC, this.getTopic());
		JSONHelper.putValue(root, PARAM_DESC, this.getDesc());
		JSONHelper.putValue(root, PARAM_PARAMS_FOR_SEARCH, this.getParamsForSearch());
		JSONHelper.putValue(root, PARAM_IS_DEFAULT, this.getIsDefault());
		JSONHelper.putValue(root, PARAM_IS_ADDRESS, this.getIsAddress());
		return root;
	}
	
	public Boolean requiresAuth(){
		return username!=null && !username.isEmpty() && password!=null && !password.isEmpty();
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


	public JSONObject getTopic() {
		return topic;
	}


	public void setTopic(JSONObject topic) {
		this.topic = topic;
	}


	public JSONObject getDesc() {
		return desc;
	}


	public void setDesc(JSONObject desc) {
		this.desc = desc;
	}


	public JSONArray getParamsForSearch() {
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

	public Boolean getIsAddress() {
		return isAddress;
	}

	public void setIsAddress(Boolean isAddress) {
		this.isAddress = isAddress;
	}

}
