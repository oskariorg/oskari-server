package fi.nls.oskari.service.capabilities;

public class CapabilitiesConstants {
    public static final String WFS3_VERSION = "3.0.0";
    public static final String WFS_DEFAULT_VERSION = "1.1.0";
    public static final String WMS_GROUP_LAYER_TYPE = "grouplayer";

    // OskariLayer capabilities json
    public static final String KEY_STYLES = "styles";
    public static final String KEY_STYLE = "style";
    public static final String KEY_SRS = "srs";
    public static final String KEY_MAX_FEATURES = "maxFeatures";
    public static final String KEY_AVAILABLE = "available";
    public static final String KEY_FORMATS = "formats";
    public static final String KEY_FEATURE_OUTPUT_FORMATS = "featureFormats";
    public static final String KEY_GEOM_NAME = "geomName";
    public static final String KEY_LAYER_COVERAGE = "geom";
    public static final String KEY_ISQUERYABLE = "isQueryable";
    public static final String KEY_KEYWORDS = "keywords";
    public static final String KEY_CRS_URI = "crs-uri";
    public static final String KEY_TIMES = "times";
    public static final String KEY_LEGEND = "legend";
    public static final String KEY_VALUE = "value";
    public static final String KEY_TILEMATRIXIDS = "tileMatrixIds";

    //result map/json
    public static final String KEY_TITLE= "title";
    public static final String KEY_LAYERS = "layers";
    public static final String KEY_UNSUPPORTED_LAYERS = "unsupportedLayers";
    public static final String KEY_ERROR_LAYERS = "layersWithErrors";
    public static final String KEY_EXISTING_LAYERS = "existingLayers";
    public static final String KEY_XML = "xml";
    public static final String KEY_WMTS_MATRIXSET = "matrixSets";
    public static final String KEY_WMS_GROUPS = "groups";
    public static final String KEY_WMS_TYPE = "type";
    public static final String KEY_WMS_SELF_LAYER = "self";

    // both
    public static final String KEY_VERSION= "version";

    //xml/collection (lowercase)
    public static final String OUTPUT_FORMAT = "outputformat";
    public static final String GET_FEATURE = "getfeature";
    public static final String COUNT = "countdefault";
    public static final String MAX_FEATURES = "defaultmaxfeatures";
}