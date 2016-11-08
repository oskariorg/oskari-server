package fi.nls.oskari.control.data;

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.SearchWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

@OskariActionRoute("GetSearchResult")
public class GetSearchResultHandler extends ActionHandler {

    private static final String PARAM_SEARCH_KEY = "searchKey";
    private static final String PARAM_EPSG_KEY = "epsg";
    private static final String PARAM_CHANNELIDS_KEY = "channels";

    private String[] channels = new String[0];

    public void init() {
        channels = PropertyUtil.getCommaSeparatedList("actionhandler.GetSearchResult.channels");
    }


    public void handleAction(final ActionParameters params) throws ActionException {
        final String search = params.getHttpParam(PARAM_SEARCH_KEY);
        if (search == null) {
            throw new ActionParamsException("Search string was null");
        }
        final String epsg = params.getHttpParam(PARAM_EPSG_KEY);

        final String error = SearchWorker.checkLegalSearch(search);

        if (!SearchWorker.STR_TRUE.equals(error)) {
            // write error message key
            ResponseHelper.writeResponse(params, error);
            return;
        }
        final Locale locale = params.getLocale();

        final SearchCriteria sc = new SearchCriteria(params.getUser());
        String[] channelIds = params.getHttpParam(PARAM_CHANNELIDS_KEY, "").split("\\s*,\\s*");

        // if channels defined in request, use them
        if(channelIds.length == 0) {
            // otherwise use configurations, service will add defaults if left empty
            channelIds = channels;
        }
        for (String id :  channelIds) {
            sc.addChannel(id);
        }
        sc.setSearchString(search);
        sc.setSRS(epsg);  // eg. EPSG:3067
        sc.setLocale(locale.getLanguage());

        final JSONObject result = SearchWorker.doSearch(sc);
        ResponseHelper.writeResponse(params, result);
    }
}
