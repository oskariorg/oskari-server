package fi.nls.oskari.search.channel;
/**
 * Search channel (GetFeatureInAU) for ELF Geolocator
 */

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.util.ELFGeoLocatorParser;
import fi.nls.oskari.util.IOHelper;

import java.net.URLEncoder;


public class ELFGeoLocatorSearchChannel implements SearchableChannel {

    /**
     * logger
     */
    private Logger log = LogFactory.getLogger(this.getClass());
    private String serviceURL = null;

    public static final String ID = "ELF_EXACT_AU_SEARCH_CHANNEL";
    public static final String PROPERTY_SERVICE_URL = "service.url";
    public static final String KEY_PLACE_HOLDER = "_PLACE_HOLDER_";
    public static final String KEY_AU_HOLDER = "_AU_HOLDER_";
    public static final String RESPONSE_CLEAN = "<?xml version='1.0' encoding='UTF-8'?>";
    public static final String REQUEST_GETFEATUREAU_TEMPLATE = "?SERVICE=WFS&VERSION=1.1.0&REQUEST=GetFeatureInAu&NAME=_PLACE_HOLDER_&AU=_AU_HOLDER_&LANGUAGE=eng";
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
        String[] parts = searchCriteria.getSearchString().split(",");
        if (parts.length < 2) {
            log.warn("AU region name is lacking after search name - add AU name after , in search string - ", searchCriteria.getSearchString());
            return null;
        }

        StringBuffer buf = new StringBuffer(serviceURL);
        String request = "";
        // Exact search to AU region - case sensitive
        request = REQUEST_GETFEATUREAU_TEMPLATE.replace(KEY_PLACE_HOLDER, URLEncoder.encode(parts[0], "UTF-8"));
        request = request.replace(KEY_AU_HOLDER, URLEncoder.encode(parts[1], "UTF-8"));
        buf.append(request);

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
