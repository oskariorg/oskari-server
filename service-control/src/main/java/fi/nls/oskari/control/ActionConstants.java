package fi.nls.oskari.control;

/**
 * Commonly used constants when handling action routes
 */
public interface ActionConstants {

    public static final String PARAM_ID = "id";
    public static final String PARAM_UUID = "uuid";
    public static final String PARAM_VIEW_ID = "viewId";

    // used for locale fields as prefix. Params are named 'name_<locale>' f.ex. 'name_en'
    public static final String PARAM_NAME_PREFIX = "name_";

    // projection
    public static final String PARAM_SRS = "epsg";

    // language
    public static final String PARAM_LANGUAGE = "lang";

    // JSON keys
    public static final String KEY_NAME = "name";
    public static final String KEY_URL = "url";
    public static final String KEY_ID = "id";
    public static final String KEY_USER = "user";
    public static final String KEY_LANG = "lang";
    public static final String KEY_STATE = "state";
    public static final String KEY_CONFIG = "config";
}
