package fi.nls.oskari.control.metadata;

import fi.mml.portti.service.search.*;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.channel.MetadataCatalogueChannelSearchService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import static fi.nls.oskari.control.ActionConstants.*;

/**
 * Forwards call to search service and returns results as JSON.
 * <pre>
 * {@code
 *   {
 *      "results" : [
 *      {
 *          "name" : "[result name]"
 *          "organization" : "[optional result producer]",
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
    private static final SearchService service = OskariComponentManager.getComponentOfType(SearchService.class);

    private static final String PARAM_USER_INPUT = "search";

    private static final String KEY_RESULTS = "results";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final SearchCriteria sc = new SearchCriteria();
        final String language = params.getLocale().getLanguage();
        sc.setLocale(language);
        sc.setSRS(params.getHttpParam(PARAM_SRS));
        for(MetadataField field : MetadataCatalogueChannelSearchService.getFields()) {
            field.getHandler().handleParam(params.getHttpParam(field.getName()), sc);
        }

        final String userInput = params.getHttpParam(PARAM_USER_INPUT);
        sc.setSearchString(userInput);

        sc.addChannel(MetadataCatalogueChannelSearchService.ID);
        // validate will throw exception if we can't make the query
        validateRequest(sc);
        // root object
        final JSONObject result = new JSONObject();
        final JSONArray results = new JSONArray();
        final Query query = service.doSearch(sc);
        final ChannelSearchResult searchResult = query.findResult(MetadataCatalogueChannelSearchService.ID);

        log.debug("done search... now creating json objects");

        for(SearchResultItem item : searchResult.getSearchResultItems()) {
            results.put(item.toJSON());
        }

        // write response
        JSONHelper.putValue(result, KEY_RESULTS, results);
        ResponseHelper.writeResponse(params, result);
    }

    private void validateRequest(final SearchCriteria sc) throws ActionParamsException {
        // check free input field content
        if(sc.getSearchString() != null && !sc.getSearchString().isEmpty()) {
            // ok if user has written anything
            return;
        }
        // check advanced options, NOT OK if we get this far and don't have any selections so throw an exception
        log.debug("No free input, params are:", sc.getParams());
        if(sc.getParams().isEmpty()) {
            throw new ActionParamsException("No search string and no additional selections");
        }
    }
}
