package fi.nls.oskari.search.channel;
/**
 * Search channel (GetFeature, exact search) for ELF Geolocator
 */

import com.vividsolutions.jts.geom.Point;
import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.util.ELFGeoLocatorParser;
import fi.nls.oskari.util.IOHelper;

import java.net.URLEncoder;


public class ELFExactSearchChannel implements SearchableChannel {

    /**
     * logger
     */
    private Logger log = LogFactory.getLogger(this.getClass());
    private String serviceURL = null;

    public static final String ID = "ELF_EXACT_SEARCH_CHANNEL";
    public static final String PROPERTY_SERVICE_URL = "service.url";
    public static final String KEY_PLACE_HOLDER = "_PLACE_HOLDER_";
    public static final String RESPONSE_CLEAN = "<?xml version='1.0' encoding='UTF-8'?>";

    public static final String REQUEST_GETFEATURE_TEMPLATE = "?SERVICE=WFS&REQUEST=GetFeature&NAMESPACE=xmlns%28iso19112=http://www.isotc211.org/19112%29&TYPENAME=SI_LocationInstance&Version=1.1.0&MAXFEATURES=10&language=eng&FILTER=%3Cogc:Filter%20xmlns:ogc=%22http://www.opengis.net/ogc%22%20xmlns:iso19112=%22http://www.isotc211.org/19112%22%3E%3Cogc:PropertyIsEqualTo%3E%3Cogc:PropertyName%3Eiso19112:alternativeGeographicIdentifiers/iso19112:alternativeGeographicIdentifier/iso19112:name%3C/ogc:PropertyName%3E%3Cogc:Literal%3E_PLACE_HOLDER_%3C/ogc:Literal%3E%3C/ogc:PropertyIsEqualTo%3E%3C/ogc:Filter%3E";
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
        StringBuffer buf = new StringBuffer(serviceURL);
        // Exact search - case sensitive
        buf.append(REQUEST_GETFEATURE_TEMPLATE.replace(KEY_PLACE_HOLDER, URLEncoder.encode(searchCriteria.getSearchString(), "UTF-8")));

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

            String epsg = searchCriteria.getSRS();

            return elfParser.parse(data, epsg);

        } catch (Exception e) {
            log.error(e, "Failed to search locations from register of ELF GeoLocator");
            return new ChannelSearchResult();
        }
    }
}
