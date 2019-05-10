package fi.nls.oskari.control.statistics.plugins.unsd.parser;

import fi.nls.oskari.control.statistics.data.IndicatorValue;
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
        List<UnsdIndicator> indicators = parser.parseGoalIndicators(targetResponse);
        assertFalse("Did not parse any indicator codes from target response.", indicators.isEmpty());

        UnsdIndicator indicator = indicators.get(0);
        parser.mergeDimensions(indicator, dimensionsResponse);
        assertNotEquals("Indicator doesn't have any dimensions.", indicator.getDataModel().getDimensions().size(), 0);

        parser.mergeSource(indicator, dataResponseUnlimited);
        assertNotNull("Indicator doesn't have source.", indicator.getSource());
    }

    @Test
    public void testIndicatorData() throws JSONException {
        Map<String, IndicatorValue> valuesByRegion = parser.parseIndicatorData(dataResponseUnlimited);
        assertFalse("Did not parse any data from response.", valuesByRegion.isEmpty());
    }

    @Test
    public void testIndicatorDataPages() throws JSONException {
        Boolean lastPage = parser.isLastPage(dataResponse2015AreaLimited);
        assertNotNull("Could not parse page info", lastPage);
        assertTrue("The response wasn't the last page.", lastPage);

        lastPage = parser.isLastPage(dataResponseUnlimited);
        assertNotNull("Could not parse page info", lastPage);
        assertFalse("The response was the last page.", lastPage);
    }
}

