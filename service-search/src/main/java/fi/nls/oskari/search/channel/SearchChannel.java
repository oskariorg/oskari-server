package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.IllegalSearchCriteriaException;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.*;

/**
 * Baseclass for annotated Search channels
 */
public abstract class SearchChannel extends OskariComponent implements SearchableChannel, ConnectionProvider {


    private static Logger log = LogFactory.getLogger(SearchChannel.class);
    private Map<String, Double> mapScalesForType = new HashMap<String, Double>();
    private double defaultScale = -1;
    private Map<String, Double> ranksForType = new HashMap<>();
    private int defaultRank = -1;
    // store encountered types here to only log about possible configs for new types
    private Set<String> types = new HashSet<String>();
    private int maxCount = 100;

    public String getId() {
        return getName();
    }

    public void init() {
        defaultScale = PropertyUtil.getOptional("search.channel." + getName() + ".scale", -1);
        initTypeMap("scale", mapScalesForType);
        initTypeMap("rank", ranksForType);
        maxCount = PropertyUtil.getOptional("search.channel." + getName() + ".maxFeatures",
                PropertyUtil.getOptional("search.max.results", maxCount));
    }
    public int getMaxResults() {
        return maxCount;
    }

    public int getMaxResults(int requested) {
        int maximum = getMaxResults();
        if(requested != -1 && requested < maximum) {
            return requested;
        }
        return maximum;
    }


    public Capabilities getCapabilities() {
        return Capabilities.TEXT;
    }

    public ChannelSearchResult doSearch(SearchCriteria searchCriteria) throws IllegalSearchCriteriaException {
        throw new IllegalSearchCriteriaException("Not implemented");
    }

    public ChannelSearchResult reverseGeocode(SearchCriteria searchCriteria) throws IllegalSearchCriteriaException {
        throw new IllegalSearchCriteriaException("Not implemented");
    }

    public JSONObject getUILabels() {
        JSONObject name = JSONHelper.createJSONObject("name", getId());
        return JSONHelper.createJSONObject(PropertyUtil.getDefaultLanguage(), name);
    }

    public boolean isValidSearchTerm(SearchCriteria criteria) {
        return true;
    }

    /**
     * Defaults to true. Can be explicitly set with properties:
     *  search.channel.CHANNEL_ID.isDefault=false
     * @return
     */
    public boolean isDefaultChannel() { return PropertyUtil.getOptional("search.channel." + getName() + ".isDefault", true); }

    /**
     * Always returns true with basic implementation
     * @param user
     * @return
     */
    public boolean hasPermission(User user) { return true; }

    /**
     * Returns debug data for search channels that can then be shown in UI.
     * Not to be used by "production code", but for debugging only!
     * @return
     */
    public Map<String, Object> getDebugData() {
        Map<String, Double> configurables = new HashMap<String, Double>();
        Map<String, Double> ranks = new HashMap<String, Double>();
        for(String type : types) {
            Double configured = mapScalesForType.get(type);
            Double rank = ranksForType.get(type);
            // include all encountered types
            // add -1 as value for those without config
            if(configured == null) {
                configured = -1d;
            }
            if(rank == null) {
                rank = -1d;
            }
            configurables.put(type, configured);
            ranks.put(type, rank);
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("defaultScale", defaultScale);
        data.put("scaleOptions", configurables);
        data.put("rankOptions", ranks);

        return data;
    }

    private void initTypeMap(final String prop, final Map<String, Double> map) {

        final String propertyPrefix = "search.channel." + getName() + "." + prop + ".";
        final List<String> headerPropNames = PropertyUtil.getPropertyNamesStartingWith(propertyPrefix);
        for (String propName : headerPropNames) {
            final String key = propName.substring(propertyPrefix.length());
            final double value = PropertyUtil.getOptional(propName, -1d);
            if(value != -1) {
                map.put(key, value);
            }
            else {
                log.warn("Property with name", propName, "should be positive integer! Config for", key, "will not work correctly.");
            }
        }
    }

    public String getProperty(String key, String defaultValue) {
        return PropertyUtil.get("search.channel." + getName() + "." + key, defaultValue);
    }

    public void calculateCommonFields(final SearchResultItem item) {
        if(item == null) {
            return;
        }
        item.setChannelId(getName());
        final String type = item.getType();
        if(type == null) {
            return;
        }
        if(!types.contains(type)) {
            types.add(type);
            log.debug("Configurable zoom/rank for channel", getName(), "type:", type);
        }
        item.setZoomScale(getZoomScale(type));
        if(item.getRank() == -1) {
            item.setRank(getRank(type));
        }
        // TODO: setup normalized ranking/channel here
        // maybe add SearchChannel.getMaxRank/getMinRank and normalize through channels
    }


    public int getRank(final String type) {
        Double value = ranksForType.get(type);
        if(value == null) {
            return defaultRank;
        }
        return (int) Math.round(value);
    }
    public double getZoomScale(final String type) {
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
}
