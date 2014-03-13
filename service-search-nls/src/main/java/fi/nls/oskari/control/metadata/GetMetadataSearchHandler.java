package fi.nls.oskari.control.metadata;

import fi.mml.portti.service.search.SearchCriteria;
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
 * Forwards call to search service and returns results as JSON.
 * <pre>
 * {@code
 *   {
 *      "results" : [
 *      {
 *          "name" : "[result name]"
 *          "organization" : "[result producer]",
 *          "id" : "[optional metadata id]"
 *      }
 *      ]
 *   }
 * }
 * </pre>
 */
@OskariActionRoute("GetMetadataSearch")
public class GetMetadataSearchHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetMetadataSearchHandler.class);

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final String userInput = params.getHttpParam("search");
        for(MetadataField field : MetadataField.values()) {
            final String fieldValue = params.getHttpParam(field.getName());
            if(fieldValue != null && !fieldValue.isEmpty() && field.isMulti()) {
                String[] values = fieldValue.split(",");
            }
        }

        SearchCriteria sc = new SearchCriteria();
        //populateMetadataSearchObject(request, sc);
        sc.setSearchString(userInput);
        sc.setLocale(params.getLocale().getLanguage());
        //sc.addChannel(MetadataCatalogueChannelSearchService.ID);

        // root object
        final JSONObject result = new JSONObject();

        // fields for form
        final JSONArray results = new JSONArray();
        for(int i = 0; i < 20; ++i) {
            final JSONObject node = JSONHelper.createJSONObject("name", "name " + i);
            JSONHelper.putValue(node, "organization", "organization " + i);
            if(i % 3 == 0) {
                JSONHelper.putValue(node, "id", "c22da116-5095-4878-bb04-dd7db3a1a341");
            }
            results.put(node);
        }
        // write response
        JSONHelper.putValue(result, "results", results);

        ResponseHelper.writeResponse(params, result);
    }
}
