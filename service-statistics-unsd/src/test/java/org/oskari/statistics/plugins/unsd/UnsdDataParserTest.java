package org.oskari.statistics.plugins.unsd;

import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.List;
import java.util.Map;

;import static org.junit.Assert.*;

public class UnsdDataParserTest {

    private static String targetResponse = ResourceHelper.readStringResource(
            "TargetList.json", UnsdDataParserTest.class);
    private static String dimensionsResponse = ResourceHelper.readStringResource(
            "Dimensions.json", UnsdDataParserTest.class);
    private static JSONObject dataResponseUnlimited = JSONHelper.createJSONObject(ResourceHelper.readStringResource(
            "IndicatorData.json", UnsdDataParserTest.class));
    private static JSONObject dataResponse2015AreaLimited = JSONHelper.createJSONObject(ResourceHelper.readStringResource(
            "IndicatorData2015.json", UnsdDataParserTest.class));

    @Test
    public void testIndicatorMetadata() throws JSONException {
        List<StatisticalIndicator> indicators = UnsdIndicatorParser.parseIndicators(targetResponse);
        assertFalse("Did not parse any indicator codes from target response.", indicators.isEmpty());

        StatisticalIndicatorDataModel model = UnsdIndicatorParser.parseDimensions(dimensionsResponse);
        assertNotEquals("Indicator doesn't have any dimensions.", model.getDimensions().size(), 0);

        Map<String, String> sources = UnsdIndicatorParser.parseSource(dataResponseUnlimited);
        assertNotNull("Indicator doesn't have source.", sources.get("en"));
    }

    @Test
    public void testIndicatorData() throws JSONException {
        Map<String, IndicatorValue> valuesByRegion = UnsdDataParser.parseIndicatorData(dataResponseUnlimited);
        assertFalse("Did not parse any data from response.", valuesByRegion.isEmpty());
    }

    @Test
    public void testIndicatorDataPages() throws JSONException {
        Boolean lastPage = UnsdDataParser.isLastPage(dataResponse2015AreaLimited);
        assertNotNull("Could not parse page info", lastPage);
        assertTrue("The response wasn't the last page.", lastPage);

        lastPage = UnsdDataParser.isLastPage(dataResponseUnlimited);
        assertNotNull("Could not parse page info", lastPage);
        assertFalse("The response was the last page.", lastPage);
    }
}

