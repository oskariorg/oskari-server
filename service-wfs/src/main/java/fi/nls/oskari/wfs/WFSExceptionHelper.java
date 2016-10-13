package fi.nls.oskari.wfs;

/**
 * TransportService exception cases
 */
public interface WFSExceptionHelper {

    public static String ERROR_LEVEL = "error";
    public static String WARNING_LEVEL = "warning";
    public static String ERROR_COMMON_PROCESS_REQUEST_FAILURE = "common_process_request_failure";
    public static String ERROR_SESSION_CREATION_FAILED = "session_creation_failed";
    public static String ERROR_WFSLAYERSTORE_PARSING_FAILED = "WFSLayerStore_parsing_failed";
    public static String ERROR_SESSIONSTORE_PARSING_FAILED = "sessionstore_parsing_failed";
    public static String ERROR_LAYER_SCALE_OUT_OF_RANGE = "layer_scale_out_of_range";
    public static String ERROR_LAYER_ADD_FAILED = "layer_add_failed";
    public static String ERROR_LAYER_REMOVE_FAILED = "layer_remove_failed";
    public static String ERROR_NO_FEATURES_DEFINED = "no_features_defined";
    public static String ERROR_PARAMETERS_NOT_SET = "parameters_not_set";
    public static String ERROR_SET_PROCESS_REQUEST_FAILED = "set_process_request_failed";
    public static String ERROR_CONFIGURATION_FAILED = "wfs_configuring_layer_failed";
    public static String ERROR_NO_PERMISSIONS = "wfs_no_permissions";
    public static String  ERROR_WFS_IMAGE_PARSING_FAILED = "wfs_image_parsing_failed";

    public static String WARNING_GEOMETRY_PARSING_FAILED = "geometry_parsing_failed";
    public static String WARNING_SLDSTYLE_PARSING_FAILED = "sldstyle_parsing_failed";

    public static String ERROR_INVALID_GEOMETRY_PROPERTY = "invalid_geometry_property";
    public static String ERROR_GETFEATURE_PAYLOAD_FAILED = "getfeature_payload_failed";
    public static String ERROR_GETFEATURE_POSTREQUEST_FAILED = "getfeature_postrequest_failed";
    public static String ERROR_GETFEATURE_ENGINE_FAILED = "getfeature_engine_failed";
    public static String ERROR_FEATURE_PARSING = "features_parsing_failed";
    public static String ERROR_COMMON_JOB_FAILURE = "common_job_failure";



}
