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
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

@OskariActionRoute("GetSearchResult")
public class GetSearchResultHandler extends ActionHandler {

    private static final String PARAM_SEARCH_KEY = "searchKey";
    private static final String PARAM_SEARCH_KEY_ALT = "q";
    private static final String PARAM_EPSG_KEY = "epsg";
    private static final String PARAM_CHANNELIDS_KEY = "channels";
    private static final String PARAM_AUTOCOMPLETE = "autocomplete";
    private static final SearchService searchService = new SearchServiceImpl();

    private String[] channels = new String[0];

    public void init() {
        channels = PropertyUtil.getCommaSeparatedList("actionhandler.GetSearchResult.channels");
    }


    public void handleAction(final ActionParameters params) throws ActionException {
        final String search = params.getHttpParam(PARAM_SEARCH_KEY, params.getHttpParam(PARAM_SEARCH_KEY_ALT));
        final String epsg = params.getHttpParam(PARAM_EPSG_KEY);
        try {
            SearchWorker.validateQuery(search);
        } catch (ServiceException e) {
            // write error message key
            ResponseHelper.writeResponse(params, e.getMessage());
            return;
        }
        final Locale locale = params.getLocale();

        final SearchCriteria sc = new SearchCriteria(params.getUser());

        // default to configuration
        String[] channelIds = channels;
        final String channelParam = params.getHttpParam(PARAM_CHANNELIDS_KEY, "").trim();
        Map<String, Object> options = getOptions(params.getHttpParam("options"));
        options.forEach((key, value) -> {
            if (key.equals("limit")) {
                if (value instanceof String) {
                    sc.setMaxResults(ConversionHelper.getInt((String)value, -1));
                } else if (value instanceof Number) {
                    sc.setMaxResults(((Number) value).intValue());
                }
            } else {
                sc.addParam(key, value);
            }
        });
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

    private Map<String, Object> getOptions(String json) {
        if (json == null) {
            return Collections.emptyMap();
        }
        JSONObject tmp = JSONHelper.createJSONObject(json);
        return JSONHelper.getObjectAsMap(tmp);
    }
}
