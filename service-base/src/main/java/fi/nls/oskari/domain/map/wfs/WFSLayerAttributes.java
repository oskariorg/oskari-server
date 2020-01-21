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
 *         }
 *     },
 *     "maxFeatures": 100,
 *     "namespaceURL": "http://oskari.org"
 * }
 */
public class WFSLayerAttributes {

    private Map<String, List<String>> params = new HashMap<>();
    private JSONObject locales = null;
    private String namespaceURL;
    private int maxFeatures = -1;
    private JSONObject attributes;

    public WFSLayerAttributes(JSONObject wfsAttrs) {
        if(wfsAttrs == null) {
            return;
        }
        attributes = wfsAttrs;
        // Preparse
        maxFeatures = wfsAttrs.optInt("maxFeatures", 1);
        namespaceURL = wfsAttrs.optString("namespaceURL");
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
        }
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
    }

    public JSONObject getAttributes() {
        return attributes;
    }
}
