package org.oskari.statistics.plugins.unsd;

import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONException;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class UnsdIndicatorValuesFetcherTest {

    private static String dataResponseUnlimited = ResourceHelper.readStringResource(
            "IndicatorData.json", UnsdIndicatorDataParserTest.class);
    private static String dataResponse2015AreaLimited = ResourceHelper.readStringResource(
            "IndicatorData2015.json", UnsdIndicatorDataParserTest.class);

    @Test
    public void testIndicatorData() throws JSONException {
        Map<String, IndicatorValue> valuesByRegion = UnsdIndicatorValuesFetcher.parseIndicatorData(dataResponseUnlimited);
        assertFalse("Did not parse any data from response.", valuesByRegion.isEmpty());
    }

    @Test
    public void testIndicatorDataPages()  {
        Boolean lastPage = UnsdIndicatorValuesFetcher.isLastPage(dataResponse2015AreaLimited);
        assertNotNull("Could not parse page info", lastPage);
        assertTrue("The response wasn't the last page.", lastPage);

        lastPage = UnsdIndicatorValuesFetcher.isLastPage(dataResponseUnlimited);
        assertNotNull("Could not parse page info", lastPage);
        assertFalse("The response was the last page.", lastPage);
    }
}