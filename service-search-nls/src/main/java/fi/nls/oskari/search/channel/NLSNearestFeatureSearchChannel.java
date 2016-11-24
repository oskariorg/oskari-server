package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.util.NLSNearestFeatureParser;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.util.Locale;

/**
 * Search channel for NLS nearest feature requests
 * sample request
 * https://ws.nls.fi/maasto/nearestfeature?TYPENAME=oso:Osoitepiste&COORDS=385445,6675125,EPSG:3067&SRSNAME=EPSG:3067&MAXFEATURES=1&BUFFER=1000
 */
@Oskari(NLSNearestFeatureSearchChannel.ID)
public class NLSNearestFeatureSearchChannel extends SearchChannel {

    private Logger log = LogFactory.getLogger(this.getClass());
    private String serviceURL = null;

    public static final String ID = "NLS_NEAREST_FEATURE_CHANNEL";
    private static final String PROPERTY_SERVICE_URL = "search.channel.NLS_NEAREST_FEATURE_CHANNEL.service.url";

    public static final String KEY_COORDS_HOLDER = "_COORDS_";
    public static final String KEY_SRSNAME_HOLDER = "_EPSG_";
    public static final String KEY_MAXFEATURES_HOLDER = "_MAXFEATURES_";
    public static final String KEY_BUFFER_HOLDER = "_BUFFER_";
    public static final String RESPONSE_CLEAN = "<?xml version='1.0' encoding='UTF-8'?>";
    public static final String REQUEST_REVERSEGEOCODE_TEMPLATE = "?TYPENAME=oso:Osoitepiste&COORDS=_COORDS_&SRSNAME=_EPSG_&MAXFEATURES=_MAXFEATURES_&BUFFER=_BUFFER_";
    private static final String PARAM_BUFFER = "buffer";

    private NLSNearestFeatureParser nearestFeatureParser = new NLSNearestFeatureParser();

    @Override
    public void init() {
        super.init();
        serviceURL = PropertyUtil.getOptional(PROPERTY_SERVICE_URL);
        log.debug("ServiceURL set to " + serviceURL);
    }

    public Capabilities getCapabilities() {
        return Capabilities.COORD;
    }

    /**
     * Returns the search raw results.
     *
     * @param sc SearchCriteria
     * @return Result data in JSON format.
     * @throws Exception
     */
    private String getData(SearchCriteria sc) throws Exception {
        log.debug("getData");

        String coords = nearestFeatureParser.transformLonLat(sc.getLon(), sc.getLat(), sc.getSRS());
        if (coords == null) {
            log.warn("Invalid lon/lat coordinates ", sc.getLon(), " ", sc.getLat());
            return null;
        }
        StringBuffer buf = new StringBuffer(serviceURL);
        String request = REQUEST_REVERSEGEOCODE_TEMPLATE.replace(KEY_COORDS_HOLDER, coords);

        // Search distance
        request = request.replace(KEY_BUFFER_HOLDER, getBuffer(sc.getParam(PARAM_BUFFER)));
        // Max features in response
        request = request.replace(KEY_MAXFEATURES_HOLDER, "" + getMaxResults(sc.getMaxResults()));
        // Srs name
        request = request.replace(KEY_SRSNAME_HOLDER, nearestFeatureParser.SERVICE_SRS);
        buf.append(request);

        return IOHelper.readString(getConnection(buf.toString()));
    }

    public int getMaxResults(int max) {
        if(max <= 0) {
            return 1;
        }
        return super.getMaxResults(max);
    }

    public String getBuffer(Object param) {
        if(param != null && param instanceof String) {
            String str = (String) param;
            if(!str.isEmpty()) {
                return str;
            }
        }
        return "1000";
    }

    public boolean isValidSearchTerm(SearchCriteria criteria) {
        return criteria.isReverseGeocode();
    }

    /**
     * Returns the channel search results.
     *
     * @param sc Search criteria.
     * @return Search results.
     */
    public ChannelSearchResult reverseGeocode(SearchCriteria sc) {
        if (serviceURL == null) {
            log.warn("ServiceURL not configured. Add property with key", PROPERTY_SERVICE_URL);
            return null;
        }
        try {
            String coords = nearestFeatureParser.transformLonLat(sc.getLon(), sc.getLat(), sc.getSRS());
            if (coords == null) {
                log.warn("Invalid lon/lat coordinates ", sc.getLon(), " ", sc.getLat());
                return null;
            }
            String data = getData(sc);
            // Clean xml version for geotools parser for faster parsing
            data = data.replace(RESPONSE_CLEAN, "");
            log.debug("Response: " + data);
            // Language
            Locale locale = new Locale(sc.getLocale());
            return nearestFeatureParser.parse(data, sc.getSRS(), locale.getISO3Language());

        } catch (Exception e) {
            log.error(e, "Failed to search locations from register of NLS nearest feature service");
            ChannelSearchResult result = new ChannelSearchResult();
            result.setQueryFailed(true);
            return result;
        }
    }
}
