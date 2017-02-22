package fi.nls.oskari.domain.map.wfs;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles layer's configuration
 *
 * Similar WFSLayerStore class can be found in transport.
 */
public class WFSLayerConfiguration {

    private static final Logger log = LogFactory
            .getLogger(WFSLayerConfiguration.class);
	public final static String KEY = "WFSLayer_";
    public final static String IMAGE_KEY = "WFSImage_";

    private static final double DEFAULT_TILE_BUFFER = 0.0d;
    private static final String KEY_DEFAULT = "default";

    protected final static String LAYER_ID = "layerId";
    protected final static String URL_PARAM = "URL";
    protected final static String USERNAME = "username";
    protected final static String PASSWORD = "password";

    protected final static String LAYER_NAME = "layerName";
    protected final static String LAYER_FRIENDLY_NAME = "uiName";

    protected final static String GML_GEOMETRY_PROPERTY = "GMLGeometryProperty";
    protected final static String SRS_NAME = "SRSName";
    protected final static String GML_VERSION = "GMLVersion";
    protected final static String GML2_SEPARATOR = "GML2Separator";
    protected final static String WFS_VERSION = "WFSVersion";
    protected final static String MAX_FEATURES = "maxFeatures";
    protected final static String FEATURE_NAMESPACE = "featureNamespace";
    protected final static String FEATURE_NAMESPACE_URI = "featureNamespaceURI";
    protected static final String GEOMETRY_NAMESPACE_URI = "geometryNamespaceURI";
    protected final static String FEATURE_ELEMENT = "featureElement";
    protected final static String OUTPUT_FORMAT = "outputFormat";

    protected final static String FEATURE_TYPE = "featureType";
    protected final static String SELECTED_FEATURE_PARAMS = "selectedFeatureParams";
    protected final static String FEATURE_PARAMS_LOCALES = "featureParamsLocales";
    protected final static String GEOMETRY_TYPE = "geometryType";
    protected final static String GET_MAP_TILES = "getMapTiles";
    protected static final String GET_HIGHLIGHT_IMAGE = "getHighlightImage";
    protected final static String GET_FEATURE_INFO = "getFeatureInfo";
    protected final static String TILE_REQUEST = "tileRequest";
    protected final static String TILE_BUFFER = "tileBuffer";
    protected final static String WMS_LAYER_ID = "WMSLayerId";

    protected final static String JOB_TYPE = "jobType";
    protected static final String REQUEST_IMPULSE = "requestImpulse";
    protected static final String ATTRIBUTES = "attributes";

    protected final static String MIN_SCALE = "minScale";
    protected final static String MAX_SCALE = "maxScale";

    protected final static String IS_PUBLISHED = "isPublished";
    protected final static String UUID = "uuid";

    protected final static String TEMPLATE_NAME = "templateName";
    protected final static String TEMPLATE_DESCRIPTION = "templateDescription";
    protected final static String TEMPLATE_TYPE = "templateType";
    protected final static String REQUEST_TEMPLATE = "requestTemplate";
    protected final static String RESPONSE_TEMPLATE = "responseTemplate";
    protected final static String PARSE_CONFIG = "parseConfig";

    protected final static String SELECTION_SLD_STYLE = "selectionSLDStyle";

    protected final static String STYLES = "styles";
    protected final static String ID = "id";
    protected final static String NAME = "name";
    protected final static String SLD_STYLE = "SLDStyle";

    private int id = -1;
	private String layerId;
	private String nameLocales;
	private String URL;
	private String username;
	private String password;

    private String layerName;

	private String GMLGeometryProperty;
	private String SRSName;
	private String GMLVersion;
    private boolean GML2Separator; // if srs url is in old format (# => :)
	private String WFSVersion;
	private int maxFeatures;
	private String featureNamespace;
	private String featureNamespaceURI;
    private String geometryNamespaceURI;
	private String featureElement;
    private String outputFormat;

	private JSONObject featureType;
	private JSONObject selectedFeatureParams; // if needed?
	private JSONObject featureParamsLocales;

	private String geometryType; // 2D/3D
	private boolean getMapTiles; // if tile images are drawn and send
    private boolean getHighlightImage; // if highlight image is drawn and send
	private boolean getFeatureInfo; // if feature json is send
    private boolean tileRequest; // if tile requests are made (map request default)
    private JSONObject tileBuffer;
	private String WMSLayerId;
    private String wps_params;  // WPS params for WFS layer eg {input_type:gs_vector}
    private int templateModelId = 0;  //id of portti_wfs_template_model row (FE configs when jobtype=feature-engine
    private String jobType;
    private String requestImpulse;
    private JSONObject attributes = new JSONObject();

	private double minScale;
	private double maxScale;

    private boolean isPublished = false;
    private String uuid;

	// Template Model
	private String templateName;
	private String templateDescription;
	private String templateType;
	private String requestTemplate;
	private String responseTemplate;
    private JSONObject parseConfig;

	private String selectionSLDStyle;

	private List<WFSSLDStyle> SLDStyles = new ArrayList<WFSSLDStyle>(); // id, name, xml

    /**
     * Constructs a QName for feature element.
     * @return
     */
    public QName getFeatureElementQName() {
        /*
        old db table        | new db table
        portti_feature_type | portti_wfs_layer field
        --------------------------------------------
        namespace_uri       | feature_namespace_uri
        localpart           | feature_element
        name prefix         | feature_namespace
        */
        return new QName(getFeatureNamespaceURI(), getFeatureElement(), getFeatureNamespace());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLayerId() {
		return layerId;
	}

	public void setLayerId(String layerId) {
		this.layerId = layerId;
	}

	public String getNameLocales() {
		return nameLocales;
	}

	public void setNameLocales(String nameLocales) {
		this.nameLocales = nameLocales;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
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

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public String getGMLGeometryProperty() {
		return GMLGeometryProperty;
	}

	public void setGMLGeometryProperty(String gMLGeometryProperty) {
		GMLGeometryProperty = gMLGeometryProperty;
	}

	public String getSRSName() {
		return SRSName;
	}

	public void setSRSName(String sRSName) {
		SRSName = sRSName;
	}

	public String getGMLVersion() {
		return GMLVersion;
	}

	public void setGMLVersion(String gMLVersion) {
		GMLVersion = gMLVersion;
	}

    public boolean isGML2Separator() {
        return GML2Separator;
    }

    public void setGML2Separator(boolean GML2Separator) {
        this.GML2Separator = GML2Separator;
    }

	public String getWFSVersion() {
		return WFSVersion;
	}

	public void setWFSVersion(String wFSVersion) {
		WFSVersion = wFSVersion;
	}

	public int getMaxFeatures() {
		return maxFeatures;
	}

	public void setMaxFeatures(int maxFeatures) {
		this.maxFeatures = maxFeatures;
	}

	public String getFeatureNamespace() {
		return featureNamespace;
	}

	public void setFeatureNamespace(String featureNamespace) {
		this.featureNamespace = featureNamespace;
	}

	public String getFeatureNamespaceURI() {
		return featureNamespaceURI;
	}

	public void setFeatureNamespaceURI(String featureNamespaceURI) {
		this.featureNamespaceURI = featureNamespaceURI;
	}

    public String getGeometryNamespaceURI() {
        return geometryNamespaceURI;
    }

    public void setGeometryNamespaceURI(String geometryNamespaceURI) {
        this.geometryNamespaceURI = geometryNamespaceURI;
    }

	public String getFeatureElement() {
		return featureElement;
	}

	public void setFeatureElement(String featureElement) {
		this.featureElement = featureElement;
	}

    /**
     * Gets output format
     * @return output format
     */
    public String getOutputFormat() { return outputFormat; }

    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }

    public JSONObject getFeatureType() {
		return featureType;
	}
    public void addFeatureType(String key, final String value) {
        if(featureType == null) {
            featureType = new JSONObject();
        }
        JSONHelper.putValue(featureType, key, value);
    }

	public void setFeatureType(String featureType) {
		this.featureType =  JSONHelper.createJSONObject(featureType);
	}

	public JSONObject getSelectedFeatureParams() {
		return selectedFeatureParams;
	}

	public void setSelectedFeatureParams(String selectedFeatureParams) {
        this.selectedFeatureParams = JSONHelper.createJSONObject(selectedFeatureParams);
	}

    public void addSelectedFeatureParams(String key, List<String> paramNames) {
        if(selectedFeatureParams == null) {
            selectedFeatureParams = new JSONObject();
        }
        JSONHelper.putValue(selectedFeatureParams, key, new JSONArray(paramNames));
    }

    /**
     * Tries to get selected feature params for given key. Defaults to DEFAULT_LOCALE if key has no
     * selected params. If anything fails or feature params is not configured, returns empty list.
     * @param key locale key like "en" or "fi"
     * @return
     */
    public List<String> getSelectedFeatureParams(String key) {
        if(getSelectedFeatureParams() == null) {
            return new ArrayList<String>(0);
        }
        JSONArray params = getSelectedFeatureParams().optJSONArray(key);
        if(params == null) {
            params = getSelectedFeatureParams().optJSONArray(KEY_DEFAULT);
        }
        if(params == null) {
            return new ArrayList<String>(0);
        }
        return JSONHelper.getArrayAsList(params);
    }

	public JSONObject getFeatureParamsLocales() {
		return featureParamsLocales;
	}

    public List<String> getFeatureParamsLocales(String key) {
        if(getFeatureParamsLocales() == null) {
            return new ArrayList<String>(0);
        }
        JSONArray params = getFeatureParamsLocales().optJSONArray(key);
        if(params == null) {
            params = getFeatureParamsLocales().optJSONArray(KEY_DEFAULT);
        }
        if(params == null) {
            return new ArrayList<String>(0);
        }
        return JSONHelper.getArrayAsList(params);
    }

    public void addFeatureParamsLocales(String key, List<String> paramNames) {
        if(featureParamsLocales == null) {
            featureParamsLocales = new JSONObject();
        }
        JSONHelper.putValue(featureParamsLocales, key, new JSONArray(paramNames));
    }
	public void setFeatureParamsLocales(String featureParamsLocales) {
		this.featureParamsLocales = JSONHelper.createJSONObject(featureParamsLocales);
	}


    public String getGeometryType() {
		return geometryType;
	}

	public void setGeometryType(String geometryType) {
		this.geometryType = geometryType;
	}

    /**
     * Checks if should get map tiles
     *
     * @return <code>true</code> if should get map tiles; <code>false</code>
     *         otherwise.
     */
	public boolean isGetMapTiles() {
        return getMapTiles;
	}

	public void setGetMapTiles(boolean getMapTiles) {
		this.getMapTiles = getMapTiles;
	}

    public boolean isGetHighlightImage() {
        return getHighlightImage;
    }

    public void setGetHighlightImage(boolean getHighlightImage) {
        this.getHighlightImage = getHighlightImage;
    }

	public boolean isGetFeatureInfo() {
		return getFeatureInfo;
	}

	public void setGetFeatureInfo(boolean getFeatureInfo) {
		this.getFeatureInfo = getFeatureInfo;
	}

    public boolean isTileRequest() {
        return tileRequest;
    }

    public void setTileRequest(boolean tileRequest) {
        this.tileRequest = tileRequest;
    }

    public JSONObject getTileBuffer() {
        return tileBuffer;
    }

    public double getTileBuffer(final String key) {
        return getTileBuffer(key, DEFAULT_TILE_BUFFER);
    }

    public double getTileBuffer(final String key, final double defaultValue) {
        if(tileBuffer == null) {
            return defaultValue;
        }
        return tileBuffer.optDouble(key, defaultValue);
    }

    public void addTileBuffer(String key, double value) {
        if(tileBuffer == null) {
            tileBuffer = new JSONObject();
        }
        JSONHelper.putValue(tileBuffer, key, value);
    }

    public void setTileBuffer(String tileBuffer) {
        this.tileBuffer = JSONHelper.createJSONObject(tileBuffer);
    }

    public String getWMSLayerId() {
		return WMSLayerId;
	}

	public void setWMSLayerId(String wMSLayerId) {
		WMSLayerId = wMSLayerId;
	}

    /**
     * Get wps params for WFS layer eg {input_type:gs_vector}
     * (default is {})
     * @return
     */
    public String getWps_params() {
        return wps_params;
    }

    /**
     * Set wps_params (basic field value in portti_wfs_layer)
     * @param wps_params
     */
    public void setWps_params(String wps_params) {
        this.wps_params = wps_params;
    }

    public void setJobType(String type) {
        this.jobType = type;
    }

    public String getJobType() {
        return this.jobType;
    }

    public void setRequestImpulse(String param) {
        this.requestImpulse = param;
    }

    public String getRequestImpulse() {
        return this.requestImpulse;
    }

    public JSONObject getAttributes() {
        return attributes;
    }

    public void setAttributes(JSONObject attributes) {
        this.attributes = attributes;
    }
    public void setAttributes(String attributes) {
        this.attributes = attributes != null ? JSONHelper.createJSONObject(attributes) : null;
    }


    /**
	 * Gets min scale
	 *
	 * @return min scale
	 */
	public double getMinScale() {
		return minScale;
	}

	/**
	 * Sets min scale
	 *
	 * @param minScale
	 */
	public void setMinScale(double minScale) {
		this.minScale = minScale;
	}

	/**
	 * Gets max scale
	 *
	 * @return max scale
	 */
	public double getMaxScale() {
		return maxScale;
	}

	/**
	 * Sets max scale
	 *
	 * @param maxScale
	 */
	public void setMaxScale(double maxScale) {
		this.maxScale = maxScale;
	}

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean isPublished) {
        this.isPublished = isPublished;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Get Template model id (row id of portti_wfs_template_model table)
     * @return
     */
    public int getTemplateModelId() {
        return templateModelId;
    }

    /**
     * Set template id
     * @param templateModelId
     */
    public void setTemplateModelId(int templateModelId) {
        this.templateModelId = templateModelId;
    }

    /**
	 * Gets template name
	 *
	 * @return template name
	 */
	public String getTemplateName() {
		return templateName;
	}

	/**
	 * Sets template name
	 *
	 * @param templateName
	 */
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	/**
	 * Gets template description
	 *
	 * @return template description
	 */
	public String getTemplateDescription() {
		return templateDescription;
	}

	/**
	 * Sets template description
	 *
	 * @param templateDescription
	 */
	public void setTemplateDescription(String templateDescription) {
		this.templateDescription = templateDescription;
	}

	/**
	 * Gets template type
	 *
	 * @return template type
	 */
	public String getTemplateType() {
		return templateType;
	}

	/**
	 * Sets template type
	 *
	 * @param templateType
	 */
	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	public String getRequestTemplate() {
		return requestTemplate;
	}

	public void setRequestTemplate(String requestTemplate) {
		this.requestTemplate = requestTemplate;
	}

	public String getResponseTemplate() {
		return responseTemplate;
	}

	public void setResponseTemplate(String responseTemplate) {
		this.responseTemplate = responseTemplate;
	}

	public String getSelectionSLDStyle() {
		return selectionSLDStyle;
	}

	public void setSelectionSLDStyle(String selectionSLDStyle) {
		this.selectionSLDStyle = selectionSLDStyle;
	}

	public List<WFSSLDStyle> getSLDStyles() {
        if(SLDStyles == null) {
            SLDStyles = new ArrayList<WFSSLDStyle>();
        }
		return SLDStyles;
	}

    public WFSSLDStyle getDefaultSLDStyle() {
        return getSLDStyle(KEY_DEFAULT);
    }

    public WFSSLDStyle getSLDStyle(final String name) {
        if(name == null) {
            return getDefaultSLDStyle();
        }
        for(WFSSLDStyle style : getSLDStyles()) {
            if(name.equals(style.getName())) {
                return style;
            }
        }
        return null;
    }

    public void addSLDStyle(WFSSLDStyle style) {
        if(style == null) {
            return;
        }
        getSLDStyles().add(style);
    }

	public void setSLDStyles(List<WFSSLDStyle> sLDStyles) {
		SLDStyles = sLDStyles;
	}

    public JSONObject getParseConfig() {
        return parseConfig;
    }

    public void setParseConfig(String parseConfig) {
        this.parseConfig = parseConfig != null ? JSONHelper.createJSONObject(parseConfig) : null;
    }

    public void save() {
        final String key = KEY + this.layerId;
        final String json = getAsJSON();
        log.debug("Writing WFS to Redis:", key, "->", json);
		JedisManager.setex(key, JedisManager.EXPIRY_TIME_DAY, json); // expire in 1 day
	}

	public void destroy() {
		JedisManager.del(KEY + this.layerId);
	}

    private String getLayerFriendlyName() {
        if(this.getNameLocales() == null) return "";
        final JSONObject loc = JSONHelper.createJSONObject(this.getNameLocales());
        final JSONObject langName = JSONHelper.getJSONObject(loc, PropertyUtil.getDefaultLanguage());
        return JSONHelper.getStringFromJSON(langName, "name", "");
    }

	public JSONObject getAsJSONObject() {
		final JSONObject root = new JSONObject();

		JSONHelper.putValue(root, LAYER_ID, this.getLayerId());
        // just for debugging so we see the layers UI name in redis
        JSONHelper.putValue(root, LAYER_FRIENDLY_NAME, getLayerFriendlyName());

		JSONHelper.putValue(root, URL_PARAM, this.getURL());
		JSONHelper.putValue(root, USERNAME, this.getUsername());
		JSONHelper.putValue(root, PASSWORD, this.getPassword());

		JSONHelper.putValue(root, LAYER_NAME, this.getLayerName());

		JSONHelper.putValue(root, GML_GEOMETRY_PROPERTY, this.getGMLGeometryProperty());
		JSONHelper.putValue(root, SRS_NAME, this.getSRSName());
		JSONHelper.putValue(root, GML_VERSION, this.getGMLVersion());
        JSONHelper.putValue(root, GML2_SEPARATOR, this.isGML2Separator());
		JSONHelper.putValue(root, WFS_VERSION, this.getWFSVersion());
		JSONHelper.putValue(root, MAX_FEATURES, this.getMaxFeatures());
		JSONHelper.putValue(root, FEATURE_NAMESPACE, this.getFeatureNamespace());
		JSONHelper.putValue(root, FEATURE_NAMESPACE_URI, this.getFeatureNamespaceURI());
        JSONHelper.putValue(root, GEOMETRY_NAMESPACE_URI, this.getGeometryNamespaceURI());
		JSONHelper.putValue(root, FEATURE_ELEMENT, this.getFeatureElement());
        JSONHelper.putValue(root, OUTPUT_FORMAT, this.getOutputFormat());

		JSONHelper.putValue(root, FEATURE_TYPE, this.getFeatureType());
		JSONHelper.putValue(root, SELECTED_FEATURE_PARAMS, getSelectedFeatureParams());
		JSONHelper.putValue(root, FEATURE_PARAMS_LOCALES, getFeatureParamsLocales());
		JSONHelper.putValue(root, GEOMETRY_TYPE, this.getGeometryType());
		JSONHelper.putValue(root, GET_MAP_TILES, this.isGetMapTiles());
        JSONHelper.putValue(root, GET_HIGHLIGHT_IMAGE, this.isGetHighlightImage());
		JSONHelper.putValue(root, GET_FEATURE_INFO, this.isGetFeatureInfo());
        JSONHelper.putValue(root, TILE_REQUEST, this.isTileRequest());
        JSONHelper.putValue(root, TILE_BUFFER, getTileBuffer());
		JSONHelper.putValue(root, WMS_LAYER_ID, this.getWMSLayerId());
        JSONHelper.putValue(root, JOB_TYPE, this.getJobType());
        JSONHelper.putValue(root, REQUEST_IMPULSE, this.getRequestImpulse());
        JSONHelper.putValue(root, ATTRIBUTES, this.getAttributes() != null ? this.getAttributes().toString() : null);

		JSONHelper.putValue(root, MIN_SCALE, this.getMinScale());
		JSONHelper.putValue(root, MAX_SCALE, this.getMaxScale());

        JSONHelper.putValue(root, IS_PUBLISHED, this.isPublished());
        JSONHelper.putValue(root, UUID, this.getUuid());

		JSONHelper.putValue(root, TEMPLATE_NAME, this.getTemplateName());
		JSONHelper.putValue(root, TEMPLATE_DESCRIPTION, this.getTemplateDescription());
		JSONHelper.putValue(root, TEMPLATE_TYPE, this.getTemplateType());
		JSONHelper.putValue(root, REQUEST_TEMPLATE, this.getRequestTemplate());
		JSONHelper.putValue(root, RESPONSE_TEMPLATE, this.getResponseTemplate());
		JSONHelper.putValue(root, SELECTION_SLD_STYLE, this.getSelectionSLDStyle());
        JSONHelper.putValue(root, PARSE_CONFIG, this.getParseConfig() != null ? this.getParseConfig().toString() : null);

    	// styles
		final JSONObject styleList = new JSONObject();
		for (final WFSSLDStyle ls : this.getSLDStyles()) {
            if(ls.getId() == null) {
                continue;
            }
	        final JSONObject style = new JSONObject();
	        JSONHelper.putValue(style, ID, ls.getId());
	        JSONHelper.putValue(style, NAME, ls.getName());
	        JSONHelper.putValue(style, SLD_STYLE, ls.getSLDStyle());
	        JSONHelper.putValue(styleList, ls.getId(), style);
		}
		JSONHelper.putValue(root, STYLES, styleList);

		return root;
	}

    public String getAsJSON() {
        return getAsJSONObject().toString();
    }

    public void setDefaults() {
        this.setSRSName("EPSG:3067");
        this.setGMLVersion("3.1.1");
        this.setGML2Separator(false);
        this.setWFSVersion("1.1.0");
        this.setMaxFeatures(2000);
        this.setGeometryNamespaceURI("");
        //this.setOutputFormat("");
        this.setFeatureType("{}");
        this.setSelectedFeatureParams("{}");
        this.setFeatureParamsLocales("{}");
        this.setGeometryType("2d");
        this.setGetMapTiles(true);
        this.setGetHighlightImage(true);
        this.setGetFeatureInfo(true);
        this.setTileRequest(false);
        this.setTileBuffer("{}");
        this.setWps_params("{}");
        this.setMinScale(15000000d);
        this.setMaxScale(1d);
        this.setPublished(false);
    }
    public void setWFS20Defaults() {
        setDefaults();
        this.setGMLVersion("3.2.1");
        this.setGML2Separator(false);
        this.setWFSVersion("2.0.0");
        this.setGMLGeometryProperty("geometry");
        this.setJobType("oskari-feature-engine");
        this.setTileBuffer("{ \"default\" : 1, \"oskari_custom\" : 1}");

    }

	public static String getCache(String layerId) {
		return JedisManager.get(KEY + layerId);
	}


    public  boolean isReverseXY(String epsg) {
        if(this.attributes != null && this.attributes.has("reverseXY")){
            return JSONHelper.getJSONObject(this.attributes, "reverseXY").has(epsg.toUpperCase());
        }
        return false;
    }

    public boolean isLongSrsName(String epsg) {
        if(this.attributes != null && this.attributes.has("longSrsName")){
            return JSONHelper.getJSONObject(this.attributes, "longSrsName").has(epsg.toUpperCase());
        }
        return false;
    }
}