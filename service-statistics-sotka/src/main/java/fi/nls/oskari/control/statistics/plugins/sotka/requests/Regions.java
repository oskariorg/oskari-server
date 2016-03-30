package fi.nls.oskari.control.statistics.plugins.sotka.requests;

import fi.nls.oskari.cache.JedisManager;

/**
 * Request class for SotkaNET statistics query to list regions.
 * @author SMAKINEN
 */
public class Regions extends SotkaRequest {

    public final static String NAME = "regions";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getRequestSpecificParams() {
        return "/regions";
    }

    @Override
    public String getData() {
        final String cacheKey = "oskari_sotka_regions_list:" + getBaseURL();
        final String cachedData = JedisManager.get(cacheKey);
        if(cachedData != null && !cachedData.isEmpty()) {
            return cachedData;
        }
        final String data = super.getData();
        JedisManager.setex(cacheKey, JedisManager.EXPIRY_TIME_DAY, data);
        return data;
    }
}
