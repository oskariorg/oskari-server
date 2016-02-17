package fi.nls.oskari.control.statistics.plugins.sotka.requests;

import fi.nls.oskari.cache.JedisManager;

/**
 * Request class for SotkaNET statistics query to list indicator metadata.
 * @author SMAKINEN
 */
public class IndicatorMetadata extends SotkaRequest {
    public final static String NAME = "indicator_metadata";
    private final static String CACHE_KEY_PREFIX = "oskari_sotka_indicator_metadata:";

    public boolean isValid () {
        return getIndicator() != null && getIndicator().isEmpty();
    }
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getRequestSpecificParams() {
        return "/indicators/" + getIndicator();
    }

    @Override
    public String getData() {
        final String cacheKey = CACHE_KEY_PREFIX + getIndicator();
        final String cachedData = JedisManager.get(cacheKey);
        if(cachedData != null && !cachedData.isEmpty()) {
            return cachedData;
        }
        final String data = super.getData();
        // TODO: Fetch new data before the cache expires in a backend thread.
        JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, data);
        return data;
    }
}
