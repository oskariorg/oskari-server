package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.net.HttpURLConnection;
import java.util.*;

/**
 * Baseclass for annotated Search channels
 */
public abstract class SearchChannel extends OskariComponent implements SearchableChannel, ConnectionProvider {

    private static Logger log = LogFactory.getLogger(SearchChannel.class);
    private Map<String, Double> mapScalesForType = new HashMap<String, Double>();
    private double defaultScale = -1;
    // store encountered types here to only log about possible configs for new types
    private Set<String> types = new HashSet<String>();
    
    public String getId() {
        return getName();
    }

    public void init() {
        defaultScale = PropertyUtil.getOptional("search.channel." + getName() + ".scale", -1);
        final String propertyPrefix = "search.channel." + getName() + ".scale.";
        final List<String> headerPropNames = PropertyUtil.getPropertyNamesStartingWith(propertyPrefix);
        for (String propName : headerPropNames) {
            final String key = propName.substring(propertyPrefix.length());
            final double scale = PropertyUtil.getOptional(propName, -1d);
            if(scale != -1) {
                mapScalesForType.put(key, scale);
            }
            else {
                log.warn("Property with name", propName, "should be positive integer! Zoom scale for", key, "will not work correctly.");
            }
        }
    }

    public void calculateCommonFields(final SearchResultItem item) {
        if(item == null) {
            return;
        }
        final String type = item.getType();
        if(type == null) {
            return;
        }
        item.setZoomScale(getZoomScale(type));
        // TODO: setup normalized ranking/channel here
        // maybe add SearchChannel.getMaxRank/getMinRank and normalize through channels
    }

    public double getZoomScale(final String type) {
        if(!types.contains(type)) {
            types.add(type);
            log.debug("Configurable scale for channel", getName(), "type:", type);
        }

        Double value = mapScalesForType.get(type);
        if(value == null) {
            return defaultScale;
        }
        return value;
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
