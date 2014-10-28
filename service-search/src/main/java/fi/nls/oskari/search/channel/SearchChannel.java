package fi.nls.oskari.search.channel;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;

/**
 * Baseclass for annotated Search channels
 */
public abstract class SearchChannel extends OskariComponent implements SearchableChannel {

    private static Logger log = LogFactory.getLogger(SearchChannel.class);
    public String getId() {
        return getName();
    }

    @Override
    @Deprecated
    public void setProperty(String propertyName, String propertyValue) {
        // this shouldn't be used anymore
        log.info("SearchableChannel.setProperty() is deprecated! - please change your SearchChannels to use PropertyUtil directly");
    }
}
