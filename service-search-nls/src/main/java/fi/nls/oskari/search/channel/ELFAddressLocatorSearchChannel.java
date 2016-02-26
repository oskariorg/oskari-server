package fi.nls.oskari.search.channel;

/**
 * Created by RLINKALA on 3.2.2016.
 */

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;

import java.net.URLEncoder;
import java.util.Locale;

/**
 * Search channel for ELF Geolocator requests
 */
@Oskari(ELFAddressLocatorSearchChannel.ID)
public class ELFAddressLocatorSearchChannel extends ELFGeoLocatorSearchChannel {

    private Logger log = LogFactory.getLogger(this.getClass());

    public static final String ID = "ELFADDRESSLOCATOR_CHANNEL";

    /**
     * Returns the search raw results.
     *
     * @param searchCriteria Search criteria.
     * @return Result data in JSON format.
     * @throws Exception
     */
    public String getData(SearchCriteria searchCriteria) throws Exception {

        if (serviceURL == null) {
            log.warn("ServiceURL not configured. Add property with key", PROPERTY_SERVICE_URL);
            return null;
        }
        // Language
        Locale locale = new Locale(searchCriteria.getLocale());
        String lang3 = locale.getISO3Language();

        StringBuffer buf = new StringBuffer(serviceURL);


        // Exact search - case sensitive
        String filter = GETFEATURE_FILTER_TEMPLATE;
       if (hasParam(searchCriteria, PARAM_COUNTRY)) {
            filter = ADMIN_FILTER_TEMPLATE;
            String country = searchCriteria.getParam(PARAM_COUNTRY).toString();
            //TODO add or filter, if there are many variations of admin names
            filter = filter.replace(KEY_ADMIN_HOLDER, URLEncoder.encode(elfParser.getAdminName(country)[0], "UTF-8"));
        }
        filter = filter.replace(KEY_PLACE_HOLDER, URLEncoder.encode(searchCriteria.getSearchString(), "UTF-8"));
        String request = REQUEST_GETFEATURE_TEMPLATE.replace(KEY_LANG_HOLDER, lang3);
        buf.append(request);
        buf.append(filter);
        buf.append("&ad=true");

        return IOHelper.readString(getConnection(buf.toString()));
    }
}
