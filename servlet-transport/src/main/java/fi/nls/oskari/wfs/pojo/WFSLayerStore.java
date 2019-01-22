package fi.nls.oskari.wfs.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.domain.map.wfs.WFSSLDStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Handles layer's configuration
 *
 * Similar WFSLayerConfiguration class can be found in oskari-OGC.
 */
public class WFSLayerStore extends WFSLayerConfiguration {
    private static final Logger log = LogFactory.getLogger(WFSLayerStore.class);

    private static final String ERROR = "error";
    public static ObjectMapper mapper = new ObjectMapper();

    // not in JSON
    private CoordinateReferenceSystem crs;

    /**
     * Constructs object without parameters
     */
    public WFSLayerStore() {
        this.setLayerName("");
    }

    /**
     * Gets URL
     *
     * @return URL
     */
    @JsonProperty("URL")
    public String getURL() {
        return super.getURL();
    }

    @JsonIgnore
    public String getWps_params() {
        // ignore this on save
        return super.getWps_params();
    }

    /**
     * Gets GML geometry property
     *
     * @return GML geometry property
     */
    @JsonProperty("GMLGeometryProperty")
    public String getGMLGeometryProperty() {
        return super.getGMLGeometryProperty();
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
     * Gets SRS name
     *
     * @return SRS name
     */
    @JsonProperty("SRSName")
    public String getSRSName() {
        return super.getSRSName();
    }

    /**
     * Gets GML version
     *
     * @return GML version
     */
    @JsonProperty("GMLVersion")
    public String getGMLVersion() {
        return super.getGMLVersion();
    }

    /**
     * Checks if using GML2 separator
     *
     * @return <code>true</code> if using GML2 separator; <code>false</code>
     *         otherwise.
     */
    @JsonProperty("GML2Separator")
    public boolean isGML2Separator() {
        return super.isGML2Separator();
    }

    /**
     * Gets WFS version
     *
     * @return WFS version
     */
    @JsonProperty("WFSVersion")
    public String getWFSVersion() {
        return super.getWFSVersion();
    }

    /**
     * Gets feature namespace URI
     *
     * @return feature namespace URI
     */
    @JsonProperty("featureNamespaceURI")
    public String getFeatureNamespaceURI() {
        return super.getFeatureNamespaceURI();
    }

    /**
     * Gets geometry namespace URI
     *
     * @return geometry namespace URI
     */
    @JsonProperty("geometryNamespaceURI")
    public String getGeometryNamespaceURI() {
        return super.getGeometryNamespaceURI();
    }

    /**
     * Gets WMS layer id
     *
     * @return WMS layer id
     */
    @JsonProperty("WMSLayerId")
    public String getWMSLayerId() {
        return super.getWMSLayerId();
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
                //TODO set Crs according to axis orientation
                this.crs = CRS.decode(this.getSRSName(),true);
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
/*
    @JsonIgnore
    public static WFSLayerStore setJSONWithoutStreamingParser(String json) throws IOException {
        WFSLayerStore store = new WFSLayerStore();
        Map<String,Object> parsed = mapper.readValue(json, Map.class);
        if(parsed.containsKey(ERROR)) {
            return null;
        }
        store.setLayerId((String) parsed.get(LAYER_ID));
        store.setURL((String) parsed.get(URL_PARAM));
        store.setUsername((String) parsed.get(USERNAME));
        store.setPassword((String) parsed.get(PASSWORD));

        store.setLayerName((String) parsed.get(LAYER_NAME));

        store.setGMLGeometryProperty((String) parsed.get(GML_GEOMETRY_PROPERTY));
        store.setSRSName((String) parsed.get(SRS_NAME));
        store.setGMLVersion((String) parsed.get(GML_VERSION));
        store.setGML2Separator((Boolean) parsed.get(GML2_SEPARATOR));

        store.setWFSVersion((String) parsed.get(WFS_VERSION));
        store.setMaxFeatures((Integer) parsed.get(MAX_FEATURES));

        store.setFeatureNamespace((String) parsed.get(FEATURE_NAMESPACE));
        store.setFeatureNamespaceURI((String) parsed.get(FEATURE_NAMESPACE_URI));
        store.setGeometryNamespaceURI((String) parsed.get(GEOMETRY_NAMESPACE_URI));

        store.setFeatureElement((String) parsed.get(FEATURE_ELEMENT));
        store.setOutputFormat((String) parsed.get(OUTPUT_FORMAT));
        //FEATURE_TYPE and the rest missing
        return store;
    }
*/
    /**
     * Transforms JSON String to object
     *
     * @param json
     * @return object
     */
    @JsonIgnore
    public static WFSLayerStore setJSON(String json) throws IOException {
        WFSLayerStore store = new WFSLayerStore();

        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(json);
        parser.nextToken();
        if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Configuration is not an object!");
        }
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            final String fieldName = parser.getCurrentName();
            parser.nextToken();
            if (fieldName == null) {
                break;
            } else if (ERROR.equals(fieldName)) {
                return null;
            } else if (LAYER_ID.equals(fieldName)) {
                store.setLayerId(parser.getText());
            } else if (LAYER_FRIENDLY_NAME.equals(fieldName)) {
                // skip, this is just so we get the UI name in Redis for easier debugging
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
                store.setGML2Separator(parser.getValueAsBoolean());
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
                        store.addFeatureType(typeName, parser.getText());
                    }
                }
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
                        store.addSelectedFeatureParams(localeName, featureParams);
                    }
                }
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
                        store.addFeatureParamsLocales(localeName, paramsLocale);
                    }
                }
            }  else if (GEOMETRY_TYPE.equals(fieldName)) {
                store.setGeometryType(parser.getText());
            } else if (GET_MAP_TILES.equals(fieldName)) {
                store.setGetMapTiles(parser.getValueAsBoolean());
            } else if (GET_HIGHLIGHT_IMAGE.equals(fieldName)) {
                store.setGetHighlightImage(parser.getValueAsBoolean());
            } else if (GET_FEATURE_INFO.equals(fieldName)) {
                store.setGetFeatureInfo(parser.getValueAsBoolean());
            } else if (TILE_REQUEST.equals(fieldName)) {
                store.setTileRequest(parser.getValueAsBoolean());
            } else if (TILE_BUFFER.equals(fieldName)) {
                if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        String styleName = parser.getCurrentName();
                        parser.nextToken();
                        store.addTileBuffer(styleName, parser.getValueAsDouble());
                    }
                }
            } else if (WMS_LAYER_ID.equals(fieldName)) {
                store.setWMSLayerId(parser.getText());
            } else if (JOB_TYPE.equals(fieldName)) {
                store.setJobType(parser.getText());
            }
            else if (MIN_SCALE.equals(fieldName)) {
                store.setMinScale(parser.getValueAsDouble());
            } else if (MAX_SCALE.equals(fieldName)) {
                store.setMaxScale(parser.getValueAsDouble());
            }

            else if (IS_PUBLISHED.equals(fieldName)) {
                store.setPublished(parser.getValueAsBoolean());
            } else if (UUID.equals(fieldName)) {
                store.setUuid(parser.getText());
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
                        final WFSSLDStyle SLDStyle = new WFSSLDStyle();
                        parser.nextToken();
                        if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                            while (parser.nextToken() != JsonToken.END_OBJECT) {
                                final String valueName = parser.getCurrentName();
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
                        store.addSLDStyle(SLDStyle);
                    }
                }
            } else if (PARSE_CONFIG.equals(fieldName)) {
                    store.setParseConfig(parser.getText());
             }else if (ATTRIBUTES.equals(fieldName)) {
                store.setAttributes(parser.getText());
            } else {
                log.warn("Unrecognized field while parsing layer JSON:", fieldName);
                // exception is thrown since the parser state should be fixed here if we don't
                // maybe calling parser.nextToken() might fix it but it seems not to be working
                // if the field has JSON structure inside.
                throw new IllegalStateException("Unrecognized field '" + fieldName + "'!");
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
        return JedisManager.get(KEY + layerId, false);
    }
    /**
     * Gets saved layer from redis
     * throws new runtime exception, if any exception found
     *
     * @param layerId
     * @return string
     */
    @JsonIgnore
    public static String getCacheNecessary(String layerId) {
        return JedisManager.getNecessary(KEY + layerId);
    }
}
