package fi.nls.oskari.domain.map.wfs;

import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * {
 *     ...,
 *     "data": {
 *         "filter": {
 *             "default": [
 *                 "kunta_fi",
 *                 "grd_id",
 *                 "id_nro"
 *             ],
 *             "fi": [
 *                 "kunta_fi",
 *                 "grd_id",
 *                 "id_nro"
 *             ],
 *             "sv": [
 *                 "kunta_sv",
 *                 "grd_id",
 *                 "id_nro"
 *             ],
 *             "en": [
 *                 "kunta_en",
 *                 "grd_id",
 *                 "id_nro"
 *             ]
 *         },
 *         "locale": {
 *             "fi": {
 *                 "grd_id": "Ruutu-ID",
 *                 "kunta_fi": "Kunta",
 *                 "id_nro": "ID-nro"
 *             },
 *             "sv": {
 *                 "grd_id": "Rut-ID",
 *                 "kunta_sv": "Kommun",
 *                 "id_nro": "ID-nr"
 *             },
 *             "en": {
 *                 "grd_id": "Grid ID",
 *                 "kunta_en": "Municipality",
 *                 "id_nro": "ID No."
 *             }
 *         },
 *         "noDataValue": -1,
 *         "commonId": "grd_id",
 *         "wpsInputType": "gs_vector"
 *     },
 *     "maxFeatures": 100,
 *     "namespaceURL": "http://oskari.org"
 * }
 */
public class WFSLayerAttributes {
    public static final String KEY_NAMESPACEURL = "namespaceURL";
    public static final String KEY_MAXFEATURES = "maxFeatures";
    public static final String KEY_NO_DATA_VALUE = "noDataValue";
    public static final String KEY_COMMON_ID = "commonId";
    public static final String KEY_WPS_TYPE = "wpsType";

    private Map<String, List<String>> params = new HashMap<>();
    private JSONObject locales = null;
    private String namespaceURL;
    private int maxFeatures = 100000;
    private Integer noDataValue;
    private String commonId;
    private JSONObject attributes;


    public WFSLayerAttributes(JSONObject wfsAttrs) {
        if (wfsAttrs == null) {
            return;
        }
        attributes = wfsAttrs;
        // Preparsing JSON to
        // responses for -1 or 0 for maxfeatures (in xml payloads):
        // Parsing failed for maxFeatures: java.lang.IllegalArgumentException: Value '-1' must be non-negative (0 or above)
        // Parsing failed for maxFeatures: java.lang.IllegalArgumentException: positiveInteger value '0' must be positive.
        maxFeatures = wfsAttrs.optInt("maxFeatures", maxFeatures);
        namespaceURL = wfsAttrs.optString("namespaceURL", namespaceURL);
        JSONObject data = wfsAttrs.optJSONObject("data");
        if (data != null) {
            locales = data.optJSONObject("locale");
            Object filterObj = data.opt("filter");
            if (filterObj instanceof JSONArray) {
                // keys are langs, values are params for language
                List<String> filteredAttrs = JSONHelper.getArrayAsList((JSONArray) filterObj);
                params.put(PropertyUtil.getDefaultLanguage(), filteredAttrs);
            } else if (filterObj instanceof JSONObject) {
                // keys are languages - values are params for language
                JSONObject localizedFilter = (JSONObject) filterObj;
                Iterator<String> languages = localizedFilter.keys();
                while (languages.hasNext()) {
                    String lang = languages.next();
                    params.put(lang, JSONHelper.getArrayAsList(localizedFilter.optJSONArray(lang)));
                }
            }
            if (data.has(KEY_NO_DATA_VALUE)) {
                noDataValue = data.optInt(KEY_NO_DATA_VALUE, -1);
            }
            commonId = data.optString(KEY_COMMON_ID, commonId);
        }
    }
    public Optional<JSONObject> getLocalization() {
        return getLocalization(PropertyUtil.getDefaultLanguage());
    }

    public Optional<JSONObject> getLocalization(String lang) {
        if (locales == null) {
            return Optional.empty();
        }
        JSONObject langLocale = locales.optJSONObject(lang);
        if (langLocale == null || !langLocale.keys().hasNext()) {
            return Optional.empty();
        }
        return Optional.of(langLocale);
    }

    public boolean hasFilter() {
        return !getSelectedAttributes().isEmpty();
    }

    public List<String> getSelectedAttributes() {
        return getSelectedAttributes(PropertyUtil.getDefaultLanguage());
    }

    public List<String> getSelectedAttributes(String lang) {
        return params.getOrDefault(lang,
                params.getOrDefault(PropertyUtil.getDefaultLanguage(), Collections.emptyList()));
    }

    public String getNamespaceURL() {
        return namespaceURL;
    }

    public int getMaxFeatures() {
        return maxFeatures;
    }

    public void setMaxFeatures(int maxFeatures) {
        this.maxFeatures = maxFeatures;
        JSONHelper.putValue(this.attributes, "maxFeatures", maxFeatures);
    }

    public void setNamespaceURL(String namespaceURL) {
        this.namespaceURL = namespaceURL;
        JSONHelper.putValue(this.attributes, "namespaceURL", namespaceURL);
    }
    public Integer getNoDataValue() {
        return noDataValue;
    }

    public String getCommonId() {
        return commonId;
    }

    public JSONObject getAttributes() {
        return attributes;
    }
    public JSONObject getAttributesData() {
        if (attributes == null) {
            return new JSONObject();
        }
        JSONObject data = JSONHelper.getJSONObject(attributes, "data");
        return data == null ? new JSONObject() : data;
    }
}
