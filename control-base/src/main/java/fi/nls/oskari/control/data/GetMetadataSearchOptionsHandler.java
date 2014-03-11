package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Constructs a JSON object describing the metadata search form selectable values.
 */
@OskariActionRoute("GetMetadataSearchOptions")
public class GetMetadataSearchOptionsHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetMetadataSearchOptionsHandler.class);

    public enum FIELD {

        TYPE("type", true),
        SERVICE_TYPE("serviceType", true),
        SERVICE_NAME("serviceName", false),
        ORGANIZATION("organization", false),
        COVERAGE("coverage", false),
        INSPIRE_THEME("inspireTheme", false),
        KEYWORD("keyword", false),
        TOPIC("topic", false);

        private String name = null;
        private boolean multi = false;

        private FIELD(String name, boolean isMulti) {
            this.name = name;
            this.multi = isMulti;
        }
        public boolean isMulti() {
            return multi;
        }
        public String getName() {
            return name;
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        // root object
        final JSONObject result = new JSONObject();

        // fields for form
        final JSONArray fields = new JSONArray();
        for(FIELD field : FIELD.values()) {
            final JSONObject node = JSONHelper.createJSONObject("field", field.getName());
            JSONHelper.putValue(node, "multi", field.isMulti());
            JSONHelper.putValue(node, "values", getValues(field));
            fields.put(node);
        }

        // write response
        JSONHelper.putValue(result, "fields", fields);
        ResponseHelper.writeResponse(params, result);
    }

    private JSONArray getValues(final FIELD field) {
        JSONArray values = new JSONArray();
        // TODO: generates dummy content for now
        int count = 20;
        if(field.isMulti()) {
            count = 5;
        }
        for(int i = 0; i < count; ++i) {
            final JSONObject value = JSONHelper.createJSONObject("val", "value " + (i + 1));
            values.put(value);
        }
        return values;
    }
}
