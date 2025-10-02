package org.oskari.control.myfeatures;

import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;

public class ImportMyFeaturesException extends ServiceException {
    private JSONObject options;
    private static final long serialVersionUID = 1L;
    private static final String ERROR_KEY = "error";
    private static final String CAUSE_KEY = "cause";

    public ImportMyFeaturesException(final String message, final Exception e) {
        super(message, e);
    }

    public ImportMyFeaturesException(final String message) {
        super(message);
    }

    public ImportMyFeaturesException (final String message, ErrorType error) {
        super(message);
        this.options = error.getJSON();
    }

    public ImportMyFeaturesException (final String message, ErrorType error, ErrorType additionalError) {
        super(message);
        JSONObject json = error.getJSON();
        JSONHelper.putValue(json, CAUSE_KEY, additionalError.getErrorKey());
        this.options = json;
    }

    public JSONObject getOptions() {
        return options;
    }

    // add or override error key
    public void addErrorKey (ErrorType error){
        if (options == null){
            options = new JSONObject();
        }
        JSONHelper.putValue(options, ERROR_KEY, error.getErrorKey());
    }

    public void addAdditionalError (ErrorType error) {
        if (options == null) {
            options = new JSONObject();
        }
        JSONHelper.putValue(options, CAUSE_KEY, error.getErrorKey());
    }

    public void addContent (InfoType key, String content) {
        if (options == null) {
            options = new JSONObject();
        }
        JSONHelper.putValue(options, key.getInfoKey(), content);
    }

    public void addContent (InfoType key, Map<String, String> map) {
        if (options == null) {
            options = new JSONObject();
        }
        JSONHelper.putValue(options, key.getInfoKey(), new JSONObject(map));
    }

    public void addContent (InfoType key, Set<String> set) {
        if (options == null) {
            options = new JSONObject();
        }
        JSONHelper.put(options, key.getInfoKey(), new JSONArray(set));
    }

    public enum ErrorType {
        // Error codes for frontEnd localization (error or cause)
        PARSER("parser_error"),
        NO_FILE("no_main_file"),
        NO_NAME("no_name"),
        NO_FEATURES("no_features"),
        NO_SOURCE_EPSG("unknown_projection"),
        MULTI_MAIN("multiple_main_file"),
        MULTI_EXT("multiple_extensions"),
        MULTI_FILES("too_many_files"),
        INVALID_EPSG("invalid_epsg"),
        INVALID_ZIP("unzip_failure"),
        INVALID_FEATURE("invalid_feature"),
        INVALID_SIZE("file_over_size"),
        INVALID_FORMAT("format_failure"),
        STORE("unable_to_store_data");

        private final String key;

        private ErrorType (String key) {
            this.key = key;
        }
        private String getErrorKey() {
            return key;
        }
        private JSONObject getJSON () {
            return JSONHelper.createJSONObject(ERROR_KEY, key);
        }
    }

    public enum InfoType {
        // Info for frontEnd additional info parsing
        IGNORED("ignored"),
        FILES("files"),
        PARSER("parser"),
        EXT_MAIN("extensions");

        private final String key;

        private InfoType (String key) {
            this.key = key;
        }
        private String getInfoKey() {
            return key;
        }
    }
}
