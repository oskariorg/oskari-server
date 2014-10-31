package fi.nls.oskari.search.channel;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.util.IOHelper;

import java.net.HttpURLConnection;

/**
 * Baseclass for annotated Search channels
 */
public abstract class SearchChannel extends OskariComponent implements SearchableChannel, ConnectionProvider {

    private static Logger log = LogFactory.getLogger(SearchChannel.class);
    
    public String getId() {
        return getName();
    }

    public HttpURLConnection getConnection(final String url) {
        try {
            final String propertyPrefix = "search.channel." + getName() + ".service.";
            log.info("Creating search url with url:", url, "and properties prefixed with", propertyPrefix);
            return IOHelper.getConnectionFromProps(url, propertyPrefix);
        }
        catch (Exception ex) {
            log.error("Couldn't open connection for search channel!");
            throw new RuntimeException("Couldn't open connection!", ex);
        }
    }

    public HttpURLConnection getConnection() {
        try {
            final String propertyPrefix = "search.channel." + getName() + ".service.";
            log.info("Creating search url with properties prefixed", propertyPrefix);
            return IOHelper.getConnectionFromProps(propertyPrefix);
        }
        catch (Exception ex) {
            log.error("Couldn't open connection for search channel!");
            throw new RuntimeException("Couldn't open connection!", ex);
        }
    }

    @Override
    @Deprecated
    public void setProperty(String propertyName, String propertyValue) {
        // this shouldn't be used anymore
        log.info("SearchableChannel.setProperty() is deprecated! - please change your SearchChannels to use PropertyUtil directly");
    }
}
