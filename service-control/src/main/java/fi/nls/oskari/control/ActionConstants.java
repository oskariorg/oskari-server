package fi.nls.oskari.control;

/**
 * Commonly used constants when handling action routes
 */
public interface ActionConstants {

    String PARAM_ID = "id";
    String PARAM_UUID = "uuid";
    String PARAM_VIEW_ID = "viewId";
    String PARAM_SECURE = "ssl";
    String PARAM_RESET = "reset";

    // used for locale fields as prefix. Params are named 'name_<locale>' f.ex. 'name_en'
    String PARAM_NAME_PREFIX = "name_";

    // projection
    String PARAM_SRS = "srs";
    String PARAM_LAT = "lat";
    String PARAM_LON = "lon";

    // language
    String PARAM_LANGUAGE = "lang";

    // JSON keys
    String KEY_NAME = "name";
    String KEY_URL = "url";
    String KEY_ID = PARAM_ID;
    String KEY_TYPE = "type";
    String KEY_UUID = PARAM_UUID;
    String KEY_USER = "user";
    String KEY_LANG = "lang";
    String KEY_STATE = "state";
    String KEY_CONF = "conf";
    String KEY_CONFIG = "config";
    String KEY_MAPOPTIONS = "mapOptions";
    String KEY_STYLE = "style";
}
