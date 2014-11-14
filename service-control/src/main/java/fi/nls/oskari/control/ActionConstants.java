package fi.nls.oskari.control;

/**
 * Commonly used constants when handling action routes
 */
public interface ActionConstants {

    public static final String PARAM_ID = "id";
    public static final String PARAM_UUID = "uuId";
    public static final String PARAM_VIEW_ID = "viewId";

    // used for locale fields as prefix. Params are named 'name_<locale>' f.ex. 'name_en'
    public static final String PARAM_NAME_PREFIX = "name_";

    // projection
    public static final String PARAM_SRS = "epsg";

    // language
    public static final String PARAM_LANGUAGE = "lang";
}
