package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.util.ELFGeoLocatorParser;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.net.URLEncoder;
import java.util.Locale;

/**
 * Search channel for ELF Geolocator requests
 */
@Oskari(ELFGeoLocatorSearchChannel.ID)
public class ELFGeoLocatorSearchChannel extends SearchChannel {

    private Logger log = LogFactory.getLogger(this.getClass());
    private String serviceURL = null;

    public static final String ID = "ELFGEOLOCATOR_CHANNEL";
    private static final String PROPERTY_SERVICE_URL = "search.channel.ELFGEOLOCATOR_CHANNEL.service.url";

    public static final String KEY_LANG_HOLDER = "_LANG_";
    public static final String KEY_LATITUDE_HOLDER = "_LATITUDE_";
    public static final String KEY_LONGITUDE_HOLDER = "_LONGITUDE_";
    public static final String KEY_PLACE_HOLDER = "_PLACE_HOLDER_";
    public static final String KEY_AU_HOLDER = "_AU_HOLDER_";
    public static final String RESPONSE_CLEAN = "<?xml version='1.0' encoding='UTF-8'?>";
    public static final String REQUEST_REVERSEGEOCODE_TEMPLATE = "?SERVICE=WFS&REQUEST=ReverseGeocode&LAT=_LATITUDE_&LON=_LONGITUDE_&LANGUAGE=_LANG_";
    public static final String REQUEST_GETFEATUREAU_TEMPLATE = "?SERVICE=WFS&VERSION=1.1.0&REQUEST=GetFeatureInAu&NAME=_PLACE_HOLDER_&AU=_AU_HOLDER_&LANGUAGE=_LANG_";
    public static final String REQUEST_FUZZY_TEMPLATE = "?SERVICE=WFS&VERSION=1.1.0&REQUEST=FuzzyNameSearch&LANGUAGE=_LANG_&NAME=";
    public static final String REQUEST_GETFEATURE_TEMPLATE = "?SERVICE=WFS&REQUEST=GetFeature&NAMESPACE=xmlns%28iso19112=http://www.isotc211.org/19112%29&TYPENAME=SI_LocationInstance&Version=1.1.0&MAXFEATURES=10&language=_LANG_&FILTER=%3Cogc:Filter%20xmlns:ogc=%22http://www.opengis.net/ogc%22%20xmlns:iso19112=%22http://www.isotc211.org/19112%22%3E%3Cogc:PropertyIsEqualTo%3E%3Cogc:PropertyName%3Eiso19112:alternativeGeographicIdentifiers/iso19112:alternativeGeographicIdentifier/iso19112:name%3C/ogc:PropertyName%3E%3Cogc:Literal%3E_PLACE_HOLDER_%3C/ogc:Literal%3E%3C/ogc:PropertyIsEqualTo%3E%3C/ogc:Filter%3E";
    private static final String PARAM_TERM = "term";
    private static final String PARAM_REGION = "region";
    private static final String PARAM_FUZZY = "fuzzy";
    private static final String PARAM_EXONYM = "exonym";
    private static final String PARAM_EPSG_KEY = "epsg";
    private static final String PARAM_LON = "lon";
    private static final String PARAM_LAT = "lat";

    private ELFGeoLocatorParser elfParser = new ELFGeoLocatorParser();

    @Override
    public void init() {
        super.init();
        serviceURL = PropertyUtil.getOptional(PROPERTY_SERVICE_URL);
        log.debug("ServiceURL set to " + serviceURL);
    }

    /**
     * Returns the search raw results.
     *
     * @param searchCriteria Search criteria.
     * @return Result data in JSON format.
     * @throws Exception
     */
    private String getData(SearchCriteria searchCriteria) throws Exception {
        log.debug("getData");
    	if (serviceURL == null) {
            log.warn("ServiceURL not configured. Add property with key", PROPERTY_SERVICE_URL);
            return null;
        }

        // Language
        Locale locale = new Locale(searchCriteria.getLocale());
        String lang3 = locale.getISO3Language();

        StringBuffer buf = new StringBuffer(serviceURL);
        if (hasParam(searchCriteria, PARAM_LON) && hasParam(searchCriteria, PARAM_LAT)) {
            // reverse geocoding
            // Transform lon,lat
            String[] lonlat = elfParser.transformLonLat(searchCriteria.getParam(PARAM_LON).toString(), searchCriteria.getParam(PARAM_LAT).toString(), searchCriteria.getSRS());
            if (lonlat == null) {
                log.warn("Invalid lon/lat coordinates ", searchCriteria.getParam(PARAM_LON).toString(), " ", searchCriteria.getParam(PARAM_LAT).toString() );
                return null;
            }
            String request = REQUEST_REVERSEGEOCODE_TEMPLATE.replace(KEY_LATITUDE_HOLDER, lonlat[1] );
            request = request.replace(KEY_LONGITUDE_HOLDER, lonlat[0] );
            request = request.replace(KEY_LANG_HOLDER, lang3);
            buf.append(request);

        } else if (hasParam(searchCriteria, PARAM_REGION)) {
            // Exact search limited to AU region - case sensitive - no fuzzy support
            String request = REQUEST_GETFEATUREAU_TEMPLATE.replace(KEY_PLACE_HOLDER, URLEncoder.encode(searchCriteria.getSearchString(), "UTF-8"));
            request = request.replace(KEY_AU_HOLDER, URLEncoder.encode(searchCriteria.getParam(PARAM_REGION).toString(), "UTF-8"));
            request = request.replace(KEY_LANG_HOLDER, lang3);
            buf.append(request);
        } else if (hasParam(searchCriteria, PARAM_FUZZY) && searchCriteria.getParam(PARAM_FUZZY).toString().equals("true")) {
            // Fuzzy search
            buf.append(REQUEST_FUZZY_TEMPLATE.replace(KEY_LANG_HOLDER, lang3));
            buf.append(URLEncoder.encode(searchCriteria.getSearchString(), "UTF-8"));
        } else {
            // Exact search - case sensitive
            String request = REQUEST_GETFEATURE_TEMPLATE.replace(KEY_LANG_HOLDER, lang3);
            buf.append(request.replace(KEY_PLACE_HOLDER, URLEncoder.encode(searchCriteria.getSearchString(), "UTF-8")));
        }

        return IOHelper.readString(getConnection(buf.toString()));
    }

    /**
     * Check if criteria has named extra parameter and it's not empty
     * @param sc
     * @param param
     * @return
     */
    private boolean hasParam(SearchCriteria sc, final String param) {
        final Object obj = sc.getParam(param);
        return obj != null && !obj.toString().isEmpty();
    }

    /**
     * Returns the channel search results.
     *
     * @param searchCriteria Search criteria.
     * @return Search results.
     */
    public ChannelSearchResult doSearch(SearchCriteria searchCriteria) {
        try {
            String data = getData(searchCriteria);

            // Clean xml version for geotools parser for faster parse
            data = data.replace(RESPONSE_CLEAN, "");
            log.debug("DATA: " + data);
            boolean exonym = false;
            if(hasParam(searchCriteria,PARAM_EXONYM)) exonym = searchCriteria.getParam(PARAM_EXONYM).toString().equals("true");
            return elfParser.parse(data, searchCriteria.getSRS(), exonym);

        } catch (Exception e) {
            log.error(e, "Failed to search locations from register of ELF GeoLocator");
            return new ChannelSearchResult();
        }
    }
}
