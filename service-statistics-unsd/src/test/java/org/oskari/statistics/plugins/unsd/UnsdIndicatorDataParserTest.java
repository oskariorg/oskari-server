package org.oskari.statistics.plugins.unsd;

import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.test.util.ResourceHelper;
import org.json.JSONException;
import org.junit.Test;

import java.util.List;
import java.util.Map;

;import static org.junit.Assert.*;

public class UnsdIndicatorDataParserTest {

    private static String targetResponse = ResourceHelper.readStringResource(
            "TargetList.json", UnsdIndicatorDataParserTest.class);
    private static String dimensionsResponse = ResourceHelper.readStringResource(
            "Dimensions.json", UnsdIndicatorDataParserTest.class);
    private static String dataResponseUnlimited = ResourceHelper.readStringResource(
            "IndicatorData.json", UnsdIndicatorDataParserTest.class);
    private static String dataResponse2015AreaLimited = ResourceHelper.readStringResource(
            "IndicatorData2015.json", UnsdIndicatorDataParserTest.class);

    private UnsdParser parser;

    public UnsdIndicatorDataParserTest() {
        parser = new UnsdParser();
    }

    @Test
    public void testIndicatorMetadata() throws JSONException {
        List<StatisticalIndicator> indicators = parser.parseIndicators(targetResponse);
        assertFalse("Did not parse any indicator codes from target response.", indicators.isEmpty());

        StatisticalIndicatorDataModel model = parser.parseDimensions(dimensionsResponse);
        assertNotEquals("Indicator doesn't have any dimensions.", model.getDimensions().size(), 0);

        Map<String, String> sources = parser.parseSource(dataResponseUnlimited);
        assertNotNull("Indicator doesn't have source.", sources.get("en"));
    }

}

