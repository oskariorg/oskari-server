package fi.nls.oskari.service.capabilities;

import fi.nls.oskari.domain.map.wfs.WFSLayerCapabilities;

public class CapabilitiesConstants {
    public static final String WFS3_VERSION = "3.0.0";
    public static final String WFS_DEFAULT_VERSION = "1.1.0";

    // OskariLayer capabilities json
    public static final String KEY_STYLES = "styles";
    public static final String KEY_STYLE = "style";
    public static final String KEY_STYLE_NAME = "name";
    public static final String KEY_STYLE_TITLE = "title";
    public static final String KEY_SRS = "srs";
    public static final String KEY_MAX_FEATURES = "maxFeatures";
    public static final String KEY_AVAILABLE = "available";
    public static final String KEY_FORMATS = "formats";
    public static final String KEY_FEATURE_OUTPUT_FORMATS = "featureFormats";
    public static final String KEY_GEOM_NAME = WFSLayerCapabilities.KEY_GEOMETRYFIELD;
    public static final String KEY_NAMESPACE_URL = WFSLayerCapabilities.KEY_NAMESPACE_URL;
    public static final String KEY_LAYER_COVERAGE = "geom";
    public static final String KEY_METADATA = "metadataUuid";
    public static final String KEY_ISQUERYABLE = "isQueryable";
    public static final String KEY_KEYWORDS = "keywords";
    public static final String KEY_CRS_URI = "crs-uri";
    public static final String KEY_TIMES = "times";
    public static final String KEY_LEGEND = "legend";
    public static final String KEY_VALUE = "value";
    public static final String KEY_TILEMATRIXIDS = "tileMatrixIds";
    public static final String KEY_LAYER_CAPABILITIES = "layerCapabilities";
    public static final String KEY_TYPE_SPECIFIC = "typeSpecific";

    //result map/json
    public static final String KEY_TITLE= "title";
    public static final String KEY_LAYERS = "layers";
    public static final String KEY_UNSUPPORTED_LAYERS = "unsupportedLayers";
    public static final String KEY_ERROR_LAYERS = "layersWithErrors";
    public static final String KEY_EXISTING_LAYERS = "existingLayers";
    public static final String KEY_NO_CAPA_LAYERS = "capabilitiesFailed";
    public static final String KEY_XML = "xml";
    public static final String KEY_WMTS_MATRIXSET = "matrixSets";
    public static final String KEY_WMS_STRUCTURE = "structure";
    public static final String KEY_LAYER_NAME = "name";

    // both
    public static final String KEY_VERSION= "version";

    //xml/collection (lowercase)
    public static final String OUTPUT_FORMAT = "outputformat";
    public static final String GET_FEATURE = "getfeature";
    public static final String COUNT = "countdefault";
    public static final String MAX_FEATURES = "defaultmaxfeatures";
}
