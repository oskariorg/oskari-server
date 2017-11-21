package fi.nls.oskari.control.statistics;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

public class GetIndicatorDataHelperTest {

    @Test
    public void testGetCacheKey() throws JSONException {
        long pluginId = 1L;
        String indicatorId = "232";
        Long layerId = 1850L;
        String selectionStr = "{\"year\":\"2015\",\"sex\":\"female\"}";
        String actual = GetIndicatorDataHelper.getCacheKey(pluginId, indicatorId, layerId, selectionStr);
        String expected = "oskari:stats:1:data:232:1850:sex=female:year=2015";
        Assert.assertEquals(expected, actual);
    }

}
