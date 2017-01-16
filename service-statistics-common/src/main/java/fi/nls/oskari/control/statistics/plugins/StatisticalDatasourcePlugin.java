package fi.nls.oskari.control.statistics.plugins;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.statistics.data.IndicatorSet;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Each statistical datasource plugin encapsulates access to a single external API
 * where statistical indicator data can be fetched and shown in Oskari.
 *
 * Each datasource has a list of named indicators.
 * These indicators can be for example:
 * - "Perusterveydenhuollon työterveyshuollon lääkärikäynnit / 1 000 15 - 64-vuotiasta"
 *
 * Each indicator has:
 * - A localized description shown to user.
 * - An ordered set of different granularity layers such as "Kunta", or "Maakunta".
 * - A set of dimensions with a localized name and type and a list of allowed values, and their localizations.
 *   These could be for example: "Gender": "Male", "Female", "Other", "All", or "Year": "2010", "2011", ....
 *
 * Each different granularity layer has:
 * - A reference to a certain map layer and version in use in Oskari.
 *   This map layer version can be the current one in use, or a previous one. Old layers are preserved in Oskari to
 *   show indicators defined for some older sets of municipalities or so.
 * - A table of data indexed by selectors so that Oskari user can select values for selectors and Oskari can then show
 *   the data for a given granularity level.
 *
 * If in the future the plugin needs to show real-time information, a notification mechanism can be implemented
 * where the plugin notifies Oskari with the plugin name to tell it to fetch the new set of data.
 * Before that, we can pretty much cache all the values using Jedis.
 */
public abstract class StatisticalDatasourcePlugin {
    String CACHE_PREFIX = "oskari:stats:";
    String CACHE_POSTFIX_LIST = ":indicators";
    String CACHE_POSTFIX_PROGRESS = ":progress";
    private StatisticalDatasource source = null;

    private static final Logger LOG = LogFactory.getLogger(StatisticalDatasourcePlugin.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Returns currently available dataset for this datasource. Should use preprocessed and cached data with scheduled update
     * triggered with the update() method.
     */
    public IndicatorSet getIndicatorSet(User user) {
        DataStatus status = getStatus();
        boolean updateRequired = status.shouldUpdate(getSource().getUpdateInterval());
        if(updateRequired) {
            // trigger update if not updated before
            update();
        }
        IndicatorSet set = new IndicatorSet();
        set.setComplete(!updateRequired && !status.isUpdating());
        final List<StatisticalIndicator> indicators = getProcessedIndicators();
        // TODO: filter by user
        set.setIndicators(indicators);
        return set;
    }

    /**
     * Trigger update on the data. Should refresh cached data for getIndicatorSet and track progress.
     */
    public void update() {
        // TODO: not always new
        new DataSourceUpdater(this).start();
    }


    protected List<StatisticalIndicator> getProcessedIndicators() {
        final List<StatisticalIndicator> existingIndicators = new ArrayList<>();
        final String cacheKey = getIndicatorListKey();
        try {
            String existingJSON = JedisManager.get(cacheKey);
            if(existingJSON != null) {
                List<StatisticalIndicator> list = MAPPER.readValue(existingJSON, new TypeReference<List<StatisticalIndicator>>(){});
                existingIndicators.addAll(list);
            }
        } catch (IOException ex) {
            // Don't print out the content as it might be pretty long
            LOG.error(ex, "Couldn't read indicator data from existing list queue. Check redis with key", cacheKey);
        }
        return existingIndicators;
    }

    /**
     * Returns a Redis key that should hold client ready indicators as JSON
     * @return
     */
    protected String getIndicatorListKey() {
        return CACHE_PREFIX + getSource().getId() + CACHE_POSTFIX_LIST;
    }

    /**
     * Returns a Redis key that should status information as JSON for this datasource:
     * { complete : [true|false], updateStart : [timestamp], lastUpdate : [timestamp] }
     * @return
     */
    protected String getStatusKey() {
        return CACHE_PREFIX + getSource().getId() + CACHE_POSTFIX_PROGRESS;
    }

    public DataStatus getStatus() {
        String status = JedisManager.get(getStatusKey());
        return new DataStatus(status);
    }

    /**
     * Returns a list of statistical data indicators, each with several granularity layers.
     * @param user 
     * @return
     */
    public abstract List<StatisticalIndicator> getIndicators(User user);
    public List<StatisticalIndicator> getIndicators(User user, boolean noMetadata) {
        return getIndicators(user);
    }

    public StatisticalIndicator getIndicator(User user, String indicatorId) {
        for (StatisticalIndicator indicator : getIndicators(user)) {
            if (indicator.getId().equals(indicatorId)) {
                return indicator;
            }
        }
        return null;
    }

    public StatisticalDatasource getSource() {
        return source;
    }

    /**
     * Hook for setting up components that the handler needs to handle requests
     */
    public void init(StatisticalDatasource source) {
        this.source = source;
    }

    /**
     * Generally true, if the data does not change all the time, for example based on the user doing the query.
     */
    public boolean canCache() {
        return true;
    }
}
