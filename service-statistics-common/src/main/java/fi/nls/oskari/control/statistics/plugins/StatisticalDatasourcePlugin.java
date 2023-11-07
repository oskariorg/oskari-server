package fi.nls.oskari.control.statistics.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.control.statistics.plugins.db.StatisticalDatasource;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
 *
 * On adapter implementations implement update(). Update() should call onIndicatorProcessed() after each indicator.
 * Optionally you can override getIndicatorSet() and getIndicator() if results can be returned fast enough.
 *
 * You should also consider overriding hasPermission() as the default implementation always returns true.
 */
public abstract class StatisticalDatasourcePlugin {
    static final String CACHE_PREFIX = "oskari:stats:";
    private static final String CACHE_POSTFIX_LIST = ":indicators";
    private static final String CACHE_POSTFIX_METADATA = ":metadata:";

    private StatisticalDatasource source = null;
    private DataSourceUpdater updater = null;

    private static final Logger LOG = LogFactory.getLogger(StatisticalDatasourcePlugin.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    // update at most 4 datasources at a time
    private static final ExecutorService UPDATE_SCHEDULER = Executors.newFixedThreadPool(4);

    /**
     * This is called when datasource should start processing the indicators. Processed indicators
     */
    public abstract void update();

    public boolean canModify(User user) {
        return false;
    }

    public StatisticalIndicator saveIndicator(StatisticalIndicator indicator, User user) {
        // NO-OP
        throw new SecurityException("Unable to save");
    }
    public void saveIndicatorData(StatisticalIndicator indicator, long regionsetId, Map<String, IndicatorValue> data, User user) {
        // NO-OP
        throw new SecurityException("Unable to save");
    }

    /**
     * Method to fetch indicator data.
     * @param indicator
     * @param params
     * @param regionset
     * @return
     */
    public abstract Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicator indicator, StatisticalIndicatorDataModel params, StatisticalIndicatorLayer regionset);
    /**
     * Returns true by default. You should override this in a plugin if restrictions are required.
     * @param indicator
     * @param user
     * @return
     */
    public boolean hasPermission(StatisticalIndicator indicator, User user) {
        return true;
    }

    /**
     * Trigger update on the data. Should refresh cached data for getIndicatorSet and track progress.
     */
    private void startUpdater(boolean cacheEmpty) {
        // TODO: cancel previous if running?
        if (cacheEmpty) {
            this.updater = new DataSourceCachePopulator(this);
        } else {
            this.updater = new DataSourceCacheUpdater(this);
        }
        UPDATE_SCHEDULER.execute(updater);
    }

    /**
     * Hook for setting up components that the handler needs to handle requests
     */
    public void init(StatisticalDatasource source) {
        this.source = source;
    }
    public StatisticalDatasource getSource() {
        return source;
    }

    /**
     * Returns currently available dataset for this datasource. Should use preprocessed and cached data with scheduled update
     * triggered with the update() method.
     */
    public IndicatorSet getIndicatorSet(User user) {
        DataStatus status = getStatus();
        List<StatisticalIndicator> indicators = getProcessedIndicators();
        boolean cacheEmpty = indicators.isEmpty();

        Duration maxUpdateDuration = Duration.ofMillis(getSource().getMaxUpdateDuration());
        Duration updateInterval = Duration.ofMillis(getSource().getUpdateInterval());

        Instant updateStarted = status.getUpdateStarted();
        Instant lastUpdate = status.getLastUpdate();

        if (updateStarted != null) {
            // Update is already running - check that it hasn't lasted too long
            if (Instant.now().isAfter(updateStarted.plus(maxUpdateDuration))) {
                // Oh dear - hopefully we are just out of sync with Redis due to server restart or something
                startUpdater(cacheEmpty);
            }
        } else if (lastUpdate == null || Instant.now().isAfter(lastUpdate.plus(updateInterval))) {
            startUpdater(cacheEmpty);
        } else if (cacheEmpty) {
            /* Update isn't running and it's not time to run another one yet
               still the cache remains empty. Most probably there was an error
               with the service. Let it recover for a while before re-trying */
            if (Instant.now().isAfter(lastUpdate.plus(Duration.ofSeconds(30)))) {
                startUpdater(cacheEmpty);
            }
        }
        // If we aren't running an update - don't poll back for more complete list
        boolean complete = true;
        // Only poll back for more complete list if we are running a full update (cache was empty)
        if (cacheEmpty && updater != null && updater.isFullUpdate()) {
            complete = false;
        }

        // filter by user
        final List<StatisticalIndicator> result = indicators.stream()
                .filter(ind -> hasPermission(ind, user))
                .collect(Collectors.toList());

        IndicatorSet set = new IndicatorSet();
        set.setComplete(complete);
        set.setIndicators(result);
        return set;
    }

    public StatisticalIndicator getIndicator(User user, String indicatorId) {
        try {
            String json = JedisManager.get(getIndicatorMetadataKey(indicatorId));
            if (json == null) {
                // someone requested an indicator we don't know about
                // client might have a saved ref to id that is no longer available OR
                // someone is fishing for data with crafted urls
                // either way, we don't need the stack trace from Jackson parsing a null value
                return null;
            }
            StatisticalIndicator indicator = MAPPER.readValue(json, StatisticalIndicator.class);
            if (hasPermission(indicator, user)) {
                // sort dimensions etc
                try {
                    handleHints(indicator);
                } catch (Exception ex) {
                    LOG.info("Problem handling hints for indicator");
                }
                return indicator;
            }
            LOG.error("User doesn't have permissions to indicator ", indicatorId);
        } catch (IOException ex) {
            LOG.error(ex, "Couldn't read indicator data for is:", indicatorId);
        }
        return null;
    }

    /**
     * Datasource config can have hints like this to for example sort out allowed values:
     * {
     *     "hints" : {
     *         "dimensions" : [ {
     *             "id" : "year",
     *             "sort" : "DESC",
     *             "default" : "2017"
     *         }]
     *     }
     * }
     * @param indicator
     */
    public void handleHints(StatisticalIndicator indicator) {
        JSONObject hints = getSource().getHints();
        JSONArray dimHints = JSONHelper.getEmptyIfNull(hints.optJSONArray("dimensions"));

        for(int i = 0; i < dimHints.length(); ++i) {
            JSONObject dimHelp = dimHints.optJSONObject(i);
            String id = dimHelp.optString("id");
            StatisticalIndicatorDataDimension dim = indicator.getDataModel().getDimension(id);
            if(dim == null) {
                continue;
            }
            if(dimHelp.has("sort")) {
                dim.sort("DESC".equalsIgnoreCase(dimHelp.optString("sort")));
            }
            dim.useDefaultValue(dimHelp.optString("default"));
        }
    }

    public void onIndicatorProcessed(StatisticalIndicator indicator) {
        // add work queue to be written for indicator listing
        if(updater != null) {
            updater.addToWorkQueue(indicator);
        } else {
            // should we save it to listing directly?
        }
        // this is used for metadata requests
        writeToCache(indicator);
    }

    private void writeToCache(StatisticalIndicator indicator) {
        try {
            String json = MAPPER.writeValueAsString(indicator);
            JedisManager.setex(getIndicatorMetadataKey(indicator.getId()), JedisManager.EXPIRY_TIME_DAY * 7, json);
        } catch (JsonProcessingException ex) {
            LOG.error(ex, "Error updating indicator metadata");
        }
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
     * Returns a Redis key that should hold client ready indicators as JSON
     * @return
     */
    protected String getIndicatorMetadataKey(String id) {
        return CACHE_PREFIX + getSource().getId() + CACHE_POSTFIX_METADATA + id;
    }

    /**
     * Returns a Redis key that should status information as JSON for this datasource:
     * { complete : [true|false], updateStart : [timestamp], lastUpdate : [timestamp] }
     * @return
     */
    protected String getStatusKey() {
        return CACHE_PREFIX + getSource().getId() + ":status";
    }

    public DataStatus getStatus() {
        String status = JedisManager.get(getStatusKey());
        return new DataStatus(status);
    }

    /**
     * Generally true, if the data does not change all the time, for example based on the user doing the query.
     */
    public boolean canCache() {
        return true;
    }
}
