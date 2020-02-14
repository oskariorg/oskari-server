package fi.nls.oskari.domain.map.wfs;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles layer's configuration
 * <p>
 * Similar WFSLayerStore class can be found in transport.
 */
public class WFSLayerConfiguration {

    protected final static String LAYER_ID = "layerId";
    protected final static String URL_PARAM = "URL";
    protected final static String USERNAME = "username";
    protected final static String PASSWORD = "password";
    protected final static String LAYER_NAME = "layerName";
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
    protected final static String FEATURE_TYPE = "featureType";
    protected final static String SELECTED_FEATURE_PARAMS = "selectedFeatureParams";
    protected final static String FEATURE_PARAMS_LOCALES = "featureParamsLocales";
    protected final static String GEOMETRY_TYPE = "geometryType";
    protected static final String ATTRIBUTES = "attributes";
    protected final static String MIN_SCALE = "minScale";
    protected final static String MAX_SCALE = "maxScale";
    protected final static String IS_PUBLISHED = "isPublished";
    protected final static String SELECTION_SLD_STYLE = "selectionSLDStyle";
    protected final static String STYLES = "styles";
    protected final static String ID = "id";
    protected final static String NAME = "name";
    protected final static String SLD_STYLE = "SLDStyle";
    private static final Logger log = LogFactory
            .getLogger(WFSLayerConfiguration.class);
    private static final String KEY_DEFAULT = "default";
    private int id = -1;
    private String layerId;
    private String URL;
    private String username;
    private String password;

    private String layerName;

    private String GMLGeometryProperty;
    private String SRSName;
    private String GMLVersion;
    private boolean GML2Separator; // if srs url is in old format (# => :)
    private String WFSVersion;
    private String geometryNamespaceURI;

    private String geometryType;

    private WFSLayerAttributes attrs = new WFSLayerAttributes(new JSONObject());
    private WFSLayerCapabilities caps = new WFSLayerCapabilities(new JSONObject());

    private double minScale;
    private double maxScale;

    private boolean isPublished = false;
    private String uuid;

    private String selectionSLDStyle;

    private List<WFSSLDStyle> SLDStyles = new ArrayList<WFSSLDStyle>(); // id, name, xml

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
        if (caps == null) {
            return null;
        }
        return caps.getGeometryAttribute();
    }

    public void setGMLGeometryProperty(String gMLGeometryProperty) {
        if (caps == null) {
            setCapabilities(new JSONObject());
        }
        caps.setGeometryAttribute(gMLGeometryProperty);
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

    public int getMaxFeatures() {
        return attrs.getMaxFeatures();
    }

    public String getFeatureNamespace() {
        String name = getLayerName();
        if (name == null) {
            return null;
        }
        String[] split = name.split(":");
        if (split.length != 2) {
            return null;
        }
        return split[0];
    }

    public String getFeatureNamespaceURI() {
        return attrs.getNamespaceURL();
    }

    public void setFeatureNamespaceURI(String featureNamespaceURI) {
        if (getAttributes() == null) {
            setAttributes(new JSONObject());
        }
        attrs.setNamespaceURL(featureNamespaceURI);
    }

    public String getGeometryNamespaceURI() {
        return geometryNamespaceURI;
    }

    public void setGeometryNamespaceURI(String geometryNamespaceURI) {
        this.geometryNamespaceURI = geometryNamespaceURI;
    }

    public String getFeatureElement() {
        String name = getLayerName();
        if (name == null) {
            return null;
        }
        String[] split = name.split(":");
        if (split.length == 1) {
            return split[0];
        }
        return split[1];
    }

    public JSONObject getSelectedFeatureParams() {
        JSONObject attributes = attrs.getAttributes();
        if (attributes == null) {
            return new JSONObject();
        }
        JSONObject data = attributes.optJSONObject("data");
        if (data == null) {
            return new JSONObject();
        }
        if (!data.has("filter")) {
            return new JSONObject();
        }
        JSONObject filterObj = data.optJSONObject("filter");
        if (filterObj != null) {
            return filterObj;
        }
        JSONArray filterarray = data.optJSONArray("filter");
        if (filterarray == null) {
            return new JSONObject();
        }
        JSONObject value = new JSONObject();
        JSONHelper.putValue(value, "default", filterarray);
        return value;
    }

    /**
     * Tries to get selected feature params for given key. Defaults to DEFAULT_LOCALE if key has no
     * selected params. If anything fails or feature params is not configured, returns empty list.
     *
     * @param key locale key like "en" or "fi"
     * @return
     */
    public List<String> getSelectedFeatureParams(String key) {
        return attrs.getSelectedAttributes(key);
    }

    public JSONObject getFeatureParamsLocales() {
        JSONObject attributes = attrs.getAttributes();
        if (attributes == null) {
            return new JSONObject();
        }
        JSONObject data = attributes.optJSONObject("data");
        if (data == null) {
            return new JSONObject();
        }
        JSONObject locale = data.optJSONObject("locale");
        if (locale == null) {
            return new JSONObject();
        }
        return locale;
    }

    public List<String> getFeatureParamsLocales(String key) {
        JSONObject locale = attrs.getLocalization(key)
                .orElse(attrs.getLocalization()
                        .orElse(new JSONObject()));
        // return a list with as many labels as there were feature params
        // default to param if localization is not there
        // TODO: refactor rest of the code to use the locale map/JSON instead of 2 separate lists
        return getSelectedFeatureParams(key).stream().map(param -> {
            String label = locale.optString(param);
            if (label == null) {
                return param;
            }
            return label;
        }).collect(Collectors.toList());
    }

    public String getGeometryType() {
        return geometryType;
    }

    public void setGeometryType(String geometryType) {
        this.geometryType = geometryType;
    }

    /**
     * Get wps params for WFS layer eg {input_type:gs_vector}
     * (default is {})
     *
     * @return
     */
    public String getWps_params() {
        if (attrs == null) {
            return "{}";
        }
        return attrs.getWpsParams();
    }


    public JSONObject getAttributes() {
        if (attrs == null) {
            return null;
        }
        return attrs.getAttributes();
    }

    public void setAttributes(JSONObject attributes) {
        this.attrs = new WFSLayerAttributes(attributes);
    }

    public void setAttributes(String attributes) {
        // TODO: merge existing to prevent setter call order to break things
        this.setAttributes(JSONHelper.createJSONObject(attributes));
    }

    public void setCapabilities(JSONObject capabilities) {
        this.caps = new WFSLayerCapabilities(capabilities);
    }
    public void setCapabilities(String capabilities) {
        // TODO: merge existing to prevent setter call order to break things
        this.setCapabilities(JSONHelper.createJSONObject(capabilities));
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean isPublished) {
        this.isPublished = isPublished;
    }

    public String getSelectionSLDStyle() {
        return selectionSLDStyle;
    }

    public void setSelectionSLDStyle(String selectionSLDStyle) {
        this.selectionSLDStyle = selectionSLDStyle;
    }

    public List<WFSSLDStyle> getSLDStyles() {
        if (SLDStyles == null) {
            SLDStyles = new ArrayList<>();
        }
        return SLDStyles;
    }

    public void setSLDStyles(List<WFSSLDStyle> sLDStyles) {
        SLDStyles = sLDStyles;
    }

    public WFSSLDStyle getDefaultSLDStyle() {
        return getSLDStyle(KEY_DEFAULT);
    }

    public WFSSLDStyle getSLDStyle(final String name) {
        if (name == null) {
            return getDefaultSLDStyle();
        }
        for (WFSSLDStyle style : getSLDStyles()) {
            if (name.equals(style.getName())) {
                return style;
            }
        }
        return null;
    }

    public void addSLDStyle(WFSSLDStyle style) {
        if (style == null) {
            return;
        }
        getSLDStyles().add(style);
    }


    public JSONObject getAsJSONObject() {
        final JSONObject root = new JSONObject();

        JSONHelper.putValue(root, LAYER_ID, this.getLayerId());

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

        JSONHelper.putValue(root, GEOMETRY_TYPE, this.getGeometryType());
        JSONHelper.putValue(root, ATTRIBUTES, this.getAttributes() != null ? this.getAttributes().toString() : null);
        JSONHelper.putValue(root, IS_PUBLISHED, this.isPublished());

        JSONHelper.putValue(root, SELECTION_SLD_STYLE, this.getSelectionSLDStyle());

        // styles
        final JSONObject styleList = new JSONObject();
        for (final WFSSLDStyle ls : this.getSLDStyles()) {
            if (ls.getId() == null) {
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
        this.setGMLVersion("3.1.1");
        this.setGML2Separator(false);
        this.setAttributes(JSONHelper.createJSONObject("maxFeatures", 2000));
        this.setGeometryNamespaceURI("");
        this.setGeometryType("2d");
        this.setPublished(false);
    }

    public void setWFS20Defaults() {
        setDefaults();
        this.setGMLVersion("3.2.1");
        this.setGML2Separator(false);
        this.setGMLGeometryProperty("geometry");

    }

    public boolean isReverseXY(String epsg) {
        JSONObject attributes = getAttributes();
        if (attributes == null) {
            return false;
        }
        if (attributes.has("reverseXY")) {
            return JSONHelper.getJSONObject(attributes, "reverseXY").has(epsg.toUpperCase());
        }
        return false;
    }

    public boolean isLongSrsName(String epsg) {
        JSONObject attributes = getAttributes();
        if (attributes == null) {
            return false;
        }
        if (attributes.has("longSrsName")) {
            return JSONHelper.getJSONObject(attributes, "longSrsName").has(epsg.toUpperCase());
        }
        return false;
    }

    public static WFSLayerConfiguration fromOskariLayer(OskariLayer layer) {
        WFSLayerConfiguration conf = new WFSLayerConfiguration();
        conf.setAttributes(layer.getAttributes());
        conf.setCapabilities(layer.getCapabilities());
        conf.setLayerId("" + layer.getId());
        conf.setURL(layer.getUrl());
        conf.setLayerName(layer.getName());
        conf.setUsername(layer.getUsername());
        conf.setPassword(layer.getPassword());
        conf.setSRSName(layer.getSrs_name());
        //conf.setGeometryNamespaceURI();
        //conf.setGeometryType();
        //conf.setPublished();
        return conf;
    }
}