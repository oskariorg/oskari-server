package fi.nls.oskari.control.statistics.plugins.sotka.requests;

import fi.nls.oskari.cache.JedisManager;

/**
 * Request class for SotkaNET statistics query to list indicators.
 * @author SMAKINEN
 */
public class Indicators extends SotkaRequest {

    public final static String NAME = "indicators";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getRequestSpecificParams() {
        return "/indicators";
    }

    @Override
    public String getData() {
        final String cacheKey = "oskari_sotka_indicators_list:" + getBaseURL();
        final String cachedData = JedisManager.get(cacheKey);
        if(cachedData != null && !cachedData.isEmpty()) {
            return cachedData;
        }
        final String data = super.getData();
        JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, data);
        return data;
    }
}
