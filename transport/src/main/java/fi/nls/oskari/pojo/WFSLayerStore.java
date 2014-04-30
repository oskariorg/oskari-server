package fi.nls.oskari.pojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;

import fi.nls.oskari.log.Logger;
import fi.nls.oskari.transport.TransportService;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.domain.map.wfs.WFSSLDStyle;


/**
 * Handles layer's configuration
 *
 * Similar WFSLayerConfiguration class can be found in oskari-OGC.
 */
public class WFSLayerStore {
    private static final Logger log = LogFactory.getLogger(WFSLayerStore.class);

    public static final String KEY = "WFSLayer_";

    private static final String LAYER_ID = "layerId";
    private static final String NAME_LOCALES = "nameLocales";
    private static final String SUBTITLE = "subtitle";
    private static final String URL_PARAM = "URL";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private static final String LAYER_NAME = "layerName";

    private static final String GML_GEOMETRY_PROPERTY = "GMLGeometryProperty";
    private static final String SRS_NAME = "SRSName";
    private static final String GML_VERSION = "GMLVersion";
    private static final String GML2_SEPARATOR = "GML2Separator";
    private static final String WFS_VERSION = "WFSVersion";
    private static final String MAX_FEATURES = "maxFeatures";
    private static final String FEATURE_NAMESPACE = "featureNamespace";
    private static final String FEATURE_NAMESPACE_URI = "featureNamespaceURI";
    private static final String GEOMETRY_NAMESPACE_URI = "geometryNamespaceURI";
    private static final String FEATURE_ELEMENT = "featureElement";
    private static final String OUTPUT_FORMAT = "outputFormat";

    private static final String FEATURE_TYPE = "featureType";
    private static final String SELECTED_FEATURE_PARAMS = "selectedFeatureParams";
    private static final String FEATURE_PARAMS_LOCALES = "featureParamsLocales";
    private static final String GEOMETRY_TYPE = "geometryType";
    private static final String GET_MAP_TILES = "getMapTiles";
    private static final String GET_HIGHLIGHT_IMAGE = "getHighlightImage";
    private static final String GET_FEATURE_INFO = "getFeatureInfo";
    private static final String TILE_REQUEST = "tileRequest";
    private final static String TILE_BUFFER = "tileBuffer";
    private static final String WMS_LAYER_ID = "WMSLayerId";
    private static final String CUSTOM_PARSER = "customParser";
    private final static String TEST_LOCATION = "testLocation";
    private final static String TEST_ZOOM = "testZoom";

    private static final String MIN_SCALE = "minScale";
    private static final String MAX_SCALE = "maxScale";

    private static final String TEMPLATE_NAME = "templateName";
    private static final String TEMPLATE_DESCRIPTION = "templateDescription";
    private static final String TEMPLATE_TYPE = "templateType";
    private static final String REQUEST_TEMPLATE = "requestTemplate";
    private static final String RESPONSE_TEMPLATE = "responseTemplate";
    
    private static final String SELECTION_SLD_STYLE = "selectionSLDStyle";

    private static final String STYLES = "styles";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String SLD_STYLE = "SLDStyle";

    private static final String ERROR = "error";
    private static final String DEFAULT_LOCALE = "default";
    
    private static final String JOB_TYPE = "jobType";
    private static final String REQUEST_IMPULSE = "requestImpulse";

    private String layerId;
    private Map<String, Map<String, String>> nameLocales;
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

    private Map<String, String> featureType;
    private Map<String, List<String>> selectedFeatureParams; // if needed?
    private Map<String, List<String>> featureParamsLocales;
    private String geometryType; // 2D/3D
    private boolean getMapTiles; // if normal images are drawn and send
    private boolean getHighlightImage; // if highlight is drawn and send
    private boolean getFeatureInfo; // if feature json is send
    private boolean tileRequest; // if tile requests are made (map request default)
    private Map<String, Double> tileBuffer;
    private String WMSLayerId;
    private boolean customParser;
    private String customParserType;
        private List<Double> testLocation;
    private int testZoom;


    private double minScale;
    private double maxScale;

    private String templateName;
    private String templateDescription;
    private String templateType;
    private String requestTemplate;
    private String responseTemplate;

    private String selectionSLDStyle;

    private Map<String, WFSSLDStyle> styles = new HashMap<String, WFSSLDStyle>(); // name,
    // xml

    // not in JSON
    private CoordinateReferenceSystem crs;

    /**
     * Constructs object without parameters
     */
    public WFSLayerStore() {
        this.layerName = "";
    }

    /**
     * Gets layer id
     *
     * @return layer id
     */
    public String getLayerId() {
        return layerId;
    }

    /**
     * Sets layer id
     *
     * @param layerId
     */
    public void setLayerId(String layerId) {
        this.layerId = layerId;
    }

    /**
     * Gets name locales
     *
     * @return name locales
     */
    public Map<String, Map<String, String>> getNameLocales() {
        return nameLocales;
    }

    /**
     * Sets name locales
     *
     * @param nameLocales
     */
    public void setNameLocales(Map<String, Map<String, String>> nameLocales) {
        this.nameLocales = nameLocales;
    }

    /**
     * Gets URL
     *
     * @return URL
     */
    @JsonProperty("URL")
    public String getURL() {
        return URL;
    }

    /**
     * Sets URL
     *
     * @param URL
     */
    public void setURL(String URL) {
        this.URL = URL;
    }

    /**
     * Gets username
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets username
     *
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets password
     *
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets password
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets layer name
     *
     * @return layer name
     */
    public String getLayerName() {
        return layerName;
    }

    /**
     * Sets layer name
     *
     * @param layerName
     */
    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    /**
     * Gets GML geometry property
     *
     * @return GML geometry property
     */
    @JsonProperty("GMLGeometryProperty")
    public String getGMLGeometryProperty() {
        return GMLGeometryProperty;
    }


    @JsonIgnore
    public String getGMLGeometryPropertyNoNamespace() {
        final String geom = getGMLGeometryProperty();
        if(geom == null) {
            return null;
        }

        String[] split = geom.split(":");
        if(split.length < 2) {
            return geom;
        }
        else {
            return split[1];
        }
    }

    /**
     * Sets GML geometry property
     *
     * @param GMLGeometryProperty
     */
    public void setGMLGeometryProperty(String GMLGeometryProperty) {
        this.GMLGeometryProperty = GMLGeometryProperty;
    }

    /**
     * Gets SRS name
     *
     * @return SRS name
     */
    @JsonProperty("SRSName")
    public String getSRSName() {
        return SRSName;
    }

    /**
     * Sets SRS name
     *
     * @param SRSName
     */
    public void setSRSName(String SRSName) {
        this.SRSName = SRSName;
    }

    /**
     * Gets GML version
     *
     * @return GML version
     */
    @JsonProperty("GMLVersion")
    public String getGMLVersion() {
        return GMLVersion;
    }

    /**
     * Sets GML version
     *
     * @param GMLVersion
     */
    public void setGMLVersion(String GMLVersion) {
        this.GMLVersion = GMLVersion;
    }

    /**
     * Checks if using GML2 separator
     *
     * @return <code>true</code> if using GML2 separator; <code>false</code>
     *         otherwise.
     */
    @JsonProperty("GML2Separator")
    public boolean isGML2Separator() {
        return GML2Separator;
    }

    /**
     * Sets if using GML2 separator
     *
     * @param GML2Separator
     */
    public void setGML2Separator(String GML2Separator) {
        if (GML2Separator.equals("true"))
            this.GML2Separator = true;
        else
            this.GML2Separator = false;
    }

    /**
     * Gets WFS version
     *
     * @return WFS version
     */
    @JsonProperty("WFSVersion")
    public String getWFSVersion() {
        return WFSVersion;
    }

    /**
     * Sets WFS version
     *
     * @param WFSVersion
     */
    public void setWFSVersion(String WFSVersion) {
        this.WFSVersion = WFSVersion;
    }

    /**
     * Gets max features
     *
     * @return max features
     */
    public int getMaxFeatures() {
        return maxFeatures;
    }

    /**
     * Sets max features
     *
     * @param maxFeatures
     */
    public void setMaxFeatures(int maxFeatures) {
        this.maxFeatures = maxFeatures;
    }

    /**
     * Gets feature namespace
     *
     * @return feature namespace
     */
    public String getFeatureNamespace() {
        return featureNamespace;
    }

    /**
     * Sets feature namespace
     *
     * @param featureNamespace
     */
    public void setFeatureNamespace(String featureNamespace) {
        this.featureNamespace = featureNamespace;
    }

    /**
     * Gets feature namespace URI
     *
     * @return feature namespace URI
     */
    @JsonProperty("featureNamespaceURI")
    public String getFeatureNamespaceURI() {
        return featureNamespaceURI;
    }

    /**
     * Sets feature namespace URI
     *
     * @param featureNamespaceURI
     */
    public void setFeatureNamespaceURI(String featureNamespaceURI) {
        this.featureNamespaceURI = featureNamespaceURI;
    }

    /**
     * Gets geometry namespace URI
     *
     * @return geometry namespace URI
     */
    @JsonProperty("geometryNamespaceURI")
    public String getGeometryNamespaceURI() {
        return geometryNamespaceURI;
    }

    /**
     * Sets geometry namespace URI
     *
     * @param geometryNamespaceURI
     */
    public void setGeometryNamespaceURI(String geometryNamespaceURI) {
        this.geometryNamespaceURI = geometryNamespaceURI;
    }

    /**
     * Gets feature element
     *
     * @return feature element
     */
    public String getFeatureElement() {
        return featureElement;
    }

    /**
     * Sets feature element
     *
     * @param featureElement
     */
    public void setFeatureElement(String featureElement) {
        this.featureElement = featureElement;
    }

    /**
     * Gets output format
     * @return output format
     */
    public String getOutputFormat() { return outputFormat; }

    /**
     * Sets output format
     *
     * @param outputFormat
     */
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }

    /**
     * Gets feature type
     *
     * @return feature type
     */
    public Map<String, String> getFeatureType() {
        return featureType;
    }

    /**
     * Sets feature type
     *
     * @param featureType
     */
    public void setFeatureType(Map<String, String> featureType) {
        this.featureType = featureType;
    }

    /**
     * Gets selected feature params
     *
     * @return selected feature params
     */
    public Map<String, List<String>> getSelectedFeatureParams() {
        return selectedFeatureParams;
    }

    /**
     * Gets selected feature params with key
     *
     * @return selected feature params language version, fallback on default if not found then null
     */
    @JsonIgnore
    public List<String> getSelectedFeatureParams(String key) {
        List<String> featureParams = selectedFeatureParams.get(key);
        if(featureParams == null) {
            featureParams = selectedFeatureParams.get(DEFAULT_LOCALE);
        }
        return featureParams;
    }

    /**
     * Sets selected feature params
     *
     * @param selectedFeatureParams
     */
    public void setSelectedFeatureParams(Map<String, List<String>> selectedFeatureParams) {
        this.selectedFeatureParams = selectedFeatureParams;
    }

    /**
     * Gets feature params locales
     *
     * @return feature params locales
     */
    public Map<String, List<String>> getFeatureParamsLocales() {
        return featureParamsLocales;
    }

    /**
     * Gets feature params locales of a key (language)
     *
     * @return feature params locales
     */
    @JsonIgnore
    public List<String> getFeatureParamsLocales(String key) {
        return featureParamsLocales.get(key);
    }

    /**
     * Sets feature params locales
     *
     * @param featureParamsLocales
     */
    public void setFeatureParamsLocales(
            Map<String, List<String>> featureParamsLocales) {
        this.featureParamsLocales = featureParamsLocales;
    }

    /**
     * Gets geometry type
     *
     * @return geometry type
     */
    public String getGeometryType() {
        return geometryType;
    }

    /**
     * Sets geometry type
     *
     * @param geometryType
     */
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

    /**
     * Sets if getting map tiles
     *
     * @param getMapTiles
     */
    public void setGetMapTiles(String getMapTiles) {
        if (getMapTiles.equals("true"))
            this.getMapTiles = true;
        else
            this.getMapTiles = false;
    }

    /**
     * Checks if should get highlight image
     *
     * @return <code>true</code> if should get highlight image; <code>false</code>
     *         otherwise.
     */
    public boolean isGetHighlightImage() {
        return getHighlightImage;
    }

    /**
     * Sets if getting highlight image
     *
     * @param getHighlightImage
     */
    public void setGetHighlightImage(String getHighlightImage) {
        if (getHighlightImage.equals("true"))
            this.getHighlightImage = true;
        else
            this.getHighlightImage = false;
    }

    /**
     * Checks if should get feature info
     *
     * @return <code>true</code> if should get feature info; <code>false</code>
     *         otherwise.
     */
    public boolean isGetFeatureInfo() {
        return getFeatureInfo;
    }

    /**
     * Sets if getting feature info
     *
     * @param getFeatureInfo
     */
    public void setGetFeatureInfo(String getFeatureInfo) {
        if (getFeatureInfo.equals("true"))
            this.getFeatureInfo = true;
        else
            this.getFeatureInfo = false;
    }

    /**
     * Checks if should make tile requests
     *
     * @return <code>true</code> if should make tile requests;
     *         <code>false</code> otherwise.
     */
    public boolean isTileRequest() {
        return tileRequest;
    }

    /**
     * Sets if tile request
     *
     * @param tileRequest
     */
    public void setTileRequest(String tileRequest) {
        if (tileRequest.equals("true"))
            this.tileRequest = true;
        else
            this.tileRequest = false;
    }

    /**
     * Gets tile buffer
     *
     * @return tile buffer
     */
    public Map<String, Double> getTileBuffer() {
        return tileBuffer;
    }

    /**
     * Sets tile buffer
     *
     * @param tileBuffer
     */
    public void setTileBuffer(Map<String, Double> tileBuffer) {
        this.tileBuffer = tileBuffer;
    }

    /**
     * Gets WMS layer id
     *
     * @return WMS layer id
     */
    @JsonProperty("WMSLayerId")
    public String getWMSLayerId() {
        return WMSLayerId;
    }

    /**
     * Sets WMS layer id
     *
     * @param WMSLayerId
     */
    public void setWMSLayerId(String WMSLayerId) {
        this.WMSLayerId = WMSLayerId;
    }

    /**
     * Checks if should be parsed with custom parser
     *
     * @return <code>true</code> if should;
     *         <code>false</code> otherwise.
     */
    public boolean isCustomParser() {
        return customParser;
    }
    
    public String getCustomParserType() {
        return customParserType;
    }

    /**
     * Sets if custom parsed
     *
     * @param customParser
     */
    public void setCustomParser(String customParser) {
        if (customParser.equals("true"))
            this.customParser = true;
        else
            this.customParser = false;
        customParserType =customParser; 
    }

    /**
     * Gets test location
     *
     * @return test location
     */
    public ArrayList<Double> getTestLocation() {
        return (ArrayList<Double>) testLocation;
    }

    /**
     * Sets test location
     *
     * @param testLocation
     */
    public void setTestLocation(List<Double> testLocation) {
        this.testLocation = testLocation;
    }

    /**
     * Gets test zoom
     *
     * @return test zoom
     */
    public int getTestZoom() {
        return testZoom;
    }

    /**
     * Sets test zoom
     *
     * @param testZoom
     */
    public void setTestZoom(int testZoom) {
        this.testZoom = testZoom;
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
        if (templateType.equals("null"))
            this.templateType = null;
        else
            this.templateType = templateType;
    }

    /**
     * Gets request template
     *
     * @return request template
     */
    public String getRequestTemplate() {
        return requestTemplate;
    }

    /**
     * Sets request template
     *
     * @param requestTemplate
     */
    public void setRequestTemplate(String requestTemplate) {
        this.requestTemplate = requestTemplate;
    }

    /**
     * Gets response template
     *
     * @return response template
     */
    public String getResponseTemplate() {
        return responseTemplate;
    }

    /**
     * Sets response template
     *
     * @param responseTemplate
     */
    public void setResponseTemplate(String responseTemplate) {
        this.responseTemplate = responseTemplate;
    }

    /**
     * Gets selection SLD style
     *
     * @return selection SLD style
     */
    public String getSelectionSLDStyle() {
        return selectionSLDStyle;
    }

    /**
     * Sets selection SLD style
     *
     * @param selectionSLDStyle
     */
    public void setSelectionSLDStyle(String selectionSLDStyle) {
        this.selectionSLDStyle = selectionSLDStyle;
    }

    /**
     * Gets styles
     *
     * @return styles
     */
    public Map<String, WFSSLDStyle> getStyles() {
        return styles;
    }

    /**
     * Sets styles
     *
     * @param styles
     */
    public void setStyles(Map<String, WFSSLDStyle> styles) {
        this.styles.clear();
        for (Map.Entry<String, WFSSLDStyle> entry : styles.entrySet()) {
            if (entry.getValue().getSLDStyle() != null
                    && !entry.getValue().getSLDStyle().equals("")) {
                this.styles.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Gets CRS
     *
     * @return crs
     */
    @JsonIgnore
    public CoordinateReferenceSystem getCrs() {
        if (this.crs == null) {
            try {
                this.crs = CRS.decode(this.getSRSName());
            } catch (FactoryException e) {
                log.error(e, "CRS decoding failed");
            }
        }
        return this.crs;
    }

    /**
     * Transforms object to JSON String
     *
     * @return JSON String
     */
    @JsonIgnore
    public String getAsJSON() {
        try {
            return TransportService.mapper.writeValueAsString(this);
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
     * Saves object to redis
     */
    public void save() {
        JedisManager.setex(KEY + this.layerId, JedisManager.EXPIRY_TIME_DAY, getAsJSON()); // expire in 1 day
    }

    /**
     * Transforms JSON String to object
     *
     * @param json
     * @return object
     */
    @JsonIgnore
    public static WFSLayerStore setJSON(String json) throws IOException {
        WFSLayerStore store = new WFSLayerStore();
        Map<String, String> locale = null;
        Map<String, Map<String, String>> nameLocales = new HashMap<String, Map<String, String>>();
        Map<String, String> featureTypes = new HashMap<String, String>();
        Map<String, List<String>> selectedFeatureParams = new HashMap<String, List<String>>();
        Map<String, List<String>> featureParamsLocales = new HashMap<String, List<String>>();
        Map<String, Double> tileBuffers = new HashMap<String, Double>();
        Map<String, WFSSLDStyle> SLDStyles = new HashMap<String, WFSSLDStyle>();
        WFSSLDStyle SLDStyle = null;

        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createJsonParser(json);
        String fieldName = null;
        String valueName = null;
        parser.nextToken();
        if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Configuration is not an object!");
        }
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            fieldName = parser.getCurrentName();
            parser.nextToken();
            if (fieldName == null) {
                break;
            } else if (ERROR.equals(fieldName)) {
                return null;
            } else if (LAYER_ID.equals(fieldName)) {
                store.setLayerId(parser.getText());
            } else if (NAME_LOCALES.equals(fieldName)) {
                if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        String localeName = parser.getCurrentName();
                        locale = new HashMap<String, String>();
                        parser.nextToken();
                        if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                            while (parser.nextToken() != JsonToken.END_OBJECT) {
                                valueName = parser.getCurrentName();
                                if (NAME.equals(valueName)) {
                                    locale.put(valueName, parser.getText());
                                } else if (SUBTITLE.equals(valueName)) {
                                    locale.put(valueName, parser.getText());
                                } else {
                                    throw new IllegalStateException(
                                            "Unrecognized value in layers '"
                                                    + valueName + "'!");
                                }
                            }
                        }
                        nameLocales.put(localeName, locale);
                    }
                }
                store.setNameLocales(nameLocales);
            } else if (URL_PARAM.equals(fieldName)) {
                store.setURL(parser.getText());
            } else if (USERNAME.equals(fieldName)) {
                store.setUsername(parser.getText());
            } else if (PASSWORD.equals(fieldName)) {
                store.setPassword(parser.getText());
            }

            else if (LAYER_NAME.equals(fieldName)) {
                store.setLayerName(parser.getText());
            }

            else if (GML_GEOMETRY_PROPERTY.equals(fieldName)) {
                store.setGMLGeometryProperty(parser.getText());
            } else if (SRS_NAME.equals(fieldName)) {
                store.setSRSName(parser.getText());
            } else if (GML_VERSION.equals(fieldName)) {
                store.setGMLVersion(parser.getText());
            } else if (GML2_SEPARATOR.equals(fieldName)) {
                store.setGML2Separator(parser.getText());
            } else if (WFS_VERSION.equals(fieldName)) {
                store.setWFSVersion(parser.getText());
            } else if (MAX_FEATURES.equals(fieldName)) {
                store.setMaxFeatures(parser.getIntValue());
            } else if (FEATURE_NAMESPACE.equals(fieldName)) {
                store.setFeatureNamespace(parser.getText());
            } else if (FEATURE_NAMESPACE_URI.equals(fieldName)) {
                store.setFeatureNamespaceURI(parser.getText());
            } else if (GEOMETRY_NAMESPACE_URI.equals(fieldName)) {
                store.setGeometryNamespaceURI(parser.getText());
            } else if (FEATURE_ELEMENT.equals(fieldName)) {
                store.setFeatureElement(parser.getText());
            } else if (OUTPUT_FORMAT.equals(fieldName)) {
                store.setOutputFormat(parser.getText());
            } else if (FEATURE_TYPE.equals(fieldName)) {
                if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        String typeName = parser.getCurrentName();
                        parser.nextToken();
                        featureTypes.put(typeName, parser.getText());
                    }
                }
                store.setFeatureType(featureTypes);
            } else if (SELECTED_FEATURE_PARAMS.equals(fieldName)) {
                if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        String localeName = parser.getCurrentName();
                        List<String> featureParams = new ArrayList<String>();
                        parser.nextToken();
                        if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                featureParams.add(parser.getText());
                            }
                        }
                        selectedFeatureParams.put(localeName, featureParams);
                    }
                }
                store.setSelectedFeatureParams(selectedFeatureParams);
            } else if (FEATURE_PARAMS_LOCALES.equals(fieldName)) {
                if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        String localeName = parser.getCurrentName();
                        List<String> paramsLocale = new ArrayList<String>();
                        parser.nextToken();

                        if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                paramsLocale.add(parser.getText());
                            }
                        }
                        featureParamsLocales.put(localeName, paramsLocale);
                    }
                }
                store.setFeatureParamsLocales(featureParamsLocales);
            } else if (GEOMETRY_TYPE.equals(fieldName)) {
                store.setGeometryType(parser.getText());
            } else if (GET_MAP_TILES.equals(fieldName)) {
                store.setGetMapTiles(parser.getText());
            } else if (GET_HIGHLIGHT_IMAGE.equals(fieldName)) {
                store.setGetHighlightImage(parser.getText());
            } else if (GET_FEATURE_INFO.equals(fieldName)) {
                store.setGetFeatureInfo(parser.getText());
            } else if (TILE_REQUEST.equals(fieldName)) {
                store.setTileRequest(parser.getText());
            } else if (TILE_BUFFER.equals(fieldName)) {
                if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        String styleName = parser.getCurrentName();
                        parser.nextToken();
                        tileBuffers.put(styleName, parser.getValueAsDouble());
                    }
                }
                store.setTileBuffer(tileBuffers);
            } else if (WMS_LAYER_ID.equals(fieldName)) {
                store.setWMSLayerId(parser.getText());
            } else if (CUSTOM_PARSER.equals(fieldName)) {
                store.setCustomParser(parser.getText());
            } else if (TEST_LOCATION.equals(fieldName)) {
                List<Double> bbox = new ArrayList<Double>();
                if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        bbox.add(parser.getValueAsDouble());
                    }
                }
                store.setTestLocation(bbox);
            } else if (TEST_ZOOM.equals(fieldName)) {
                store.setTestZoom(parser.getIntValue());
            }

            else if (MIN_SCALE.equals(fieldName)) {
                store.setMinScale(parser.getValueAsDouble());
            } else if (MAX_SCALE.equals(fieldName)) {
                store.setMaxScale(parser.getValueAsDouble());
            }

            else if (TEMPLATE_NAME.equals(fieldName)) {
                store.setTemplateName(parser.getText());
            } else if (TEMPLATE_DESCRIPTION.equals(fieldName)) {
                store.setTemplateDescription(parser.getText());
            } else if (TEMPLATE_TYPE.equals(fieldName)) {
                store.setTemplateType(parser.getText());
            } else if (REQUEST_TEMPLATE.equals(fieldName)) {
                store.setRequestTemplate(parser.getText());
            } else if (RESPONSE_TEMPLATE.equals(fieldName)) {
                store.setResponseTemplate(parser.getText());
            } else if (SELECTION_SLD_STYLE.equals(fieldName)) {
                store.setSelectionSLDStyle(parser.getText());
            } else if (STYLES.equals(fieldName)) {
                if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        SLDStyle = new WFSSLDStyle();
                        parser.nextToken();
                        if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                            while (parser.nextToken() != JsonToken.END_OBJECT) {
                                valueName = parser.getCurrentName();
                                if (ID.equals(valueName)) {
                                    SLDStyle.setId(parser.getText());
                                } else if (NAME.equals(valueName)) {
                                    SLDStyle.setName(parser.getText());
                                } else if (SLD_STYLE.equals(valueName)) {
                                    SLDStyle.setSLDStyle(parser.getText());
                                } else {
                                    throw new IllegalStateException(
                                            "Unrecognized value in layers '"
                                                    + valueName + "'!");
                                }
                            }
                        }
                        SLDStyles.put(SLDStyle.getName(), SLDStyle);
                    }
                }
                store.setStyles(SLDStyles);
            } else {
                throw new IllegalStateException("Unrecognized field '"
                        + fieldName + "'!");
            }
        }
        parser.close();



        return store;
    }

    /**
     * Gets saved layer from redis
     *
     * @param layerId
     * @return layer as JSON String
     */
    @JsonIgnore
    public static String getCache(String layerId) {
        return JedisManager.get(KEY + layerId);
    }
}
