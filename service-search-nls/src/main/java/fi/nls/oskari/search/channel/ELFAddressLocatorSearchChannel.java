package fi.nls.oskari.search.channel;

/**
 * Created by RLINKALA on 3.2.2016.
 */

import fi.mml.portti.service.search.SearchCriteria;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;

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
        String filter = getFilter(searchCriteria);
        String request = REQUEST_GETFEATURE_TEMPLATE.replace(KEY_LANG_HOLDER, lang3);
        buf.append(request);
        buf.append(filter);
        buf.append("&ad=true");

        return IOHelper.readString(getConnection(buf.toString()));
    }
}
