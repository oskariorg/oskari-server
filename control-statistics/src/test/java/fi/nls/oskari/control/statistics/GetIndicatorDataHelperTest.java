package fi.nls.oskari.control.statistics;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class GetIndicatorDataHelperTest {

    @Test
    public void testGetCacheKey() throws JSONException {
        long pluginId = 1L;
        String indicatorId = "232";
        long layerId = 1850L;
        String selectionStr = "{\"year\":\"2015\",\"sex\":\"female\"}";
        JSONObject selectionJSON = new JSONObject(selectionStr);
        String actual = GetIndicatorDataHelper.getCacheKey(pluginId, indicatorId, layerId, selectionJSON);
        String expected = "oskari:stats:1:data:232:1850:sex=female:year=2015";
        Assert.assertEquals(expected, actual);
    }

}
