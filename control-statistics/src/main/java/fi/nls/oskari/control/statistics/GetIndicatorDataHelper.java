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
            Long layerId, String selectorsStr) throws JSONException {
        StringBuilder cacheKey = new StringBuilder("oskari:stats:");
        cacheKey.append(datasourceId);
        cacheKey.append(":data:");
        cacheKey.append(indicatorId);
        cacheKey.append(':');
        cacheKey.append(layerId);
        JSONObject selector = new JSONObject(selectorsStr);
        Iterator<String> it = selector.sortedKeys();
        while (it.hasNext()) {
            String key = it.next();
            cacheKey.append(':');
            cacheKey.append(key);
            cacheKey.append('=');
            cacheKey.append(selector.get(key));
        }
        return cacheKey.toString();
    }

}
