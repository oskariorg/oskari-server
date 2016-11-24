package fi.nls.oskari.control.metadata;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.channel.MetadataCatalogueChannelSearchService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Constructs a JSON object describing the metadata search form selectable values.
 * The fields returned are configured with MetadataCatalogueChannelSearchService.fields():
 * search.channel.METADATA_CATALOGUE_CHANNEL.fields=type,serviceType,Title,OrganisationName,Subject,TopicCategory,Language,ResourceLanguage
 * search.channel.METADATA_CATALOGUE_CHANNEL.field.<name>.space.char = ? this replaces wanted value spaces to wanted mark e.g. ?
 */
@OskariActionRoute("GetMetadataSearchOptions")
public class GetMetadataSearchOptionsHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetMetadataSearchOptionsHandler.class);

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final String language = params.getLocale().getLanguage();

        // root object
        final JSONObject result = new JSONObject();

        // fields for form
        final JSONArray fields = new JSONArray();
        for(MetadataField field : MetadataCatalogueChannelSearchService.getFields()) {
            final JSONObject node = JSONHelper.createJSONObject("field", field.getName());
            JSONHelper.putValue(node, "multi", field.isMulti());
            JSONHelper.putValue(node, "shownIf", field.getShownIf());

            JSONHelper.putValue(node, "values", field.getHandler().getOptions(language));
            fields.put(node);
        }

        // write response
        JSONHelper.putValue(result, "fields", fields);
        ResponseHelper.writeResponse(params, result);
    }
}