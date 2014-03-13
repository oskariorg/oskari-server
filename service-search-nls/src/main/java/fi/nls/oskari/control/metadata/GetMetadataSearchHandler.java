package fi.nls.oskari.control.metadata;

import fi.mml.portti.service.search.*;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.channel.MetadataCatalogueChannelSearchService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.util.ServiceFactory;
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
    private static final SearchService service = ServiceFactory.getSearchService();

    private static final String PARAM_USER_INPUT = "search";

    private static final String KEY_RESULTS = "results";
    private static final String KEY_RESULT_ID = "id";
    private static final String KEY_RESULT_NAME = "name";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final SearchCriteria sc = new SearchCriteria();
        final String userInput = params.getHttpParam(PARAM_USER_INPUT);
        final String language = params.getLocale().getLanguage();
        for(MetadataField field : MetadataField.values()) {
            field.getHandler().handleParam(params.getHttpParam(field.getName()), language, sc);
        }
        sc.setSearchString(userInput);

        sc.setLocale(language);
        sc.addChannel(MetadataCatalogueChannelSearchService.ID);

        // root object
        final JSONObject result = new JSONObject();
        final JSONArray results = new JSONArray();

        final Query query = service.doSearch(sc);
        final ChannelSearchResult searchResult = query.findResult(MetadataCatalogueChannelSearchService.ID);
        for(SearchResultItem item : searchResult.getSearchResultItems()) {
            final JSONObject node = JSONHelper.createJSONObject(KEY_RESULT_NAME, item.getTitle());
            JSONHelper.putValue(node, KEY_RESULT_ID, item.getResourceId());
            //JSONHelper.putValue(node, "organization", "organization");
            results.put(node);
        }

        // write response
        JSONHelper.putValue(result, KEY_RESULTS, results);
        ResponseHelper.writeResponse(params, result);
    }
}
