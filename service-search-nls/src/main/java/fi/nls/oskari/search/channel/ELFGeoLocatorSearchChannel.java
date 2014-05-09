package fi.nls.oskari.search.channel;
/**
 * Search channel for ELF Geolocator requests
 */

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.util.ELFGeoLocatorParser;
import fi.nls.oskari.util.IOHelper;

import java.net.URLEncoder;
import java.util.Locale;


public class ELFGeoLocatorSearchChannel implements SearchableChannel {

    /**
     * logger
     */
    private Logger log = LogFactory.getLogger(this.getClass());
    private String serviceURL = null;

    public static final String ID = "ELFGEOLOCATOR_CHANNEL";
    public static final String PROPERTY_SERVICE_URL = "service.url";
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

    private ELFGeoLocatorParser elfParser = new ELFGeoLocatorParser();

    public void setProperty(String propertyName, String propertyValue) {
        if (PROPERTY_SERVICE_URL.equals(propertyName)) {
            serviceURL = propertyValue;
            log.debug("ServiceURL set to " + serviceURL);
        } else {
            log.warn("Unknown property for " + ID + " search channel: " + propertyName);
        }
    }

    public String getId() {
        return ID;
    }

    /**
     * Returns the search raw results.
     *
     * @param searchCriteria Search criteria.
     * @return Result data in JSON format.
     * @throws Exception
     */
    private String getData(SearchCriteria searchCriteria) throws Exception {
        if (serviceURL == null) {
            log.warn("ServiceURL not configured. Add property with key", PROPERTY_SERVICE_URL);
            return null;
        }

        // Language
        Locale locale = new Locale(searchCriteria.getLocale());
        String lang3 = locale.getISO3Language();


        StringBuffer buf = new StringBuffer(serviceURL);
        if (!searchCriteria.getLon().isEmpty() && !searchCriteria.getLat().isEmpty() ) {
            // reverse geocoding
            String request = REQUEST_REVERSEGEOCODE_TEMPLATE.replace(KEY_LATITUDE_HOLDER,searchCriteria.getLat() );
            request = request.replace(KEY_LONGITUDE_HOLDER, searchCriteria.getLon() );
            request = request.replace(KEY_LANG_HOLDER, lang3);
            buf.append(request);

        } else if (!searchCriteria.getRegion().isEmpty()) {
            // Exact search limited to AU region - case sensitive - no fuzzy support
            String request = REQUEST_GETFEATUREAU_TEMPLATE.replace(KEY_PLACE_HOLDER, URLEncoder.encode(searchCriteria.getSearchString(), "UTF-8"));
            request = request.replace(KEY_AU_HOLDER, URLEncoder.encode(searchCriteria.getRegion(), "UTF-8"));
            request = request.replace(KEY_LANG_HOLDER, lang3);
            buf.append(request);
        } else if (searchCriteria.getFuzzy()) {
            // Fuzzy search
            buf.append(REQUEST_FUZZY_TEMPLATE.replace(KEY_LANG_HOLDER, lang3));
            buf.append(URLEncoder.encode(searchCriteria.getSearchString(), "UTF-8"));
        } else {
            // Exact search - case sensitive
            String request = REQUEST_GETFEATURE_TEMPLATE.replace(KEY_LANG_HOLDER, lang3);
            buf.append(request.replace(KEY_PLACE_HOLDER, URLEncoder.encode(searchCriteria.getSearchString(), "UTF-8")));
        }


        return IOHelper.getURL(buf.toString());

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

            searchCriteria.getSRS();

            return elfParser.parse(data, searchCriteria.getSRS(), searchCriteria.getExonym());

        } catch (Exception e) {
            log.error(e, "Failed to search locations from register of ELF GeoLocator");
            return new ChannelSearchResult();
        }
    }
}
