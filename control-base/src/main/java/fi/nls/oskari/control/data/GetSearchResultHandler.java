package fi.nls.oskari.control.data;

import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchService;
import fi.mml.portti.service.search.SearchServiceImpl;
import fi.nls.oskari.SearchWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

import java.util.Locale;

@OskariActionRoute("GetSearchResult")
public class GetSearchResultHandler extends ActionHandler {

    private static final String PARAM_SEARCH_KEY = "searchKey";
    private static final String PARAM_EPSG_KEY = "epsg";
    private static final String PARAM_CHANNELIDS_KEY = "channels";
    private static final String PARAM_AUTOCOMPLETE = "autocomplete";
    private static final SearchService searchService = new SearchServiceImpl();

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

        // default to configuration
        String[] channelIds = channels;
        final String channelParam = params.getHttpParam(PARAM_CHANNELIDS_KEY, "").trim();

        // if channels defined in request, use channels from request
        if(!channelParam.isEmpty()) {
            channelIds = channelParam.split("\\s*,\\s*");
        }
        // service will add defaults if channels not defined
        for (String id :  channelIds) {
            sc.addChannel(id);
        }
        sc.setSearchString(search);
        sc.setSRS(epsg);  // eg. EPSG:3067
        sc.setLocale(locale.getLanguage());

        if (params.getHttpParam(PARAM_AUTOCOMPLETE, false)) {
            ResponseHelper.writeResponse(params, searchService.doSearchAutocomplete(sc));
        } else {
            ResponseHelper.writeResponse(params, SearchWorker.doSearch(sc));
        }
    }
}
