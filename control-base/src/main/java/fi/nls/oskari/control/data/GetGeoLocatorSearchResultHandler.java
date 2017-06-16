package fi.nls.oskari.control.data;

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.SearchWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
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
    private static final String PARAM_COUNTRY = "country";
    private static final String PARAM_FILTER = "filter";
    private static final String PARAM_NORMAL = "normal";
    private static final String PARAM_FUZZY = "fuzzy";
    private static final String PARAM_EXONYM = "exonym";
    private static final String PARAM_EPSG_KEY = "epsg";
    private static final String PARAM_LON = "lon";
    private static final String PARAM_LAT = "lat";
    private static final String PARAM_GEO_NAMES = "geographical_names";
    private static final String PARAM_ADDRESSES = "addresses";

    // New features related fields
    private static final String PARAM_LOCATION_TYPE = "locationtype";
    private static final String PARAM_NAME_LANG = "namelanguage";
    private static final String PARAM_NEAREST = "nearest";
    
    private static final String ELFGEOLOCATOR_CHANNEL = "ELFGEOLOCATOR_CHANNEL";
    private static final String ELFADDRESSLOCATOR_CHANNEL = "ELFADDRESSLOCATOR_CHANNEL";

    private final static Logger log = LogFactory.getLogger(GetGeoLocatorSearchResultHandler.class);

    private String[] channels = new String[0];

    public void init()
    {
        channels = PropertyUtil.getCommaSeparatedList("actionhandler.GetSearchResult.channels");
    }

    public void handleAction(final ActionParameters params) throws ActionException {
        log.debug("in handle action");

        final String term = params.getHttpParam(PARAM_TERM);
        if (term == null || term.equals("")) {
            throw new ActionParamsException("Search string was null");
        }

        final String error = SearchWorker.checkLegalSearch(term);

        if (!SearchWorker.STR_TRUE.equals(error)) {
            // write error message key
            ResponseHelper.writeResponse(params, error);
        } else {
            final Locale locale = params.getLocale();
            final SearchCriteria sc = new SearchCriteria();            

            final String geographical_names = params.getHttpParam(PARAM_GEO_NAMES);
            final String addresses = params.getHttpParam(PARAM_ADDRESSES);
            final String epsg = params.getHttpParam(PARAM_EPSG_KEY);
            
            sc.setSearchString(term);
            sc.setSRS(epsg);  // eg. EPSG:3067
            sc.setLocale(locale.getLanguage());

            sc.addParam(PARAM_REGION, params.getHttpParam(PARAM_REGION, ""));
            sc.addParam(PARAM_COUNTRY, params.getHttpParam(PARAM_COUNTRY, ""));
            sc.addParam(PARAM_FILTER, params.getHttpParam(PARAM_FILTER, "false"));
            sc.addParam(PARAM_NORMAL, params.getHttpParam(PARAM_NORMAL, "false"));
            sc.addParam(PARAM_LON, params.getHttpParam(PARAM_LON, ""));
            sc.addParam(PARAM_LAT, params.getHttpParam(PARAM_LAT, ""));
            sc.addParam(PARAM_ADDRESSES, params.getHttpParam(PARAM_ADDRESSES));
            sc.addParam(PARAM_LOCATION_TYPE, params.getHttpParam(PARAM_LOCATION_TYPE, ""));
            sc.addParam(PARAM_NAME_LANG, params.getHttpParam(PARAM_NAME_LANG, ""));
            sc.addParam(PARAM_NEAREST, params.getHttpParam(PARAM_NEAREST, ""));

            if(isFuzzy(sc)) {
                sc.addParam(PARAM_FUZZY, "true");
            }
            
            for (String channelId : channels) {
                if(geographical_names != null && geographical_names.equals("true") && channelId.equals(ELFGEOLOCATOR_CHANNEL)){
                    log.debug("adding channel: ELFGEOLOCATOR_CHANNEL");
                    sc.addChannel(channelId);
                }
                if(addresses != null && addresses.equals("true") && channelId.equals(ELFADDRESSLOCATOR_CHANNEL)){
                    log.debug("adding channel: ELFADDRESSLOCATOR_CHANNEL");
                    sc.addChannel(channelId);
                }
            }

            final JSONObject result = SearchWorker.doSearch(sc);
            ResponseHelper.writeResponse(params, result);
        }
    }
    
    private boolean isFuzzy(SearchCriteria sc) {
        
        // exclude: term, addresses, geographical_names
        final String[] excludes = {PARAM_TERM, PARAM_ADDRESSES, PARAM_GEO_NAMES};
        
        for (String key : sc.getParams().keySet()) {
            String value = (String) sc.getParam(key);
            
            boolean exclude = false;
            for (String e : excludes) {
                if (e.equals(key)) {
                    exclude = true;
                }
            }
            
            if (!exclude && value != null && value.length() > 0 && !value.equalsIgnoreCase("false")) {
                log.debug("Fuzzy on because: " + key + "/" + value);
                return true;
            } 
        }
        return false;
    }
}
