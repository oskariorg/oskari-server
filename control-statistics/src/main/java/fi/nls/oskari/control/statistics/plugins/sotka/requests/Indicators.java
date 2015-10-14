package fi.nls.oskari.control.statistics.plugins.sotka.requests;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.ActionException;

/**
 * Request class for SotkaNET statistics query to list indicators.
 * @author SMAKINEN
 */
public class Indicators extends SotkaRequest {

    private final static String CACHE_KEY = "oskari_sotka_indicators_list";
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
    public String getData() throws ActionException {
        final String cachedData = JedisManager.get(CACHE_KEY);
        if(cachedData != null && !cachedData.isEmpty()) {
            return cachedData;
        }
        final String data = super.getData();
        JedisManager.setex(CACHE_KEY, JedisManager.EXPIRY_TIME_DAY, data);
        return data;
    }
}
