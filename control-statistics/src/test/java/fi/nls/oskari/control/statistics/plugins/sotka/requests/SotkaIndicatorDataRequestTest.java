package fi.nls.oskari.control.statistics.plugins.sotka.requests;

import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import fi.nls.oskari.control.statistics.plugins.sotka.requests.IndicatorData;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;
import fi.nls.test.util.ResourceHelper;

import static org.junit.Assert.*;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

@RunWith(PowerMockRunner.class)
public class SotkaIndicatorDataRequestTest {
    private static String testResponse = ResourceHelper.readStringResource("SotkaIndicatorData.csv",
            SotkaIndicatorDataRequestTest.class);
    private static String expectedParsingResult = ResourceHelper.readStringResource("SotkaIndicatorData.json",
            SotkaIndicatorDataRequestTest.class);

    @Test
    public void testParseIndicators() {
        SotkaRequest request = SotkaRequest.getInstance(IndicatorData.NAME);
        String json = request.getJsonFromCSV(testResponse);
        assertEquals(expectedParsingResult.trim(), json);
    }
}
