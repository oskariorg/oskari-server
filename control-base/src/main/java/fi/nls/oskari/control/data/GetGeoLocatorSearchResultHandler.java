package fi.nls.oskari.control.data;

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.SearchWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Get search result of ELF Geolocator request
 * <p/>
 * e.g. request oskari-map?action_route=GetGeoLocatorSearchResult&lang=fi&epsg=EPSG:3035&term=Helsinki&filter=&fuzzy=true&exonym=false"
 */
@OskariActionRoute("GetGeoLocatorSearchResult")
public class GetGeoLocatorSearchResultHandler extends ActionHandler {

    private static final String PARAM_TERM = "term";
    private static final String PARAM_REGION = "region";
    private static final String PARAM_FUZZY = "fuzzy";
    private static final String PARAM_EXONYM = "exonym";
    private static final String PARAM_EPSG_KEY = "epsg";
    private static final String PARAM_LON = "lon";
    private static final String PARAM_LAT = "lat";


    private String[] channels = new String[0];

    public void init() {
        channels = PropertyUtil.getCommaSeparatedList("actionhandler.GetSearchResult.channels");
    }


    public void handleAction(final ActionParameters params) throws ActionException {

        {

            final String search = params.getHttpParam(PARAM_TERM,"foo");
            if (search == null) {
                throw new ActionParamsException("Search string was null");
            }

            final String epsg = params.getHttpParam(PARAM_EPSG_KEY);

            final String error = SearchWorker.checkLegalSearch(search);

            if (!SearchWorker.STR_TRUE.equals(error)) {
                // write error message key
                ResponseHelper.writeResponse(params, error);
            } else {
                final Locale locale = params.getLocale();

                final SearchCriteria sc = new SearchCriteria();
                sc.setSearchString(search);
                sc.setSRS(epsg);  // eg. EPSG:3067

                sc.setLocale(locale.getLanguage());
                sc.setRegion(params.getHttpParam(PARAM_REGION, ""));
                sc.setFuzzy(params.getHttpParam(PARAM_FUZZY, "false"));
                sc.setExonym(params.getHttpParam(PARAM_EXONYM, "false"));
                sc.setLon(params.getHttpParam(PARAM_LON, ""));
                sc.setLat(params.getHttpParam(PARAM_LAT, ""));

                for (String channelId : channels) {
                    sc.addChannel(channelId);
                }
                final JSONObject result = SearchWorker.doSearch(sc);
                ResponseHelper.writeResponse(params, result);
            }
        }
    }
}
