package fi.nls.oskari.control.statistics;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class for GetIndicatorDataHandler
 *
 * Reason for a separate class is that GetIndicatorDataHandler
 * has a static class member that throws an exception while
 * instantiating if certain properties have not been set
 * causing unit tests that load the class to fail. If this
 * issue gets fixed feel free to move these functions around.
 */
public class GetIndicatorDataHelper {

    protected static String getCacheKey(long datasourceId, String indicatorId,
            long layerId, JSONObject selectorJSON) {
        StringBuilder cacheKey = new StringBuilder("oskari:stats:");
        cacheKey.append(datasourceId);
        cacheKey.append(":data:");
        cacheKey.append(indicatorId);
        cacheKey.append(':');
        cacheKey.append(layerId);
        Iterator<String> it = selectorJSON.sortedKeys();
        while (it.hasNext()) {
            String key = it.next();
            cacheKey.append(':');
            cacheKey.append(key);
            cacheKey.append('=');
            try {
                cacheKey.append(selectorJSON.get(key));
            } catch (JSONException e) {
                // Ignore, we are iterating the keys, the key _does_ exist
            }
        }
        return cacheKey.toString();
    }

}
