package flyway.myplaces;

import fi.nls.oskari.domain.map.OskariLayer;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.usercontent.LayerHelper;

/**
 * The label for field "link" on my places layers for english, finnish and swedish
 */
public class V1_0_14__change_link_label extends BaseJavaMigration {

    private static final String NAME = "oskari:my_places";

    public void migrate(Context ignored) throws Exception {
        OskariLayer layer = LayerHelper.getLayerWithName(NAME);
        JSONObject attrs = layer.getAttributes();
        modifyAttributes(attrs);
        layer.setAttributes(attrs);
        LayerHelper.update(layer);
    }

    /**
     * Set up for overriding to make future migration easier
     * @param attrs
     * @throws JSONException
     */
    void modifyAttributes(JSONObject attrs) throws JSONException {
        String field = "link";
        // this repeats digging the locale from attributes multiple times
        // but allows developers to easily adjust other fields and languages as well in the future
        changeLabel(attrs, "en", field, "More information");
        changeLabel(attrs, "fi", field, "Lisätiedot");
        changeLabel(attrs, "sv", field, "Mera information");
    }

    /**
     * Note! This can throw null pointer if the database was modified outside the
     * Flyway migrations and these blocks were removed.
     *
     * But it's better to catch that with failing migration than
     * let the migration die silently
     *
     * @param attrs
     * @return
     * @throws JSONException
     */
    void changeLabel(JSONObject attrs, String lang, String field, String label) throws JSONException {
        JSONObject data = attrs.optJSONObject("data");
        JSONObject locale = data.optJSONObject("locale");
        JSONObject en = locale.optJSONObject(lang);
        en.put(field, label);
    }
}
